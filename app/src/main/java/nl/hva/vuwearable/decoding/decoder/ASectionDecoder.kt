package nl.hva.vuwearable.decoding.decoder

import android.util.Log
import nl.hva.vuwearable.decoding.PacketDecoding
import nl.hva.vuwearable.decoding.getInt
import nl.hva.vuwearable.decoding.models.ASection
import java.nio.ByteBuffer

/**
 * Parses and decodes the A section of a packet
 *
 * @author Bunyamin Duduk
 */
class ASectionDecoder : PacketDecoding<Map<Int, ASection>> {

    companion object {
        const val A_FIRST_BYTE: UByte = 65u
        const val A_SECOND_BYTE: UByte = 28u
        const val A_THIRD_BYTE: UByte = 0u

        const val A_PART_LENGTH = 28
        const val BYTE_LENGTH = 4

        const val A0 = 0.0
        const val A1 = 0.00047683721641078591
        const val A0_T = 24.703470230102539
        const val A1_T = 0.00097313715377822518

        val ECG_FORMULA = { value: Int -> A0 + A1 * value }
        val ICG_FORMULA = { value: Int -> A0 + A1 * value }
    }

    override fun parsePacket(data: MutableList<UByte>): LinkedHashMap<Int, MutableList<UByte>> {
        val array = mutableListOf<UByte>()
        var isInASection = false
        var i = 0

        // Loop through each of the characters in the encoded packet
        data.forEachIndexed { index, byte ->
            // To check if we are at the 'A' section, check if the first bytes are corresponding to a real 'A' section
            if (!isInASection && byte == A_FIRST_BYTE && data[index + 1] == A_SECOND_BYTE && data[index + 2] == A_THIRD_BYTE) {
                isInASection = true
            }

            // If A is fully parsed
            if (i == A_PART_LENGTH) {
                i = 0
                isInASection = false
            }

            // Add the char code if we are in the 'A' section
            if (isInASection) {
                i++
                array.add(byte)
            }
        }

        return separateIntoSections(array)
    }

    override fun separateIntoSections(array: MutableList<UByte>): LinkedHashMap<Int, MutableList<UByte>> {
        val map = LinkedHashMap<Int, MutableList<UByte>>()

        var currentStart = 0

        /*
         Till the end of the array, split up all the 'A's into its own section.
         Example:
         (65 char code == 'A')
         List: [65, 49 ,45, 65, 34, 98]
         Into: 0: [65, 49, 45], 1: [65, 34, 98]
         */
        while (currentStart + A_PART_LENGTH <= array.size - 1) {
            map[map.size] = array.subList(currentStart, currentStart + A_PART_LENGTH)
//            Log.i("TEST", subList.toString())
            currentStart += A_PART_LENGTH
        }

        return map
    }

    override fun convertBytes(
        array: MutableList<UByte>,
        byteBuffer: ByteBuffer
    ): Map<Int, ASection> {
        val parsedSections = parsePacket(array)
        val results = mutableMapOf<Int, ASection>()

        parsedSections.values.forEachIndexed { index, sectionArray ->
            // Get tick count section
            val tickCountArray = arrayOf(
                sectionArray[4],
                sectionArray[5],
                sectionArray[6],
                sectionArray[7]
            )

            val statusCountArray = arrayOf(
                sectionArray[8],
                sectionArray[9],
                sectionArray[10],
                sectionArray[11]
            )

            // Get ICG section
            val icgArray = arrayOf(
                sectionArray[12],
                sectionArray[13],
                sectionArray[14],
                sectionArray[15]
            )

            // Get ECG section
            val ecgArray = arrayOf(
                sectionArray[16],
                sectionArray[17],
                sectionArray[18],
                sectionArray[19]
            )

            getBinaryStatusOfA(statusCountArray)

            // Put the result in the map with the corresponding values
            results[index] = ASection(
                byteBuffer.getInt(tickCountArray),
                getBinaryStatusOfA(statusCountArray),
                ICG_FORMULA(byteBuffer.getInt(icgArray)),
                ECG_FORMULA(byteBuffer.getInt(ecgArray))
            )
        }
        return results
    }

    /**
     * Function which splits up the binary representation of the electrode status send in an
     * A Packet
     */
    private fun getBinaryStatusOfA(bytes: Array<UByte>): Map<String, String> {
        // converts the UInt to a Binary readable string
        Log.i("TEST", bytes[1].toUInt().and(48u).toString(radix = 2))
        Log.i("TEST", bytes[1].toUInt().and(128u).toString(radix = 2))
        Log.i("TEST", bytes[0].toUInt().and(160u).toString(radix = 2))

        val keyedMap: MutableMap<String, String> = mutableMapOf()
        keyedMap["ECGV1"] = bytes[1].toUInt().and(48u).toString(radix = 2)
        keyedMap["ECGV2"] = bytes[1].toUInt().and(128u).toString(radix = 2)
        keyedMap["VN"] = bytes[0].toUInt().and(160u).toString(radix = 2)

        return keyedMap
    }
}