package com.github.danielchemko.winmdj.java

import com.github.danielchemko.winmdj.parser.WinMdNavigator
import java.nio.file.Path

/**
 * Just a test app to play with the data
 */
fun main() {
    val navigator = WinMdNavigator()
    navigator.parseFile(Path.of("Windows.Win32.winmd"))
    ModuleBuilder(navigator).doMagic()
}