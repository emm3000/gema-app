package com.emm.gema.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

fun inMemoryGemaDb(): GemaDb {
    val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    GemaDb.Schema.create(driver)
    return GemaDb(driver)
}
