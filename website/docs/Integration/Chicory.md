---
sidebar_label: 'Chicory'
sidebar_position: 2
description: 'Implementation of Emscripten host functions for Chicory'
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Chicory Integration

[Chicory] is a zero-dependency, pure Java WebAssembly runtime. 

Key Features:

- Compatible with Android API 33+ and JVM JDK 17+.
- Simple JVM-only runtime with minimal dependencies.
- Supports single-threaded execution only.

This integration allows you to run WebAssembly binaries using the Emscripten and WASI Preview 1 APIs on the JVM 
with Chicory.

The runtime is actively developed, and its public interfaces are subject to frequent changes.  
Our integration is compatible with version **[0.0.12][Chicory_version]** of Chicory.

## Installation

Add the required dependencies:

<Tabs>
    <TabItem value="gradle" label="Gradle" default>

```kotlin
dependencies {
    implementation("at.released.weh:bindings-chicory:0.1-alpha01")
    implementation("com.dylibso.chicory:runtime:0.0.12")
}
```
    </TabItem>

    <TabItem value="maven" label="Maven">
```xml
<dependencies>
    <dependency>
        <groupId>at.released.weh</groupId>
        <artifactId>bindings-chicory-jvm</artifactId>
        <version>0.1-alpha01</version>
    </dependency>
    <dependency>
        <groupId>com.dylibso.chicory</groupId>
        <artifactId>runtime</artifactId>
        <version>0.0.12</version>
    </dependency>
</dependencies>
```
    </TabItem>
</Tabs>


## Usage

Below is an example demonstrating the execution of **helloworld.wasm**, prepared
in the "[Emscripten Example](../Emscripten#example)".

<Tabs>
    <TabItem value="kotlin" label="Kotlin" default>

```kotlin
import at.released.weh.bindings.chicory.ChicoryHostFunctionInstaller
import at.released.weh.bindings.chicory.ChicoryHostFunctionInstaller.ChicoryEmscriptenInstaller
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.HostGlobal
import com.dylibso.chicory.runtime.HostImports
import com.dylibso.chicory.runtime.HostMemory
import com.dylibso.chicory.runtime.HostTable
import com.dylibso.chicory.runtime.Memory
import com.dylibso.chicory.runtime.Module
import com.dylibso.chicory.wasm.types.MemoryLimits
import com.dylibso.chicory.wasm.types.Value

// You can use `wasm-objdump -x helloworld.wasm -j Memory` to get the memory limits
// declared in the WebAssembly binary.
const val INITIAL_MEMORY_SIZE_PAGES = 258

fun main() {
    // Prepare Host memory
    val memory = HostMemory(
        /* moduleName = */ "env",
        /* fieldName = */ "memory",
        /* memory = */
        Memory(
            MemoryLimits(INITIAL_MEMORY_SIZE_PAGES),
        ),
    )

    // Prepare WASI and Emscripten host imports
    val installer = ChicoryHostFunctionInstaller(
        memory = memory.memory(),
    )
    val wasiFunctions: List<HostFunction> = installer.setupWasiPreview1HostFunctions()
    val emscriptenInstaller: ChicoryEmscriptenInstaller = installer.setupEmscriptenFunctions()
    val hostImports = HostImports(
        /* functions = */ (emscriptenInstaller.emscriptenFunctions + wasiFunctions).toTypedArray(),
        /* globals = */ arrayOf<HostGlobal>(),
        /* memory = */ memory,
        /* tables = */ arrayOf<HostTable>(),
    )

    // Setup Chicory Module
    val module = Module
        .builder("helloworld.wasm")
        .withHostImports(hostImports)
        .withInitialize(true)
        .withStart(false)
        .build()

    // Instantiate the WebAssembly module
    val instance = module.instantiate()

    // Finalize initialization after module instantiation
    val emscriptenRuntime = emscriptenInstaller.finalize(instance)

    // Initialize Emscripten runtime environment
    emscriptenRuntime.initMainThread()

    // Execute code
    instance.export("main").apply(
        /* argc */ Value.i32(0),
        /* argv */ Value.i32(0),
    )[0].asInt()
}
```

    </TabItem>

    <TabItem value="java" label="Java">

```java
import at.released.weh.bindings.chicory.ChicoryHostFunctionInstaller;
import at.released.weh.bindings.chicory.ChicoryHostFunctionInstaller.ChicoryEmscriptenInstaller;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostGlobal;
import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.HostMemory;
import com.dylibso.chicory.runtime.HostTable;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import com.dylibso.chicory.wasm.types.Value;
import java.util.ArrayList;

public class App {
    // You can use `wasm-objdump -x helloworld.wasm -j Memory` to get the memory limits
    // declared in the WebAssembly binary.
    private static final int INITIAL_MEMORY_SIZE_PAGES = 258;

    public static void main(String[] args) {
        // Prepare Host memory
        var memory = new HostMemory(
                /* moduleName = */ "env",
                /* fieldName = */ "memory",
                /* memory = */ new Memory(new MemoryLimits(INITIAL_MEMORY_SIZE_PAGES))
        );

        // Prepare WASI and Emscripten host imports
        var installer = new ChicoryHostFunctionInstaller.Builder(memory.memory()).build();
        var hostFunctions = new ArrayList<>(installer.setupWasiPreview1HostFunctions());
        ChicoryEmscriptenInstaller emscriptenInstaller = installer.setupEmscriptenFunctions();
        hostFunctions.addAll(emscriptenInstaller.getEmscriptenFunctions());

        var hostImports = new HostImports(
                /* functions = */ hostFunctions.toArray(new HostFunction[0]),
                /* globals = */ new HostGlobal[0],
                /* memory = */ new HostMemory[]{memory},
                /* tables = */ new HostTable[0]
        );

        // Setup Chicory Module
        var module = Module.builder("helloworld.wasm")
                .withHostImports(hostImports)
                .withInitialize(true)
                .withStart(false)
                .build();

        // Instantiate the WebAssembly module
        var instance = module.instantiate();

        // Finalize initialization after module instantiation
        var emscriptenRuntime = emscriptenInstaller.finalize(instance);

        // Initialize Emscripten runtime environment
        emscriptenRuntime.initMainThread();

        // Execute code
        instance.export("main").apply(
                /* argc */ Value.i32(0),
                /* argv */ Value.i32(0)
        )[0].asInt();
    }
}
```

    </TabItem>
</Tabs>

You can also check out the example in the repository:

* Gradle project with Kotlin code: [samples/wasm-gradle/app-chicory]
* Maven project with Java code: [samples/wasm-maven/chicory-maven]

## Internal WASI Preview 1 implementation

Chicory includes its own implementation of the WASI Preview 1 interfaces.  
You can find documentation on how to use it here: [chicory/wasi].

## Runtime Optimizations

Chicory is also working on an Ahead-of-Time (AOT) compiler that translates WebAssembly into JVM code.
To experiment with this feature, you can add the following dependency:

```kotlin
implementation("com.dylibso.chicory:aot:0.0.12")
```

And add `Engine` when building `Module`:

```kotlin
import com.dylibso.chicory.aot.AotMachine

val module = Module
    .builder("helloworld.wasm")
    .withHostImports(hostImports)
    .withInitialize(true)
    .withStart(false)
    .withMachineFactory(::AotMachine)
    .build()

```

For the latest updates, check this link: [chicory/aot].

[Chicory]: https://github.com/dylibso/chicory
[Chicory_version]: https://github.com/dylibso/chicory/releases/tag/0.0.12
[samples/wasm-gradle/app-chicory]: https://github.com/illarionov/wasi-emscripten-host/tree/main/samples/wasm-gradle/app-chicory
[samples/wasm-maven/chicory-maven]: https://github.com/illarionov/wasi-emscripten-host/tree/main/samples/wasm-maven/chicory-maven
[chicory/wasi]: https://github.com/dylibso/chicory/tree/main/wasi
[chicory/aot]: https://github.com/dylibso/chicory/tree/main/aot
