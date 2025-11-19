package com.example.eventify

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform