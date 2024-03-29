package com.github.danielchemko.winmdj.core.mdspec

data class AssemblyVersion(
    val majorVersion: UShort,
    val minorVersion: UShort,
    val buildNumber: UShort,
    val revisionNumber: UShort,
) {
    override fun toString(): String {
        return "$majorVersion.$minorVersion.$buildNumber.$revisionNumber"
    }

    companion object {
        fun from(version: ULong): AssemblyVersion {
            val versionInt = version.toInt()
            return AssemblyVersion(
                (versionInt and 0xffff).toUShort(),
                ((versionInt shr 16) and 0xffff).toUShort(),
                ((versionInt shr 32) and 0xffff).toUShort(),
                ((versionInt shr 48) and 0xffff).toUShort()
            )
        }
    }
}