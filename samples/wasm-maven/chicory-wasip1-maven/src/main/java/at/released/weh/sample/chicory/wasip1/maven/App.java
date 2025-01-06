package at.released.weh.sample.chicory.wasip1.maven;

import at.released.weh.bindings.chicory.exception.ProcExitException;
import at.released.weh.bindings.chicory.wasip1.ChicoryWasiPreview1Builder;
import at.released.weh.host.EmbedderHost;
import at.released.weh.host.EmbedderHostBuilder;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import java.util.Collection;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {
        // Load WebAssembly binary
        final WasmModule wasmModule;
        try (var str = Thread.currentThread().getContextClassLoader().getResourceAsStream("helloworld_wasi.wasm")) {
            wasmModule = Parser.parse(str);
        }

        // Prepare Host
        var hostBuilder = new EmbedderHostBuilder();
        hostBuilder.fileSystem().addPreopenedDirectory(".", "/data");

        try (var embedderHost = hostBuilder.build()) {
            executeWasmCode(embedderHost, wasmModule);
        }
    }

    private static void executeWasmCode(EmbedderHost embedderHost, WasmModule wasmModule) {
        // Prepare WASI host imports
        Collection<HostFunction> wasiImports = new ChicoryWasiPreview1Builder().setHost(embedderHost).build();

        var hostImports = ImportValues.builder().withFunctions(List.copyOf(wasiImports)).build();

        // Instantiate the WebAssembly module
        var instance = Instance.builder(wasmModule)
                .withImportValues(hostImports)
                .withInitialize(true)
                .withStart(false)
                .build();

        // Execute code
        try {
            instance.export("_start").apply();
        } catch (ProcExitException pre) {
            if (pre.getExitCode() != 0) {
                System.exit(pre.getExitCode());
            }
        }
    }
}
