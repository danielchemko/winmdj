package com.github.danielchemko.winmdj.core.mdspec

import com.github.danielchemko.winmdj.core.mdspec.CLRMetadataType.*
import com.github.danielchemko.winmdj.core.mdspec.LookupType.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.experimental.and

/***********************************************************************************************************************
 * This file contains all the class definitions, relations, and database index lookups necessary to construct all object
 * views. It uses Kotlin reflection to construct the view implementations based on these interface definitions. This
 * should be the only way that data is read from the MD base storage except for reading the PE/COFF headers
 **********************************************************************************************************************/

private const val ZERO_BYTE = 0x0.toByte()
private const val ONE_BYTE = 0x1.toByte()

/********************************************************
 * MD object definitions that will be constructed based on their annotated properties as well as handwritten
 * default methods which use auto-stubbed accessors t the underlying data
 */

@ObjectType(MODULE)
interface Module : WinMdObject, ResolutionScope, HasCustomAttribute {
    /* OUT Relationships */

    @ObjectColumn(TABLE_VALUE, 0)
    fun getGeneration(): UShort

    @ObjectColumn(STRING, 1)
    fun getName(): String

    @ObjectColumn(GUID, 2)
    fun getBaseGenerationId(): ByteArray

    @ObjectColumn(GUID, 3)
    fun getGenerationId(): ByteArray

    @ObjectColumn(GUID, 4)
    fun getMVid(): ByteArray
}

@ObjectType(TYPE_REF)
interface TypeReference : WinMdObject, TypeDefOrRef, MemberRefParent, ResolutionScope, HasCustomAttribute {
    /* OUT Relationships */
    @ObjectColumn(TARGET, 0)
    fun getResolutionScope(): ResolutionScope?

    @ObjectColumn(STRING, 1)
    fun getName(): String

    @ObjectColumn(STRING, 2)
    fun getNamespace(): String

    fun isNested(): Boolean {
        return getResolutionScope() is TypeReference
    }
}

@ObjectType(TYPE_DEF)
interface TypeDefinition : WinMdObject, TypeDefOrRef, TypeOrMethodDef, HasDeclSecurity, MemberRefParent,
    HasCustomAttribute {
    /* OUT Relationships */

    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    @ObjectColumn(STRING, 1)
    fun getName(): String

    @ObjectColumn(STRING, 2)
    fun getNamespace(): String

    @ObjectColumn(TARGET, 3)
    fun getExtends(): TypeDefOrRef?

    @ObjectColumn(TARGET_LIST, 4, childListTerminator = CHILD_LIST_TERMINATOR_PARENT_SEQUENTIAL)
    fun getFields(): List<Field>

    @ObjectColumn(TARGET_LIST, 5, childListTerminator = CHILD_LIST_TERMINATOR_PARENT_SEQUENTIAL)
    fun getMethods(): List<MethodDefinition>

    /* IN relationships */

    @ObjectColumn(REVERSE_TARGET, 0)
    fun getInterfaceImpl(): List<InterfaceImplementation>

    @ObjectColumn(REVERSE_TARGET, 1)
    fun getInterfaceDecl(): List<InterfaceImplementation>

    @ObjectColumn(REVERSE_TARGET, 0)
    fun getPropertyMaps(): List<PropertyMap>

    @ObjectColumn(REVERSE_TARGET, 2)
    fun getMethodDeclaration(): List<MethodImplementation>

    @ObjectColumn(REVERSE_TARGET, 1)
    fun getMethodBody(): List<MethodImplementation>


    // TODO add field bit helpers
//    visibility
//    class layout ??
//    class semantics ??
//    string format 20000
//    custom format ??
//    abstract 0080
//    sealed 0100
//    special name 0400
//    import 1000
//    serializable 2000
//    Windows Runtime 4000
//    Before Field Init 100000
//    RTSpecialName 0800
//    HasSecurity 40000
//    fun isNested(): Boolean {
//        return getAttributes().marshalInto(Visibility::class).isNested()
//    }
}

// 0-8 == VISIBILITY
val FIELD_ATTRIB_STATIC: Int = 9
val FIELD_ATTRIB_INITONLY: Int = 10
val FIELD_ATTRIB_LITERAL: Int = 11
val FIELD_ATTRIB_NOTSERIALIZED: Int = 12
val FIELD_ATTRIB_SPECIALNAME: Int = 18
val FIELD_ATTRIB_RTSPECIALNAME: Int = 19
val FIELD_ATTRIB_HASRVA: Int = 17
val FIELD_ATTRIB_PINVOKEIMPL: Int = 26

@ObjectType(FIELD)
interface Field : WinMdObject, HasConstant, HasFieldMarshal, MemberForwarded, HasCustomAttribute {
    /* OUT relationships */

    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    @ObjectColumn(STRING, 1)
    fun getName(): String

    @ObjectColumn(BLOB, 2)
    fun getSignature(): ByteArray

    // TODO add flags for attributes
//    field access
//    static
//    init only
//    literal
//    not-serialized
//    special-name
//    pinvokeimpl
//    rtspecialname
//    hasfieldmarshal
//    hasdefault
//    hasfieldrva

    fun isRtSpecialName(): Boolean {
        return getAttributes().get(FIELD_ATTRIB_RTSPECIALNAME)
    }

    /* IN Relationships */

    @ObjectColumn(REVERSE_TARGET, 4)
    fun getParent(): TypeDefinition?
}

@ObjectType(METHOD_DEF)
interface MethodDefinition : WinMdObject, TypeOrMethodDef, HasDeclSecurity, MemberRefParent, MethodDefOrRef,
    MemberForwarded, CustomAttributeType, HasCustomAttribute {
    /* OUT Relationships */

    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    @ObjectColumn(BITSET, 1)
    fun getImplementationAttributes(): BitSet

    @ObjectColumn(TABLE_VALUE, 2)
    fun getRva(): UShort

    @ObjectColumn(STRING, 3)
    fun getName(): String

    @ObjectColumn(BLOB, 4)
    fun getSignature(): ByteArray

    @ObjectColumn(TARGET_LIST, 5, 1, childListTerminator = CHILD_LIST_TERMINATOR_ASCENDING)
    fun getParameters(): List<Parameter>

    /* IN Relationships */

    @ObjectColumn(REVERSE_TARGET, 5)
    fun getParent(): TypeDefinition?
}

@ObjectType(PARAM)
interface Parameter : WinMdObject, HasConstant, HasFieldMarshal, HasCustomAttribute {

    /* OUT Relationships */

    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    // IN 1
    // OUT 2
    // LCID 3
    // RETVAL 4
    // OPTIONAL 9
    // HASDEFAULT 25
    // HASFIELDMARSHAL 26
    // RESERVED3 27
    // RESERVED4 28

    @ObjectColumn(TABLE_VALUE, 1)
    fun getSequence(): UShort

    @ObjectColumn(STRING, 2)
    fun getName(): String

    /* IN Relationships */


}

@ObjectType(INTERFACE_IMPL)
interface InterfaceImplementation : WinMdObject, HasCustomAttribute {
    @ObjectColumn(TARGET, 0)
    fun getTypeDefinition(): TypeDefinition

    @ObjectColumn(TARGET, 1)
    fun getInterface(): TypeDefOrRef
}

@ObjectType(MEMBER_REF)
interface MemberReference : WinMdObject, MethodDefOrRef, CustomAttributeType, HasCustomAttribute {
    @ObjectColumn(TARGET, 0)
    fun getParent(): MemberRefParent?

    @ObjectColumn(STRING, 1)
    fun getName(): String

    @ObjectColumn(BLOB, 2)
    fun getSignature(): ByteArray
}

@ObjectType(CONSTANT)
interface Constant : WinMdObject {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getRawType(): UShort

    fun getType(): PrimitiveType {
        // Throws if garbage -- should revise?
        return PrimitiveType.values().first { it.code == getRawType() }
    }

    @ObjectColumn(TARGET, 1)
    fun getParent(): HasConstant?

    @ObjectColumn(BLOB, 2)
    fun getValueBlob(): ByteArray

    fun getAsBoolean(): Boolean {
        return (getValueBlob()[0] and ONE_BYTE) == ONE_BYTE
    }

    fun getChar(): Char {
        return StandardCharsets.UTF_16LE.decode(ByteBuffer.wrap(getValueBlob())).get()
    }

    fun getByte(): Byte {
        return getValueBlob()[0]
    }

    fun getUByte(): UByte {
        return getValueBlob()[0].toUByte()
    }

    fun getShort(): Short {
        return ByteBuffer.wrap(getValueBlob()).order(ByteOrder.LITTLE_ENDIAN).getShort()
    }

    fun getUShort(): UShort {
        return ByteBuffer.wrap(getValueBlob()).order(ByteOrder.LITTLE_ENDIAN).getShort().toUShort()
    }

    fun getInt(): Int {
        return ByteBuffer.wrap(getValueBlob()).order(ByteOrder.LITTLE_ENDIAN).getInt()
    }

    fun getUInt(): UInt {
        return ByteBuffer.wrap(getValueBlob()).order(ByteOrder.LITTLE_ENDIAN).getInt().toUInt()
    }

    fun getLong(): Long {
        return ByteBuffer.wrap(getValueBlob()).order(ByteOrder.LITTLE_ENDIAN).getLong()
    }

    fun getULong(): ULong {
        return ByteBuffer.wrap(getValueBlob()).order(ByteOrder.LITTLE_ENDIAN).getLong().toULong()
    }

    fun getFloat(): Float {
        return ByteBuffer.wrap(getValueBlob()).order(ByteOrder.LITTLE_ENDIAN).getFloat()
    }

    fun getDouble(): Double {
        return ByteBuffer.wrap(getValueBlob()).order(ByteOrder.LITTLE_ENDIAN).getDouble()
    }

    fun getString(): String {
        return String(getValueBlob(), StandardCharsets.UTF_16LE)
    }

    fun getValueClass(): Any? {
        return null
    }

    fun getValue(): Any? {
        return when (getType()) {
            PrimitiveType.BOOLEAN -> getAsBoolean()
            PrimitiveType.CHAR -> getChar()
            PrimitiveType.FLOAT32 -> getFloat()
            PrimitiveType.FLOAT64 -> getDouble()
            PrimitiveType.INT8 -> getByte()
            PrimitiveType.UINT8 -> getUByte()
            PrimitiveType.INT16 -> getShort()
            PrimitiveType.UINT16 -> getUShort()
            PrimitiveType.INT32 -> getInt()
            PrimitiveType.UINT32 -> getUInt()
            PrimitiveType.INT64 -> getLong()
            PrimitiveType.UINT64 -> getULong()
            PrimitiveType.STRING -> getString()
            PrimitiveType.CLASS -> getValueClass()
        }
    }
}

@ObjectType(CUSTOM_ATTRIBUTE)
interface CustomAttribute : WinMdObject {
    @ObjectColumn(TARGET, 0)
    fun getParent(): HasCustomAttribute?

    @ObjectColumn(TARGET, 1)
    fun getConstructor(): CustomAttributeType?

    @ObjectColumn(BLOB, 2)
    fun getValue(): ByteArray
}

@ObjectType(FIELD_MARSHAL)
interface FieldMarshal : WinMdObject {
    @ObjectColumn(TARGET, 0)
    fun getParent(): HasFieldMarshal?

    @ObjectColumn(BLOB, 1)
    fun getNativeType(): ByteArray
}

@ObjectType(DECL_SECURITY)
interface SecurityAttribute : WinMdObject {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getAction(): UShort

    @ObjectColumn(TARGET, 1)
    fun getParent(): HasDeclSecurity?

    @ObjectColumn(BLOB, 2)
    fun getPermissionSet(): ByteArray
}

@ObjectType(CLASS_LAYOUT)
interface ClassLayout : WinMdObject {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getPackingSize(): UShort

    @ObjectColumn(TABLE_VALUE, 1)
    fun getClassSize(): UInt

    @ObjectColumn(TARGET, 2)
    fun getParent(): TypeDefinition?
}

@ObjectType(FIELD_LAYOUT)
interface FieldLayout : WinMdObject {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getFieldOffset(): UInt

    @ObjectColumn(TARGET, 1)
    fun getField(): Field?
}

@ObjectType(STAND_ALONE_SIG)
interface StandAloneMethodSignature : WinMdObject, HasCustomAttribute {
    @ObjectColumn(BLOB, 0)
    fun getSignature(): ByteArray
}

@ObjectType(EVENT_MAP)
interface EventMap : WinMdObject {
    @ObjectColumn(TARGET, 0)
    fun getParent(): TypeDefinition?

    @ObjectColumn(TARGET_LIST, 1, childListTerminator = CHILD_LIST_TERMINATOR_PARENT_SEQUENTIAL)
    fun getEvents(): List<Event>
}

@ObjectType(EVENT)
interface Event : WinMdObject, HasSemantics, HasCustomAttribute {
    /* OUT Relationships */

    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    @ObjectColumn(TABLE_VALUE, 1)
    fun getName(): String

    @ObjectColumn(TARGET, 2)
    fun getEventType(): TypeDefOrRef?

    /* IN Relationships */

    @ObjectColumn(REVERSE_TARGET, 1)
    fun getEventMap(): EventMap
}

@ObjectType(PROPERTY_MAP)
interface PropertyMap : WinMdObject {
    @ObjectColumn(TARGET, 0)
    fun getParent(): TypeDefinition?

    @ObjectColumn(TARGET_LIST, 1, childListTerminator = CHILD_LIST_TERMINATOR_PARENT_SEQUENTIAL)
    fun getProperties(): List<Property>
}

@ObjectType(PROPERTY)
interface Property : WinMdObject, HasConstant, HasSemantics, HasCustomAttribute {
    /* OUT Relationships */

    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    @ObjectColumn(STRING, 1)
    fun getName(): String

    @ObjectColumn(BLOB, 2)
    fun getSignature(): ByteArray

    /* IN Relationships */

    @ObjectColumn(REVERSE_TARGET, 1, childListTerminator = CHILD_LIST_TERMINATOR_PARENT_SEQUENTIAL)
    fun getPropertyMap(): PropertyMap
}

@ObjectType(METHOD_SEMANTICS)
interface MethodSemantics : WinMdObject {
    @ObjectColumn(BITSET, 0)
    fun getAttribute(): BitSet

    @ObjectColumn(TARGET, 1)
    fun getMethod(): MethodDefinition?

    @ObjectColumn(TARGET, 2)
    fun getAssociation(): HasSemantics?
}

@ObjectType(METHOD_IMPL)
interface MethodImplementation : WinMdObject {
    @ObjectColumn(TARGET, 0)
    fun getType(): TypeDefinition?

    @ObjectColumn(TARGET, 1)
    fun getMethodDeclaration(): MethodDefOrRef?

    @ObjectColumn(TARGET, 2)
    fun getMethodBody(): MethodDefOrRef?
}

@ObjectType(MODULE_REF)
interface ModuleReference : WinMdObject, MemberRefParent, ResolutionScope, HasCustomAttribute {
    @ObjectColumn(STRING, 0)
    fun getName(): String
}

@ObjectType(TYPE_SPEC)
interface TypeSpecification : WinMdObject, TypeDefOrRef, MemberRefParent, HasCustomAttribute {
    @ObjectColumn(BLOB, 0)
    fun getSignatureRaw(): ByteArray

    fun getSignature(): String {
        getSignatureRaw()
        // Parse from signature.h
        TODO()
    }
}

@ObjectType(IMPL_MAP)
interface ImplementationMap : WinMdObject {
    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    // charset
    // calling convention
    // NO_MANGLE 1
    // SUPPORTS_LAST_ERROR 11
    // BEST_FIT_ENABLED 9
    // BEST_FIT_DISABLED 10
    // THROW_ON_UNMAPPABLE_CHAR_ENABLED 25
    // THROW_ON_UNMAPPABLE_CHAR_DISABLED 26

    @ObjectColumn(TARGET, 1)
    fun getMemberForwarded(): MemberForwarded?

    @ObjectColumn(STRING, 2)
    fun getImportName(): String

    @ObjectColumn(TARGET, 3)
    fun getTargetScope(): ModuleReference?
}

@ObjectType(FIELD_RVA)
interface FieldRva : WinMdObject {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getFieldOffset(): UInt

    @ObjectColumn(TARGET, 1)
    fun getField(): Field?
}

@ObjectType(ASSEMBLY)
interface Assembly : WinMdObject, HasDeclSecurity, HasCustomAttribute {
    /* OUT Relationships */

    @ObjectColumn(BITSET_ENUM, 0)
    fun getHashAlgorithmId(): List<AssemblyHashAlgorithm>

    @ObjectColumn(TABLE_VALUE, 1)
    fun getVersionRaw(): ULong

    fun getVersion(): AssemblyVersion {
        return AssemblyVersion.from(getVersionRaw())
    }

    @ObjectColumn(BITSET_ENUM, 2)
    fun getFlags(): List<AssemblyFlags>

    @ObjectColumn(BLOB, 3)
    fun getPublicKey(): ByteArray

    @ObjectColumn(STRING, 4)
    fun getName(): String

    @ObjectColumn(STRING, 5)
    fun getCulture(): String
}

@ObjectType(ASSEMBLY_PROCESSOR)
interface AssemblyProcessor : WinMdObject {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getProcessor(): UInt
}

@ObjectType(ASSEMBLY_OS)
interface AssemblyOs : WinMdObject {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getOSPlatformId(): UInt

    @ObjectColumn(TABLE_VALUE, 1)
    fun getOSMajorVersion(): UInt

    @ObjectColumn(TABLE_VALUE, 2)
    fun getOSMinorVersion(): UInt
}

@ObjectType(ASSEMBLY_REF)
interface AssemblyReference : WinMdObject, Implementation, ResolutionScope, HasCustomAttribute {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getVersionRaw(): ULong

    fun getVersion(): AssemblyVersion {
        return AssemblyVersion.from(getVersionRaw())
    }

    @ObjectColumn(BITSET_ENUM, 1)
    fun getFlags(): List<AssemblyFlags>

    @ObjectColumn(TABLE_VALUE, 2)
    fun getAsToken(): UInt

    @ObjectColumn(BLOB, 2)
    fun getAsPublicKey(): ByteArray

    @ObjectColumn(STRING, 3)
    fun getName(): String

    @ObjectColumn(STRING, 4)
    fun getCulture(): String

    @ObjectColumn(BLOB, 5)
    fun getAnonymousBlob(): ByteArray
}

@ObjectType(ASSEMBLY_REF_PROCESSOR)
interface AssemblyReferenceProcessor : WinMdObject {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getProcessor(): UInt

    @ObjectColumn(TARGET, 1)
    fun getAssemblyRef(): AssemblyReference?
}

@ObjectType(ASSEMBLY_REF_OS)
interface AssemblyReferenceOs : WinMdObject {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getOSPlatformId(): UInt

    @ObjectColumn(TABLE_VALUE, 1)
    fun getOSMajorVersion(): UInt

    @ObjectColumn(TABLE_VALUE, 2)
    fun getOSMinorVersion(): UInt

    @ObjectColumn(TARGET, 3)
    fun getAssemblyReference(): AssemblyReference?
}

@ObjectType(FILE)
interface File : WinMdObject, Implementation, HasCustomAttribute {
    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    @ObjectColumn(STRING, 1)
    fun getFileName(): String

    @ObjectColumn(BLOB, 2)
    fun getContents(): ByteArray
}

@ObjectType(EXPORTED_TYPE)
interface ExportedType : WinMdObject, Implementation, HasCustomAttribute {
    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    @ObjectColumn(TABLE_VALUE, 1)
    fun getTypeDefId(): UInt

    @ObjectColumn(STRING, 2)
    fun getTypeName(): String

    @ObjectColumn(STRING, 3)
    fun getTypeNamespace(): String

    @ObjectColumn(TARGET, 4)
    fun getImplementation(): Implementation?
}

@ObjectType(MANIFEST_RESOURCE)
interface ManifestResource : WinMdObject, HasCustomAttribute {
    @ObjectColumn(BITSET, 0)
    fun getAttributes(): BitSet

    @ObjectColumn(TABLE_VALUE, 0)
    fun getMiscXXX(): UInt

    @ObjectColumn(STRING, 2)
    fun getName(): String

    @ObjectColumn(TARGET, 3)
    fun getImplementation(): Implementation?
}

@ObjectType(NESTED_CLASS)
interface NestedClass : WinMdObject {
    @ObjectColumn(TARGET, 0)
    fun getNestedClass(): TypeDefinition

    @ObjectColumn(TARGET, 1)
    fun getEnclosingType(): TypeDefinition
}

@ObjectType(GENERIC_PARAM)
interface GenericParameter : WinMdObject, HasCustomAttribute {
    @ObjectColumn(TABLE_VALUE, 0)
    fun getNumber(): UShort

    @ObjectColumn(BITSET, 1)
    fun getAttributes(): BitSet

    @ObjectColumn(TARGET, 2)
    fun getOwner(): TypeOrMethodDef?

    @ObjectColumn(STRING, 3)
    fun getName(): String
}

@ObjectType(METHOD_SPEC)
interface MethodSpecification : WinMdObject, HasCustomAttribute {
    @ObjectColumn(TARGET, 0)
    fun getMethod(): MethodDefOrRef?

    @ObjectColumn(BLOB, 1)
    fun getSignature(): ByteArray
}

@ObjectType(GENERIC_PARAM_CONSTRAINT)
interface GenericParameterConstraint : WinMdObject, HasCustomAttribute {
    @ObjectColumn(TARGET, 0)
    fun getOwner(): GenericParameter

    @ObjectColumn(TARGET, 1)
    fun getType(): TypeDefinition
}
