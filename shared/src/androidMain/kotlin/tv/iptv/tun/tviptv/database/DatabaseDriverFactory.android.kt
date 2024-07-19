package tv.iptv.tun.tviptv.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import tv.iptv.tun.tviptv.App

private val androidSqliteDriver by lazy {
    AndroidSqliteDriver(Database.Schema, App.get(), "IVIPTV.db")
}
actual val sqlDriverFactory: SqlDriver
    get() = androidSqliteDriver
