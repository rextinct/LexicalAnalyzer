package lexer;

/**
 * TokenType.java
 * Lists every token type used by the GamiPhi lexical analyzer.
 * The names follow the official GamiPhi token list exactly.
 *
 * Whitespace (space, tab, newline) is NOT a token type. The lexer reads
 * whitespace so it can count lines and columns, but it skips it.
 */
public enum TokenType {

    // ===== A. Literals =====
    INTEGER_LITERAL,
    DECIMAL_LITERAL,
    STRING_LITERAL,
    CHARACTER_LITERAL,

    // ===== B. Operators =====
    // Assignment operators
    ASSIGNMENT_OPERATOR,                 // =
    ADDITION_ASSIGNMENT_OPERATOR,        // +=
    SUBTRACTION_ASSIGNMENT_OPERATOR,     // -=
    MULTIPLICATION_ASSIGNMENT_OPERATOR,  // *=
    DIVISION_ASSIGNMENT_OPERATOR,        // /=
    MODULO_ASSIGNMENT_OPERATOR,          // %=
    LIVE_ASSIGNMENT_OPERATOR,            // ~=

    // Arithmetic operators
    ADDITION_OPERATOR,                   // +
    SUBTRACTION_OPERATOR,                // -
    MULTIPLICATION_OPERATOR,             // *
    DIVISION_OPERATOR,                   // /
    MODULO_OPERATOR,                     // %
    EXPONENT_OPERATOR,                   // **

    // Increment and decrement operators
    INCREMENT_OPERATOR,                  // ++
    DECREMENT_OPERATOR,                  // --

    // Boolean operators
    NOT_OPERATOR,                        // !
    AND_OPERATOR,                        // &&
    OR_OPERATOR,                         // ||

    // Relational operators
    EQUALITY_OPERATOR,                   // ==
    NOT_EQUAL_OPERATOR,                  // !=
    LESS_THAN_OPERATOR,                  // <
    MORE_THAN_OPERATOR,                  // >
    LESS_THAN_EQUAL_OPERATOR,            // <=
    MORE_THAN_EQUAL_OPERATOR,            // >=

    // ===== C. Delimiters =====
    STATEMENT_END_DELIMITER,             // ;
    COMMA_DELIMITER,                     // ,
    ACCESS_OBJECT_DELIMITER,             // .
    OPEN_PARENTHESIS_DELIMITER,          // (
    CLOSE_PARENTHESIS_DELIMITER,         // )
    OPEN_LIST_DELIMITER,                 // [
    CLOSE_LIST_DELIMITER,                // ]
    OPEN_BLOCK_DELIMITER,                // {
    CLOSE_BLOCK_DELIMITER,               // }

    // ===== D. Keywords (case-sensitive) =====
    MARK_KEYWORD,                        // Mark
    BOOL_DATA_TYPE,                      // Bool
    CHAR_DATA_TYPE,                      // Char
    DOUBLE_DATA_TYPE,                    // Double
    FLOAT_DATA_TYPE,                     // Float
    INT_DATA_TYPE,                       // Int
    STRING_DATA_TYPE,                    // String
    VOID_DATA_TYPE,                      // Void
    BOOLEAN_TRUE_KEYWORD,                // True
    BOOLEAN_FALSE_KEYWORD,               // False
    FOR_LOOP_KEYWORD,                    // For
    DO_LOOP_KEYWORD,                     // Do
    WHILE_LOOP_KEYWORD,                  // While
    BREAK_KEYWORD,                       // Break
    CONTINUE_KEYWORD,                    // Continue
    IF_KEYWORD,                          // If
    ELSE_KEYWORD,                        // Else
    PRINT_KEYWORD,                       // Print
    WRITE_KEYWORD,                       // Write
    RETURN_STATEMENT_KEYWORD,            // Return

    // ===== E. Reserved words =====
    ENTITY_RESERVE_WORD,                 // ENTITY
    COMP_RESERVE_WORD,                   // COMP
    BEHAVIOR_RESERVE_WORD,               // BEHAVIOR
    USE_RESERVE_WORD,                    // USE
    ADD_RESERVE_WORD,                    // ADD
    SPAWN_RESERVE_WORD,                  // SPAWN
    DESTROY_RESERVE_WORD,                // DESTROY
    RANK_RESERVE_WORD,                   // RANK
    SPOTLIGHT_RESERVE_WORD,              // SPOTLIGHT
    SCENE_RESERVE_WORD,                  // SCENE
    LOAD_RESERVE_WORD,                   // LOAD
    KEEP_RESERVE_WORD,                   // KEEP
    OVER_RESERVE_WORD,                   // OVER
    BACK_RESERVE_WORD,                   // BACK
    WHEN_RESERVE_WORD,                   // WHEN
    TOUCH_RESERVE_WORD,                  // TOUCH
    INPUT_RESERVE_WORD,                  // INPUT
    TRIGGER_RESERVE_WORD,                // TRIGGER
    TRIGGER_ON_RESERVE_WORD,             // TRIGGER_ON (one single token)
    AFTER_RESERVE_WORD,                  // AFTER
    SECONDS_RESERVE_WORD,                // SECONDS
    WAIT_RESERVE_WORD,                   // WAIT
    PAUSE_RESERVE_WORD,                  // PAUSE
    RESUME_RESERVE_WORD,                 // RESUME
    PARALLEL_RESERVE_WORD,               // PARALLEL
    SPAN_RESERVE_WORD,                   // SPAN

    // ===== F. Identifier =====
    IDENTIFIER,                          // user-defined name, max 16 characters

    // ===== G. Comments =====
    SINGLE_LINE_COMMENT,                 // # ...
    MULTI_LINE_COMMENT,                  // ## ... ##

    // ===== H. Control tokens =====
    EOF,                                 // end of source code
    ERROR                                // invalid or unrecognized lexeme
}