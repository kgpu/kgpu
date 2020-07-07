package com.noahcharlton.wgpuj.jnrgen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JNRGenerator {

    public static void main(String[] args) {
        if(args.length < 1){
            throw new RuntimeException("Please provide an argument for the output location");
        }

        File outputDirectory = createOutputDirectory(args[0]);
        OutputHandler outputHandler = new OutputHandler(outputDirectory);
        String header = readHeaderFile();

        List<Token> tokens = new Scanner(header).getTokens();
        List<Item> items = new Parser(tokens).getItems();
        for(Item item: items){
            try{
                item.preSave(outputHandler);
            }catch(RuntimeException e){
                System.out.println("Failed to parse type from item " + item + ": " + e.getLocalizedMessage());
            }
        }
        Hooks.preSave(outputHandler);

        for(Item item: items){
            try{
                item.save(outputHandler);
            }catch(RuntimeException | IOException e){
                System.out.println("Failed to save item " + item + ": " + e.getLocalizedMessage());
            }
        }

        try{
            outputHandler.saveConstants();
        }catch(IOException e){
            System.out.println("Failed to save constants: " + e);
        }
    }

    private static String readHeaderFile() {
        InputStream headerStream = JNRGenerator.class.getResourceAsStream("/wgpu.h");

        if(headerStream == null){
            throw new RuntimeException("Failed to find wgpu.h in the classpath!");
        }

        try {
            return new String(headerStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch(IOException e) {
            throw new RuntimeException("Failed to read");
        }
    }

    private static File createOutputDirectory(String arg){
        File file = new File(arg + "/jnr-gen/");
        System.out.println("Output Directory: " + file);

        if(file.exists()){
            System.out.println("Output directory already exists, deleting!");

            if(!file.delete()){
                throw new RuntimeException("Failed to delete previous output directory!");
            }
        }

        if(!file.mkdir()){
            throw new RuntimeException("Failed to make the output directory!");
        }

        return file;
    }
}
