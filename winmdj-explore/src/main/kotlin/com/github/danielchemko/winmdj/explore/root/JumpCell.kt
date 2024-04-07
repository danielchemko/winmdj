package com.github.danielchemko.winmdj.explore.root

import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType
import com.github.danielchemko.winmdj.core.mdspec.WinMdCompositeReference
import com.github.danielchemko.winmdj.core.mdspec.WinMdObject
import com.github.danielchemko.winmdj.core.mdspec.getClassInterface
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Hyperlink
import javafx.scene.control.TableCell
import javafx.scene.paint.Paint
import javafx.scene.text.Text
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

@OptIn(ExperimentalStdlibApi::class)
class JumpCell<T : Any>(
    val navigator: WinMdNavigator,
    val clazz: KClass<T>,
    val targetType: CLRMetadataType?,
    val targetClazz: KClass<*>,
    val jumpCallback: (KClass<*>, Int) -> Unit,
) : TableCell<T, Any?>() {
    private val link = Hyperlink("...")
    private val text = Text("...")

    init {
        link.textFill = Paint.valueOf("black")
        link.style = "-fx-focus-color: transparent; -fx-underline: true;"
        link.onAction = EventHandler { _: ActionEvent? ->
            if (WinMdObject::class.isSuperclassOf(targetClazz)) {
                val rowVal = super.getItem()!!.toString().toUInt().toInt()
                jumpCallback.invoke(targetClazz, rowVal)
            } else if (WinMdCompositeReference::class.isSuperclassOf(targetClazz)) {
                val ptrVal = super.getItem()!!.toString().toUInt()
                navigator.calculateInterfacePtr(targetClazz as KClass<out WinMdCompositeReference>, ptrVal)
                    ?.let { (concreteClazz, row) -> jumpCallback.invoke(concreteClazz, row) }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun updateItem(item: Any?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            graphic = null
            return
        } else if(item is String) {
            text.text = item.toString()
            graphic = text
            return
        }

        // TODO does this work on shorts???

//        val valueStr: String
        val linkGood = if (WinMdObject::class.isSuperclassOf(targetClazz)) {
            val valueNum = item!!.toString().toUInt().toInt()
            if (valueNum <= 0 && valueNum > navigator.getCount(targetType!!)) {
//                valueStr = item.toString()
                false
            } else {
//                valueStr = valueNum.toString()
                true
            }
        } else if (WinMdCompositeReference::class.isSuperclassOf(targetClazz)) {
//            val vrow = navigator.calculateInterfacePtr(targetClazz as KClass<out WinMdCompositeReference>, valueNum.toUInt())
//            if (vrow != null) {
//                valueStr = vrow.second.toUInt().toHexString(HexFormat.UpperCase)
                true
//            } else {
////                valueStr = "(Invalid)"
//                false
//            }
        } else {
//            valueStr = "(N/A)"
            false
        }

        if (linkGood) {
            link.text = item.toString()
            graphic = link
        } else {
            text.text = item.toString()
            graphic = text
        }
    }
}