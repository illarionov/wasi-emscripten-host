/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path

/**
 * Path within the virtual file system.
 *
 * It matches the definition of a path in the WASI file system but can also be an absolute path. Paths can be
 * expressed as a sequence of Unicode Scalar Values (USVs),
 *
 * The directory separator in is always the forward-slash (`/`).
 */
public typealias VirtualPath = String
