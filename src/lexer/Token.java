package lexer;

public final class Token {

    private final String lexeme;
    private final TokenType type;
    private final int line;
    private final int column;

    /**
     * Creates a new token.
     *
     * @param lexeme the exact source text of the token (may be "" for EOF)
     * @param type   the token type
     * @param line   the starting line number (1-based)
     * @param column the starting column number (1-based)
     */
    public Token(String lexeme, TokenType type, int line, int column) {
        if (lexeme == null) {
            throw new IllegalArgumentException("Lexeme must not be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Token type must not be null.");
        }
        this.lexeme = lexeme;
        this.type = type;
        this.line = line;
        this.column = column;
    }

    // -- Getters

    public String getLexeme() {
        return lexeme;
    }

    public TokenType getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    // -- Helper methods

    // Returns a new token with a different type but the same lexeme, line, and column. Pass 2 uses this when it corrects a classification.
    public Token withType(TokenType newType) {
        return new Token(lexeme, newType, line, column);
    }

    // True if this token is an ERROR token.
    public boolean isError() {
        return type == TokenType.ERROR;
    }

    // True if this token is a single-line or multi-line comment.
    public boolean isComment() {
        return type == TokenType.SINGLE_LINE_COMMENT
                || type == TokenType.MULTI_LINE_COMMENT;
    }

    // Returns a printable version of the lexeme for the tables.
    public String getDisplayLexeme() {
        if (type == TokenType.EOF) {
            return "[EOF]";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lexeme.length(); i++) {
            char c = lexeme.charAt(i);
            if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Token{lexeme='" + getDisplayLexeme()
                + "', type=" + type
                + ", line=" + line
                + ", column=" + column + "}";
    }
}