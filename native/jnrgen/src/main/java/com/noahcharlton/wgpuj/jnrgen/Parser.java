package com.noahcharlton.wgpuj.jnrgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Parser {

    private final List<Item> items = new ArrayList<>();
    private final List<Token> tokens;

    private Token lastComment;
    private int index = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;

        parse();
    }

    private void parse() {
        Token token;

        while((token = poll()) != null){
            var item = createItem(token);

            if(item != null){
                lastComment = null;
                items.add(item);
            }
        }
    }

    private Item createItem(Token token) {
        if(Token.identifier("typedef").equals(token)){
            var next = poll();

            if(Token.identifier("enum").equals(next)){
                return createEnum();
            }else if(Token.identifier("struct").equals(next)){
                return createStruct();
            }else if(Token.identifier("void").equals(next)){
                //Callback definition
            }else{
                return createTypeAlias(next);
            }
        }else if(token.getType() == Token.TokenType.HASH){
            var macroType = pollExpect(Token.TokenType.IDENTIFIER);

            if(Token.identifier("define").equals(macroType)){
                return createConstant();
            }
        }else if(token.getType() == Token.TokenType.COMMENT){
            lastComment = token;
        }else if(Token.identifier("enum").equals(token)){
            return createEnum();
        }
        return null;
    }

    private Item createConstant() {
        var name = pollExpect(Token.TokenType.IDENTIFIER);

        if(Token.TokenType.IDENTIFIER != peek().getType()) {
            return null;
        }

        var def = pollExpect(Token.TokenType.IDENTIFIER);

        return new ConstantItem(name.getText(), def.getText(), getLastCommentOrEmpty());
    }

    private Item createTypeAlias(Token token) {
        List<Token> tokens = new ArrayList<>();

        while(token != null && token.getType() != Token.TokenType.SEMICOLON){
            tokens.add(token);
            token = poll();
        }

        String alias = tokens.remove(tokens.size() - 1).getText();
        String original = tokens.stream().map(Token::getText).collect(Collectors.joining(" "));

        return new TypeAliasItem(alias, original);
    }

    private Item createStruct() {
        List<StructItem.StructField> fields = new ArrayList<>();
        skipWhitespace();
        pollExpect(Token.TokenType.IDENTIFIER);
        skipWhitespace();

        //might be something like typedef struct WGPUSamplerDescriptor WGPUSamplerDescriptor;
        if(!new Token(Token.TokenType.OPEN_BRACKET).equals(peek())){
            return null;
        }

        pollExpect(Token.TokenType.OPEN_BRACKET);
        skipWhitespace();

        while(!new Token(Token.TokenType.CLOSE_BRACKET).equals(peek())){
            if(Token.identifier("const").equals(peek())){
                poll();
            }

            if(Token.identifier("union").equals(peek())){
                return null; //Unions in structs currently not supported
            }

            Token fieldType = pollExpect(Token.TokenType.IDENTIFIER);
            Token fieldName = pollExpect(Token.TokenType.IDENTIFIER);
            pollExpect(Token.TokenType.SEMICOLON);
            skipWhitespace();

            fields.add(new StructItem.StructField(fieldType.getText(), fieldName.getText()));
        }

        pollExpect(Token.TokenType.CLOSE_BRACKET);
        Token structName = pollExpect(Token.TokenType.IDENTIFIER);
        pollExpect(Token.TokenType.SEMICOLON);

        return new StructItem(structName.getText(), fields);
    }

    private Item createEnum() {
        List<EnumItem.EnumField> fields = new ArrayList<>();

        skipWhitespace();
        var enumIdentifier = pollExpect(Token.TokenType.IDENTIFIER);
        skipWhitespace();
        pollExpect(Token.TokenType.OPEN_BRACKET);
        lastComment = null;
        skipWhitespace();

        Token token;
        while((token = poll()).getType() != Token.TokenType.CLOSE_BRACKET){
            Token identifier = Objects.requireNonNull(token);
            Token equalOrComma = Objects.requireNonNull(poll());

            if(equalOrComma.getType() == Token.TokenType.COMMA){
                fields.add(new EnumItem.EnumField(identifier.getText(), fields.size(), getLastCommentOrEmpty()));
            }else{
                Token valueToken = pollExpect(Token.TokenType.IDENTIFIER);
                int value = Integer.parseInt(valueToken.getText());

                fields.add(new EnumItem.EnumField(identifier.getText(), value, getLastCommentOrEmpty()));

                pollExpect(Token.TokenType.COMMA);
            }

            lastComment = null;
            skipWhitespace();
        }

        return new EnumItem(enumIdentifier.getText(), fields);
    }

    private void skipWhitespace(){
        while(peek().getType() == Token.TokenType.NEWLINE || peek().getType() == Token.TokenType.COMMENT){
            if(peek().getType() == Token.TokenType.COMMENT)
                lastComment = peek();

            poll();
        }
    }

    private Token pollExpect(Token.TokenType type){
        var token = poll();

        if(token == null || token.getType() != type){
            throw new RuntimeException("Expected " + type + " but found " + token);
        }

        return token;
    }

    private String getLastCommentOrEmpty(){
        return lastComment == null ? "" : lastComment.getText();
    }

    private Token poll(){
        if(index >= tokens.size())
            return null;

        return tokens.get(index++);
    }

    private Token peek(){
        if(index >= tokens.size())
            return null;

        return tokens.get(index);
    }

    public List<Item> getItems() {
        return items;
    }
}
