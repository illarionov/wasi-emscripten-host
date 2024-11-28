/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.filesystem.assertions.mode

public enum class PosixFileModeBit {
    SUID,
    SGID,
    STICKY,
    USER_READ,
    USER_WRITE,
    USER_EXECUTE,
    GROUP_READ,
    GROUP_WRITE,
    GROUP_EXECUTE,
    OTHER_READ,
    OTHER_WRITE,
    OTHER_EXECUTE,
}
