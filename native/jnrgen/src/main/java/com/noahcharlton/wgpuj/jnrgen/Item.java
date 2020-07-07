package com.noahcharlton.wgpuj.jnrgen;

import java.io.IOException;

public interface Item {

    void save(OutputHandler outputHandler) throws IOException;

    default void preSave(OutputHandler outputHandler){}

    String getJavaTypeName();
}
