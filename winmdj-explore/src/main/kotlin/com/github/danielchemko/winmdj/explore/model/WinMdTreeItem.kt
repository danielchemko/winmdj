package com.github.danielchemko.winmdj.explore.model

import javafx.scene.image.Image

data class WinMdTreeItem(
    val text: String? = null,
    val icon: Image? = null,
    val actions: List<WinMdTreeItemAction> = emptyList()
) {
    override fun toString(): String {
        return text ?: ""
    }
}

data class WinMdTreeItemAction(val actionText: String, val action: () -> Unit)
