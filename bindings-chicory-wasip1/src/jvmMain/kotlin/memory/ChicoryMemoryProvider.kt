/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.memory

import at.released.weh.wasm.core.memory.Memory
import com.dylibso.chicory.runtime.Instance

public fun interface ChicoryMemoryProvider {
    public fun get(instance: Instance): Memory
}
