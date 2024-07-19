package tv.iptv.tun.tviptv.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

private val nativeSqlDrive by lazy {
    NativeSqliteDriver(Database.Schema, "IVIPTV.db", maxReaderConnections = 10)
}
actual val sqlDriverFactory: SqlDriver
    get() = nativeSqlDrive
