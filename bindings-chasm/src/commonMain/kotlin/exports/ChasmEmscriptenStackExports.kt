/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NULLABLE_PROPERTY_TYPE")

package at.released.weh.bindings.chasm.exports

import at.released.weh.host.emscripten.export.stack.EmscriptenStackExports
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Store

internal class ChasmEmscriptenStackExports(
    store: Store,
    instance: Instance,
) : EmscriptenStackExports {
    private val globalsBindings = ChasmIntGlobalsBindings(
        store = store,
        instance = instance,
        exportNames = setOf(
            "__stack_pointer",
            "__stack_end",
            "__stack_base",
        ),
    )
    private val functionBindings = ChasmFunctionBindings(
        store = store,
        instance = instance,
        exportNames = setOf(
            "__set_stack_limits",
            "emscripten_stack_init",
            "emscripten_stack_get_free",
            "emscripten_stack_get_base",
            "emscripten_stack_get_end",
            "emscripten_stack_get_current",
            "emscripten_stack_set_limits",
            "_emscripten_stack_alloc",
            "_emscripten_stack_restore",
        ),
    )
    override var __stack_pointer by globalsBindings.required
    override var __stack_end by globalsBindings.optional
    override var __stack_base by globalsBindings.optional
    override val __set_stack_limits by functionBindings.optional
    override val emscripten_stack_init by functionBindings.optional
    override val emscripten_stack_get_free by functionBindings.optional
    override val emscripten_stack_get_base by functionBindings.optional
    override val emscripten_stack_get_end by functionBindings.optional
    override val emscripten_stack_get_current by functionBindings.required
    override val emscripten_stack_set_limits by functionBindings.required
    override val _emscripten_stack_alloc by functionBindings.required
    override val _emscripten_stack_restore by functionBindings.required
}
