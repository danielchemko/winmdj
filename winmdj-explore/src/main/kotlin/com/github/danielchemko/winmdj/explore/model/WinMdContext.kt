package com.github.danielchemko.winmdj.explore.model

import java.util.concurrent.ConcurrentHashMap

class WinMdContext {
    val navigators: MutableMap<String, WinMdFileContext> = ConcurrentHashMap()
}