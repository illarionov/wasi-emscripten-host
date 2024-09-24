/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.parser

import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesLexer
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.EnumTypedefContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.FlagsTypedefContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.HandleTypedefContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.IdentifierWithCommentContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.ListTypedefContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.NumberTypedefContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.RecordFieldContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.RecordFieldTypeContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.RecordTypedefContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.TypedefContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.TypenameWithCommentContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxTypenamesParser.UnionTypedefContext
import at.released.weh.gradle.wasm.codegen.witx.parser.ext.parseComment
import at.released.weh.gradle.wasm.codegen.witx.parser.ext.parseIdentifier
import at.released.weh.gradle.wasm.codegen.witx.parser.ext.parseNumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.ext.parseUnsignedNumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.IdentifierWithComment
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.EnumType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.FlagsType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.Handle
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.ListType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.NumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType.RecordField
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType.RecordFieldType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.UnionType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiTypename
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.TerminalNode
import java.io.File

internal object TypenamesParser {
    fun parse(
        typenamesFile: File,
    ): List<WasiTypename> = parse(CharStreams.fromFileName(typenamesFile.toString()))

    private fun parse(
        typenames: CharStream,
    ): List<WasiTypename> {
        val lexer = WitxTypenamesLexer(typenames).apply {
            addErrorListener(RethrowErrorListener)
        }

        val tokenStream = CommonTokenStream(lexer)
        val parser = WitxTypenamesParser(tokenStream).apply {
            addErrorListener(RethrowErrorListener)
        }

        return parser.typenames().typenameWithComment().map(::parseTypename)
    }

    private fun parseTypename(typename: TypenameWithCommentContext): WasiTypename {
        return WasiTypename(
            comment = parseComment(typename.COMMENT()),
            identifier = parseIdentifier(typename.item().IDENTIFIER()),
            typedef = parseTypedef(typename.item().typedef()),
        )
    }

    private fun parseTypedef(typedefContext: TypedefContext): WasiType {
        return when (val context = typedefContext.getChild(0)) {
            is NumberTypedefContext -> parseNumberType(context)
            is EnumTypedefContext -> parseEnumType(context)
            is FlagsTypedefContext -> parseFlagsType(context)
            is RecordTypedefContext -> parseRecordType(context)
            is ListTypedefContext -> parseListType(context)
            is UnionTypedefContext -> parseUnionType(context)
            is HandleTypedefContext -> Handle
            else -> error("Unsupported type $context")
        }
    }

    private fun parseNumberType(context: NumberTypedefContext): NumberType {
        return context.children.firstNotNullOf { child ->
            (child as? TerminalNode)?.let { node ->
                NumberType(parseNumberType(node))
            }
        }
    }

    private fun parseEnumType(context: EnumTypedefContext): EnumType = EnumType(
        tag = parseUnsignedNumberType(context.enumWitxTag().UXX()),
        values = context.identifierWithComment().map(::parseIdentifierWithComment),
    )

    private fun parseFlagsType(context: FlagsTypedefContext): FlagsType = FlagsType(
        repr = parseUnsignedNumberType(context.flagsWitxTag().UXX()),
        flags = context.identifierWithComment().map(::parseIdentifierWithComment),
    )

    private fun parseRecordType(context: RecordTypedefContext): RecordType = RecordType(
        fields = context.recordField().map { parseRecordField(it) },
    )

    private fun parseRecordField(context: RecordFieldContext): RecordField = RecordField(
        identifier = parseIdentifier(context.IDENTIFIER()),
        type = parseFieldType(context.recordFieldType()),
        comment = parseComment(context.COMMENT()),
    )

    private fun parseFieldType(context: RecordFieldTypeContext): RecordFieldType {
        val identifier = context.IDENTIFIER()
        if (identifier != null) {
            return RecordFieldType.IdentifierField(parseIdentifier(identifier))
        }
        val dstType = parseUnsignedNumberType(context.UXX())
        return when {
            context.POINTER() != null -> RecordFieldType.Pointer(dstType, false)
            context.CONST_POINTER() != null -> RecordFieldType.Pointer(dstType, true)
            else -> error("Unknown type")
        }
    }

    private fun parseListType(context: ListTypedefContext): ListType = ListType(
        identifier = parseIdentifier(context.IDENTIFIER()),
    )

    private fun parseUnionType(context: UnionTypedefContext): UnionType = UnionType(
        tag = parseIdentifier(context.unionWitxTag().IDENTIFIER()),
        members = context.IDENTIFIER().map(::parseIdentifier),
    )

    private fun parseIdentifierWithComment(context: IdentifierWithCommentContext): IdentifierWithComment =
        IdentifierWithComment(
            identifier = parseIdentifier(context.IDENTIFIER()),
            comment = parseComment(context.COMMENT()),
        )
}
