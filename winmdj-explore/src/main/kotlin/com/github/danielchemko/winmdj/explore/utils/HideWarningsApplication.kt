package com.github.danielchemko.winmdj.explore.utils

import javafx.application.Application
import java.io.ByteArrayOutputStream
import java.io.PrintStream

abstract class HideWarningsApplication : Application() {
    val oldOutStream: PrintStream = System.out
    val oldErrStream: PrintStream = System.err

    override fun init() {
        System.out.flush()
        System.err.flush()
        System.setOut(oldOutStream)
        System.setErr(oldErrStream)
    }
}