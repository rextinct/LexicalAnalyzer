package lexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lexer.java
 * The two-pass lexical analyzer for GamiPhi.
 *
 * WHITESPACE
 *   Spaces, tabs and newlines are read (so line and column numbers stay
 *   correct) but they are NOT tokenized. They are skipped between tokens.
 *
 * PASS 1 (runPass1) - scanning and recognition
 *   Reads the source one character at a time using the State enum (a DFA).
 *   It uses lookahead for longest-match operators and decimal points.
 *   It produces an INTERMEDIATE list of tokens and creates ERROR tokens
 *   for malformed lexemes (bad strings, bad characters, bad numbers, ...).
 *   It never adds EOF.
 *
 * PASS 2 (runPass2) - validation and finalization
 *   Does NOT look at the source text again. It only walks the intermediate
 *   token list and, for each token:
 *     - re-checks keyword / reserved word / identifier classification
 *     - enforces the 16-character identifier limit
 *     - checks operators, delimiters, literals and comments
 *       against their official lexemes
 *   Then it appends exactly one EOF token.
 *
 * No syntax or semantic analysis is done here.
 */
public class Lexer {

    /** Maximum identifier length allowed by GamiPhi. */
    public static final int MAX_IDENTIFIER_LENGTH = 16;

    /**
     * Supported escape characters after a backslash in strings and
     * character literals. (ASSUMPTION - not in the official token list.)
     * Change this set if your language design uses different escapes.
     */
    private static final Set<Character> VALID_ESCAPES =
            new HashSet<>(Arrays.asList('n', 't', 'r', '0', '\\', '\'', '"'));

    // ===== Lookup tables =====

    /** Case-sensitive keywords. */
    private static final Map<String, TokenType> KEYWORDS = new LinkedHashMap<>();

    /** Case-sensitive reserved words. */
    private static final Map<String, TokenType> RESERVED_WORDS = new LinkedHashMap<>();

    /** Every operator and delimiter lexeme. Used by Pass 2 to validate symbols. */
    private static final Map<String, TokenType> SYMBOLS = new LinkedHashMap<>();

    static {
        // Keywords
        KEYWORDS.put("Mark", TokenType.MARK_KEYWORD);
        KEYWORDS.put("Bool", TokenType.BOOL_DATA_TYPE);
        KEYWORDS.put("Char", TokenType.CHAR_DATA_TYPE);
        KEYWORDS.put("Double", TokenType.DOUBLE_DATA_TYPE);
        KEYWORDS.put("Float", TokenType.FLOAT_DATA_TYPE);
        KEYWORDS.put("Int", TokenType.INT_DATA_TYPE);
        KEYWORDS.put("String", TokenType.STRING_DATA_TYPE);
        KEYWORDS.put("True", TokenType.BOOLEAN_TRUE_KEYWORD);
        KEYWORDS.put("False", TokenType.BOOLEAN_FALSE_KEYWORD);
        KEYWORDS.put("For", TokenType.FOR_LOOP_KEYWORD);
        KEYWORDS.put("Do", TokenType.DO_LOOP_KEYWORD);
        KEYWORDS.put("While", TokenType.WHILE_LOOP_KEYWORD);
        KEYWORDS.put("Break", TokenType.BREAK_KEYWORD);
        KEYWORDS.put("Continue", TokenType.CONTINUE_KEYWORD);
        KEYWORDS.put("If", TokenType.IF_KEYWORD);
        KEYWORDS.put("Else", TokenType.ELSE_KEYWORD);
        KEYWORDS.put("Print", TokenType.PRINT_KEYWORD);
        KEYWORDS.put("Write", TokenType.WRITE_KEYWORD);
        KEYWORDS.put("Return", TokenType.RETURN_STATEMENT_KEYWORD);
        KEYWORDS.put("Switch", TokenType.SWITCH_KEYWORD);
        KEYWORDS.put("Case", TokenType.CASE_KEYWORD);

        // Reserved words
        RESERVED_WORDS.put("ENTITY", TokenType.ENTITY_RESERVE_WORD);
        RESERVED_WORDS.put("COMP", TokenType.COMP_RESERVE_WORD);
        RESERVED_WORDS.put("BEHAVIOR", TokenType.BEHAVIOR_RESERVE_WORD);
        RESERVED_WORDS.put("USE", TokenType.USE_RESERVE_WORD);
        RESERVED_WORDS.put("ADD", TokenType.ADD_RESERVE_WORD);
        RESERVED_WORDS.put("SPAWN", TokenType.SPAWN_RESERVE_WORD);
        RESERVED_WORDS.put("DESTROY", TokenType.DESTROY_RESERVE_WORD);
        RESERVED_WORDS.put("RANK", TokenType.RANK_RESERVE_WORD);
        RESERVED_WORDS.put("SPOTLIGHT", TokenType.SPOTLIGHT_RESERVE_WORD);
        RESERVED_WORDS.put("SCENE", TokenType.SCENE_RESERVE_WORD);
        RESERVED_WORDS.put("LOAD", TokenType.LOAD_RESERVE_WORD);
        RESERVED_WORDS.put("KEEP", TokenType.KEEP_RESERVE_WORD);
        RESERVED_WORDS.put("OVER", TokenType.OVER_RESERVE_WORD);
        RESERVED_WORDS.put("BACK", TokenType.BACK_RESERVE_WORD);
        RESERVED_WORDS.put("WHEN", TokenType.WHEN_RESERVE_WORD);
        RESERVED_WORDS.put("TOUCH", TokenType.TOUCH_RESERVE_WORD);
        RESERVED_WORDS.put("INPUT", TokenType.INPUT_RESERVE_WORD);
        RESERVED_WORDS.put("TRIGGER", TokenType.TRIGGER_RESERVE_WORD);
        RESERVED_WORDS.put("TRIGGER_ON", TokenType.TRIGGER_ON_RESERVE_WORD);
        RESERVED_WORDS.put("AFTER", TokenType.AFTER_RESERVE_WORD);
        RESERVED_WORDS.put("SECONDS", TokenType.SECONDS_RESERVE_WORD);
        RESERVED_WORDS.put("WAIT", TokenType.WAIT_RESERVE_WORD);
        RESERVED_WORDS.put("PAUSE", TokenType.PAUSE_RESERVE_WORD);
        RESERVED_WORDS.put("RESUME", TokenType.RESUME_RESERVE_WORD);
        RESERVED_WORDS.put("PARALLEL", TokenType.PARALLEL_RESERVE_WORD);
        RESERVED_WORDS.put("SPAN", TokenType.SPAN_RESERVE_WORD);

        //JAVA Keywords
            //Access Modifiers
        RESERVED_WORDS.put("public", TokenType.PUBLIC_RESERVE_WORD);
        RESERVED_WORDS.put("private", TokenType.PRIVATE_RESERVE_WORD);
        RESERVED_WORDS.put("protected", TokenType.PROTECTED_RESERVE_WORD);
            //OOP & Class Structures
        RESERVED_WORDS.put("void", TokenType.VOID_RESERVE_WORD);
        RESERVED_WORDS.put("abstract", TokenType.ABSTRACT_RESERVE_WORD);
        RESERVED_WORDS.put("class", TokenType.CLASS_RESERVE_WORD);
        RESERVED_WORDS.put("enum", TokenType.ENUM_RESERVE_WORD);
        RESERVED_WORDS.put("extends", TokenType.EXTENDS_RESERVE_WORD);
        RESERVED_WORDS.put("implements", TokenType.IMPLEMENTS_RESERVE_WORD);
        RESERVED_WORDS.put("interface", TokenType.INTERFACE_RESERVE_WORD);
        RESERVED_WORDS.put("native", TokenType.NATIVE_RESERVE_WORD);
        RESERVED_WORDS.put("static", TokenType.STATIC_RESERVE_WORD);
        RESERVED_WORDS.put("new", TokenType.NEW_RESERVE_WORD);
        RESERVED_WORDS.put("super", TokenType.SUPER_RESERVE_WORD);
        RESERVED_WORDS.put("this", TokenType.THIS_RESERVE_WORD);
            //Exception Handling
        RESERVED_WORDS.put("assert", TokenType.ASSERT_RESERVE_WORD);
        RESERVED_WORDS.put("catch", TokenType.CATCH_RESERVE_WORD);
        RESERVED_WORDS.put("finally", TokenType.FINALLY_RESERVE_WORD);
        RESERVED_WORDS.put("throw", TokenType.THROW_RESERVE_WORD);
        RESERVED_WORDS.put("throws", TokenType.THROWS_RESERVE_WORD);
        RESERVED_WORDS.put("try", TokenType.TRY_RESERVE_WORD);
            //Import and Packages
        RESERVED_WORDS.put("import", TokenType.IMPORT_RESERVE_WORD);
        RESERVED_WORDS.put("package", TokenType.PACKAGE_RESERVE_WORD);


        // Assignment operators
        SYMBOLS.put("=", TokenType.ASSIGNMENT_OPERATOR);
        SYMBOLS.put("+=", TokenType.ADDITION_ASSIGNMENT_OPERATOR);
        SYMBOLS.put("-=", TokenType.SUBTRACTION_ASSIGNMENT_OPERATOR);
        SYMBOLS.put("*=", TokenType.MULTIPLICATION_ASSIGNMENT_OPERATOR);
        SYMBOLS.put("/=", TokenType.DIVISION_ASSIGNMENT_OPERATOR);
        SYMBOLS.put("%=", TokenType.MODULO_ASSIGNMENT_OPERATOR);
        SYMBOLS.put("~=", TokenType.LIVE_ASSIGNMENT_OPERATOR);
        // Arithmetic operators
        SYMBOLS.put("+", TokenType.ADDITION_OPERATOR);
        SYMBOLS.put("-", TokenType.SUBTRACTION_OPERATOR);
        SYMBOLS.put("*", TokenType.MULTIPLICATION_OPERATOR);
        SYMBOLS.put("/", TokenType.DIVISION_OPERATOR);
        SYMBOLS.put("%", TokenType.MODULO_OPERATOR);
        SYMBOLS.put("**", TokenType.EXPONENT_OPERATOR);
        // Increment / decrement
        SYMBOLS.put("++", TokenType.INCREMENT_OPERATOR);
        SYMBOLS.put("--", TokenType.DECREMENT_OPERATOR);
        // Boolean operators
        SYMBOLS.put("!", TokenType.NOT_OPERATOR);
        SYMBOLS.put("&&", TokenType.AND_OPERATOR);
        SYMBOLS.put("||", TokenType.OR_OPERATOR);
        // Relational operators
        SYMBOLS.put("==", TokenType.EQUALITY_OPERATOR);
        SYMBOLS.put("!=", TokenType.NOT_EQUAL_OPERATOR);
        SYMBOLS.put("<", TokenType.LESS_THAN_OPERATOR);
        SYMBOLS.put(">", TokenType.MORE_THAN_OPERATOR);
        SYMBOLS.put("<=", TokenType.LESS_THAN_EQUAL_OPERATOR);
        SYMBOLS.put(">=", TokenType.MORE_THAN_EQUAL_OPERATOR);
        // Delimiters
        SYMBOLS.put(";", TokenType.STATEMENT_END_DELIMITER);
        SYMBOLS.put(",", TokenType.COMMA_DELIMITER);
        SYMBOLS.put(".", TokenType.ACCESS_OBJECT_DELIMITER);
        SYMBOLS.put("(", TokenType.OPEN_PARENTHESIS_DELIMITER);
        SYMBOLS.put(")", TokenType.CLOSE_PARENTHESIS_DELIMITER);
        SYMBOLS.put("[", TokenType.OPEN_LIST_DELIMITER);
        SYMBOLS.put("]", TokenType.CLOSE_LIST_DELIMITER);
        SYMBOLS.put("{", TokenType.OPEN_BLOCK_DELIMITER);
        SYMBOLS.put("}", TokenType.CLOSE_BLOCK_DELIMITER);
    }

    // ===== Lexical error record =====

    /** One lexical error, shown in the error table. */
    public static final class LexicalError {
        private final String lexeme;
        private final String description;
        private final int line;
        private final int column;

        public LexicalError(String lexeme, String description, int line, int column) {
            this.lexeme = lexeme;
            this.description = description;
            this.line = line;
            this.column = column;
        }

        public String getLexeme() { return lexeme; }
        public String getDescription() { return description; }
        public int getLine() { return line; }
        public int getColumn() { return column; }

        /** Printable lexeme for the tables (tabs and newlines made visible). */
        public String getDisplayLexeme() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lexeme.length(); i++) {
                char ch = lexeme.charAt(i);
                if (ch == '\n') {
                    sb.append("\\n");
                } else if (ch == '\r') {
                    sb.append("\\r");
                } else if (ch == '\t') {
                    sb.append("\\t");
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
    }

    // ===== Instance fields =====

    private final String source;
    private int pos;
    private int line;
    private int column;

    /** Position just after the last character. Pass 2 gives EOF this position. */
    private int eofLine = 1;
    private int eofColumn = 1;

    private final List<Token> intermediateTokens = new ArrayList<>();
    private List<Token> finalTokens = new ArrayList<>();
    private final List<LexicalError> errors = new ArrayList<>();

    /** Error descriptions created in Pass 1, keyed by the ERROR token itself. */
    private final Map<Token, String> errorMessages = new IdentityHashMap<>();

    /**
     * Creates a lexer for the given source code. The source can be any length.
     * A leading byte-order mark (\uFEFF), which some editors add, is removed.
     */
    public Lexer(String source) {
        if (source == null) {
            throw new IllegalArgumentException("Source code must not be null.");
        }
        if (source.startsWith("\uFEFF")) {
            source = source.substring(1);
        }
        this.source = source;
    }

    // ===== Public API =====

    /** Runs both passes and returns the final token list (ends with exactly one EOF). */
    public List<Token> tokenize() {
        List<Token> intermediate = runPass1();
        return runPass2(intermediate);
    }

    /** The intermediate tokens from Pass 1 (no EOF). */
    public List<Token> getIntermediateTokens() {
        return Collections.unmodifiableList(intermediateTokens);
    }

    /** The finalized tokens from Pass 2. */
    public List<Token> getTokens() {
        return Collections.unmodifiableList(finalTokens);
    }

    /** The lexical errors, in source order. */
    public List<LexicalError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    // =====================================================================
    // PASS 1 - Scanning and recognition
    // =====================================================================

    /** Scans the whole source and returns the intermediate token list. */
    public List<Token> runPass1() {
        pos = 0;
        line = 1;
        column = 1;
        intermediateTokens.clear();
        finalTokens = new ArrayList<>();
        errors.clear();
        errorMessages.clear();

        while (pos < source.length()) {
            if (isWhitespace(peek(0))) {
                skipWhitespace();       // read but do not tokenize
            } else {
                scanToken();
            }
        }

        eofLine = line;
        eofColumn = column;
        return getIntermediateTokens();
    }

    /**
     * Recognizes exactly one lexeme, starting at the current position,
     * by moving through the states of the DFA until a final state is reached.
     * The caller guarantees that the current character is not whitespace.
     */
    private void scanToken() {
        final int startPos = pos;
        final int startLine = line;
        final int startColumn = column;

        State state = State.START;
        TokenType result = null;   // token type when the lexeme is valid
        String errorMsg = null;    // description when the lexeme is malformed

        while (!state.isFinal()) {
            int c = peek(0);       // current character, or -1 at end of source

            switch (state) {

                // ---------- Beginning of a lexeme ----------
                case START:
                    if (isWordStart(c)) {
                        advance();
                        state = State.IN_WORD;
                    } else if (isDigit(c)) {
                        advance();
                        state = State.IN_INTEGER;
                    } else if (c == '"') {
                        advance();
                        state = State.IN_STRING;
                    } else if (c == '\'') {
                        advance();
                        state = State.CHAR_OPEN;
                    } else if (c == '#') {
                        advance();
                        state = State.HASH;
                    } else if (operatorStartState(c) != null) {
                        State next = operatorStartState(c);
                        advance();
                        state = next;
                    } else if (delimiterType(c) != null) {
                        result = delimiterType(c);
                        advance();
                        state = State.DONE;
                    } else {
                        // Not part of any GamiPhi token
                        int codePoint = source.codePointAt(pos);
                        advance();
                        if (Character.charCount(codePoint) == 2) {
                            advance();          // second half of a surrogate pair
                            column--;           // but it is still one column
                        }
                        errorMsg = "Invalid character " + describeChar(codePoint);
                        state = State.ERROR_STATE;
                    }
                    break;

                // ---------- Words ----------
                case IN_WORD:
                    if (isWordPart(c)) {
                        advance();
                    } else {
                        // The complete word has been read, so classify it now.
                        result = classifyWord(source.substring(startPos, pos));
                        state = State.DONE;
                    }
                    break;

                // ---------- Numbers ----------
                case IN_INTEGER:
                    if (isDigit(c)) {
                        advance();
                    } else if (c == '.' && isDigit(peek(1))) {
                        advance();              // lookahead: digit follows, so this is a decimal point
                        state = State.DECIMAL_POINT;
                    } else if (isWordPart(c)) {
                        consumeWordRest();
                        errorMsg = "Malformed number: unexpected characters after the digits";
                        state = State.ERROR_STATE;
                    } else {
                        result = TokenType.INTEGER_LITERAL;
                        state = State.DONE;
                    }
                    break;

                case DECIMAL_POINT:
                    if (isDigit(c)) {
                        advance();
                        state = State.IN_DECIMAL;
                    } else {
                        errorMsg = "Malformed decimal literal: digit expected after '.'";
                        state = State.ERROR_STATE;
                    }
                    break;

                case IN_DECIMAL:
                    if (isDigit(c)) {
                        advance();
                    } else if (c == 'f') {
                        advance();              // optional 'f' suffix, as in 2.0f
                        if (isWordPart(peek(0))) {
                            consumeWordRest();
                            errorMsg = "Malformed decimal literal: unexpected characters after 'f'";
                            state = State.ERROR_STATE;
                        } else {
                            result = TokenType.DECIMAL_LITERAL;
                            state = State.DONE;
                        }
                    } else if (isWordPart(c)) {
                        consumeWordRest();
                        errorMsg = "Malformed decimal literal: unexpected characters after the digits";
                        state = State.ERROR_STATE;
                    } else {
                        result = TokenType.DECIMAL_LITERAL;
                        state = State.DONE;
                    }
                    break;

                // ---------- String literals ----------
                case IN_STRING:
                    if (c == -1 || isNewline(c)) {
                        errorMsg = "Unterminated string literal: closing '\"' is missing";
                        state = State.ERROR_STATE;
                    } else if (c == '"') {
                        advance();
                        if (errorMsg != null) {
                            state = State.ERROR_STATE;   // an invalid escape was found earlier
                        } else {
                            result = TokenType.STRING_LITERAL;
                            state = State.DONE;
                        }
                    } else if (c == '\\') {
                        advance();
                        state = State.STRING_ESCAPE;
                    } else {
                        advance();
                    }
                    break;

                case STRING_ESCAPE:
                    if (c == -1 || isNewline(c)) {
                        errorMsg = "Unterminated string literal: closing '\"' is missing";
                        state = State.ERROR_STATE;
                    } else {
                        if (!VALID_ESCAPES.contains((char) c) && errorMsg == null) {
                            errorMsg = "Invalid escape sequence '\\" + (char) c + "' in string literal";
                        }
                        advance();
                        state = State.IN_STRING;
                    }
                    break;

                // ---------- Character literals ----------
                case CHAR_OPEN:
                    if (c == -1 || isNewline(c)) {
                        errorMsg = "Unterminated character literal: closing ''' is missing";
                        state = State.ERROR_STATE;
                    } else if (c == '\'') {
                        advance();
                        errorMsg = "Empty character literal";
                        state = State.ERROR_STATE;
                    } else if (c == '\\') {
                        advance();
                        state = State.CHAR_ESCAPE;
                    } else {
                        advance();
                        state = State.CHAR_BODY;
                    }
                    break;

                case CHAR_ESCAPE:
                    if (c == -1 || isNewline(c)) {
                        errorMsg = "Unterminated character literal: closing ''' is missing";
                        state = State.ERROR_STATE;
                    } else {
                        if (!VALID_ESCAPES.contains((char) c)) {
                            errorMsg = "Invalid escape sequence '\\" + (char) c + "' in character literal";
                        }
                        advance();
                        state = State.CHAR_BODY;
                    }
                    break;

                case CHAR_BODY:
                    if (c == -1 || isNewline(c)) {
                        errorMsg = "Unterminated character literal: closing ''' is missing";
                        state = State.ERROR_STATE;
                    } else if (c == '\'') {
                        advance();
                        if (errorMsg != null) {
                            state = State.ERROR_STATE;
                        } else {
                            result = TokenType.CHARACTER_LITERAL;
                            state = State.DONE;
                        }
                    } else {
                        // More than one character before the closing quote.
                        if (errorMsg == null) {
                            errorMsg = "Malformed character literal: it must contain exactly one character";
                        }
                        advance();
                    }
                    break;

                // ---------- Comments ----------
                case HASH:
                    if (c == '#') {
                        advance();                       // "##" starts a multi-line comment
                        state = State.IN_MULTI_LINE_COMMENT;
                    } else {
                        state = State.IN_SINGLE_LINE_COMMENT;   // do not consume c
                    }
                    break;

                case IN_SINGLE_LINE_COMMENT:
                    if (c == -1 || isNewline(c)) {
                        result = TokenType.SINGLE_LINE_COMMENT;  // the newline itself is skipped later
                        state = State.DONE;
                    } else {
                        advance();
                    }
                    break;

                case IN_MULTI_LINE_COMMENT:
                    if (c == -1) {
                        errorMsg = "Unterminated multi-line comment: closing '##' is missing";
                        state = State.ERROR_STATE;
                    } else if (c == '#') {
                        advance();
                        state = State.MULTI_LINE_COMMENT_HASH;
                    } else {
                        advance();                       // advance() keeps line numbers correct
                    }
                    break;

                case MULTI_LINE_COMMENT_HASH:
                    if (c == -1) {
                        errorMsg = "Unterminated multi-line comment: closing '##' is missing";
                        state = State.ERROR_STATE;
                    } else if (c == '#') {
                        advance();
                        result = TokenType.MULTI_LINE_COMMENT;
                        state = State.DONE;
                    } else {
                        state = State.IN_MULTI_LINE_COMMENT;    // a single '#' does not close it
                    }
                    break;

                // ---------- Operators (longest match) ----------
                case PLUS:
                    if (c == '+') {
                        advance();
                        result = TokenType.INCREMENT_OPERATOR;
                    } else if (c == '=') {
                        advance();
                        result = TokenType.ADDITION_ASSIGNMENT_OPERATOR;
                    } else {
                        result = TokenType.ADDITION_OPERATOR;
                    }
                    state = State.DONE;
                    break;

                case MINUS:
                    if (c == '-') {
                        advance();
                        result = TokenType.DECREMENT_OPERATOR;
                    } else if (c == '=') {
                        advance();
                        result = TokenType.SUBTRACTION_ASSIGNMENT_OPERATOR;
                    } else {
                        result = TokenType.SUBTRACTION_OPERATOR;
                    }
                    state = State.DONE;
                    break;

                case STAR:
                    if (c == '*') {
                        advance();
                        result = TokenType.EXPONENT_OPERATOR;
                    } else if (c == '=') {
                        advance();
                        result = TokenType.MULTIPLICATION_ASSIGNMENT_OPERATOR;
                    } else {
                        result = TokenType.MULTIPLICATION_OPERATOR;
                    }
                    state = State.DONE;
                    break;

                case SLASH:
                    if (c == '=') {
                        advance();
                        result = TokenType.DIVISION_ASSIGNMENT_OPERATOR;
                    } else {
                        result = TokenType.DIVISION_OPERATOR;
                    }
                    state = State.DONE;
                    break;

                case PERCENT:
                    if (c == '=') {
                        advance();
                        result = TokenType.MODULO_ASSIGNMENT_OPERATOR;
                    } else {
                        result = TokenType.MODULO_OPERATOR;
                    }
                    state = State.DONE;
                    break;

                case EQUAL:
                    if (c == '=') {
                        advance();
                        result = TokenType.EQUALITY_OPERATOR;
                    } else {
                        result = TokenType.ASSIGNMENT_OPERATOR;
                    }
                    state = State.DONE;
                    break;

                case BANG:
                    if (c == '=') {
                        advance();
                        result = TokenType.NOT_EQUAL_OPERATOR;
                    } else {
                        result = TokenType.NOT_OPERATOR;
                    }
                    state = State.DONE;
                    break;

                case LESS:
                    if (c == '=') {
                        advance();
                        result = TokenType.LESS_THAN_EQUAL_OPERATOR;
                    } else {
                        result = TokenType.LESS_THAN_OPERATOR;
                    }
                    state = State.DONE;
                    break;

                case GREATER:
                    if (c == '=') {
                        advance();
                        result = TokenType.MORE_THAN_EQUAL_OPERATOR;
                    } else {
                        result = TokenType.MORE_THAN_OPERATOR;
                    }
                    state = State.DONE;
                    break;

                case AMPERSAND:
                    if (c == '&') {
                        advance();
                        result = TokenType.AND_OPERATOR;
                        state = State.DONE;
                    } else {
                        errorMsg = "Invalid character '&': GamiPhi only has the '&&' operator";
                        state = State.ERROR_STATE;
                    }
                    break;

                case PIPE:
                    if (c == '|') {
                        advance();
                        result = TokenType.OR_OPERATOR;
                        state = State.DONE;
                    } else {
                        errorMsg = "Invalid character '|': GamiPhi only has the '||' operator";
                        state = State.ERROR_STATE;
                    }
                    break;

                case TILDE:
                    if (c == '=') {
                        advance();
                        result = TokenType.LIVE_ASSIGNMENT_OPERATOR;
                        state = State.DONE;
                    } else {
                        errorMsg = "Invalid character '~': GamiPhi only has the '~=' operator";
                        state = State.ERROR_STATE;
                    }
                    break;

                default:
                    errorMsg = "Internal lexer error: unexpected state " + state;
                    state = State.ERROR_STATE;
                    break;
            }
        }

        // Safety net: every lexeme must consume at least one character.
        if (pos == startPos) {
            advance();
        }

        String lexeme = source.substring(startPos, pos);

        if (state == State.ERROR_STATE) {
            Token errorToken = new Token(lexeme, TokenType.ERROR, startLine, startColumn);
            intermediateTokens.add(errorToken);
            errorMessages.put(errorToken, errorMsg);
        } else {
            intermediateTokens.add(new Token(lexeme, result, startLine, startColumn));
        }
    }

    // =====================================================================
    // PASS 2 - Validation and finalization
    // =====================================================================

    /**
     * Validates the intermediate tokens and returns the final token list.
     * This method never reads the original source text.
     */
    public List<Token> runPass2(List<Token> intermediate) {
        errors.clear();
        List<Token> result = new ArrayList<>();

        for (Token token : intermediate) {
            if (token.getType() == TokenType.EOF) {
                continue;                       // EOF is added only once, below
            }
            result.add(finalizeToken(token));
        }

        result.add(new Token("", TokenType.EOF, eofLine, eofColumn));
        finalTokens = result;
        return getTokens();
    }

    /** Validates one token. Returns the same token, a corrected copy, or an ERROR token. */
    private Token finalizeToken(Token token) {
        TokenType type = token.getType();
        String lexeme = token.getLexeme();

        // ERROR tokens from Pass 1: keep them and record their description.
        if (type == TokenType.ERROR) {
            String description = errorMessages.get(token);
            if (description == null) {
                description = "Invalid or unrecognized lexeme";
            }
            errors.add(new LexicalError(lexeme, description, token.getLine(), token.getColumn()));
            return token;
        }

        // Words: keywords, reserved words, identifiers
        if (isWordType(type)) {
            if (!isValidWord(lexeme)) {
                return rejectToken(token, "Malformed word: invalid identifier characters");
            }
            TokenType expected = classifyWord(lexeme);          // keywords first, then identifier
            if (expected == TokenType.IDENTIFIER && lexeme.length() > MAX_IDENTIFIER_LENGTH) {
                return rejectToken(token, "Identifier exceeds the maximum length of "
                        + MAX_IDENTIFIER_LENGTH + " characters (found " + lexeme.length() + ")");
            }
            return (expected == type) ? token : token.withType(expected);
        }

        // Operators and delimiters: the lexeme must match the official table
        if (SYMBOLS.containsValue(type)) {
            TokenType expected = SYMBOLS.get(lexeme);
            if (expected == null) {
                return rejectToken(token, "Unknown operator or delimiter '" + lexeme + "'");
            }
            return (expected == type) ? token : token.withType(expected);
        }

        switch (type) {
            case INTEGER_LITERAL:
                return isValidInteger(lexeme) ? token : rejectToken(token, "Invalid integer literal");
            case DECIMAL_LITERAL:
                return lexeme.matches("[0-9]+\\.[0-9]+f?")
                        ? token : rejectToken(token, "Invalid decimal literal");
            case STRING_LITERAL:
                return (lexeme.length() >= 2 && lexeme.startsWith("\"") && lexeme.endsWith("\""))
                        ? token : rejectToken(token, "Invalid string literal");
            case CHARACTER_LITERAL:
                return (lexeme.length() >= 3 && lexeme.startsWith("'") && lexeme.endsWith("'"))
                        ? token : rejectToken(token, "Invalid character literal");
            case SINGLE_LINE_COMMENT:
                return (lexeme.startsWith("#") && !lexeme.startsWith("##"))
                        ? token : rejectToken(token, "Invalid single-line comment");
            case MULTI_LINE_COMMENT:
                return (lexeme.length() >= 4 && lexeme.startsWith("##") && lexeme.endsWith("##"))
                        ? token : rejectToken(token, "Invalid multi-line comment");
            default:
                return token;
        }
    }

    /** Converts a token that failed validation into an ERROR token and records the error. */
    private Token rejectToken(Token token, String description) {
        Token errorToken = new Token(token.getLexeme(), TokenType.ERROR,
                token.getLine(), token.getColumn());
        errors.add(new LexicalError(token.getLexeme(), description,
                token.getLine(), token.getColumn()));
        return errorToken;
    }

    // =====================================================================
    // Helper methods
    // =====================================================================

    /** Decides whether a complete word is a keyword, reserved word, or identifier. */
    private static TokenType classifyWord(String word) {
        TokenType type = KEYWORDS.get(word);
        if (type != null) {
            return type;
        }
        type = RESERVED_WORDS.get(word);
        if (type != null) {
            return type;
        }
        return TokenType.IDENTIFIER;
    }

    /** True for IDENTIFIER and for every keyword or reserved word type. */
    private static boolean isWordType(TokenType type) {
        return type == TokenType.IDENTIFIER
                || KEYWORDS.containsValue(type)
                || RESERVED_WORDS.containsValue(type);
    }

    /** A valid word starts with a letter or '_' and contains only letters, digits, '_'. */
    private static boolean isValidWord(String word) {
        if (word.isEmpty() || !isWordStart(word.charAt(0))) {
            return false;
        }
        for (int i = 1; i < word.length(); i++) {
            if (!isWordPart(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidInteger(String text) {
        if (text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Maps the first character of a multi-character operator to its starting state. */
    private static State operatorStartState(int c) {
        switch (c) {
            case '+': return State.PLUS;
            case '-': return State.MINUS;
            case '*': return State.STAR;
            case '/': return State.SLASH;
            case '%': return State.PERCENT;
            case '=': return State.EQUAL;
            case '!': return State.BANG;
            case '<': return State.LESS;
            case '>': return State.GREATER;
            case '&': return State.AMPERSAND;
            case '|': return State.PIPE;
            case '~': return State.TILDE;
            default:  return null;
        }
    }

    /** Maps a single-character delimiter to its token type, or null if c is not one. */
    private static TokenType delimiterType(int c) {
        switch (c) {
            case ';': return TokenType.STATEMENT_END_DELIMITER;
            case ',': return TokenType.COMMA_DELIMITER;
            case '.': return TokenType.ACCESS_OBJECT_DELIMITER;
            case '(': return TokenType.OPEN_PARENTHESIS_DELIMITER;
            case ')': return TokenType.CLOSE_PARENTHESIS_DELIMITER;
            case '[': return TokenType.OPEN_LIST_DELIMITER;
            case ']': return TokenType.CLOSE_LIST_DELIMITER;
            case '{': return TokenType.OPEN_BLOCK_DELIMITER;
            case '}': return TokenType.CLOSE_BLOCK_DELIMITER;
            default:  return null;
        }
    }

    /** Describes a bad character for an error message (printable or U+XXXX code). */
    private static String describeChar(int codePoint) {
        if (codePoint > 32 && codePoint < 127) {
            return "'" + (char) codePoint + "'";
        }
        return String.format("U+%04X", codePoint);
    }

    // ===== Character classification (ASCII only - an assumption) =====

    private static boolean isLetter(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isWordStart(int c) {
        return isLetter(c) || c == '_';
    }

    private static boolean isWordPart(int c) {
        return isLetter(c) || isDigit(c) || c == '_';
    }

    /** Space, tab, or a line break. These are skipped, never tokenized. */
    private static boolean isWhitespace(int c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private static boolean isNewline(int c) {
        return c == '\n' || c == '\r';
    }

    // ===== Reading the source =====

    /** Looks at the character 'offset' positions ahead without consuming it. Returns -1 past the end. */
    private int peek(int offset) {
        int index = pos + offset;
        return (index < source.length()) ? source.charAt(index) : -1;
    }

    /**
     * Consumes one character and updates the line and column counters.
     * A line break is "\n", "\r\n" (counted once, on the "\n"), or a lone "\r".
     */
    private void advance() {
        char ch = source.charAt(pos++);
        boolean loneCarriageReturn = ch == '\r'
                && (pos >= source.length() || source.charAt(pos) != '\n');
        if (ch == '\n' || loneCarriageReturn) {
            line++;
            column = 1;
        } else {
            column++;
        }
    }

    /** Consumes spaces, tabs and line breaks without producing any token. */
    private void skipWhitespace() {
        while (isWhitespace(peek(0))) {
            advance();
        }
    }

    /** Consumes the rest of a run of letters, digits and underscores. */
    private void consumeWordRest() {
        while (isWordPart(peek(0))) {
            advance();
        }
    }
}


