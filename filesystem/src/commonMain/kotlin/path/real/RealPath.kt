/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real

/**
 * Represents a platform-specific path on the real file system.
 *
 * Path can be either absolute or relative.
 *
 * Path is not guaranteed to be a valid Unicode string. The rules for valid paths and the set of acceptable
 * characters depend on the underlying platform, but we assume the following general restrictions:
 *   * Null character (`'\0'`) is not allowed in a path.
 */
public typealias RealPath = String
