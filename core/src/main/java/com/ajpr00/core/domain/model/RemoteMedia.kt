package com.ajpr00.core.domain.model

data class RemoteMedia(
    val id: String,
    val name: String,
    val type: FormatType,
    val thumbnailBytes: ByteArray // ByteArray para representar la imagen
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteMedia

        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (!thumbnailBytes.contentEquals(other.thumbnailBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + thumbnailBytes.contentHashCode()
        return result
    }
}