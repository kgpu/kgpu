package com.noahcharlton.wgpuj.jnrgen;

import java.io.IOException;

public class TypeAliasItem implements Item {

    private final String newType;
    private final String originalType;

    public TypeAliasItem(String newType, String originalType) {
        this.newType = newType.replace("*", "");
        this.originalType = originalType.replace("*", "");;
    }

    @Override
    public void save(OutputHandler outputHandler) throws IOException {
    }

    @Override
    public void preSave(OutputHandler outputHandler) {
        outputHandler.registerTypeAlias(originalType, newType);
    }

    @Override
    public String getJavaTypeName() {
        //This function is only called if this item is used as a field
        throw new UnsupportedOperationException("This is not a java type.");
    }
}
