package nl.hva.vuwearable.udp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import nl.hva.vuwearable.models.Measurement
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class UDPConnection(
    private val context: Context,
    private val firstDelay: Long,
    private val everyDelay: Long,
    private val setConnectedCallback: (isConnected: Boolean, isReceivingData: Boolean) -> Unit
) : Runnable {

    companion object {
        const val UDP_TAG = "UDP"
        const val UDP_PORT = 1234
        const val BUFFER_LENGTH = 2048
        const val DEVICE_NETWORK_NAME = "AndroidWifi"
        const val CONNECTION_TIMEOUT_SECONDS = 3
        private const val TIME_TITLE = "Tickcount"

        private const val A0_ALL = 0.0
        private const val A1_ALL = 0.00047683721641078591
        private const val A0_T = 24.703470230102539
        private const val A1_T = 0.00097313715377822518

        private val HEADER = Measurement(32, "Header")
        private val TICK_COUNT = Measurement(32, "Tickcount")
        private val STATUS = Measurement(32, "Status")
        private val ICG = Measurement(32, "ICG") { value: Double -> A0_ALL + A1_ALL * value }
        private val ECG = Measurement(32, "ECG") { value: Double -> A0_ALL + A1_ALL * value }
        private val IRSC = Measurement(32, "IRSC") { value: Double -> A0_ALL + A1_ALL * value }
        private val T = Measurement(32, "T") { value: Double -> A0_T + A1_T * value }

        private val TYPE_A_DATA_SET = listOf(HEADER, TICK_COUNT, STATUS, ICG, ECG, IRSC, T)

        private const val A_PART_LENGTH = 28
    }

    override fun run() {
        var lastReceivedPacketDate: Date? = null

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            if (lastReceivedPacketDate === null && userIsOnline()) {
                setConnectedCallback(true, false)
                return@scheduleAtFixedRate
            }

            if (lastReceivedPacketDate === null || !userIsOnline()) {
                Log.i(UDP_TAG, "No stable connection")
                setConnectedCallback(false, false)
                return@scheduleAtFixedRate
            }

            val currentDate = Date()
            val diff = currentDate.time - lastReceivedPacketDate!!.time
            val secondsDifference = diff / 1000

            // Connection is not stable
            if (secondsDifference >= CONNECTION_TIMEOUT_SECONDS) {
                Log.i(UDP_TAG, "No stable connection!")
                setConnectedCallback(false, false)
            } else {
                // Connection is stable
                Log.i(UDP_TAG, "Stable connection")
                setConnectedCallback(true, true)
            }
        }, firstDelay, everyDelay, TimeUnit.SECONDS)
        try {
            val udpSocket = DatagramSocket(UDP_PORT)
            val buffer = ByteArray(BUFFER_LENGTH)
            val packet = DatagramPacket(buffer, buffer.size)

            while (true) {
                //Log.i(UDP_TAG, "Waiting to receive")
                udpSocket.receive(packet)

                // Receive and show the incoming packet data
                val text = String(packet.data, 0, packet.data.size)
                // Log.i(UDP_TAG, text)

                val map = getPartOfA(text)
                val results = getMeasurementValuesForTypeA(map)

                // Set the last received date to see if there is a delay between next packet
                lastReceivedPacketDate = Date()
            }
        } catch (e: SocketException) {
            Log.e(UDP_TAG, "Socket error", e)
            setConnectedCallback(false, false)
        } catch (e: IOException) {
            Log.e(UDP_TAG, "IO error", e)
            setConnectedCallback(false, false)
        }
    }

    private fun getPartOfA(text: String): Map<Int, List<Int>> {
        val charArray = text.toCharArray()
        val array = mutableListOf<Int>()

        var isInASection = false
        charArray.forEach {
            val code = it.code

            if (it == 'M') {
                isInASection = false
            }

            if (it == 'A') {
                isInASection = true
            }

            if (isInASection) {
                array.add(code)
            }
        }
        return splitIntoSections(array)
    }

    private fun splitIntoSections(array: List<Int>): Map<Int, List<Int>> {
        val map = mutableMapOf<Int, List<Int>>()

        var currentStart = 0

        while (currentStart + A_PART_LENGTH <= array.size - 1) {
            map[map.size] = array.subList(currentStart, currentStart + A_PART_LENGTH)
            currentStart += A_PART_LENGTH
        }

        return map
    }

    /**
     * @author Lorenzo Bindemann
     */
    private fun getMeasurementValuesForTypeA(map: Map<Int, List<Int>>): LinkedHashMap<Double, List<Measurement>> {
        val results = LinkedHashMap<Double, List<Measurement>>()
        val byteToBit = 8
        // loop through all the different measurement chunks we receive
        map.values.forEach { measurement ->
            val measurements = mutableListOf<Measurement>()
            var startCount = 0
            var timeInUnix = 0.0
            // loop through the structure of an A data set
            TYPE_A_DATA_SET.forEach { type ->
                val totalElementToCount = type.totalBytes / byteToBit

                // check per measurement the value
                var measurementTotal = 0.0
                for (i in startCount until totalElementToCount + startCount) {
                    measurementTotal += measurement[i]
                }
                type.value =
                    if (type.formula != null) type.formula?.let { it(measurementTotal) }!! else measurementTotal
                if (type.title == TIME_TITLE) timeInUnix = measurementTotal

                measurements.add(type)
                // go to the next value
                startCount += totalElementToCount
            }

            // add all the different measurements to your specific time
            results[timeInUnix] = measurements
        }
        return results
    }


    /**
     * Function which checks if the current user is connected to the correct network (if any)
     * and checks if the current network (if available) has WiFi capabilities
     */
    private fun userIsOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        var ssid: String? = null
        val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {
            // remove double quotes from ssid format
            ssid = wifiInfo.ssid.replace("\"", "")
        }

        return ssid.toString().contains(DEVICE_NETWORK_NAME) &&
                capabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}