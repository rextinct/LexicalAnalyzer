package lexer;

/**
 * State.java
 * Defines the states used by Pass 1 of the GamiPhi lexer.
 *
 * Pass 1 works like a DFA (deterministic finite automaton). It always sits
 * in exactly one State, reads one character, and moves to the next State.
 * Each State means "this is what I have seen so far".
 *
 * Whitespace (space, tab, newline) has no state. The lexer skips it before
 * a lexeme begins, so START never sees it. Single-character delimiters
 * (; , . ( ) [ ] { }) need no state of their own either. START can finish
 * them immediately.
 *
 * Example of longest match for "+=":
 *   START --'+'--> PLUS --'='--> DONE (ADDITION_ASSIGNMENT_OPERATOR)
 * Example for "+" followed by a letter:
 *   START --'+'--> PLUS --other--> DONE (ADDITION_OPERATOR)
 */
public enum State {

    // ===== Starting and finishing =====

    /** Beginning of a new lexeme. The lexer decides where to go from here. */
    START,

    /** A token was completed and can be stored. The lexer then returns to START. */
    DONE,

    /** A malformed lexeme was found. The lexer stores an ERROR token, then returns to START. */
    ERROR_STATE,

    // ===== Words =====

    /**
     * Inside a word (letters, digits, underscore).
     * The complete word is scanned first. Whether it is a keyword,
     * reserved word, or identifier is decided afterward.
     */
    IN_WORD,

    // ===== Numbers =====

    /** Inside a run of digits, such as 25. */
    IN_INTEGER,

    /** A '.' was read right after digits (such as "1."). A digit must follow for a valid decimal. */
    DECIMAL_POINT,

    /** Inside the digits after the decimal point, such as 1.5. */
    IN_DECIMAL,

    // ===== String literals =====

    /** Inside a double-quoted string, after the opening quote. */
    IN_STRING,

    /** A backslash was read inside a string. The next character is an escape character. */
    STRING_ESCAPE,

    // ===== Character literals =====

    /** The opening single quote was read. The character content comes next. */
    CHAR_OPEN,

    /** A backslash was read inside a character literal. */
    CHAR_ESCAPE,

    /** Exactly one character (or one escape) was read. A closing single quote must come next. */
    CHAR_BODY,

    // ===== Comments =====

    /** One '#' was read. A second '#' makes it a multi-line comment. Anything else makes it a single-line comment. */
    HASH,

    /** Inside a single-line comment. It runs until a newline or the end of the file. */
    IN_SINGLE_LINE_COMMENT,

    /** Inside a multi-line comment, after the opening "##". */
    IN_MULTI_LINE_COMMENT,

    /** A '#' was read inside a multi-line comment. A second '#' closes the comment. */
    MULTI_LINE_COMMENT_HASH,

    // ===== Operators that can be longer than one character =====

    /** '+' was read. Could be +, ++, or +=. */
    PLUS,

    /** '-' was read. Could be -, --, or -=. */
    MINUS,

    /** '*' was read. Could be *, **, or *=. */
    STAR,

    /** '/' was read. Could be / or /=. */
    SLASH,

    /** '%' was read. Could be % or %=. */
    PERCENT,

    /** '=' was read. Could be = or ==. */
    EQUAL,

    /** '!' was read. Could be ! or !=. */
    BANG,

    /** '<' was read. Could be < or <=. */
    LESS,

    /** '>' was read. Could be > or >=. */
    GREATER,

    /** '&' was read. Only && is valid in GamiPhi. A lone & is an error. */
    AMPERSAND,

    /** '|' was read. Only || is valid in GamiPhi. A lone | is an error. */
    PIPE,

    /** '~' was read. Only ~= is valid in GamiPhi. A lone ~ is an error. */
    TILDE;

    /**
     * True for the two states that end a lexeme (DONE and ERROR_STATE).
     * The lexer can use this to decide when to stop scanning the current lexeme.
     */
    public boolean isFinal() {
        return this == DONE || this == ERROR_STATE;
    }
}