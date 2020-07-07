package com.noahcharlton.wgpuj.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is simply a marker used by JNR-Gen to mark a pointer
 * as a string pointer. When a field is marked with this,
 * JNR-Gen will make the setters/getters automatically convert
 * from java strings to rust strings.
 *
 * @see RustCString
 */
@Retention(RetentionPolicy.SOURCE)
@Documented
@Target({ElementType.FIELD})
public @interface CStrPointer {
}
