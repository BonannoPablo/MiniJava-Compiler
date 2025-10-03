package compiler.token;

public interface Token {
    public enum TokenType {
        METVARID("method or variable identifier"),
        CLASSID("class identifier"),
        INTLITERAL("int literal"),
        CHARLITERAL("char literal"),
        STRINGLITERAL("string literal"),
        CLASS_WORD("class"),
        INTERFACE_WORD("interface"),
        EXTENDS_WORD("extends"),
        IMPLEMENTS_WORD("implements"),
        PUBLIC_WORD("public"),
        PRIVATE_WORD("private"),
        STATIC_WORD("static"),
        VOID_WORD("void"),
        BOOLEAN_WORD("boolean"),
        CHAR_WORD("char"),
        INT_WORD("int"),
        ABSTRACT_WORD("abstract"),
        FINAL_WORD("final"),
        IF_WORD("if"),
        ELSE_WORD("else"),
        WHILE_WORD("while"),
        RETURN_WORD("return"),
        VAR_WORD("var"),
        THIS_WORD("this"),
        NEW_WORD("new"),
        NULL_WORD("null"),
        TRUE_WORD("true"),
        FALSE_WORD("false"),
        OPENING_PAREN("("),
        CLOSING_PAREN(")"),
        OPENING_BRACE("{"),
        CLOSING_BRACE("}"),
        SEMICOLON(";"),
        COMMA(","),
        PERIOD("."),
        COLON(":"),
        LESS_THAN("<"),
        EQUAL_LESS_THAN("<="),
        GREATER_THAN(">"),
        EQUAL_GREATER_THAN(">="),
        EXCLAMATION_POINT("!"),
        DIFERENT("!="),
        EQUAL("="),
        EQUALS_COMPARISON("=="),
        AND("&&"),
        OR("||"),
        PERCENT("%"),
        PLUS("+"),
        PLUS1("++"),
        MINUS("-"),
        MINUS1("--"),
        MULTIPLY("*"),
        SLASH("/"),
        EOF("end of file"),
        FOR_WORD("for"),
        QUESTION_MARK("?"),
        RECOVERY("");


        private final String representation;

        TokenType(String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return representation;
        }
    }


    TokenType getTokenType();
    String getLexeme();
    int getLineNumber();
}
