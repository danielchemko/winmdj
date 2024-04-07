package com.github.danielchemko.winmdj.explore.utils

import com.github.danielchemko.winmdj.explore.model.WinMdContext
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

abstract class WinMdController {
    lateinit var stage: Stage
    lateinit var context: WinMdContext

    fun setup(stage: Stage, context: WinMdContext) {
        this.stage = stage
        this.context = context
    }

    fun getWindow(): Stage {
        return stage
    }

    companion object {
        fun <T : WinMdController> loadAndShowBlockingFxmlResource(
            clazz: KClass<T>,
            context: WinMdContext,
            rootStage: Stage
        ): FxmlResourceLoaded {
            val fxmlBinder = clazz.findAnnotation<FxmlResource>()
                ?: throw IllegalStateException("No FXMLResource attached to ${clazz.simpleName}")

            val loader = FXMLLoader(clazz.java.getResource(fxmlBinder.resource))

            val stage: Stage = if (fxmlBinder.newStage) {
                Stage()
            } else {
                rootStage
            }

            val root = loader.load<Parent>()
            val scene = Scene(root, fxmlBinder.sceneX.toDouble(), fxmlBinder.sceneY.toDouble())

            loader.getController<WinMdController>().setup(stage, context)

            stage.setTitle(fxmlBinder.windowTitle)
            stage.setScene(scene)
            stage.show()

            return FxmlResourceLoaded(loader, root)
        }
    }
}

data class FxmlResourceLoaded(val loader: FXMLLoader, val rootObject: Parent)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class FxmlResource(
    val resource: String,
    val newStage: Boolean = false,
    val sceneX: Int = 800,
    val sceneY: Int = 600,
    val windowTitle: String = "New Window"
)