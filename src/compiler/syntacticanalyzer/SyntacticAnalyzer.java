package compiler.syntacticanalyzer;

import compiler.exceptions.LexicalException;
import compiler.exceptions.SyntacticException;
import compiler.exceptions.SyntacticExceptions;
import compiler.lexicalanalyzer.ILexicalAnalyzer;
import compiler.token.IToken;
import compiler.token.Token;
import utils.CustomHashSet;
import utils.CustomSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SyntacticAnalyzer {
    private final ILexicalAnalyzer lexicalAnalyzer;
    private IToken currentToken;
    private Queue<SyntacticException> exceptions;
    private boolean panicMode = false;

    private enum NonTerminal {
        CLASS_AND_INTERFACE_LIST2,
        CLASS_STATEMENT,
        INTERFACE_STATEMENT,
        MODIFIER,
        INTERFACE_MEMBER,
        FORMAL_ARGS,
        TYPE,
        PRIMITIVE_TYPE,
        BLOCK,
        SENTENCE,
        ASSIGNMENT_CALL_OR_LOCALVAR,
        LOCAL_VAR_WITH_VAR,
        LOCAL_VAR_WITH_PRIMITIVE_TYPE,
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
        FORMAL_ARGS_AND_OPTIONAL_BLOCK, FORMAL_ARG, OPERAND, REFERENCE, COMPOUND_EXPRESSION,
    }

    public SyntacticAnalyzer(ILexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        exceptions = new LinkedList<>();
    }

    public void start() throws LexicalException, SyntacticExceptions {
        retrieveNextToken();
        classAndInterfaceList();
        match(IToken.TokenType.EOF);
        if (!exceptions.isEmpty()) {
            throw new SyntacticExceptions(exceptions);
        }
    }

    private void classAndInterfaceList() throws LexicalException {
        optionalModifier();
        if (first(NonTerminal.CLASS_AND_INTERFACE_LIST2).contains(currentToken.getTokenType())) {
            classAndInterfaceList2();
        } else {
            //Empty production
        }
    }

    private void classAndInterfaceList2() throws LexicalException {
        if (first(NonTerminal.CLASS_STATEMENT).contains(currentToken.getTokenType())) {
            classStatement();
            classAndInterfaceList();
        } else if (first(NonTerminal.INTERFACE_STATEMENT).contains(currentToken.getTokenType())) {
            interfaceStatement();
            classAndInterfaceList();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "a class or interface")); //This exception should be impossible to reach
                recovery();
            }
        }
    }

    private void classStatement() throws LexicalException {
        match(IToken.TokenType.CLASS_WORD);
        match(IToken.TokenType.CLASSID);
        optionalGenerics();
        optionalInheritance();
        match(IToken.TokenType.OPENING_BRACE);
        memberList();
        match(IToken.TokenType.CLOSING_BRACE);
    }

    private void interfaceStatement() throws LexicalException {
        match(IToken.TokenType.INTERFACE_WORD);
        match(IToken.TokenType.CLASSID);
        optionalGenerics();
        optionalExtends();
        match(IToken.TokenType.OPENING_BRACE);
        interfaceMemberList();
        match(IToken.TokenType.CLOSING_BRACE);
    }

    private void modifier() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.ABSTRACT_WORD:
            case IToken.TokenType.FINAL_WORD:
            case IToken.TokenType.STATIC_WORD:
                retrieveNextToken();
                break;
            default:
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "a modifier"));
                    recovery();
                }
        }
    }

    private void optionalModifier() throws LexicalException {
        if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            modifier();
        } else {
            //Empty production
        }
    }

    private void optionalVisibility() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.PUBLIC_WORD:
            case IToken.TokenType.PRIVATE_WORD:
                retrieveNextToken();
                break;
            default:
                //Empty production
        }
    }

    private void optionalPrivate() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.PRIVATE_WORD)) {
            retrieveNextToken();
        } else {
            //Empty production
        }
    }

    private void optionalInheritance() throws LexicalException {
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

    private void optionalExtends() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.EXTENDS_WORD)) {
            retrieveNextToken();
            match(IToken.TokenType.CLASSID);
            optionalGenerics();
        } else {
            //Empty production
        }
    }

    private void optionalGenericsOrDiamond() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.LESS_THAN)) {
            retrieveNextToken();
            optionalClassId();
            match(IToken.TokenType.GREATER_THAN);
        } else {
            //Empty production
        }
    }

    private void optionalClassId() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.CLASSID))
            retrieveNextToken();
        else {
            //Empty production
        }
    }

    private void optionalGenerics() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.LESS_THAN)) {
            retrieveNextToken();
            match(IToken.TokenType.CLASSID);
            match(IToken.TokenType.GREATER_THAN);
        } else {
            //Empty production
        }
    }

    private void memberList() throws LexicalException {
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

    private void interfaceMemberList() throws LexicalException {
        optionalVisibility();
        if (first(NonTerminal.INTERFACE_MEMBER).contains(currentToken.getTokenType())) {
            interfaceMember();
            interfaceMemberList();
        } else {
            //Empty production
        }
    }

    private void member() throws LexicalException {
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
            retrieveNextToken();
            constructorOrMember();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an attribute, method or constructor declaration"));
                recovery();
            }
        }
    }

    private void interfaceMember() throws LexicalException {
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
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an attribute or method declaration"));
                recovery();
            }
        }
    }

    private void constructorOrMember() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.METVARID)) {
            retrieveNextToken();
            closingAttributeMethod();
        } else if (first(NonTerminal.FORMAL_ARGS).contains(currentToken.getTokenType())) {
            formalArgs();
            block();
        }
    }

    private void attributeOrMethod() throws LexicalException {
        if (first(NonTerminal.TYPE).contains(currentToken.getTokenType())) {
            type();
            match(IToken.TokenType.METVARID);
            closingAttributeMethod();
        } else if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            modifier();
            methodType();
            match(IToken.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        }
    }

    private void closingAttributeMethod() throws LexicalException {
        if (first(NonTerminal.FORMAL_ARGS_AND_OPTIONAL_BLOCK).contains(currentToken.getTokenType())) {
            formalArgsAndOptionalBlock();
        } else {
            optionalAssignment();
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void formalArgsAndOptionalBlock() throws LexicalException {
        formalArgs();
        optionalBlock();
    }

    private void optionalAssignment() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.EQUAL)) {
            retrieveNextToken();
            expression();
        } else {
            //Empty production
        }
    }

    private void methodType() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
        } else if (first(NonTerminal.TYPE).contains(currentToken.getTokenType())) {
            type();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "a method type"));
                recovery();
            }
        }
    }

    private void type() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.CLASSID)) {
            retrieveNextToken();
            optionalGenerics();
        } else if (first(NonTerminal.PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            primitiveType();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "a type"));
                recovery();
            }
        }
    }

    private void primitiveType() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.INT_WORD:
            case IToken.TokenType.BOOLEAN_WORD:
            case IToken.TokenType.CHAR_WORD:
                retrieveNextToken();
                break;
            default:
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "a primitive type"));
                    recovery();
                }
        }
    }

    private void formalArgs() throws LexicalException {
        match(IToken.TokenType.OPENING_PAREN);
        optionalFormalArgsList();
        match(IToken.TokenType.CLOSING_PAREN);
    }

    private void optionalFormalArgsList() throws LexicalException {
        if (first(NonTerminal.FORMAL_ARGS_LIST).contains(currentToken.getTokenType())) {
            formalArgsList();
        } else {
            //Empty production
        }
    }

    private void formalArgsList() throws LexicalException {
        formalArg();
        formalArgsList2();
    }

    private void formalArgsList2() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.COMMA)) {
            retrieveNextToken();
            formalArg();
            formalArgsList2();
        } else {
            //Empty production
        }
    }

    private void formalArg() throws LexicalException {
        type();
        match(IToken.TokenType.METVARID);
    }

    private void optionalBlock() throws LexicalException {
        if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            block();
        } else {
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void block() throws LexicalException {
        match(IToken.TokenType.OPENING_BRACE);
        sentenceList();
        match(IToken.TokenType.CLOSING_BRACE);
    }

    private void sentenceList() throws LexicalException {
        if (first(NonTerminal.SENTENCE).contains(currentToken.getTokenType())) {
            sentence();
            sentenceList();
        }
    }

    private void sentence() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.FOR_WORD)) {
            forSentence();
        } else if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            block();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.WHILE_WORD)) {
            whileSentence();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.IF_WORD)) {
            ifSentence();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.RETURN_WORD)) {
            returnSentence();
            match(IToken.TokenType.SEMICOLON);
        } else if (first(NonTerminal.ASSIGNMENT_CALL_OR_LOCALVAR).contains(currentToken.getTokenType())) {
            assignmentCallOrLocalVar();
            match(IToken.TokenType.SEMICOLON);
        } else {
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void assignmentCallOrLocalVar() throws LexicalException {
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
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an assignment, call or local variable declaration"));
                recovery();
            }
        }
    }

    private void staticMethodOrLocalVar() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.PERIOD)) {
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            actualArgs();
            reference2();
            compoundExpression2();
            expression2();
        } else {
            optionalGenerics();
            match(IToken.TokenType.METVARID);               //Maybe I want to check if the token is metvarid here and throw exception if it's not
            multipleDeclaration();
            optionalAssignment();
        }
    }

    private void expressionWOStaticMethodCall() throws LexicalException {
        compoundExpressionWOStaticMethodCall();
        expression2();
    }

    private void compoundExpressionWOStaticMethodCall() throws LexicalException {
        basicExpression();
        compoundExpression2();
    }

    private void localVarWithVar() throws LexicalException {
        match(IToken.TokenType.VAR_WORD);
        match((IToken.TokenType.METVARID));
        match(IToken.TokenType.EQUAL);
        expression();
    }

    private void localVarWithPrimitiveType() throws LexicalException {
        primitiveType();
        match(IToken.TokenType.METVARID);
        multipleDeclaration();
        optionalAssignment();
    }

    private void forSentence() throws LexicalException {
        match(IToken.TokenType.FOR_WORD);
        match(IToken.TokenType.OPENING_PAREN);
        forSentence2();
        forCondition();
        match(IToken.TokenType.CLOSING_PAREN);
        sentence();
    }

    private void forSentence2() throws LexicalException {
        if (first(NonTerminal.LOCAL_VAR_WITH_VAR).contains(currentToken.getTokenType())) {
            localVarWithVar();
        } else if (first(NonTerminal.PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            primitiveType();
            match(IToken.TokenType.METVARID);
            multipleDeclaration();
            optionalAssignment();
        } else if (first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
            expressionWOStaticMethodCall();
        } else if (currentToken.getTokenType().equals(IToken.TokenType.CLASSID)) {
            match(IToken.TokenType.CLASSID);
            staticMethodOrLocalVar();
        } else {
            //Empty production
        }
    }

    private void forCondition() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.SEMICOLON:
                match(IToken.TokenType.SEMICOLON);
                optionalCompoundExpression();
                match(IToken.TokenType.SEMICOLON);
                optionalExpression();
                break;
            case IToken.TokenType.COLON:
                retrieveNextToken();
                expression();
                break;
            default:
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, ": or ;"));
                    recovery();
                }
        }
    }

    private void multipleDeclaration() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.COMMA)) {
            retrieveNextToken();
            match(IToken.TokenType.METVARID);
            multipleDeclaration();
        } else {
            //Empty production
        }
    }

    private void returnSentence() throws LexicalException {
        match(IToken.TokenType.RETURN_WORD);
        optionalExpression();
    }

    private void optionalExpression() throws LexicalException {
        if (first(NonTerminal.EXPRESSION).contains(currentToken.getTokenType())) {
            expression();
        }
    }

    private void ifSentence() throws LexicalException {
        match(IToken.TokenType.IF_WORD);
        match(IToken.TokenType.OPENING_PAREN);
        expression();
        match(IToken.TokenType.CLOSING_PAREN);
        sentence();
        elseSentence();
    }

    private void elseSentence() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.ELSE_WORD)) {
            retrieveNextToken();
            sentence();
        } else {
            //Empty production
        }
    }

    private void whileSentence() throws LexicalException {
        match(IToken.TokenType.WHILE_WORD);
        match(IToken.TokenType.OPENING_PAREN);
        expression();
        match(IToken.TokenType.CLOSING_PAREN);
        sentence();
    }

    private void expression() throws LexicalException {
        compoundExpression();
        expression2();
    }

    private void expression2() throws LexicalException {
        if (first(NonTerminal.ASSIGNMENT_OPERATOR).contains(currentToken.getTokenType())) {
            assignmentOperator();
            compoundExpression();
            optionalTernaryOperator();
        } else {
            optionalTernaryOperator();
        }
    }

    private void optionalTernaryOperator() throws LexicalException {
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

    private void assignmentOperator() throws LexicalException {
        match(IToken.TokenType.EQUAL);
    }

    private void compoundExpression() throws LexicalException {
        if (first(NonTerminal.BASIC_EXPRESSION).contains(currentToken.getTokenType())) {
            basicExpression();
            compoundExpression2();
        } else if (first(NonTerminal.STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
            staticMethodCall();
            reference2();
            compoundExpression2();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an expression"));
                recovery();
            }
        }
    }

    private void optionalCompoundExpression() throws LexicalException {
        if (first(NonTerminal.COMPOUND_EXPRESSION).contains(currentToken.getTokenType())) {
            compoundExpression();
        } else {
            //Empty production
        }
    }

    private void compoundExpression2() throws LexicalException {
        if (first(NonTerminal.BINARY_OPERATOR).contains(currentToken.getTokenType())) {
            binaryOperator();
            compoundExpression();
        } else {
            //Empty production
        }
    }

    private void binaryOperator() throws LexicalException {
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
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "binary operator"));
                    recovery();
                }
        }
    }

    private void basicExpression() throws LexicalException {
        if (first(NonTerminal.UNARY_OPERATOR).contains(currentToken.getTokenType())) {
            unaryOperator();
            operand();
        } else if (first(NonTerminal.OPERAND).contains(currentToken.getTokenType())) {
            operand();
        }
    }

    private void unaryOperator() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.PLUS:
            case IToken.TokenType.PLUS1:
            case IToken.TokenType.MINUS:
            case IToken.TokenType.MINUS1:
            case IToken.TokenType.EXCLAMATION_POINT:
                retrieveNextToken();
                break;
            default:
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "unary operator"));
                    recovery();
                }
        }
    }

    private void operand() throws LexicalException {
        if (first(NonTerminal.PRIMITIVE).contains(currentToken.getTokenType())) {
            primitive();
        } else if (first(NonTerminal.REFERENCE).contains(currentToken.getTokenType())) {
            reference();
        }
    }

    private void primitive() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.INTLITERAL:
            case IToken.TokenType.CHARLITERAL:
            case IToken.TokenType.TRUE_WORD:
            case IToken.TokenType.FALSE_WORD:
            case IToken.TokenType.NULL_WORD:
                retrieveNextToken();
                break;
            default:
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "a primitive"));
                    recovery();
                }
        }
    }

    private void reference() throws LexicalException {
        primary();
        reference2();
    }

    private void reference2() throws LexicalException {
        if (first(NonTerminal.CHAINED_VAR_METHOD).contains(currentToken.getTokenType())) {
            chainedVarMethod();
            reference2();
        } else {
            //empty production
        }
    }

    private void primary() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.THIS_WORD:
            case IToken.TokenType.STRINGLITERAL:
                retrieveNextToken();
                break;
            case IToken.TokenType.OPENING_PAREN:
                parenthesizedExpression();
                break;
            case IToken.TokenType.METVARID:
                varAccessMethodCall();
                break;
            default:
                if (first(NonTerminal.CONSTRUCTOR_CALL).contains(currentToken.getTokenType())) {
                    constructorCall();
                } else if (first(NonTerminal.STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
                    staticMethodCall();
                } else {
                    if (!panicMode) {
                        exceptions.add(new SyntacticException(currentToken, "a primary"));
                        recovery();
                    }
                }
        }
    }

    private void varAccessMethodCall() throws LexicalException {
        match(IToken.TokenType.METVARID);
        optionalActualArgs();
    }

    private void constructorCall() throws LexicalException {
        match(IToken.TokenType.NEW_WORD);
        match(IToken.TokenType.CLASSID);
        optionalGenericsOrDiamond();
        actualArgs();
    }

    private void parenthesizedExpression() throws LexicalException {
        match(IToken.TokenType.OPENING_PAREN);
        expression();
        match(IToken.TokenType.CLOSING_PAREN);
    }

    private void staticMethodCall() throws LexicalException {
        match(IToken.TokenType.CLASSID);
        match(IToken.TokenType.PERIOD);
        match(IToken.TokenType.METVARID);
        actualArgs();
    }

    private void actualArgs() throws LexicalException {
        match(IToken.TokenType.OPENING_PAREN);
        optionalExpressionList();
        match(IToken.TokenType.CLOSING_PAREN);
    }

    private void optionalExpressionList() throws LexicalException {
        if (first(NonTerminal.EXPRESSION_LIST).contains(currentToken.getTokenType())) {
            expressionList();
        } else {
            //Empty production
        }
    }

    private void expressionList() throws LexicalException {
        expression();
        expressionList2();
    }

    private void expressionList2() throws LexicalException {
        if (currentToken.getTokenType().equals(IToken.TokenType.COMMA)) {
            retrieveNextToken();
            expression();
            expressionList2();
        } else {
            //Empty produciton
        }
    }

    private void chainedVarMethod() throws LexicalException {
        match(IToken.TokenType.PERIOD);
        match(IToken.TokenType.METVARID);
        optionalActualArgs();
    }

    private void optionalActualArgs() throws LexicalException {
        if (first(NonTerminal.ACTUAL_ARGS).contains(currentToken.getTokenType())) {
            actualArgs();
        } else {
            //Empty production
        }
    }

    private void match(IToken.TokenType expectedTokenType) throws LexicalException {
        if (panicMode && (expectedTokenType.equals(IToken.TokenType.CLOSING_BRACE) || expectedTokenType.equals(IToken.TokenType.SEMICOLON) || expectedTokenType.equals(IToken.TokenType.OPENING_BRACE))) {
            panicMode = false;
            while (!(expectedTokenType.equals(currentToken.getTokenType())) && !currentToken.getTokenType().equals(IToken.TokenType.EOF)) {
                retrieveNextToken();
            }
        }
        if (expectedTokenType.equals(currentToken.getTokenType())) {
            retrieveNextToken();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, expectedTokenType.toString()));
                recovery();
            }
        }
    }

    private void retrieveNextToken() throws LexicalException {
        currentToken = lexicalAnalyzer.nextToken();
    }

    private void recovery() throws LexicalException {
        panicMode = true;
    }

    private CustomSet<IToken.TokenType> first(NonTerminal nonTerminal) {
        return switch (nonTerminal) {
            case CLASS_AND_INTERFACE_LIST2 ->
                    first(NonTerminal.CLASS_STATEMENT).appendAll(first(NonTerminal.INTERFACE_STATEMENT));
            case CLASS_STATEMENT -> new CustomHashSet<>(List.of(IToken.TokenType.CLASS_WORD));
            case INTERFACE_STATEMENT -> new CustomHashSet<>(List.of(IToken.TokenType.INTERFACE_WORD));
            case MODIFIER ->
                    new CustomHashSet<>(List.of(IToken.TokenType.ABSTRACT_WORD, IToken.TokenType.FINAL_WORD, IToken.TokenType.STATIC_WORD));
            case ATTRIBUTE_OR_METHOD, INTERFACE_MEMBER ->
                    first(NonTerminal.MODIFIER).appendAll(first(NonTerminal.TYPE)).append(IToken.TokenType.VOID_WORD);
            case PRIMITIVE_TYPE ->
                    new CustomHashSet<>(List.of(IToken.TokenType.BOOLEAN_WORD, IToken.TokenType.CHAR_WORD, IToken.TokenType.INT_WORD));
            case TYPE -> first(NonTerminal.PRIMITIVE_TYPE).append(IToken.TokenType.CLASSID);
            case FORMAL_ARGS, ACTUAL_ARGS -> new CustomHashSet<>(List.of(IToken.TokenType.OPENING_PAREN));
            case FORMAL_ARGS_AND_OPTIONAL_BLOCK -> first(NonTerminal.FORMAL_ARGS);
            case FORMAL_ARGS_LIST -> first(NonTerminal.FORMAL_ARG);
            case FORMAL_ARG -> first(NonTerminal.TYPE);
            case BLOCK -> new CustomHashSet<>(List.of(IToken.TokenType.OPENING_BRACE));
            case SENTENCE ->
                    first(NonTerminal.ASSIGNMENT_CALL_OR_LOCALVAR).appendAll(new CustomHashSet<>(List.of(IToken.TokenType.RETURN_WORD, IToken.TokenType.IF_WORD, IToken.TokenType.WHILE_WORD, IToken.TokenType.FOR_WORD, IToken.TokenType.SEMICOLON, IToken.TokenType.OPENING_BRACE)));
            case ASSIGNMENT_CALL_OR_LOCALVAR ->
                    first(NonTerminal.PRIMITIVE_TYPE).appendAll(first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).append(IToken.TokenType.VAR_WORD).append(IToken.TokenType.CLASSID));
            case EXPRESSION_WO_STATIC_METHOD_CALL, BASIC_EXPRESSION ->
                    first(NonTerminal.UNARY_OPERATOR).appendAll(first(NonTerminal.OPERAND));
            case UNARY_OPERATOR ->
                    new CustomHashSet<>(List.of(IToken.TokenType.PLUS, IToken.TokenType.MINUS, IToken.TokenType.PLUS1, IToken.TokenType.MINUS1, IToken.TokenType.EXCLAMATION_POINT));
            case OPERAND -> first(NonTerminal.PRIMITIVE).appendAll(first(NonTerminal.REFERENCE));
            case PRIMITIVE ->
                    new CustomHashSet<>(List.of(IToken.TokenType.INTLITERAL, IToken.TokenType.CHARLITERAL, IToken.TokenType.TRUE_WORD, IToken.TokenType.FALSE_WORD, IToken.TokenType.NULL_WORD));
            case REFERENCE ->
                    new CustomHashSet<>(List.of(IToken.TokenType.METVARID, IToken.TokenType.THIS_WORD, IToken.TokenType.STRINGLITERAL, IToken.TokenType.NEW_WORD, IToken.TokenType.OPENING_PAREN));
            case LOCAL_VAR_WITH_VAR -> new CustomHashSet<>(List.of(IToken.TokenType.VAR_WORD));
            case LOCAL_VAR_WITH_PRIMITIVE_TYPE -> first(NonTerminal.PRIMITIVE_TYPE);
            case EXPRESSION, COMPOUND_EXPRESSION ->
                    first(NonTerminal.BASIC_EXPRESSION).appendAll(first(NonTerminal.STATIC_METHOD_CALL));
            case STATIC_METHOD_CALL -> new CustomHashSet<>(List.of(IToken.TokenType.CLASSID));
            case ASSIGNMENT_OPERATOR -> new CustomHashSet<>(List.of(IToken.TokenType.EQUAL));
            case BINARY_OPERATOR ->
                    new CustomHashSet<>(List.of(IToken.TokenType.OR, IToken.TokenType.AND, IToken.TokenType.EQUALS_COMPARISON, IToken.TokenType.DIFERENT, IToken.TokenType.LESS_THAN, IToken.TokenType.GREATER_THAN, IToken.TokenType.EQUAL_LESS_THAN, IToken.TokenType.EQUAL_GREATER_THAN, IToken.TokenType.PLUS, IToken.TokenType.MINUS, IToken.TokenType.MULTIPLY, IToken.TokenType.SLASH, IToken.TokenType.PERCENT));
            case CHAINED_VAR_METHOD -> new CustomHashSet<>(List.of(IToken.TokenType.PERIOD));
            case CONSTRUCTOR_CALL -> new CustomHashSet<>(List.of(IToken.TokenType.NEW_WORD));
            case EXPRESSION_LIST -> first(NonTerminal.EXPRESSION);
        };
    }
}
