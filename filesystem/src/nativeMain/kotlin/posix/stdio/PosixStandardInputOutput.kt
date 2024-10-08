/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.stdio

import at.released.weh.filesystem.stdio.SinkProvider
import at.released.weh.filesystem.stdio.SourceProvider
import at.released.weh.filesystem.stdio.StandardInputOutput

public data object PosixStandardInputOutput : StandardInputOutput {
    override val stdinProvider: SourceProvider = PosixStdinSourceProvider
    override val stdoutProvider: SinkProvider = PosixSinkProvider.stdoutProvider
    override val stderrProvider: SinkProvider = PosixSinkProvider.stderrProvider
}
