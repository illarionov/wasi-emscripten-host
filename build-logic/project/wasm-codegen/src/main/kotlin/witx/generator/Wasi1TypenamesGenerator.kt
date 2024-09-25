/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator

import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.EnumType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.FlagsType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.Handle
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.ListType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.NumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.UnionType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiTypename
import com.squareup.kotlinpoet.FileSpec
import java.io.File

internal class Wasi1TypenamesGenerator private constructor(
    typenames: List<WasiTypename>,
    private val typenamesPackage: String,
    private val outputDirectory: File,
) {
    private val typenames = typenames.associateBy(WasiTypename::identifier)
    private val superInterfaces: Map<Identifier, Set<Identifier>> = run {
        val dstMap: MutableMap<Identifier, MutableSet<Identifier>> = mutableMapOf()
        typenames.forEach { typename ->
            if (typename.typedef is UnionType) {
                typename.typedef.members.forEach { parentInterface ->
                    dstMap.getOrPut(parentInterface) { mutableSetOf() }.add(typename.identifier)
                }
            }
        }
        dstMap
    }

    fun generate() {
        val specs = typenames.values.map(::generateFileSpec)
        specs.forEach { it.writeTo(outputDirectory) }
    }

    fun generateFileSpec(
        typename: WasiTypename,
    ): FileSpec = when (val typedef = typename.typedef) {
        is Handle -> NumberTypeGenerator(
            identifier = typename.identifier,
            typedef = WasiNumberType.UnsignedNumber.U32,
            typenameRaw = typename.source,
            comment = typename.comment,
            typenamesPackage = typenamesPackage,
        ).generate()

        is EnumType -> EnumTypeGenerator(
            identifier = typename.identifier,
            typedef = typedef,
            comment = typename.comment,
            typenameRaw = typename.source,
            typenamesPackage = typenamesPackage,
        ).generate()

        is FlagsType -> FlagsTypeGenerator(
            identifier = typename.identifier,
            typedef = typedef,
            comment = typename.comment,
            typenameRaw = typename.source,
            typenamesPackage = typenamesPackage,
        ).generate()

        is ListType -> ListTypeGenerator(
            identifier = typename.identifier,
            typedef = typedef,
            typenameRaw = typename.source,
            comment = typename.comment,
            typenamesPackage = typenamesPackage,
        ).generate()

        is NumberType -> NumberTypeGenerator(
            identifier = typename.identifier,
            typedef = typedef.type,
            typenameRaw = typename.source,
            comment = typename.comment,
            typenamesPackage = typenamesPackage,
        ).generate()

        is RecordType -> RecordTypeGenerator(
            identifier = typename.identifier,
            typedef = typedef,
            comment = typename.comment,
            typenameRaw = typename.source,
            typenamesPackage = typenamesPackage,
            typenames = typenames,
            implementMembers = superInterfaces[typename.identifier] ?: emptySet(),
        ).generate()

        is UnionType -> UnionTypeGenerator(
            identifier = typename.identifier,
            typedef = typedef,
            comment = typename.comment,
            typenameRaw = typename.source,
            typenamesPackage = typenamesPackage,
        ).generate()
    }

    companion object {
        fun generate(
            typenames: List<WasiTypename>,
            typenamesPackage: String,
            outputDirectory: File,
        ) = Wasi1TypenamesGenerator(typenames, typenamesPackage, outputDirectory).generate()
    }
}
