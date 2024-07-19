package tv.iptv.tun.tviptv.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

private val nativeSqlDrive by lazy {
    NativeSqliteDriver(Database.Schema, "IVIPTV.db")
}
actual val sqlDriverFactory: SqlDriver
    get() = nativeSqlDrive
