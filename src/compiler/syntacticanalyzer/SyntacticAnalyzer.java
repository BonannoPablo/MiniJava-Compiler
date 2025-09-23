package compiler.syntacticanalyzer;

import compiler.exceptions.LexicalException;
import compiler.exceptions.SyntacticException;
import compiler.lexicalanalyzer.ILexicalAnalyzer;
import compiler.token.IToken;
import utils.customSet;

public class SyntacticAnalyzer {
    private final ILexicalAnalyzer lexicalAnalyzer;
    private IToken currentToken;

    private enum NonTerminal {
        CLASS_AND_INTERFACE_LIST2,
        CLASS_STATEMENT,
        INTERFACE_STATEMENT,
        MODIFIER,
        OPTIONAL_EXTENDS,
        OPTIONAL_GENERICS_OR_DIAMOND,
        MEMBER_LIST,
        INTERFACE_MEMBER,
        FORMAL_ARGS,
        TYPE,
        CONSTRUCTOR,
        PRIMITIVE_TYPE,
        BLOCK,
        SENTENCE,
        WHILE_SENTENCE,
        IF_SENTENCE,
        RETURN_SENTENCE,
        ASSIGNMENT_CALL_OR_LOCALVAR,
        LOCAL_VAR_WITH_VAR,
        LOCAL_VAR_WITH_PRIMITIVE_TYPE,
        FOR_SENTENCE,
        EXPRESSION_WO_STATIC_METHOD_CALL,
        EXPRESSION, ASSIGNMENT_OPERATOR,
        BASIC_EXPRESSION, STATIC_METHOD_CALL,
        BINARY_OPERATOR, UNARY_OPERATOR,
        PRIMITIVE,
        CHAINED_VAR_METHOD,
        FORMAL_ARGS_LIST,
        EXPRESSION_LIST,
        CONSTRUCTOR_CALL,
        ACTUAL_ARGS,
        ATTRIBUTE_OR_METHOD,
        FORMAL_ARGS_AND_OPTIONAL_BLOCK,
    }

    public SyntacticAnalyzer(ILexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        LoadFirstMap();
    }

    public void start() throws LexicalException, SyntacticException {
        retrieveNextToken();
        classAndInterfaceList();
        match(IToken.TokenType.EOF);
    }

    private void classAndInterfaceList() throws LexicalException, SyntacticException {
        optionalModifier();
        if (first(NonTerminal.CLASS_AND_INTERFACE_LIST2).contains(currentToken.getTokenType())) {
            classAndInterfaceList2();
        } else {
            //Empty production
        }
    }

    private void classAndInterfaceList2() throws LexicalException, SyntacticException {
        if (first(NonTerminal.CLASS_STATEMENT).contains(currentToken.getTokenType())) {
            classStatement();
        } else if (first(NonTerminal.INTERFACE_STATEMENT).contains(currentToken.getTokenType())) {
            interfaceStatement();
        } else {
            throw new SyntacticException(currentToken, "a class or interface");
        }
    }

    private void classStatement() throws LexicalException, SyntacticException {
        match(IToken.TokenType.CLASS_WORD);
        match(IToken.TokenType.CLASSID);
        optionalGenerics();
        optionalInheritance();
        match(IToken.TokenType.OPENING_BRACE);
        memberList();
        match(IToken.TokenType.CLOSING_BRACE);
    }

    private void interfaceStatement() throws LexicalException, SyntacticException {
        match(IToken.TokenType.INTERFACE_WORD);
        match(IToken.TokenType.CLASSID);
        optionalGenerics();
        optionalExtends();
        match(IToken.TokenType.OPENING_BRACE);
        interfaceMemberList();
        match(IToken.TokenType.CLOSING_BRACE);
    }

    private void modifier() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.ABSTRACT_WORD:
            case IToken.TokenType.FINAL_WORD:
            case IToken.TokenType.STATIC_WORD:
                retrieveNextToken();
                break;
            default:
                throw new SyntacticException(currentToken, "a modifier");
        }
    }

    private void optionalModifier() throws LexicalException, SyntacticException {
        if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            modifier();
        } else {
            //Empty production
        }
    }

    private void optionalVisibility() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.PUBLIC_WORD:
            case IToken.TokenType.PRIVATE_WORD:
                retrieveNextToken();
                break;
            default:
                //Empty production
        }
    }

    private void optionalPrivate() throws LexicalException, SyntacticException{
        if(currentToken.getTokenType().equals(IToken.TokenType.PRIVATE_WORD)){
            retrieveNextToken();
        } else {
            //Empty production
        }
    }

    private void optionalInheritance() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.EXTENDS_WORD:
            case IToken.TokenType.IMPLEMENTS_WORD:
                retrieveNextToken();
                match(IToken.TokenType.CLASSID);
                optionalGenerics();
                break;
            default:
                //Empty production
        }
    }

    private void optionalExtends() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.EXTENDS_WORD)) {
            retrieveNextToken();
            match(IToken.TokenType.CLASSID);
            optionalGenerics();
        } else {
            //Empty production
        }
    }

    private void optionalGenericsOrDiamond() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.LESS_THAN)) {
            retrieveNextToken();
            optionalClassId();
            match(IToken.TokenType.GREATER_THAN);
        } else {
            //Empty production
        }
    }

    private void optionalClassId() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.CLASSID))
            retrieveNextToken();
        else {
            //Empty production
        }
    }

    private void optionalGenerics() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.LESS_THAN)) {
            retrieveNextToken();
            match(IToken.TokenType.CLASSID);
            match(IToken.TokenType.GREATER_THAN);
        } else {
            //Empty production
        }
    }

    private void memberList() throws LexicalException, SyntacticException {
        if (first(NonTerminal.ATTRIBUTE_OR_METHOD).contains(currentToken.getTokenType()) || currentToken.getTokenType().equals(IToken.TokenType.PRIVATE_WORD)) {
            optionalPrivate();
            attributeOrMethod();
            memberList();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.PUBLIC_WORD)) {
            retrieveNextToken();
            member();
            memberList();
        } else {
            //Empty production
        }
    }

    private void interfaceMemberList() throws LexicalException, SyntacticException {
        optionalVisibility();
        if (first(NonTerminal.INTERFACE_MEMBER).contains(currentToken.getTokenType())) {
            interfaceMember();
            interfaceMemberList();
        } else {
            //Empty production
        }
    }

    private void member() throws LexicalException, SyntacticException {
        if (first(NonTerminal.PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            primitiveType();
            match(IToken.TokenType.METVARID);
            closingAttributeMethod();
        } else if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            modifier();
            methodType();
            match(IToken.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.CLASSID)) {
            constructorOrMember();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        } else {
            throw new SyntacticException(currentToken, "an attribute, method or constructor declaration");
        }
    }

    private void interfaceMember() throws LexicalException, SyntacticException {
        if (first(NonTerminal.TYPE).contains(currentToken.getTokenType())) {
            type();
            match(IToken.TokenType.METVARID);
            match(IToken.TokenType.EQUAL);
            expression();
        } else if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            modifier();
            methodType();
            match(IToken.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        } else {
            throw new SyntacticException(currentToken, "an attribute or method declaration");
        }
    }

    private void constructorOrMember() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.METVARID)) {
            retrieveNextToken();
            closingAttributeMethod();
        } else if(first(NonTerminal.FORMAL_ARGS).contains(currentToken.getTokenType())){
            formalArgs();
            block();
        }
    }

    private void attributeOrMethod() throws LexicalException, SyntacticException {
        if(first(NonTerminal.TYPE).contains(currentToken.getTokenType())){
            type();
            match(IToken.TokenType.METVARID);
            closingAttributeMethod();
        } else if(first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())){
            modifier();
            methodType();
            match(IToken.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        } else if(currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)){
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        }
    }

    private void closingAttributeMethod() throws LexicalException, SyntacticException {
        if (first(NonTerminal.FORMAL_ARGS_AND_OPTIONAL_BLOCK).contains(currentToken.getTokenType())) {
            formalArgsAndOptionalBlock();
        } else {
            optionalAssignment();
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void formalArgsAndOptionalBlock() throws LexicalException, SyntacticException {
        formalArgs();
        optionalBlock();
    }

    private void optionalAssignment() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.EQUAL)) {
            retrieveNextToken();
            expression();
        } else {
            //Empty production
        }
    }

    private void initializedAttribute() throws LexicalException, SyntacticException {  //I believe I never use this one
        type();
        match(IToken.TokenType.METVARID);
        match(IToken.TokenType.EQUAL);
        expression();
        match(IToken.TokenType.SEMICOLON);
    }


    private void method() throws LexicalException, SyntacticException {
        match(IToken.TokenType.METVARID);
        formalArgs();
        optionalBlock();
    }

    private void constructor() throws LexicalException, SyntacticException {
        match(IToken.TokenType.CLASSID);
        formalArgs();
        block();
    }

    private void methodType() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
        } else if (first(NonTerminal.TYPE).contains(currentToken.getTokenType())) {
            type();
        } else {
            throw new SyntacticException(currentToken, "a method type");
        }
    }

    private void type() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.CLASSID)) {
            retrieveNextToken();
            optionalGenerics();
        } else if (first(NonTerminal.PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            primitiveType();
        } else {
            throw new SyntacticException(currentToken, "a type");
        }
    }

    private void primitiveType() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.INT_WORD:
            case IToken.TokenType.BOOLEAN_WORD:
            case IToken.TokenType.CHAR_WORD:
                retrieveNextToken();
                break;
            default:
                throw new SyntacticException(currentToken, "a primitive type");
        }
    }

    private void formalArgs() throws LexicalException, SyntacticException {
        match(IToken.TokenType.OPENING_PAREN);
        optionalFormalArgsList();
        match(IToken.TokenType.CLOSING_PAREN);
    }

    private void optionalFormalArgsList() throws LexicalException, SyntacticException {
        if (first(NonTerminal.FORMAL_ARGS_LIST).contains(currentToken.getTokenType())) {
            formalArgsList();
        } else {
            //Empty production
        }
    }

    private void formalArgsList() throws LexicalException, SyntacticException {
        formalArg();
        formalArgsList2();
    }

    private void formalArgsList2() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.COMMA)) {
            retrieveNextToken();
            formalArg();
            formalArgsList2();
        } else {
            //Empty production
        }
    }

    private void formalArg() throws LexicalException, SyntacticException {
        type();
        match(IToken.TokenType.METVARID);
    }

    private void optionalBlock() throws LexicalException, SyntacticException {
        if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            block();
        } else {
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void block() throws LexicalException, SyntacticException {
        match(IToken.TokenType.OPENING_BRACE);
        sentenceList();
        match(IToken.TokenType.CLOSING_BRACE);
    }

    private void sentenceList() throws LexicalException, SyntacticException {
        if (first(NonTerminal.SENTENCE).contains(currentToken.getTokenType())) {
            sentence();
            sentenceList();
        }
    }

    private void sentence() throws LexicalException, SyntacticException {
        if (first(NonTerminal.FOR_SENTENCE).contains(currentToken.getTokenType())) {
            forSentence();
        } else if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            block();
        } else if (first(NonTerminal.WHILE_SENTENCE).contains(currentToken.getTokenType())) {
            whileSentence();
        } else if (first(NonTerminal.IF_SENTENCE).contains(currentToken.getTokenType())) {
            ifSentence();
        } else if (first(NonTerminal.RETURN_SENTENCE).contains(currentToken.getTokenType())) {
            returnSentence();
            match(IToken.TokenType.SEMICOLON);
        } else if (first(NonTerminal.ASSIGNMENT_CALL_OR_LOCALVAR).contains(currentToken.getTokenType())) {
            assignmentCallOrLocalVar();
            match(IToken.TokenType.SEMICOLON);
        } else {
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void assignmentCallOrLocalVar() throws LexicalException, SyntacticException {
        if (first(NonTerminal.LOCAL_VAR_WITH_VAR).contains(currentToken.getTokenType())) {
            localVarWithVar();
        } else if (first(NonTerminal.LOCAL_VAR_WITH_PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            localVarWithPrimitiveType();
        } else if (first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
            expressionWOStaticMethodCall();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.CLASSID)) {
            retrieveNextToken();
            staticMethodOrLocalVar();
        } else {
            throw new SyntacticException(currentToken, "an assignment, call or local variable declaration");
        }
    }

    private void staticMethodOrLocalVar() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.PERIOD)) {
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            actualArgs();
            reference2();
            binaryOperator();
            basicExpression();
            compoundExpression2();
        } else {
            optionalGenerics();
            match(IToken.TokenType.METVARID);               //Maybe I want to check if the token is metvarid here and throw exception if it's not
            multipleDeclaration();
            optionalAssignment();
        }
    }

    private void expressionWOStaticMethodCall() throws LexicalException, SyntacticException {
        compoundExpressionWOStaticMethodCall();
        expression2();
    }

    private void compoundExpressionWOStaticMethodCall() throws LexicalException, SyntacticException {
        basicExpression();
        binaryOperator();
        basicExpression();
        compoundExpression2();
    }

    private void localVarWithVar() throws LexicalException, SyntacticException {
        match(IToken.TokenType.VAR_WORD);
        match((IToken.TokenType.METVARID));
        match(IToken.TokenType.EQUAL);
        compoundExpression();
    }

    private void localVarWithPrimitiveType() throws LexicalException, SyntacticException {
        primitiveType();
        match(IToken.TokenType.METVARID);
        multipleDeclaration();
        optionalAssignment();
    }

    private void forSentence() throws LexicalException, SyntacticException {
        match(IToken.TokenType.FOR_WORD);
        match(IToken.TokenType.OPENING_PAREN);
        forSentence2();
        forCondition();
        match(IToken.TokenType.CLOSING_PAREN);
    }

    private void forSentence2() throws LexicalException, SyntacticException {
        if (first(NonTerminal.LOCAL_VAR_WITH_VAR).contains(currentToken.getTokenType())) {
            localVarWithVar();
        } else if (first(NonTerminal.PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            primitiveType();
            match(IToken.TokenType.METVARID);
            multipleDeclaration();
            match(IToken.TokenType.EQUAL);
            expression();
        } else if (first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
            expressionWOStaticMethodCall();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.CLASSID)) {
            match(IToken.TokenType.CLASSID);
            staticMethodOrInitializedLocalVar();
        } else {
            throw new SyntacticException(currentToken, "an expression or local variable declaration");
        }
    }

    private void forCondition() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.SEMICOLON:
                retrieveNextToken();
                compoundExpression();
                match(IToken.TokenType.SEMICOLON);
                expression();
                break;
            case IToken.TokenType.COLON:
                retrieveNextToken();
                expression();
                break;
            default:
                throw new SyntacticException(currentToken, ": or ;");
        }
    }

    private void staticMethodOrInitializedLocalVar() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.PERIOD)) {
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            actualArgs();
            reference2();
            binaryOperator();
            basicExpression();
            compoundExpression2();
        } else {
            optionalGenerics();
            match(IToken.TokenType.METVARID);
            multipleDeclaration();
            match(IToken.TokenType.EQUAL);
            expression();
        }
    }

    private void multipleDeclaration() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.COMMA)) {
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            multipleDeclaration();
        } else {
            //Empty production
        }
    }

    private void localVar() throws LexicalException, SyntacticException {
        match(IToken.TokenType.VAR_WORD);
        match((IToken.TokenType.METVARID));
        match(IToken.TokenType.EQUAL);
        compoundExpression();
    }

    private void returnSentence() throws LexicalException, SyntacticException {
        match(IToken.TokenType.RETURN_WORD);
        optionalExpression();
    }

    private void optionalExpression() throws LexicalException, SyntacticException {
        if (first(NonTerminal.EXPRESSION).contains(currentToken.getTokenType())) {
            expression();
        }
    }

    private void ifSentence() throws LexicalException, SyntacticException {
        match(IToken.TokenType.IF_WORD);
        match(IToken.TokenType.OPENING_PAREN);
        expression();
        match(IToken.TokenType.CLOSING_PAREN);
        sentence();
        elseSentence();
    }

    private void elseSentence() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.ELSE_WORD)) {
            retrieveNextToken();
            sentence();
        } else {
            //Empty production
        }
    }

    private void whileSentence() throws LexicalException, SyntacticException {
        match(IToken.TokenType.WHILE_WORD);
        match(IToken.TokenType.OPENING_PAREN);
        expression();
        match(IToken.TokenType.CLOSING_PAREN);
        sentence();
    }

    private void expression() throws LexicalException, SyntacticException {
        compoundExpression();
        expression2();
    }

    private void expression2() throws LexicalException, SyntacticException {
        if (first(NonTerminal.ASSIGNMENT_OPERATOR).contains(currentToken.getTokenType())) {
            assignmentOperator();
            compoundExpression();
            optionalTernaryOperator();
        } else {
            optionalTernaryOperator();
        }
    }

    private void optionalTernaryOperator() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.QUESTION_MARK)) {
            retrieveNextToken();
            expression();
            match(IToken.TokenType.COLON);
            compoundExpression();
            optionalTernaryOperator();
        } else {
            //Empty produciton
        }
    }

    private void assignmentOperator() throws LexicalException, SyntacticException {
        match(IToken.TokenType.EQUAL);
    }

    private void compoundExpression() throws LexicalException, SyntacticException {
        if (first(NonTerminal.BASIC_EXPRESSION).contains(currentToken.getTokenType())) {
            basicExpression();
            binaryOperator();
            basicExpression();
            compoundExpression2();
        } else if (first(NonTerminal.STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
            staticMethodCall();
            reference2();
            binaryOperator();
            basicExpression();
            compoundExpression2();
        }
    }

    private void compoundExpression2() throws LexicalException, SyntacticException {
        if (first(NonTerminal.BINARY_OPERATOR).contains(currentToken.getTokenType())) {
            binaryOperator();
            basicExpression();
            compoundExpression2();
        } else {
            //Empty production
        }
    }

    private void binaryOperator() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.OR:
            case IToken.TokenType.AND:
            case IToken.TokenType.EQUALS_COMPARISON:
            case IToken.TokenType.DIFERENT:
            case IToken.TokenType.LESS_THAN:
            case IToken.TokenType.GREATER_THAN:
            case IToken.TokenType.EQUAL_LESS_THAN:
            case IToken.TokenType.EQUAL_GREATER_THAN:
            case IToken.TokenType.PLUS:
            case IToken.TokenType.MINUS:
            case IToken.TokenType.MULTIPLY:
            case IToken.TokenType.SLASH:
            case IToken.TokenType.PERCENT:
                retrieveNextToken();
                break;
            default:
                throw new SyntacticException(currentToken, "binary operator");
        }
    }

    private void basicExpression() throws LexicalException, SyntacticException {
        if (first(NonTerminal.UNARY_OPERATOR).contains(currentToken.getTokenType())) {
            unaryOperator();
            operand();
        } else {
            operand();
        }
    }

    private void unaryOperator() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.PLUS:
            case IToken.TokenType.PLUS1:
            case IToken.TokenType.MINUS:
            case IToken.TokenType.MINUS1:
            case IToken.TokenType.EXCLAMATION_POINT:
                retrieveNextToken();
                break;
            default:
                throw new SyntacticException(currentToken, "unary operator");
        }
    }

    private void operand() throws LexicalException, SyntacticException {
        if (first(NonTerminal.PRIMITIVE).contains(currentToken.getTokenType())) {
            primitive();
        } else {
            reference();
        }
    }

    private void primitive() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.INTLITERAL:
            case IToken.TokenType.CHARLITERAL:
            case IToken.TokenType.TRUE_WORD:
            case IToken.TokenType.FALSE_WORD:
            case IToken.TokenType.NULL_WORD:
                retrieveNextToken();
                break;
            default:
                throw new SyntacticException(currentToken, "a primitive");
        }
    }

    private void reference() throws LexicalException, SyntacticException {
        primary();
        reference2();
    }

    private void reference2() throws LexicalException, SyntacticException {
        if (first(NonTerminal.CHAINED_VAR_METHOD).contains(currentToken.getTokenType())) {
            chainedVarMethod();
            reference2();
        } else {
            //empty production
        }
    }

    private void primary() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.THIS_WORD:
            case IToken.TokenType.STRINGLITERAL:
                retrieveNextToken();
                break;
            case IToken.TokenType.OPENING_PAREN:
                parenthesizedExpression();
            case IToken.TokenType.METVARID:
                varAccessMethodCall();
                break;
            default:
                if (first(NonTerminal.CONSTRUCTOR_CALL).contains(currentToken.getTokenType())) {
                    constructorCall();
                } else if (first(NonTerminal.STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
                    staticMethodCall();
                } else {
                    throw new SyntacticException(currentToken, "a primary");
                }
        }
    }

    private void varAccessMethodCall() throws LexicalException, SyntacticException {
        match(IToken.TokenType.METVARID);
        optionalActualArgs();
    }

    private void constructorCall() throws LexicalException, SyntacticException {
        match(IToken.TokenType.NEW_WORD);
        match(IToken.TokenType.CLASSID);
        optionalGenericsOrDiamond();
        actualArgs();
    }

    private void parenthesizedExpression() throws LexicalException, SyntacticException {
        match(IToken.TokenType.OPENING_PAREN);
        expression();
        match(IToken.TokenType.CLOSING_PAREN);
    }

    private void staticMethodCall() throws LexicalException, SyntacticException {
        match(IToken.TokenType.CLASSID);
        match(IToken.TokenType.PERIOD);
        match(IToken.TokenType.METVARID);
        actualArgs();
    }

    private void actualArgs() throws LexicalException, SyntacticException {
        match(IToken.TokenType.OPENING_PAREN);
        optionalExpressionList();
        match(IToken.TokenType.CLOSING_PAREN);
    }

    private void optionalExpressionList() throws LexicalException, SyntacticException {
        if (first(NonTerminal.EXPRESSION_LIST).contains(currentToken.getTokenType())) {
            expressionList();
        } else {
            //Empty production
        }
    }

    private void expressionList() throws LexicalException, SyntacticException {
        expression();
        expressionList2();
    }

    private void expressionList2() throws LexicalException, SyntacticException {
        if (currentToken.getTokenType().equals(IToken.TokenType.COMMA)) {
            retrieveNextToken();
            expression();
            expressionList2();
        } else {
            //Empty produciton
        }
    }

    private void chainedVarMethod() throws LexicalException, SyntacticException {
        match(IToken.TokenType.PERIOD);
        match(IToken.TokenType.METVARID);
        optionalActualArgs();
    }

    private void optionalActualArgs() throws LexicalException, SyntacticException {
        if (first(NonTerminal.ACTUAL_ARGS).contains(currentToken.getTokenType())) {
            actualArgs();
        } else {
            //Empty production
        }
    }

    private void match(IToken.TokenType expectedTokenType) throws LexicalException, SyntacticException {
        if (expectedTokenType.equals(currentToken.getTokenType())) {
            retrieveNextToken();
        } else {
            throw new SyntacticException(currentToken, expectedTokenType.toString());
        }
    }

    private void retrieveNextToken() throws LexicalException {
        currentToken = lexicalAnalyzer.nextToken();
    }

    private void LoadFirstMap() {
    }

    private customSet<IToken.TokenType> first(NonTerminal nonTerminal) {
        return switch (nonTerminal) {
        }
    }



   /* private Set<IToken.TokenType> first(String productionName) {
        Set<IToken.TokenType> set = null;
        //set = first(productionName);
        switch (productionName) {
            case "classList":
                return first("classStatement");
            case "classStatement":
                set = first("optionalModifier");
                set.add((IToken.TokenType.CLASS_WORD));
                return set;
            case "modifier":
                return new HashSet<>(Arrays.asList(IToken.TokenType.ABSTRACT_WORD, IToken.TokenType.FINAL_WORD, IToken.TokenType.STATIC_WORD));
            case "optionalModifier":
                return first("modifier");
            case "optionalInheritance":
                return new HashSet<>(List.of(IToken.TokenType.EXTENDS_WORD));
            case "memberList":
                return first("member");
            case "member":
                set = first("type");
                set.addAll(first("modifier"));
                set.addAll(first("constructor"));
                set.add(IToken.TokenType.VOID_WORD);
                return set;
            case "attributeMethod":
                set = first("formalArgs");
                set.add(IToken.TokenType.SEMICOLON);
                return set;
            case "method":
                return new HashSet<>(List.of(IToken.TokenType.METVARID));
            case "constructor":
                return new HashSet<>(List.of(IToken.TokenType.PUBLIC_WORD));
            case "methodType":
                set = first("type");
                set.add(IToken.TokenType.VOID_WORD);
                return set;
            case "type":
                set = first("primitiveType");
                set.add(IToken.TokenType.CLASSID);
                return set;
            case "primitiveType":
                return new HashSet<>(Arrays.asList(IToken.TokenType.INT_WORD, IToken.TokenType.BOOLEAN_WORD, IToken.TokenType.CHAR_WORD));
            case "formalArgs", "parenthesizedExpression", "actualArgs":
                return new HashSet<>(List.of(IToken.TokenType.OPENING_PAREN));
            case "optionalFormalArgsList":
                return first("formalArgsList");
            case "formalArgsList":
                return first("formalArg");
            case "formalArgsList2", "expressionList2":
                return new HashSet<>(List.of(IToken.TokenType.COMMA));
            case "formalArg":
                return first("type");
            case "optionalBlock":
                set = first("block");
                set.add(IToken.TokenType.SEMICOLON);
                return set;
            case "block":
                return new HashSet<>(List.of(IToken.TokenType.OPENING_BRACE));
            case "sentenceList":
                return first("sentence");
            case "sentence":
                set = first("assignmentCall");
                set.addAll(first("ifSentence"));
                set.addAll(first("whileSentence"));
                set.addAll(first("returnSentence"));
                set.addAll(first("localVar"));
                set.addAll(first("block"));
                set.add(IToken.TokenType.SEMICOLON);
                return set;
            case "assignmentCall", "expressionList", "optionalExpression":
                return first("expression");
            case "localVar":
                return new HashSet<>(List.of(IToken.TokenType.VAR_WORD));
            case "returnSentence":
                return new HashSet<>(List.of(IToken.TokenType.RETURN_WORD));
            case "ifSentence":
                return new HashSet<>(List.of(IToken.TokenType.IF_WORD));
            case "elseSentence":
                return new HashSet<>(List.of(IToken.TokenType.ELSE_WORD));
            case "whileSentence":
                return new HashSet<>(List.of(IToken.TokenType.WHILE_WORD));
            case "expression":
                return first("compoundExpression");
            case "assignmentExpression":
                return first("assignmentOperator");
            case "assignmentOperator":
                return new HashSet<>(List.of(IToken.TokenType.EQUAL));
            case "compoundExpression":
                return first("basicExpression");
            case "compoundExpression2":
                return first("binaryOperator");
            case "binaryOperator":
                return new HashSet<>(Arrays.asList(IToken.TokenType.OR, IToken.TokenType.AND, IToken.TokenType.EQUALS_COMPARISON, IToken.TokenType.DIFERENT, IToken.TokenType.LESS_THAN,
                        IToken.TokenType.GREATER_THAN, IToken.TokenType.EQUAL_LESS_THAN, IToken.TokenType.EQUAL_GREATER_THAN, IToken.TokenType.PLUS, IToken.TokenType.MINUS,
                        IToken.TokenType.MULTIPLY, IToken.TokenType.SLASH, IToken.TokenType.PERCENT));
            case "basicExpression":
                set = first("unaryOperator");
                set.addAll(first("operand"));
                return set;
            case "unaryOperator":
                return new HashSet<>(Arrays.asList(IToken.TokenType.PLUS, IToken.TokenType.PLUS1, IToken.TokenType.MINUS, IToken.TokenType.MINUS1, IToken.TokenType.EXCLAMATION_POINT));
            case "operand":
                set = first("primitive");
                set.addAll(first("reference"));
                return set;
            case "primitive":
                return new HashSet<>(Arrays.asList(IToken.TokenType.INTLITERAL, IToken.TokenType.CHARLITERAL, IToken.TokenType.TRUE_WORD, IToken.TokenType.FALSE_WORD, IToken.TokenType.NULL_WORD));
            case "reference":
                return first("primary");
            case "reference2":
                return first("chainedVarMethod");
            case "primary":
                set = new HashSet<>(Arrays.asList(IToken.TokenType.THIS_WORD, IToken.TokenType.STRINGLITERAL, IToken.TokenType.METVARID));
                set.addAll(first("staticMethodCall"));
                set.addAll(first("constructorCall"));
                set.addAll(first("parenthesizedExpression"));
                return set;
            case "varAccessMethodCall", "optionalActualArgs":
                return first("actualArgs");
            case "constructorCall":
                return new HashSet<>(List.of(IToken.TokenType.NEW_WORD));
            case "staticMethodCall":
                return new HashSet<>(List.of(IToken.TokenType.CLASSID));
            case "chainedVarMethod":
                return new HashSet<>(List.of(IToken.TokenType.PERIOD));
            default:
                System.out.println("Error: production not found");
                return new HashSet<>();
        }
    }*/


}
