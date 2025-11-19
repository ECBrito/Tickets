package com.example.eventify.db

import app.cash.sqldelight.db.SqlDriver

// Declaramos que esperamos uma implementação desta classe em cada plataforma
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}