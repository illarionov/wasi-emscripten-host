/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.export.pthread

import at.released.weh.wasi.preview1.type.Errno

public class PthreadException : RuntimeException {
    public val errCode: Errno?

    public constructor(
        message: String?,
        errCode: Int? = null,
    ) : super(message) {
        this.errCode = errCode?.let(Errno::fromCode)
    }

    public constructor(
        message: String?,
        cause: Throwable?,
        errCode: Int? = null,
    ) : super(message, cause) {
        this.errCode = errCode?.let(Errno::fromCode)
    }
}
