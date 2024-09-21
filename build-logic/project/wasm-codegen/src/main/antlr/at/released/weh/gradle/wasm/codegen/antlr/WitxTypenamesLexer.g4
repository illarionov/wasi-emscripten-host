/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Lexer used to parse WASI Preview 1 type names from the file "typenames.witx" which in the legacy
 * [WITX](https://github.com/WebAssembly/WASI/blob/main/legacy/tools/witx-docs.md) format.
 *
 * See: https://github.com/WebAssembly/WASI/blob/main/legacy/preview1/witx/typenames.witx
 */
lexer grammar WitxTypenamesLexer;

LPAR : '(';
RPAR : ')';

CONST_POINTER : 'const_pointer' ;
ENUM          : 'enum' ;
FIELD         : 'field' ;
FLAGS         : 'flags' ;
HANDLE        : 'handle' ;
LIST          : 'list' ;
POINTER       : 'pointer' ;
RECORD        : 'record' ;
REPR          : 'repr' ;
TAG           : 'tag' ;
TYPENAME      : 'typename' ;
UNION         : 'union' ;
WITX          : '@witx' ;

IDENTIFIER : '$' [0-9a-z_]+ ;

UXX  : 'u' ('8' | '16' | '32' | '64')  ;
SXX  : 's' ('8' | '16' | '32' | '64')  ;

COMMENT: ( '(;' .*? ';)' | ';;' .*? '\n') ;

SPACE: [ \t\r\n] -> skip;
