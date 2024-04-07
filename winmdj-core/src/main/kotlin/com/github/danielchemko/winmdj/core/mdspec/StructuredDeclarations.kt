package com.github.danielchemko.winmdj.core.mdspec

/* The target remote object table column values always walk positive integers starting at 1 */
const val CHILD_LIST_TERMINATOR_ASCENDING = 1
/* The target remote object table column values are always the same underlying value */
const val CHILD_LIST_TERMINATOR_REPEATING = 2
/* The termination point of this link is the next outward number in the origin list */
const val CHILD_LIST_TERMINATOR_PARENT_SEQUENTIAL = 3

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
