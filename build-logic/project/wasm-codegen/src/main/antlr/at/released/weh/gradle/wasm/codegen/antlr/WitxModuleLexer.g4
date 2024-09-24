/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Lexer used to parse WASI Preview 1 interfaces from the file "wasi_snapshot_preview1.witx" which in the legacy
 * [WITX](https://github.com/WebAssembly/WASI/blob/main/legacy/tools/witx-docs.md) format.
 *
 * See: https://github.com/WebAssembly/WASI/blob/main/legacy/preview1/witx/wasi_snapshot_preview1.witx
 */
lexer grammar WitxModuleLexer;

LPAR : '(';
RPAR : ')';

CONST_POINTER : 'const_pointer' ;
ERROR         : 'error' ;
EXPECTED      : 'expected' ;
EXPORT        : 'export' ;
FUNC          : 'func' ;
IMPORT        : 'import' ;
INTERFACE     : '@interface' ;
MEMORY        : 'memory' ;
MODULE        : 'module' ;
NORETURN      : 'noreturn' ;
PARAM         : 'param' ;
POINTER       : 'pointer' ;
RESULT        : 'result' ;
STRING        : 'string' ;
TUPLE         : 'tuple' ;
USE           : 'use' ;
WITX          : '@witx' ;

UXX  : 'u' ('8' | '16' | '32' | '64')  ;
SXX  : 's' ('8' | '16' | '32' | '64')  ;

IDENTIFIER     : '$' [0-9a-z_]+ ;
STRING_NAME    : '"' [0-9a-z_.]+ '"' ;
COMMENT        : ( '(;' .*? ';)' | ';;' .*? '\n') ;

SPACE: [ \t\r\n] -> skip;
