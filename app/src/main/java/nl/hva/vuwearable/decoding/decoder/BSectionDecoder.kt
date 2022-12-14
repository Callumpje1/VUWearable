package nl.hva.vuwearable.decoding.decoder

import android.nfc.Tag
import android.util.Log
import nl.hva.vuwearable.decoding.PacketDecoding
import nl.hva.vuwearable.decoding.getInt
import nl.hva.vuwearable.decoding.models.ASection
import nl.hva.vuwearable.decoding.models.BSection
import java.nio.ByteBuffer
import java.util.*

class BSectionDecoder: PacketDecoding<Map<Int, BSection>> {

    companion object{

        const val B_FIRST_BYTE: Byte  = 42
        const val B_SECOND_BYTE: Byte  = 12

        const val B_PART_LENGTH = 12

        private const val A0 = 0.0
        private const val A1 = 1

        val BATTERY_FORMULA = { value: Int -> A0 + A1 * value }
    }


    override fun parsePacket(data: ByteArray): LinkedHashMap<Int, ByteArray> {
        val array = LinkedList<Byte>()
        var isInBSection = false
        var i = 0

        // Loop through each of the characters in the encoded packet
        data.forEachIndexed { index, byte ->
            // To check if we are at the 'A' section, check if the first bytes are corresponding to a real 'A' section
            if (!isInBSection && byte == B_FIRST_BYTE && data[index + 1] == B_SECOND_BYTE) {
                isInBSection = true
            }

            // If A is fully parsed
            if (i == B_PART_LENGTH) {
                i = 0
                isInBSection = false
            }

            // Add the char code if we are in the 'A' section
            if (isInBSection) {
                i++
                array.add(byte)
            }

            Log.i("parse",array.toString())
        }

        return separateIntoSections(array)
    }


    override fun separateIntoSections(array: LinkedList<Byte>): LinkedHashMap<Int, ByteArray> {
        val map = LinkedHashMap<Int, ByteArray>()

        var currentStart = 0

        /*
         Till the end of the array, split up all the 'A's into its own section.
         Example:
         (65 char code == 'A')
         List: [65, 49 ,45, 65, 34, 98]
         Into: 0: [65, 49, 45], 1: [65, 34, 98]
         */
        while (currentStart + B_PART_LENGTH <= array.size - 1) {
            val subList = array.subList(currentStart, currentStart + B_PART_LENGTH)
            map[map.size] = subList.toByteArray()
            currentStart += B_PART_LENGTH
        }
        Log.i("seperate",map.toString())

        return map
    }

    override fun convertBytes(array: ByteArray, byteBuffer: ByteBuffer): Map<Int, BSection> {
        val parsedSections = parsePacket(array)
        val results = mutableMapOf<Int, BSection>()

        parsedSections.values.forEachIndexed { index, sectionArray ->
            // Get tick count section
            val tickCountArray = byteArrayOf(
                sectionArray[4],
                sectionArray[5],
                sectionArray[6],
                sectionArray[7]
            )

            // Get ICG section
            val icgArray = byteArrayOf(
                sectionArray[12],
                sectionArray[13],
                sectionArray[14],
                sectionArray[15]
            )

            // Put the result in the map with the corresponding values
            results[index] = BSection(
                byteBuffer.getInt(tickCountArray),
                BATTERY_FORMULA(byteBuffer.getInt(icgArray))
            )
        }
        Log.i("convert",results.toString())

        return results
    }
}