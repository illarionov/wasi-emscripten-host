/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Parser used to parse WASI Preview 1 type names from the file "typenames.witx" which in the legacy
 * [WITX](https://github.com/WebAssembly/WASI/blob/main/legacy/tools/witx-docs.md) format.
 *
 * See: https://github.com/WebAssembly/WASI/blob/main/legacy/preview1/witx/typenames.witx
 */
parser grammar WitxTypenamesParser;

options {
    tokenVocab = WitxTypenamesLexer ;
}

typenames : typenameWithComment* EOF ;

typenameWithComment : COMMENT* item;

item
    : LPAR TYPENAME IDENTIFIER typedef RPAR
    ;

typedef
    : numberTypedef
    | enumTypedef
    | flagsTypedef
    | recordTypedef
    | listTypedef
    | unionTypedef
    | handleTypedef
    ;

numberTypedef
    : UXX
    | SXX
    ;

enumTypedef
    : LPAR ENUM enumWitxTag identifierWithComment+ RPAR ;

enumWitxTag
    : LPAR WITX TAG UXX RPAR
    ;

flagsTypedef
    : LPAR FLAGS flagsWitxTag identifierWithComment+ RPAR ;

flagsWitxTag
    : LPAR WITX REPR UXX RPAR
    ;

recordTypedef
    : LPAR RECORD recordField+ RPAR
    ;

recordField
    : COMMENT* LPAR FIELD IDENTIFIER recordFieldType RPAR
    ;

recordFieldType
    : IDENTIFIER
    | LPAR WITX (POINTER | CONST_POINTER) UXX RPAR
    ;

listTypedef
    : LPAR LIST IDENTIFIER RPAR
    ;

unionTypedef
    : LPAR UNION unionWitxTag IDENTIFIER+ RPAR
    ;

unionWitxTag
    : LPAR WITX TAG IDENTIFIER RPAR
    ;

identifierWithComment
    : COMMENT* IDENTIFIER
    ;

handleTypedef
    : LPAR HANDLE RPAR
    ;
