/**
 * @author Hugo Zuidema
 */
package nl.hva.vuwearable.models

data class Electrode(
    val wireName: String,

    // circle array structure: array[cx, cy, circleRadius)
    val circleCoordinates: Array<Float>,
    val relatedChannels: Array<String>,
    var isFailing: Boolean,
    val location: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Electrode

        if (wireName != other.wireName) return false
        if (!circleCoordinates.contentEquals(other.circleCoordinates)) return false
        if (!relatedChannels.contentEquals(other.relatedChannels)) return false
        if (isFailing != other.isFailing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = wireName.hashCode()
        result = 31 * result + circleCoordinates.contentHashCode()
        result = 31 * result + relatedChannels.contentHashCode()
        result = 31 * result + isFailing.hashCode()
        return result
    }
}
