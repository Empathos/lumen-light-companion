package org.lumen.lightcompanion.domain.event

import kotlinx.serialization.json.Json

val CompanionJson = Json {
    explicitNulls = false
    ignoreUnknownKeys = false
    encodeDefaults = true
}
