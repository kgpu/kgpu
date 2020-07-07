package com.noahcharlton.wgpuj.jnrgen;

import java.io.IOException;

public class MockStructItem implements Item{

    private final String javaType;

    public MockStructItem(String javaType) {
        this.javaType = javaType;
    }

    @Override
    public void save(OutputHandler outputHandler) throws IOException {

    }

    @Override
    public String getJavaTypeName() {
        return javaType;
    }
}
