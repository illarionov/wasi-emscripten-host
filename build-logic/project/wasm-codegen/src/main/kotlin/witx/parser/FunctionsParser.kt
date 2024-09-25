/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.parser

import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleLexer
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser.ExpectedErrorContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser.FuncParamContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser.FuncParamTypeContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser.FuncResultContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser.FuncResultExpectedItemContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser.ModuleInterfaceContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser.ModuleSectionContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser.NumberTypeContext
import at.released.weh.gradle.wasm.codegen.antlr.WitxModuleParser.PointerTypeContext
import at.released.weh.gradle.wasm.codegen.witx.parser.ext.RethrowErrorListener
import at.released.weh.gradle.wasm.codegen.witx.parser.ext.getRawSource
import at.released.weh.gradle.wasm.codegen.witx.parser.ext.parseComment
import at.released.weh.gradle.wasm.codegen.witx.parser.ext.parseIdentifier
import at.released.weh.gradle.wasm.codegen.witx.parser.ext.parseNumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam.ParamType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam.ParamType.NumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam.ParamType.Pointer
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam.ParamType.WasiType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult.ExpectedData
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.TerminalNode
import java.io.File

internal object FunctionsParser {
    fun parse(
        interfacesFile: File,
    ): List<WasiFunc> = parse(CharStreams.fromFileName(interfacesFile.toString()))

    private fun parse(
        typenames: CharStream,
    ): List<WasiFunc> {
        val lexer = WitxModuleLexer(typenames).apply {
            addErrorListener(RethrowErrorListener)
        }

        val tokenStream = CommonTokenStream(lexer)
        val parser = WitxModuleParser(tokenStream).apply {
            addErrorListener(RethrowErrorListener)
        }

        return parser
            .parse()
            .module()
            .moduleSection()
            .mapNotNull(ModuleSectionContext::moduleInterface)
            .map(::parseInterfaceFun)
    }

    private fun parseInterfaceFun(
        context: ModuleInterfaceContext,
    ): WasiFunc {
        val result = if (context.funcNoReturn() != null) {
            null
        } else {
            parseFuncResult(context.funcResult() ?: error("Can not parse function result"))
        }

        return WasiFunc(
            export = parseStringName(context.funcExport().STRING_NAME()),
            params = context.funcParam().map(::parseParam),
            result = result,
            comment = parseComment(context.COMMENT()),
            source = context.getRawSource(),
        )
    }

    private fun parseParam(
        context: FuncParamContext,
    ): WasiFuncParam = WasiFuncParam(
        name = parseIdentifier(context.IDENTIFIER()),
        type = parseFuncParamType(context.funcParamType()),
        comment = parseComment(context.COMMENT()),
    )

    @Suppress("ReturnCount")
    private fun parseFuncParamType(context: FuncParamTypeContext): ParamType {
        context.STRING()?.let {
            return ParamType.String
        }
        context.IDENTIFIER()?.let {
            return WasiType(parseIdentifier(it))
        }
        context.numberType()?.let { numberTypeContext: NumberTypeContext ->
            val type = parseNumberType(numberTypeContext.UXX() ?: numberTypeContext.SXX())
            return NumberType(type)
        }
        context.pointerType()?.let { pointerTypeContext: PointerTypeContext ->
            return Pointer(
                isConstPointer = pointerTypeContext.CONST_POINTER() != null,
                dst = parseFuncParamType(pointerTypeContext.funcParamType()),
            )
        }
        error("Can not parse parameter of the function $context")
    }

    private fun parseFuncResult(
        context: FuncResultContext,
    ): WasiFuncResult = WasiFuncResult(
        identifier = parseIdentifier(context.IDENTIFIER()),
        expectedData = context.funcResultExpected().funcResultExpectedItem()?.let { parseExpectedData(it) },
        expectedError = parseExpectedError(context.funcResultExpected().expectedError()),
        comment = parseComment(context.COMMENT()),
    )

    private fun parseExpectedError(context: ExpectedErrorContext): Identifier = parseIdentifier(context.IDENTIFIER())

    private fun parseExpectedData(context: FuncResultExpectedItemContext): ExpectedData {
        context.expectedTuple()?.let { tupleContext ->
            return ExpectedData.Tuple(
                parseIdentifier(tupleContext.IDENTIFIER(0)),
                parseIdentifier(tupleContext.IDENTIFIER(1)),
            )
        }
        return ExpectedData.WasiType(parseIdentifier(context.IDENTIFIER()))
    }

    private fun parseStringName(
        node: TerminalNode,
    ): String = node.text.substringAfter("\"").substringBeforeLast("\"")
}
