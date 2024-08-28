/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.include

import at.released.weh.common.ext.maskToString

public fun Fcntl.oMaskToString(mask: UInt): String {
    val startNames = if (mask.and(O_ACCMODE) == 0U) {
        listOf(Fcntl::O_RDONLY.name)
    } else {
        emptyList()
    }

    return maskToString(
        mask,
        listOf(
            Fcntl::O_WRONLY,
            Fcntl::O_RDWR,
            Fcntl::O_CREAT,
            Fcntl::O_EXCL,
            Fcntl::O_NOCTTY,
            Fcntl::O_TRUNC,
            Fcntl::O_APPEND,
            Fcntl::O_NONBLOCK,
            Fcntl::O_SYNC,
            Fcntl::O_TMPFILE,
            Fcntl::O_DSYNC,
            Fcntl::O_ASYNC,
            Fcntl::O_DIRECT,
            Fcntl::O_LARGEFILE,
            Fcntl::O_DIRECTORY,
            Fcntl::O_NOFOLLOW,
            Fcntl::O_NOATIME,
            Fcntl::O_CLOEXEC,
            Fcntl::O_PATH,
        ),
        startNames,
    )
}
