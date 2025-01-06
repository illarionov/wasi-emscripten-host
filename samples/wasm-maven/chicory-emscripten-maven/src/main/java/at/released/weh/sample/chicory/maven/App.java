package at.released.weh.sample.chicory.maven;

import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller;
import at.released.weh.host.EmbedderHost;
import at.released.weh.host.EmbedderHostBuilder;
import com.dylibso.chicory.runtime.ImportFunction;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) throws Exception {
        // Prepare Host
        var hostBuilder = new EmbedderHostBuilder();
        hostBuilder.fileSystem().setUnrestricted(true);
        try (var embedderHost = hostBuilder.build()) {
            executeWasmCode(embedderHost);
        }
    }

    private static void executeWasmCode(EmbedderHost embedderHost) throws Exception {
        // Prepare WASI and Emscripten host imports
        var installer = new ChicoryEmscriptenHostInstaller.Builder()
                .setHost(embedderHost)
                .build();

        ArrayList<ImportFunction> hostFunctions = new ArrayList<>(installer.setupWasiPreview1HostFunctions());
        var emscriptenFinalizer = installer.setupEmscriptenFunctions();
        hostFunctions.addAll(emscriptenFinalizer.getEmscriptenFunctions());

        var hostImports = ImportValues.builder().withFunctions(hostFunctions).build();

        // Load WebAssembly binary
        final WasmModule wasmModule;
        try (var str = Thread.currentThread().getContextClassLoader().getResourceAsStream("helloworld.wasm")) {
            wasmModule = Parser.parse(str);
        }

        // Instantiate the WebAssembly module
        var instance = Instance.builder(wasmModule)
                .withImportValues(hostImports)
                .withInitialize(true)
                .withStart(false)
                .build();

        // Finalize initialization after module instantiation
        var emscriptenRuntime = emscriptenFinalizer.finalize(instance);

        // Initialize Emscripten runtime environment
        emscriptenRuntime.initMainThread();

        // Execute code
        long exitCode = instance.export("main").apply(
                /* argc */ 0,
                /* argv */ 0
        )[0];
    }
}
