package io.github.kgpu.wgpuj.jnrgen;

import java.util.Map;
import java.util.function.Consumer;

/** Custom handlers for specific structs with strange properties like padding, weird names, etc. */
public class Hooks {

    private static final Map<String, Consumer<Item>> hooks =
            Map.of(
                    "WgpuBindGroupEntry",
                    Hooks::bindGroupEntryPadding,
                    "WgpuBindGroupLayoutEntry",
                    Hooks::bindGroupLayoutEntryPadding);

    private static int paddingCount = 0;

    public static void preSave(OutputHandler handler) {
        handler.registerType("WGPUBindingResource", new MockStructItem("WgpuBindingResource"));
    }

    public static void bindGroupEntryPadding(Item item) {
        if (!(item instanceof StructItem))
            throw new RuntimeException("Expected struct, found " + item);
        var struct = (StructItem) item;

        struct.getFields().add(1, createPadding("uint32_t"));
    }

    public static void bindGroupLayoutEntryPadding(Item item) {
        if (!(item instanceof StructItem))
            throw new RuntimeException("Expected struct, found " + item);
        var struct = (StructItem) item;

        struct.getFields().add(createPadding("uint32_t"));
    }

    private static StructItem.StructField createPadding(String type) {
        return new StructItem.StructField(
                type, StructItem.doNotUsePrefix + "padding_" + paddingCount++, false);
    }

    public static Map<String, Consumer<Item>> getHooks() {
        return hooks;
    }
}
