import io.github.kgpu.Kgpu;
import io.github.kgpu.PowerPreference
import io.github.kgpu.Window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main(){
    Kgpu.init();

    val window = Window()
    window.setTitle("Kgpu - Desktop")

    GlobalScope.launch {
        suspend {
            val adapter = window.requestAdapterAsync(PowerPreference.DEFAULT)
            println("Adapter: $adapter")
            val device = adapter.requestDeviceAsync();
            println("Device: $device")
        }.invoke()

        Kgpu.runLoop(window) {
        }
    }

    Thread.sleep(5000)
}
