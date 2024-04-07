package com.github.danielchemko.winmdj.explore.root

import javafx.scene.control.TableCell
import javafx.scene.text.Text

@OptIn(ExperimentalStdlibApi::class)
class HexCell<T : Any>(private val renderedDigits: Int) : TableCell<T, Number?>() {
    private val text = Text("...")

    @OptIn(ExperimentalStdlibApi::class)
    override fun updateItem(item: Number?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            graphic = null
        } else {
            // TODO can be neater
            text.text = when (item) {
                is Long -> item.toHexString(HexFormat.UpperCase)
                    .let { val l = it.length; it.substring(l - renderedDigits, l) }

                is Integer -> item.toInt().toHexString(HexFormat.UpperCase)
                    .let { val l = it.length; it.substring(l - renderedDigits, l) }

                is Short -> item.toHexString(HexFormat.UpperCase)
                    .let { val l = it.length; it.substring(l - renderedDigits, l) }

                else -> item!!.toString().toLong().toHexString(HexFormat.UpperCase)
            }

            graphic = text
        }
    }
}