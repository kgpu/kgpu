package com.noahcharlton.wgpuj.jnrgen;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class EnumItem implements Item {

    private final String name;
    private final List<EnumField> fields;

    public EnumItem(String name, List<EnumField> fields) {
        this.name = name.replace("_", "");
        this.fields = fields;

        this.fields.sort(Comparator.comparingInt(field -> field.index));
    }

    @Override
    public void preSave(OutputHandler outputHandler) {
        outputHandler.registerType(name, this);
    }

    @Override
    public void save(OutputHandler outputHandler) throws IOException {
        String className = this.name.replace("WGPU", "Wgpu");

        if(outputHandler.isExcluded(name)) {
            return;
        }

        BufferedWriter writer = outputHandler.startFile(className + ".java");

        writer.write("public enum ");
        writer.write(className.replace("WGPU", "Wgpu"));
        writer.write(" {\n");

        saveFields(writer);

        writer.write("}");

        writer.flush();
        writer.close();
    }

    private void saveFields(BufferedWriter writer) throws IOException {
        for(EnumField field : fields){
            if(!field.comment.isEmpty()){
                writer.write("     ");
                writer.write(field.comment.replace("\n", "\n    "));
                writer.write("\n");
            }

            writer.write("    ");
            writer.write(toFieldName(field.name));
            writer.write(",\n");
        }
    }

    @Override
    public String getJavaTypeName() {
        return this.name.replace("WGPU", "Wgpu");
    }

    private String toFieldName(String fieldName) {
        //Replace tag due to CBindgen adding "Tag" for enum names, but not for each field for some reason
        String cFieldHeader = this.name.replace("Tag", "");

        fieldName = fieldName.replace(cFieldHeader, "").replace("_", "");
        StringBuilder output = new StringBuilder();

        for(char c : fieldName.toCharArray()){
            if(Character.isUpperCase(c) && !output.toString().isEmpty())
                output.append("_");

            output.append(Character.toUpperCase(c));
        }

        return OutputHandler.toExportName(output.toString());
    }

    @Override
    public String toString() {
        return "Enum(" + name + ")";
    }

    static class EnumField{
        private final String name;
        private final int index;
        private final String comment;

        public EnumField(String name, int index, String comment) {
            this.name = name;
            this.index = index;
            this.comment = comment;
        }
    }
}
