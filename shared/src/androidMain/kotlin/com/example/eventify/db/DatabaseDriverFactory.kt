package com.example.eventify.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.eventify.db.AppDatabase // Importante: Esta classe foi gerada pelo SQLDelight

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        // "eventify.db" é o nome do ficheiro que ficará no telemóvel
        return AndroidSqliteDriver(AppDatabase.Schema, context, "eventify.db")
    }
}