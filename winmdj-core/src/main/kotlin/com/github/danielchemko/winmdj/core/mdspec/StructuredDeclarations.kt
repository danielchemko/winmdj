package com.github.danielchemko.winmdj.core.mdspec

import com.github.danielchemko.winmdj.core.autoobject.model.CLRMetadataType

/* The target remote object table column values always walk positive integers starting at 1 */
const val CHILD_LIST_TERMINATOR_ASCENDING = 1
/* The target remote object table column values are always the same underlying value */
const val CHILD_LIST_TERMINATOR_REPEATING = 2

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ObjectColumn(
    val table: LookupType,
    val ordinal: Int = -1,
    val subOrdinal: Int = -1,
    val childListTerminator: Int = 0,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ObjectType(val objectType: CLRMetadataType)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InterfaceSpec(val typePrefixBits: Int, val classOrder: Array<String>)