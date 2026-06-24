package com.revzion.siitglobe

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.time.LocalDateTime

private val logFile: File by lazy {
    val dir = File(System.getProperty("user.home"), "SiiT-logs")
    dir.mkdirs()
    File(dir, "startup.log")
}

private fun log(msg: String) {
    val line = "[${LocalDateTime.now()}] $msg\n"
    print(line)
    runCatching { logFile.appendText(line) }
}

fun main() {
    log("=== SiiT starting ===")
    log("OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
    log("JVM: ${System.getProperty("java.version")} (${System.getProperty("java.vendor")})")
    log("User home: ${System.getProperty("user.home")}")
    log("Log file: ${logFile.absolutePath}")

    try {
        // macOS: follow system dark/light mode for window chrome
        System.setProperty("apple.awt.application.appearance", "system")
        log("System properties set")

        application {
            log("Application block entered")

            val iconBitmap = runCatching {
                val stream = object {}.javaClass.getResourceAsStream("/icon.png")
                if (stream != null) {
                    log("Icon resource found")
                    BitmapPainter(loadImageBitmap(stream))
                } else {
                    log("WARNING: icon.png resource not found")
                    null
                }
            }.onFailure { log("ERROR loading icon: ${it.message}") }.getOrNull()

            runCatching {
                if (System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true) {
                    val taskbar = java.awt.Taskbar.getTaskbar()
                    val url = object {}.javaClass.getResource("/icon.png")
                    if (url != null) taskbar.iconImage = javax.imageio.ImageIO.read(url)
                    log("macOS dock icon set")
                }
            }.onFailure { log("WARNING: macOS dock icon failed: ${it.message}") }

            log("Creating Window...")
            Window(
                onCloseRequest = {
                    log("Window close requested")
                    exitApplication()
                },
                title = "SiiT",
                icon = iconBitmap,
            ) {
                log("Window created, loading App()")
                try {
                    App()
                    log("App() loaded successfully")
                } catch (e: Throwable) {
                    log("FATAL: App() crashed: ${e.message}\n${e.stackTraceToString()}")
                    throw e
                }
            }
        }

        log("Application exited normally")
    } catch (e: Throwable) {
        log("FATAL CRASH: ${e.message}\n${e.stackTraceToString()}")
        // Show a plain AWT error dialog so the user isn't left with nothing
        try {
            val msg = "SiiT failed to start.\n\nError: ${e.message}\n\nCheck log: ${logFile.absolutePath}"
            javax.swing.JOptionPane.showMessageDialog(null, msg, "SiiT Error", javax.swing.JOptionPane.ERROR_MESSAGE)
        } catch (_: Throwable) {}
    }
}
