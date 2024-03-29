package com.github.danielchemko.winmdj.core.autoobject.model

sealed interface BitsetEnum {
    fun getBitNumber()
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
