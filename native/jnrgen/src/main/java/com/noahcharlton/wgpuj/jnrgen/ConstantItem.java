package com.noahcharlton.wgpuj.jnrgen;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;

public class ConstantItem implements Item {

    private final String cName;
    private final String value;
    private final String comment;

    private String javaType;
    private String variableName;

    public ConstantItem(String cName, String value, String comment) {
        this.cName = cName;
        this.value = value;
        this.comment = comment;
    }

    @Override
    public void save(OutputHandler outputHandler) throws IOException {

    }

    public void write(BufferedWriter writer, String indent) throws IOException {
        if(!comment.isEmpty()){
            writer.write(indent);
            writer.write(comment.replace("\n", "\n" + indent));
            writer.write("\n");
        }

        writer.write(indent);
        writer.write("public static final ");
        writer.write(javaType);
        writer.write(" ");
        writer.write(variableName);
        writer.write(" = ");

        if(value.contains("ULL")){
            writer.write("Long.parseUnsignedLong(\"");
            writer.write(value.replace("ULL", ""));
            writer.write("\")");
        }else{
            writer.write(value);
        }

        writer.write(";\n");
    }

    @Override
    public void preSave(OutputHandler outputHandler) {
        determineType();

        String[] nameParts = cName.split("_", 2);
        String parentType = nameParts[0];

        if(parentType.equals(parentType.toUpperCase())){
            outputHandler.registerConstant("", this);
            variableName = cName.substring(4);//Remove wgpu
        }else{
            variableName = nameParts[1];
            outputHandler.registerConstant(parentType.replace("WGPU", "Wgpu"), this);
        }
    }

    private void determineType() {
        if(value.contains("ULL")){
            javaType = "long";
            return;
        }

        try{
            BigInteger value = new BigInteger(this.value);

            if(value.equals(BigInteger.valueOf(value.intValue()))){
                javaType = "int";
            }else if(value.equals(BigInteger.valueOf(value.longValue()))){
                javaType = "long";
            }else{
                throw new RuntimeException("Type is too big for java!");
            }
        }catch(RuntimeException e){
            throw new RuntimeException("Failed to get type for " + this + ": " + e.getLocalizedMessage());
        }
    }

    @Override
    public String getJavaTypeName() {
        return javaType;
    }

    @Override
    public String toString() {
        return "Constant(" + cName + " = " + value +")";
    }
}
