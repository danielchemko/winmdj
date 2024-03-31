package com.github.danielchemko.winmdj.core.mdspec

import java.util.*
import kotlin.reflect.KClass

sealed interface ValueEnum<T, R> {
    fun getCode(): Any
}


fun <T : ValueEnum<R, *>, R> BitSet.marshalInto(enumClass: KClass<T>): T {
//    enumClass.java.enumConstants.map { (it as ValueEnum<*,*>).getCode() == this. }
    return enumClass.java.enumConstants.first { this.get((it as ValueEnum<*, *>).getCode().toString().toInt()) }
}

enum class CLRMetadataType(val bitSetIndex: Int) {
    MODULE(0x00),
    TYPE_REF(0x01),
    TYPE_DEF(0x02),
    FIELD(0x04),
    METHOD_DEF(0x06),
    PARAM(0x08),
    INTERFACE_IMPL(0x09),
    MEMBER_REF(0x0A),
    CONSTANT(0x0B),
    CUSTOM_ATTRIBUTE(0x0C),
    FIELD_MARSHAL(0x0D),
    DECL_SECURITY(0x0E),
    CLASS_LAYOUT(0x0F),
    FIELD_LAYOUT(0x10),
    STAND_ALONE_SIG(0x11),
    EVENT_MAP(0x12),
    EVENT(0x14),
    PROPERTY_MAP(0x15),
    PROPERTY(0x17),
    METHOD_SEMANTICS(0x18),
    METHOD_IMPL(0x19),
    MODULE_REF(0x1A),
    TYPE_SPEC(0x1B),
    IMPL_MAP(0x1C),
    FIELD_RVA(0x1D),
    ASSEMBLY(0x20),
    ASSEMBLY_PROCESSOR(0x21),
    ASSEMBLY_OS(0x22),
    ASSEMBLY_REF(0x23),
    ASSEMBLY_REF_PROCESSOR(0x24),
    ASSEMBLY_REF_OS(0x25),
    FILE(0x26),
    EXPORTED_TYPE(0x27),
    MANIFEST_RESOURCE(0x28),
    NESTED_CLASS(0x29),
    GENERIC_PARAM(0x2A),
    METHOD_SPEC(0x2B),
    GENERIC_PARAM_CONSTRAINT(0x2c),
    ;

    companion object {
        fun fromIndex(idx: Int): CLRMetadataType? {
            return entries.firstOrNull { it.bitSetIndex == idx }
        }
    }
}


enum class LookupType {
    /* String table direct lookup */
    STRING,

    /* Blob table direct lookup */
    BLOB,

    /* GUID table direct lookup */
    GUID,

    /* Directed Pointer to an Object */
    TARGET,

    /* Extract the value directly from the table field */
    TABLE_VALUE,

    /* Directed Pointer to the 0th item of a list value series */
    TARGET_LIST,

    /* Find a list of foreign objects referring to this object based on _their_ TARGET column specified in this
       annotation */
    REVERSE_TARGET,

    /* Derived from bitset field */
    BITSET,

    /* Derived from bitset field to an enum */
    BITSET_ENUM,
}

enum class PrimitiveType(val code: UShort) : ValueEnum<UShort, PrimitiveType> {
    BOOLEAN(0x02.toUShort()),
    CHAR(0x03.toUShort()),
    INT8(0x04.toUShort()),
    UINT8(0x05.toUShort()),
    INT16(0x06.toUShort()),
    UINT16(0x07.toUShort()),
    INT32(0x08.toUShort()),
    UINT32(0x09.toUShort()),
    INT64(0x0a.toUShort()),
    UINT64(0x0b.toUShort()),
    FLOAT32(0x0c.toUShort()),
    FLOAT64(0x0d.toUShort()),
    STRING(0x0e.toUShort()),
    CLASS(0x12.toUShort()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class ElementType(val code: UByte) : ValueEnum<UByte, ElementType> {
    BOOLEAN(0x02.toUByte()),
    CHAR(0x03.toUByte()),
    FLOAT32(0x0c.toUByte()),
    FLOAT64(0x0d.toUByte()),
    INT8(0x04.toUByte()),
    UINT8(0x05.toUByte()),
    INT16(0x06.toUByte()),
    UINT16(0x07.toUByte()),
    INT32(0x08.toUByte()),
    UINT32(0x09.toUByte()),
    INT64(0x0a.toUByte()),
    UINT64(0x0b.toUByte()),
    PTR(0x18.toUByte()),

    /* System.IntPtr */
    UPTR(0x19.toUByte()),

    /* System.UIntPtr */
    STRING(0x0e.toUByte()),

    /*  Followed by TypeDef or TypeRef */
    CLASS(0x12.toUByte()),

    /* System.Object */
    OBJECT(0x1c.toUByte()),

    /* Sentinel value */
    END(0x00.toUByte()),
    VOID(0x01.toUByte()),

    /* Followed by TypeSig */
    TYPE_SIG_PTR(0x0f.toUByte()),

    /* Followed by TypeSig */
    TYPE_SIG_BY_REFERENCE(0x10.toUByte()),

    /* Followed by TypeDef or TypeRef */
    VALUE_TYPE(0x11.toUByte()),

    /* Generic parameter in a type definition, represented as unsigned integer */
    VAR_HANDLE(0x13.toUByte()),
    ARRAY(0x14.toUByte()),
    GENERIC_INST(0x15.toUByte()),
    TYPED_BY_REF(0x16.toUByte()),

    /* Followed by full method signature */
    FUNCTION_PTR(0x1b.toUByte()),
    STRING_ARRAY(0x1d.toUByte()),

    /* Generic parameter in a method definition, represented as unsigned integer */
    METHOD_VAR_HANDLE(0x1e.toUByte()),

    /* CModReqdRequired modifier, followed by a TypeDef or TypeRef */
    MODIFIER_REQUIRED(0x1f.toUByte()),

    /* Optional modifier, followed by a TypeDef or TypeRef */
    MODIFIER_OPTIONAL(0x20.toUByte()),
    INTERNAL(0x21.toUByte()),

    /* Or'd with folowing element types */
    MODIFIER(0x40.toUByte()),

    /* Sentinel for vararg method signature */
    SENTINEL(0x41.toUByte()),

    PINNED(0x45.toUByte()),

    /* System.Type */
    TYPE(0x50.toUByte()),

    /* Boxed object (in custom attributes) */
    TAGGED_OBJECT(0x51.toUByte()),

    /* Custom attribute field */
    CUSTOM_ATTRIBUTE_FIELD(0x53.toUByte()),

    /* Custom attribute property */
    CUSTOM_ATTRIBUTE_PROPERTY(0x54.toUByte()),

    /* Custom attribute enum */
    CUSTOM_ATTRIBUTE_ENUM(0x55.toUByte()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class Visibility(val code: UInt) : ValueEnum<UInt, Visibility> {
    NOT_PUBLIC(0x00000000.toUInt()),
    PUBLIC(0x00000001.toUInt()),
    NESTED_PUBLIC(0x00000002.toUInt()),
    NESTED_PRIVATE(0x00000003.toUInt()),
    NESTED_FAMILY(0x00000004.toUInt()),
    NESTED_ASSEMBLY(0x00000005.toUInt()),
    NESTED_FAMILY_AND_ASSEMBLY(0x00000006.toUInt()),
    NESTED_FAMILY_OR_ASSEMBLY(0x00000007.toUInt()),
    ;

    fun isNested(): Boolean {
        return this != NOT_PUBLIC && this != PUBLIC
    }

    override fun getCode(): Any {
        return code
    }

}

enum class MemberAccess(val code: UShort) : ValueEnum<UShort, MemberAccess> {
    /* Member not referencable */
    COMPILER_CONTROLLED(0x0000.toUShort()),

    /* It's private, jim! */
    PRIVATE(0x0001.toUShort()),

    /* Accessible by subtypes only in this Assembly */
    FAMILY_AND_ASSEMBLY(0x0002.toUShort()),

    /* Accessible by anyone in this Assembly */
    ASSEMBLY(0x0003.toUShort()),

    /* Protected */
    FAMILY(0x0004.toUShort()),

    /* Accessible by subtypes anywhere, plus anyone in this Assembly */
    FAMILY_OR_ASSEMBLY(0x0005.toUShort()),

    /* Come get me! */
    PUBLIC(0x0006.toUShort()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class TypeLayout(val code: UInt) : ValueEnum<UInt, TypeLayout> {
    AUTO_LAYOUT(0x00000000.toUInt()),
    SEQUENTIAL(0x00000008.toUInt()),
    EXPLICIT(0x00000010.toUInt()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class TypeSemantics(val code: UInt) : ValueEnum<UInt, TypeSemantics> {
    CLASS(0x00000000.toUInt()),
    INTERFACE(0x00000020.toUInt()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class StringFormat(val code: UInt) : ValueEnum<UInt, StringFormat> {
    ANSI_CLASS(0x00000000.toUInt()),
    UNICODE_CLASS(0x00010000.toUInt()),
    AUTO_CLASS(0x00020000.toUInt()),
    CUSTOM_FORMAT_CLASS(0x00030000.toUInt()),
    CUSTOM_FORMAT_MASK(0x00C00000.toUInt()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class CodeType(val code: UShort) : ValueEnum<UShort, CodeType> {
    /* Method impl is CIL */
    IL(0x0000.toUShort()),

    /* Method impl is native */
    NATIVE(0x0001.toUShort()),

    /* Reserved: shall be zero in conforming implementations */
    OPTIL(0x0002.toUShort()),

    /* Method impl is provided by the runtime */
    RUNTIME(0x0003.toUShort()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class Managed(val code: UShort) : ValueEnum<UShort, Managed> {
    MANAGED(0x0000.toUShort()),
    UNMANAGED(0x0004.toUShort()),
    ;

    override fun getCode(): Any {
        return code
    }
};

enum class VtableLayout(val code: UShort) : ValueEnum<UShort, VtableLayout> {
    /* Method reuses existing slot in a vtable */
    REUSE_SLOT(0x0000.toUShort()),

    /* Method always gets a new slot in the vtable */
    NEW_SLOT(0x0100.toUShort()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class GenericParamVariance(val code: UShort) : ValueEnum<UShort, GenericParamVariance> {
    NONE(0x0000.toUShort()),
    COVARIANT(0x0001.toUShort()),
    CONTRAVARIANT(0x0002.toUShort()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class GenericParamSpecialConstraint(val code: UShort) : ValueEnum<UShort, GenericParamSpecialConstraint> {
    REFERENCE_TYPE_CONSTRAINT(0x0004.toUShort()),
    NON_NULL_VALUE_TYPE_CONSTRAINT(0x0008.toUShort()),
    DEFAULT_CONSTRUCTOR_CONSTRAINT(0x0010.toUShort()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class CallingConvention(val code: UByte) : ValueEnum<UByte, CallingConvention> {
    DEFAULT(0x00.toUByte()),
    VARIABLE_ARGUMENT(0x05.toUByte()),
    FIELD(0x06.toUByte()),
    LOCAL_SIGNATURE(0x07.toUByte()),
    PROPERTY(0x08.toUByte()),
    GENERIC_INST(0x10.toUByte()),
    MASK(0x0f.toUByte()),
    HAS_THIS(0x20.toUByte()),
    EXPLICIT_THIS(0x40.toUByte()),
    GENERIC(0x10.toUByte()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class AssemblyHashAlgorithm(val code: UInt) : ValueEnum<UInt, AssemblyHashAlgorithm> {
    NONE(0x00000000.toUInt()),
    RESERVED_MD5(0x00008003.toUInt()),
    SHA1(0x00008004.toUInt()),
    ;

    override fun getCode(): Any {
        return code
    }
}

enum class AssemblyFlags(val code: UInt) : ValueEnum<UInt, AssemblyFlags> {
    /* The assembly reference holds the full (unhashed) public key */
    PUBLIC_KEY(0x00000001.toUInt()),
    RETARGETABLE(0x00000100.toUInt()),
    WINDOWS_RUNTIME(0x00000200.toUInt()),
    DISABLE_JIT_COMPILE_OPTIMIZER(0x00004000.toUInt()),
    ENABLE_JIT_COMPILE_TRACKING(0x00008000.toUInt()),
    ;

    override fun getCode(): Any {
        return code
    }
};
