package nl.hva.vuwearable.decoding.models

/**
 * Represents an 'A' section of a packet
 *
 * @author Bunyamin Duduk
 */
data class ASection(val tickCount: Int, val status: List<String>,
                    val icg: Double, val ecg: Double)