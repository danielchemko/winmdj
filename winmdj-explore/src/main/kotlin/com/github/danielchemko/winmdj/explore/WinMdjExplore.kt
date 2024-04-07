package com.github.danielchemko.winmdj.explore

import com.github.danielchemko.winmdj.explore.model.WinMdContext
import com.github.danielchemko.winmdj.explore.root.RootController
import com.github.danielchemko.winmdj.explore.utils.HideWarningsApplication
import com.github.danielchemko.winmdj.explore.utils.WinMdController.Companion.loadAndShowBlockingFxmlResource
import com.sun.javafx.application.LauncherImpl.launchApplication
import javafx.stage.Stage
import mu.KotlinLogging

class WinMdjExplore : HideWarningsApplication() {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun start(stage: Stage) {
        val context = WinMdContext()

        stage.setOnCloseRequest {
            context.navigators.forEach { it.value.navigator.close() }
        }

        loadAndShowBlockingFxmlResource(RootController::class, context, stage)
    }
}

fun main(vararg args: String) {
    launchApplication(WinMdjExplore::class.java, args)
}
