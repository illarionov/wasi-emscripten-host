package at.released.weh.sample.chicory.maven;

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
