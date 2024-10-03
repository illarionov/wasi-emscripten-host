/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1Exception
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.type.Exitcode
import at.released.weh.wasi.preview1.type.ExitcodeType

/**
 * Handler for the [WasiPreview1HostFunction.PROC_EXIT] function.
 *
 * Throws [ProcExitException] that should be handled by WASM runtime adapter.
 *
 * An exit code of 0 indicates successful termination of the program.
 * The meanings of other values is dependent on the environment.
 */
public class ProcExitFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.ENVIRON_GET, host) {
    /**
     * Throws [ProcExitException] that should be handled by WASM runtime adapter
     */
    public fun execute(
        @ExitcodeType exitCode: Exitcode,
    ): Nothing = throw ProcExitException(exitCode)

    public class ProcExitException(
        public val exitCode: Int,
    ) : WasiPreview1Exception()
}
