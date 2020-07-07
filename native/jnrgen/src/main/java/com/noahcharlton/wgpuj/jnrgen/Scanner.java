package com.noahcharlton.wgpuj.jnrgen;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private final List<Token> tokens = new ArrayList<>();
    private final String text;

    private int index;

    public Scanner(String text) {
        this.text = text;

        parse();
    }

    private void parse() {
        while(index < text.length()){
            Token token = parseToken(poll());

            if(token != null){
                tokens.add(token);
            }
        }

        tokens.add(new Token(Token.TokenType.EOF));
    }

    private Token parseToken(char c) {
        switch(c){
            case '#':
                return new Token(Token.TokenType.HASH);
            case '(':
                return new Token(Token.TokenType.LEFT_PARENTHESIS);
            case ')':
                return new Token(Token.TokenType.RIGHT_PARENTHESIS);
            case '{':
                return new Token(Token.TokenType.OPEN_BRACKET);
            case '}':
                return new Token(Token.TokenType.CLOSE_BRACKET);
            case '<':
                return new Token(Token.TokenType.LESS_THAN);
            case '>':
                return new Token(Token.TokenType.GREATER_THAN);
            case ',':
                return new Token(Token.TokenType.COMMA);
            case ';':
                return new Token(Token.TokenType.SEMICOLON);
            case '.':
                return new Token(Token.TokenType.PERIOD);
            case '=':
                return new Token(Token.TokenType.EQUAL);
            case '\n':
                return new Token(Token.TokenType.NEWLINE);
            case '\r':
            case ' ':
                return null;
            case '/':
                char next = poll();
                if(next == '*'){
                    String comment = readComment();

                    return new Token(Token.TokenType.COMMENT, comment);
                }else{
                    System.out.println("Expected * after / but found: " + next);
                    return null;
                }
            default:
                if(isIdentifierCharacter(c)){
                    String text = readIdentifier(c);

                    return Token.identifier(text);
                }else{
                    System.out.println("Unknown character: " + c);

                    return null;
                }
        }
    }

    private String readComment() {
        StringBuilder builder = new StringBuilder("/*");

        char c;
        char prev = 0;

        while((c = poll()) != 0){
            builder.append(c);

            if(c == '/' && prev == '*'){
                break;
            }

            prev = c;
        }

        return builder.toString();
    }

    private String readIdentifier(char c) {
        StringBuilder builder = new StringBuilder(String.valueOf(c));

        while(index < text.length() && isIdentifierCharacter(peek())){
            builder.append(poll());
        }

        return builder.toString();
    }

    private boolean isIdentifierCharacter(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '*';
    }

    private char poll(){
        if(index >= text.length())
            return 0;

        return text.charAt(index++);
    }

    private char peek(){
        if(index >= text.length())
            return 0;

        return text.charAt(index);
    }

    public List<Token> getTokens() {
        return tokens;
    }
}
