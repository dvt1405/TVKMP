package tv.iptv.tun.tviptv.repository.iptv

open class IPTVSourceConfig constructor(
    var sourceName: String,
    val sourceUrl: String,
    var type: Type = Type.TV_CHANNEL
) {

    enum class Type {
        TV_CHANNEL, FOOTBALL, MOVIE
    }

    companion object {
        val test by lazy {
            IPTVSourceConfig(
                "Test",
                "https://raw.githubusercontent.com/phuhdtv/vietngatv/master/vietngatv.m3u"
            )
        }
        val test2 by lazy {
            IPTVSourceConfig(
                "Test K+",
                "https://s.id/nhamng"
            )
        }
    }

    override fun toString(): String {
        return "{" +
                "sourceName: $sourceName,\n" +
                "sourceUrl: $sourceUrl,\n" +
                "type: $type\n" +
                "}"
    }

    override fun equals(other: Any?): Boolean {
        if (other is IPTVSourceConfig) {
            return sourceName == other.sourceName && sourceUrl == other.sourceName
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = sourceName.hashCode()
        result = 31 * result + sourceUrl.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}