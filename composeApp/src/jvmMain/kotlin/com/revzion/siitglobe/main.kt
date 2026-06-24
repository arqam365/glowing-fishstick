package com.revzion.siitglobe

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    // Tell macOS to follow system dark/light mode for window chrome (title bar, traffic lights)
    System.setProperty("apple.awt.application.appearance", "system")

    application {
        val iconBitmap = runCatching {
            val stream = object {}.javaClass.getResourceAsStream("/icon.png")
            if (stream != null) BitmapPainter(loadImageBitmap(stream)) else null
        }.getOrNull()

        runCatching {
            if (System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true) {
                val taskbar = java.awt.Taskbar.getTaskbar()
                val url = object {}.javaClass.getResource("/icon.png")
                if (url != null) taskbar.iconImage = javax.imageio.ImageIO.read(url)
            }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "SiiT",
            icon = iconBitmap,
        ) {
            App()
        }
    }
}
