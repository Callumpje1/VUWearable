import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nl.hva.vuwearable.udp.UDPConnection
import nl.hva.vuwearable.websocket.SocketService
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder( MethodSorters.NAME_ASCENDING )
class SocketTest {

    companion object {
        private val socketService = SocketService()

        private var isReceivingData = false
        private var isConnected = false

        @BeforeClass @JvmStatic fun setup() {
            // things to execute once and keep around for the class
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

    @Test
    fun testAUseAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("nl.hva.vuwearable", appContext.packageName)
    }

    @Test
    fun testBCheckSetupIsSuccessful() {
        Thread.sleep(10000)
        Assert.assertTrue(isConnected)
    }

    @Test
    fun testCStartMeasurement() {
        socketService.sendMessage("r")
        Thread.sleep(3000)
    }

    @Test
    fun testDStartLiveData() {
        socketService.sendMessage("3a")
        Thread.sleep(3000)
        Assert.assertTrue(isReceivingData)
    }

    @Test
    fun testEStopLiveData() {
        socketService.sendMessage("0a")
        Thread.sleep(3000)
        Assert.assertFalse(isReceivingData)
    }

    @Test
    fun testFStopMeasurement() {
        socketService.sendMessage("s")
        Thread.sleep(3000)
    }
}