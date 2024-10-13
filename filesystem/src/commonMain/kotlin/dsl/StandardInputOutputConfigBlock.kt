/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.dsl

import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.filesystem.stdio.SinkProvider
import at.released.weh.filesystem.stdio.SourceProvider

@WasiEmscriptenHostDsl
public class StandardInputOutputConfigBlock internal constructor() {
    public var stdinProvider: SourceProvider? = null
    public var stdoutProvider: SinkProvider? = null
    public var stderrProvider: SinkProvider? = null
}
