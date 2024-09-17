/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.function

import at.released.weh.common.api.Logger
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.HostFunction

public open class HostFunctionHandle(
    public val function: HostFunction,
    public val host: EmbedderHost,
) {
    protected val logger: Logger = host.rootLogger.withTag("wasm-func:$${function.wasmName}")
}
