import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.vuwearable.udp.UDPConnection
import nl.hva.vuwearable.websocket.SocketService
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SocketTest {

    companion object {
        private val socketService = SocketService()

        private var isReceivingData = false
        private var isConnected = false

        @BeforeClass @JvmStatic fun setup() {
            // things to execute once and keep around for the class
            @BeforeClass
            fun setupDeviceConnection() {
                Thread(
                    UDPConnection(
                        InstrumentationRegistry.getInstrumentation().targetContext,
                        3,
                        3,
                        setConnectedCallback = { isConnectedDevice, isReceivingDataDevice ->
                            isReceivingData = isReceivingDataDevice
                            isConnected = isConnectedDevice
                        },
                        setASectionMeasurement = {
                        })
                ).start()

                socketService.openConnection()
            }
        }
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("nl.hva.vuwearable", appContext.packageName)
    }

    @Test
    fun startMeasurement() {
        socketService.sendMessage("r")
        Thread.sleep(400)
    }

    @Test
    fun startLiveData() {
        socketService.sendMessage("3a")
        Thread.sleep(400)
        Assert.assertTrue(isReceivingData)
    }

    @Test
    fun stopLiveData() {
        socketService.sendMessage("0a")
        Thread.sleep(400)
        Assert.assertFalse(isReceivingData)
    }

    @Test
    fun stopMeasurement() {
        socketService.sendMessage("s")
        Thread.sleep(400)
    }
}