package lexer;

public enum State {

    // -- Starting and finishing
    START,                    // Beginning of a new lexeme.
    DONE,                     // A token was completed and can be stored. The lexer then returns to START.
    ERROR_STATE,              // A malformed lexeme was found. The lexer stores an ERROR token, then returns to START.

    // -- Words
    IN_WORD,                  // The complete word is scanned first. Whether it is a keyword, reserved word, or identifier is decided afterward.

    // -- Numbers
    IN_INTEGER,               // Inside a run of digits.
    DECIMAL_POINT,            // A '.' was read right after digits (such as "1."). A digit must follow for a valid decimal.
    IN_DECIMAL,               // Inside the digits after the decimal point, such as 1.5.

    // -- String literals
    IN_STRING,                // Inside a double-quoted string, after the opening quote.
    STRING_ESCAPE,            // A backslash was read inside a string. The next character is an escape character.

    // -- Character literals
    CHAR_OPEN,                // The opening single quote was read. The character content comes next.
    CHAR_ESCAPE,              // A backslash was read inside a character literal.
    CHAR_BODY,                // Exactly one character (or one escape) was read. A closing single quote must come next.

    // -- Comments
    HASH,                     // One '#' was read. A second '#' makes it a multi-line comment. Anything else makes it a single-line comment.
    IN_SINGLE_LINE_COMMENT,   // Inside a single-line comment. It runs until a newline or the end of the file.
    IN_MULTI_LINE_COMMENT,    // Inside a multi-line comment, after the opening "##".
    MULTI_LINE_COMMENT_HASH,  // A '#' was read inside a multi-line comment. A second '#' closes the comment.

    // -- Operators that can be longer than one character
    PLUS,                     // '+' was read. Could be +, ++, or +=.
    MINUS,                    // '-' was read. Could be -, --, or -=.
    STAR,                     // '*' was read. Could be *, **, or *=.
    SLASH,                    // '/' was read. Could be / or /=.
    PERCENT,                  // '%' was read. Could be % or %=.
    EQUAL,                    // '=' was read. Could be = or ==.
    BANG,                     // '!' was read. Could be ! or !=.
    LESS,                     // '<' was read. Could be < or <=.
    GREATER,                  // '>' was read. Could be > or >=.
    AMPERSAND,                // '&' was read. Only && is valid in GamiPhi. A lone & is an error.
    PIPE,                     // '|' was read. Only || is valid in GamiPhi. A lone | is an error.
    TILDE;                    // '~' was read. Only ~= is valid in GamiPhi. A lone ~ is an error.

    public boolean isFinal() {
        return this == DONE || this == ERROR_STATE;
    }
}

