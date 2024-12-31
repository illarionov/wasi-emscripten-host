/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.dsl

import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.host.EmbedderHost
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Memory as ChasmMemory

@WasiEmscriptenHostDsl
public class ChasmHostFunctionDsl internal constructor() {
    /**
     * Implementation of a host object that provides access from the WebAssembly to external host resources.
     */
    public var host: EmbedderHost? = null

    /**
     * Sets memory in the Chasm WebAssembly Store used for all operations. For multi-memory scenarios.
     */
    public var memoryProvider: (Store.() -> ChasmMemory)? = null
}
