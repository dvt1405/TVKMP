package tv.iptv.tun.tviptv.jsonxml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("tv")
data class ProgrammeResponseDTO(
    @XmlSerialName("date")
    val date: String?,
    @XmlSerialName("source-info-name")
    val infoName: String?,
    @XmlSerialName("source-info-url")
    val infoUrl: String?,
    @XmlSerialName("generator-info-name")
    val generatorTime: String?,
    val listChannel: List<Channel>,
    val listProgram: List<ProgramDTO>
) {
    @Serializable
    @XmlSerialName("channel")
    data class Channel(
        @XmlSerialName("id")
        val id: String,
        @XmlElement(true)
        @XmlSerialName("display-name")
        val name: String,
        @XmlElement(true)
        @XmlSerialName("display-number")
        val number: String,
        @XmlSerialName("icon")
        val icon: Icon
    ) {
        @Serializable
        data class Icon(
            @XmlSerialName("src")
            val src: String
        )

    }


    @XmlSerialName("programme")
    @Serializable
    data class ProgramDTO(
        @XmlSerialName("title")
        @XmlElement(true)
        val title: String,
        @XmlSerialName("desc")
        @XmlElement(true)
        val desc: String,
        @XmlSerialName("category")
        @XmlElement(true)
        val category: String?,
        @XmlSerialName("icon")
        val icon: Icon?,
        @XmlSerialName("channel")
        val channel: String,
        @XmlSerialName("channel-number")
        val channelNumber: String?,
        @XmlSerialName("start")
        val start: String,
        @XmlSerialName("stop")
        val stop: String,
        @XmlSerialName("credits")
        val credits: Credits?
    ) {
        @Serializable
        data class Icon(
            @XmlSerialName("src")
            val src: String
        )

        @Serializable
        data class Credits(
            @XmlElement(true)
            @XmlSerialName("director")
            val director: String?,
            @XmlElement
            @XmlSerialName("actor")
            val listActor: List<String>?
        )
    }
}