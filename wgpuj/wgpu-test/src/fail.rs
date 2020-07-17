use std::ffi::CString;
use std::os::raw::c_char;

macro_rules! assert_ffi {
    ($left:expr, $right:expr) => ({
        match (&$left, &$right) {
            (left_val, right_val) => {
                if !(*left_val == *right_val) {
                    $crate::fail::fail_test(format!(r#"assertion failed: `(left == right)`
  left: `{:?}`,
 right: `{:?}`"#, &*left_val, &*right_val))
                }
            }
        }
    });
    ($left:expr, $right:expr,) => ({
        $crate::assert_ffi!($left, $right)
    });
    ($left:expr, $right:expr, $($arg:tt)+) => ({
        match (&($left), &($right)) {
            (left_val, right_val) => {
                if !(*left_val == *right_val) {
                    $crate::fail::fail_test(format!(r#"assertion failed: `(left == right)`
  left: `{:?}`,
 right: `{:?}`: {}"#, &*left_val, &*right_val,
                           $crate::format_args!($($arg)+)))
                }
            }
        }
    });
}

pub type TestFailCallback = unsafe extern "C" fn(msg: *const c_char);

static mut FAIL_CALLBACK: Option<TestFailCallback> = None;

#[no_mangle]
pub extern "C" fn fail_test(msg: String) {
    let msg = CString::new(msg).expect("Fail Message had null bytes!");

    unsafe {
        let callback = FAIL_CALLBACK.unwrap();

        callback(msg.as_ptr());
    }
}

#[no_mangle]
pub unsafe extern "C" fn set_fail_callback(callback: TestFailCallback) {
    match &FAIL_CALLBACK {
        Some(_) => panic!("The fail callback can only be set once."),
        None => (),
    }

    FAIL_CALLBACK = Some(callback);

    std::panic::set_hook(Box::new(|panic_info| {
    if let Some(s) = panic_info.payload().downcast_ref::<&str>() {
        println!("Rust panicked: {:?}", s);
        fail_test(String::new());
    } else {
        println!("Rust panicked for unknown reason!");
        fail_test(String::new());
    }
}));
}
