/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.dsl

import at.released.weh.host.EmbedderHost
import io.github.charlietap.chasm.executor.runtime.store.Address

@ChasmBindingsDsl
public class ChasmHostFunctionInstallerDsl internal constructor() {
    public var host: EmbedderHost? = null
    public var memoryAddress: Address.Memory = Address.Memory(0)
}
