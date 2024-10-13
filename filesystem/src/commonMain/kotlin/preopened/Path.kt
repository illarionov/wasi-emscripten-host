/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.preopened

/**
 * Path on the real file system.
 * If it is relative, the final path will be resolved using current working directory of the virtual file system.
 */
public typealias RealPath = String

public typealias VirtualPath = String
