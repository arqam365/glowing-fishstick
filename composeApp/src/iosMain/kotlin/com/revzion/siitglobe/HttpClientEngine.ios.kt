package com.revzion.siitglobe.data.api

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun createHttpClientEngine(): HttpClientEngine = Darwin.create()
