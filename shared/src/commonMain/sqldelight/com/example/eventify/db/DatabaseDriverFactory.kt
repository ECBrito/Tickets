package com.example.eventify.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        // "eventify.db" é o nome do ficheiro que ficará no telemóvel
        return AndroidSqliteDriver(AppDatabase.Schema, context, "eventify.db")
    }
}