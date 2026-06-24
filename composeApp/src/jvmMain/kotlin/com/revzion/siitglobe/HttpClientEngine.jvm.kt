package com.revzion.siitglobe.data.api

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun createHttpClientEngine(): HttpClientEngine = CIO.create()
