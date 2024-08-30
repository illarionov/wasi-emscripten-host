/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.exception

import io.github.charlietap.chasm.error.ChasmError

public class ChasmErrorException(
    public val error: ChasmError,
    message: String? = null,
    cause: Throwable? = null,
) : ChasmException(message, cause)
