package com.github.danielchemko.winmdj.core.mdspec

import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.autoobject.model.CLRMetadataType
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import java.util.*
import kotlin.reflect.KClass


/********************************************************
 * Interface stereotypes that can be attached to many different MD object types. These are declarations and accessors
 * that are shared among more than a single type of MD object type
 */

sealed interface WinMdCompositeReference

interface WinMdStub {
    fun setRowNumberIndex(index: Int)
    fun getRowNumber(): Int
    fun getObjectMapper(): MdObjectMapper
    fun getNavigator(): WinMdNavigator
    fun getToken(type: CLRMetadataType): UInt
    fun getObjectTableOffset(type: CLRMetadataType, columnIndex: Int): Int
    fun getObjectTableValue(type: CLRMetadataType, columnIndex: Int): Any
    fun getRandomObjectTableValue(type: CLRMetadataType, row: Int, columnIndex: Int): Any
    fun <T : Any> lookupTableValue(type: CLRMetadataType, columnIndex: Int, responseType: KClass<T>?): T
    fun lookupString(type: CLRMetadataType, columnIndex: Int): String
    fun lookupBlob(type: CLRMetadataType, columnIndex: Int): ByteArray
    fun lookupGuid(type: CLRMetadataType, columnIndex: Int): ByteArray
    fun <T : WinMdCompositeReference> lookupInterfaceReferent(
        type: CLRMetadataType,
        columnIndex: Int,
        kClass: KClass<T>
    ): T?

    fun <T : WinMdObject> lookupConcreteReferent(type: CLRMetadataType, columnIndex: Int, kClass: KClass<T>): T?
    fun <T : Any> getReverseReferentSingle(
        remoteType: CLRMetadataType,
        remoteColumnIndex: Int,
        kClass: KClass<T>,
        matchValue: Any
    ): T?

    fun <T : Any> getReverseReferentPlural(
        remoteType: CLRMetadataType,
        remoteColumnIndex: Int,
        kClass: KClass<T>,
        matchValue: Any
    ): List<T>

    fun lookupBitset(type: CLRMetadataType, columnIndex: Int): BitSet
    fun <T : ValueEnum<*, *>> lookupBitsetEnum(type: CLRMetadataType, columnIndex: Int, enumClazz: KClass<T>): List<T>
    fun <T : WinMdObject> lookupList(
        type: CLRMetadataType,
        columnIndex: Int,
        subOrdinal: Int,
        childListTerminator: Int,
        kClass: KClass<T>
    ): List<T>
}

sealed interface WinMdObject {
    fun getToken(): UInt
    fun getOffset(): UInt
    fun getStub(): WinMdStub
    fun copy(rowNum: Int): WinMdObject
}

@InterfaceSpec(2, classOrder = ["Module", "ModuleReference", "AssemblyReference", "TypeReference"])
sealed interface ResolutionScope : WinMdCompositeReference

@InterfaceSpec(
    5, classOrder = ["MethodDefinition", "Field", "TypeReference", "TypeDefinition", "Parameter",
        "InterfaceImplementation", "MemberReference", "Module", "Property", "Event", "StandAloneMethodSignature",
        "ModuleReference", "TypeSpecification", "Assembly", "AssemblyReference", "File", "ExportedType",
        "ManifestResource", "GenericParameter", "GenericParameterConstraint", "MethodSpecification"]
)
sealed interface HasCustomAttribute : WinMdCompositeReference

@InterfaceSpec(2, classOrder = ["TypeDefinition", "TypeReference", "TypeSpecification"])
sealed interface TypeDefOrRef : WinMdCompositeReference

@InterfaceSpec(3, classOrder = ["TypeDefinition", "TypeReference", "ModuleReference", "MethodDefinition", "TypeSpecification"])
sealed interface MemberRefParent : WinMdCompositeReference

@InterfaceSpec(2, classOrder = ["Field", "Parameter", "Property"])
sealed interface HasConstant : WinMdCompositeReference {
    @ObjectColumn(LookupType.REVERSE_TARGET, 1)
    fun getConstant(): Constant?
}

@InterfaceSpec(1, classOrder = ["TypeDefinition", "MethodDefinition"])
sealed interface TypeOrMethodDef : WinMdCompositeReference {
    @ObjectColumn(LookupType.REVERSE_TARGET, 2)
    fun getGenericParameters(): List<GenericParameter>
}

@InterfaceSpec(1, classOrder = ["Field", "Parameter"])
sealed interface HasFieldMarshal : WinMdCompositeReference {
    @ObjectColumn(LookupType.REVERSE_TARGET, 0)
    fun getFieldMarshal(): FieldMarshal?
}

@InterfaceSpec(2, classOrder = ["TypeDefinition", "MethodDefinition", "Assembly"])
sealed interface HasDeclSecurity : WinMdCompositeReference {
    @ObjectColumn(LookupType.REVERSE_TARGET, 1)
    fun getSecurityAttribute(): SecurityAttribute?
}

@InterfaceSpec(1, classOrder = ["Event", "Property"])
sealed interface HasSemantics : WinMdCompositeReference {
    @ObjectColumn(LookupType.REVERSE_TARGET, 0)
    fun getSemantics(): MethodSemantics
}

@InterfaceSpec(1, classOrder = ["MethodDefinition", "MemberReference"])
sealed interface MethodDefOrRef : WinMdCompositeReference

@InterfaceSpec(1, classOrder = ["Field", "MethodDefinition"])
sealed interface MemberForwarded : WinMdCompositeReference

@InterfaceSpec(2, classOrder = ["File", "AssemblyReference", "ExportedType"])
sealed interface Implementation : WinMdCompositeReference

@InterfaceSpec(3, classOrder = ["", "", "MethodDefinition", "MemberReference", ""])
sealed interface CustomAttributeType : WinMdCompositeReference
