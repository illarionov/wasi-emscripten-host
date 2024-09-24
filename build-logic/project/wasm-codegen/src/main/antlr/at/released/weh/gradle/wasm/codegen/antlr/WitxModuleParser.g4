/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Parser used to parse WASI Preview 1 type names from the file "wasi_snapshot_preview1.witx" which in the legacy
 * [WITX](https://github.com/WebAssembly/WASI/blob/main/legacy/tools/witx-docs.md) format.
 *
 * See: https://github.com/WebAssembly/WASI/blob/main/legacy/preview1/witx/wasi_snapshot_preview1.witx
 */
parser grammar WitxModuleParser;

options {
    tokenVocab = WitxModuleLexer ;
}

parse
    : COMMENT* useTypenames module EOF ;

useTypenames
    : LPAR USE STRING_NAME RPAR
    ;

module
    : LPAR MODULE IDENTIFIER moduleSection* RPAR
    ;

moduleSection
    : moduleImport
    | moduleInterface
    ;

moduleImport
    : COMMENT* LPAR IMPORT STRING_NAME LPAR MEMORY RPAR RPAR ;

moduleInterface
    : COMMENT* LPAR INTERFACE FUNC funcExport funcParam* (funcResult | funcNoReturn) RPAR
    ;

funcExport
    : LPAR EXPORT STRING_NAME RPAR
    ;

funcParam
    : COMMENT* LPAR PARAM IDENTIFIER funcParamType RPAR
    ;

funcParamType
    : IDENTIFIER
    | STRING
    | numberType
    | pointerType
    ;

numberType
    : UXX
    | SXX
    ;

pointerType
    : LPAR WITX (POINTER | CONST_POINTER) funcParamType RPAR
    ;

funcResult
    : COMMENT* LPAR RESULT IDENTIFIER funcResultExpected RPAR
    ;

funcResultExpected
    : LPAR EXPECTED funcResultExpectedItem? expectedError RPAR
    ;

expectedError
    : LPAR ERROR IDENTIFIER RPAR
    ;

funcResultExpectedItem
    : IDENTIFIER
    | expectedTuple
    ;

expectedTuple
    : LPAR TUPLE IDENTIFIER IDENTIFIER RPAR
    ;

funcNoReturn
    : LPAR WITX NORETURN RPAR
    ;
