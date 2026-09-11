package com.emm.gema.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

private const val DATABASE_NAME = "gema.db"

fun createGemaDb(context: Context): GemaDb {
    val driver: SqlDriver = AndroidSqliteDriver(
        schema = GemaDb.Schema,
        context = context,
        name = DATABASE_NAME,
    )
    return GemaDb(driver)
}
