package nl.hva.vuwearable.decoding.decoder

import android.nfc.Tag
import android.util.Log
import nl.hva.vuwearable.decoding.PacketDecoding
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

            Log.i("hallo",array.toString())
        }

        return separateIntoSections(array)
    }

    override fun separateIntoSections(array: LinkedList<Byte>): LinkedHashMap<Int, ByteArray> {
        TODO("Not yet implemented")
    }

    override fun convertBytes(array: ByteArray, byteBuffer: ByteBuffer): Map<Int, BSection> {
        TODO("Not yet implemented")
    }
}