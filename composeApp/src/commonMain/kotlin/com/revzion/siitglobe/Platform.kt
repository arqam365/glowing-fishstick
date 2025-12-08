package com.revzion.siitglobe

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform