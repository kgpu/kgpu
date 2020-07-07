package com.noahcharlton.wgpuj.jnrgen;

import java.util.Objects;

public class Token {

    public enum TokenType {
        COMMENT,
        IDENTIFIER,
        HASH,
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS,
        OPEN_BRACKET,
        CLOSE_BRACKET,
        LESS_THAN,
        GREATER_THAN,
        COMMA,
        SEMICOLON,
        PERIOD,
        EQUAL,
        NEWLINE,
        EOF,
    }

    private final TokenType type;
    private final String text;

    public Token(TokenType type, String text) {
        this.type = type;
        this.text = text;
    }

    public Token(TokenType type) {
        this(type, "");
    }

    public static Token identifier(String text){
        return new Token(TokenType.IDENTIFIER, text);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Token)) return false;
        Token token = (Token) o;
        return getType() == token.getType() &&
                Objects.equals(getText(), token.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getText());
    }

    @Override
    public String toString() {
        String text = this.text;

        if(text.length() > 50)
            text = text.substring(0, 50) + "...";

        return String.format("%s(%s)", type, text);
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
