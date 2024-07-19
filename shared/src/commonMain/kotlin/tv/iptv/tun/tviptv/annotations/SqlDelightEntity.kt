package tv.iptv.tun.tviptv.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlDelightEntity(val tableName: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlDelightColumn(val name: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlDelightPrimaryKey(
    val autoGenerate: Boolean = false
)