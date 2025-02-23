# Changelog

All notable changes to this project will be documented in this file.

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
