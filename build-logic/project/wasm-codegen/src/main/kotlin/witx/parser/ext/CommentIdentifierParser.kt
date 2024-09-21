/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.parser.ext

import org.antlr.v4.runtime.tree.TerminalNode

internal fun parseIdentifier(node: TerminalNode) = node.text.substringAfter('$')

internal fun parseComment(nodes: List<TerminalNode>): String = nodes.joinToString(" ") { commentNode ->
    commentNode.text
        .trimStart { it == ';' || it.isWhitespace() }
        .trimEnd { it == ';' || it.isWhitespace() || it == '\r' || it == '\n' }
}
