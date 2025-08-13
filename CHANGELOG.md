# Changelog

All notable changes to this project will be documented in this file.

## 0.6.0 - 2025-08-13

This version is compatible with the following WebAssembly runtimes:

- Chicory 1.5.1
- Chasm 1.0.0
- GraalVM 24.1

#### Implemented interfaces

- WASI Preview 1
- Emscripten 4.0.1 (partial)

#### 💥 Breaking Change

- Minimum required Kotlin version raised to 2.1
- Replaced `kotlinx.datetime.Clock` with `kotlin.time.Clock`

#### 🔧 Maintenance

- Updated Chasm to 1.0.0
- Updated Chicory to 1.5.1
- Updated Kotlin to 2.2.0
- Bumped other build dependencies

## 0.5 - 2025-04-11

#### 🚀 New Feature

- Update Chasm to 0.9.61. This is breaking API Change in the `bindings-chasm-*` modules.
- Update Chicory to 1.2.1

#### 🔧 Maintenance

- Bump versions: Kotlin 2.1.20, other build dependencies.

## 0.4 - 2025-03-13

#### 🚀 New Feature

- Update Chasm to 0.9.61. This is breaking API Change in the `bindings-chasm-*` modules.

#### 🔧 Maintenance

- Bump other versions: Chicory 1.1.1, kotlinx-io 0.7.0, other build dependencies.

## 0.3 - 2025-02-23

#### 🚀 New Feature

- Update Chicory to 1.1.0
- Update Chasm to 0.9.53
- Update Graalvm to 24.1.2
- Update minimum supported Emscripten version to 4.0.1, add `_emscripten_runtime_keepalive_clear`, `settimetimer`,
  `_emscripten_notify_mailbox_postmessage` stubs

#### 🐛 Bug Fix

- Fixed symlink handling in `fd_readdir` on JVM Nio

#### 🔧 Maintenance

- Move tempfolder to https://github.com/illarionov/tempfolder-kmp
- Run WASI test suite with Chicory configured for ByteArrayMemory 
- Replace Dokkatoo with Dokka 2.0

#### 🤖 Dependencies

- Kotlin 2.1.0
- Gradle 8.12.1
- Arrow 2.0.1
- Atomicfu 0.17.0
- Dokka 2.0
- Spotless 7.0.0

## 0.2 - 2025-01-13

### Changed

- Updated Chasm to version 0.9.4.  
  This is breaking API Change in the `bindings-chasm-*` modules.
  The MinGW target has been disabled due to Windows support being temporarily dropped in Chasm.

## 0.1 - 2025-01-09

### Added

- WASI Preview 1 implementation

## [0.1-alpha01] - 2024-09-08

- Initial test release. API will be changed significantly.
