package com.noahcharlton.wgpuj.jnrgen;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Custom handlers for specific structs with strange properties
 * like padding, weird names, etc.
 */
public class Hooks {

    private static final Map<String, Consumer<Item>> hooks = Map.of(
            "WgpuBindGroupEntry", Hooks::bindGroupEntryPadding
    );

    public static void preSave(OutputHandler handler){
        handler.registerType("WGPUBindingResource", new MockStructItem("WgpuBindingResource"));
    }

    public static void bindGroupEntryPadding(Item item){
        if(!(item instanceof StructItem))
            throw new RuntimeException("Expected struct, found " + item);
        var struct = (StructItem) item;
        var padding = new StructItem.StructField("uint32_t", StructItem.doNotUsePrefix + "padding", false);

        struct.getFields().add(1, padding);
    }

    public static Map<String, Consumer<Item>> getHooks() {
        return hooks;
    }
}
