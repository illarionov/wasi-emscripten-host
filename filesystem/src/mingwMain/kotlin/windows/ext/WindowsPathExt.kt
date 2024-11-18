/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.ext

/**
 * \\?\ Prefix disables string parsing and send string directly to file system.
 *
 * https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file
 */
internal const val WIN32_FILE_NAMESPACE_PREFIX = """\\?\"""

// https://stackoverflow.com/questions/23041983/path-prefixes-and/46019856#46019856
internal const val WIN32_NT_KERNEL_DEVICES_PREFIX = """\??\"""
