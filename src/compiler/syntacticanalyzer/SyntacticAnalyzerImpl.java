package compiler.syntacticanalyzer;

import compiler.exceptions.LexicalException;
import compiler.exceptions.SemanticException;
import compiler.exceptions.SyntacticException;
import compiler.exceptions.SyntacticExceptions;
import compiler.lexicalanalyzer.LexicalAnalyzer;
import compiler.symboltable.*;
import compiler.token.Token;
import utils.CustomHashSet;
import utils.CustomSet;

import java.util.*;

public class SyntacticAnalyzerImpl implements SyntacticAnalyzer {
    public static SymbolTable symbolTable;

    private final LexicalAnalyzer lexicalAnalyzer;
    private Token currentToken;
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
        FORMAL_ARGS_AND_OPTIONAL_BLOCK,
        FORMAL_ARG,
        OPERAND,
        REFERENCE,
        COMPOUND_EXPRESSION,
    }

    public SyntacticAnalyzerImpl(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        exceptions = new LinkedList<>();
    }

    public void start() throws LexicalException, SyntacticExceptions, SemanticException {
        symbolTable = new SymbolTable();

        retrieveNextToken();
        classAndInterfaceList();
        match(Token.TokenType.EOF);
        if (!exceptions.isEmpty()) {
            throw new SyntacticExceptions(exceptions);
        }
    }

    private void classAndInterfaceList() throws LexicalException, SemanticException {
        Token modifier = optionalModifier();
        if (first(NonTerminal.CLASS_AND_INTERFACE_LIST2).contains(currentToken.getTokenType())) {
            classAndInterfaceList2(modifier);
        } else {
            //Empty production
        }
    }

    private void classAndInterfaceList2(Token modifier) throws LexicalException, SemanticException {
        if (first(NonTerminal.CLASS_STATEMENT).contains(currentToken.getTokenType())) {
            classStatement(modifier);
            classAndInterfaceList();
        } else if (first(NonTerminal.INTERFACE_STATEMENT).contains(currentToken.getTokenType())) {
            interfaceStatement(modifier);
            classAndInterfaceList();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "a class or interface")); //This exception should be impossible to reach
                recovery();
            }
        }
    }

    private void classStatement(Token modifier) throws LexicalException, SemanticException {
        match(Token.TokenType.CLASS_WORD);
        Token classId = currentToken;
        match(Token.TokenType.CLASSID);

        ClassEntry classEntry = new ClassEntry(classId);
        classEntry.setModifier(modifier);
        symbolTable.putClass(classEntry);
        classEntry.addGenericType(optionalGenerics());
        optionalInheritance();

        match(Token.TokenType.OPENING_BRACE);
        memberList();
        match(Token.TokenType.CLOSING_BRACE);

    }

    private void interfaceStatement(Token modifier) throws LexicalException, SemanticException {
        match(Token.TokenType.INTERFACE_WORD);
        Token classId = currentToken;
        match(Token.TokenType.CLASSID);
        InterfaceEntry interfaceEntry = new InterfaceEntry(classId);
        symbolTable.putInterface(interfaceEntry);
        interfaceEntry.setModifier(modifier);
        interfaceEntry.addGenericType(optionalGenerics());
        optionalExtends();
        match(Token.TokenType.OPENING_BRACE);
        interfaceMemberList();
        match(Token.TokenType.CLOSING_BRACE);
    }

    private Token modifier() throws LexicalException {
        var modifier = currentToken;
        return switch (currentToken.getTokenType()) {
            case Token.TokenType.ABSTRACT_WORD, Token.TokenType.FINAL_WORD, Token.TokenType.STATIC_WORD -> {
                retrieveNextToken();
                yield modifier;
            }
            default -> {
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "a modifier"));
                    recovery();
                }
                yield null;
            }
        };
    }

    private Token optionalModifier() throws LexicalException {
        if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            return modifier();
        } else {
            return null;
            //Empty production
        }
    }

    private Token optionalVisibility() throws LexicalException {
        var visibility = currentToken;
        return switch (currentToken.getTokenType()) {
            case Token.TokenType.PUBLIC_WORD, Token.TokenType.PRIVATE_WORD -> {
                retrieveNextToken();
                yield visibility;
            }
            default -> null;
            //Empty production
        };
    }

    private void optionalPrivate() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.PRIVATE_WORD)) {
            retrieveNextToken();
        } else {
            //Empty production
        }
    }

    private void optionalInheritance() throws LexicalException, SemanticException {
        switch (currentToken.getTokenType()) {
            case Token.TokenType.EXTENDS_WORD:
                retrieveNextToken();
                var parentClass = currentToken;
                match(Token.TokenType.CLASSID);
                symbolTable.getCurrentClass().setParent(parentClass);
                optionalGenerics();
                break;
            case Token.TokenType.IMPLEMENTS_WORD:
                retrieveNextToken();
                var interfaceClass = currentToken;
                match(Token.TokenType.CLASSID);
                symbolTable.getCurrentClass().setImplementedInterface(interfaceClass);
                optionalGenerics();
                break;
            default:
                symbolTable.getCurrentClass().setImplementedInterface(null);
                //Empty production
        }
    }

    private void optionalExtends() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.EXTENDS_WORD)) {
            retrieveNextToken();
            var interfaceClass = currentToken;
            match(Token.TokenType.CLASSID);
            symbolTable.getCurrentInterface().setParent(interfaceClass);
            optionalGenerics();
        } else {
            symbolTable.getCurrentInterface().setParent(null);
            //Empty production
        }
    }

    private void optionalGenericsOrDiamond() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.LESS_THAN)) {
            retrieveNextToken();
            optionalClassId();
            match(Token.TokenType.GREATER_THAN);
        } else {
            //Empty production
        }
    }

    private void optionalClassId() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.CLASSID))
            retrieveNextToken();
        else {
            //Empty production
        }
    }

    private Token optionalGenerics() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.LESS_THAN)) {
            match(Token.TokenType.LESS_THAN);
            var genericType = currentToken;
            match(Token.TokenType.CLASSID);
            match(Token.TokenType.GREATER_THAN);
            return genericType;
        } else {
            return null;
            //Empty production
        }
    }

    private void memberList() throws LexicalException, SemanticException {
        Token visibility = optionalVisibility();
        if (first(NonTerminal.ATTRIBUTE_OR_METHOD).contains(currentToken.getTokenType())){
            member(visibility);
            memberList();
        }else {
            //Empty production
        }

        /*if (first(NonTerminal.ATTRIBUTE_OR_METHOD).contains(currentToken.getTokenType()) || currentToken.getTokenType().equals(Token.TokenType.PRIVATE_WORD)) {
            optionalPrivate();
            attributeOrMethod();
            memberList();
        } else if (currentToken.getTokenType().equals(Token.TokenType.PUBLIC_WORD)) {
            retrieveNextToken();
            member();
            memberList();
        } else {
            //Empty production
        }*/
    }

    private void interfaceMemberList() throws LexicalException, SemanticException {
        Token visibility = optionalVisibility();
        if (first(NonTerminal.INTERFACE_MEMBER).contains(currentToken.getTokenType())) {
            interfaceMember(visibility);
            interfaceMemberList();
        } else {
            //Empty production
        }
    }

    private void member(Token visibility) throws LexicalException, SemanticException {
        if (first(NonTerminal.PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            Type type = primitiveType();
            Token metVarIdToken = currentToken;
            match(Token.TokenType.METVARID);
            closingAttributeMethod(type, metVarIdToken, visibility);
        } else if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            Token modifier = modifier();
            Type type = methodType();
            Token metVarId = currentToken;
            match(Token.TokenType.METVARID);

            var methodEntry = new MethodEntry(metVarId);
            methodEntry.setReturnType(type);
            methodEntry.setModifier(modifier);
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentClass().addMethod(methodEntry);
            formalArgsAndOptionalBlock();
        } else if (currentToken.getTokenType().equals(Token.TokenType.CLASSID)) {
            var classIdToken = currentToken;
            match(Token.TokenType.CLASSID);
            constructorOrMember(classIdToken, visibility);
        } else if (currentToken.getTokenType().equals(Token.TokenType.VOID_WORD)) {
            match(Token.TokenType.VOID_WORD);
            Token metVarIdToken = currentToken;
            match(Token.TokenType.METVARID);
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(null);
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentClass().addMethod(methodEntry);
            formalArgsAndOptionalBlock();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an attribute, method or constructor declaration"));
                recovery();
            }
        }
    }

    private void interfaceMember(Token visibility) throws LexicalException, SemanticException {
        if (first(NonTerminal.TYPE).contains(currentToken.getTokenType())) {
            Type type = type();
            Token metVarIdToken = currentToken;
            match(Token.TokenType.METVARID);

            closingAttributeMethodInterface(metVarIdToken, type, visibility);
        } else if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            Token modifier = modifier();
            Type type = methodType();
            Token metVarIdToken = currentToken;
            match(Token.TokenType.METVARID);
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(type);
            methodEntry.setVisibility(visibility);
            methodEntry.setModifier(modifier);
            symbolTable.getCurrentInterface().addMethod(methodEntry);
            formalArgsAndOptionalBlock();
        } else if (currentToken.getTokenType().equals(Token.TokenType.VOID_WORD)) {
            retrieveNextToken();
            Token metVarIdToken = currentToken;
            match(Token.TokenType.METVARID);

            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(null);
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentInterface().addMethod(methodEntry);

            formalArgsAndOptionalBlock();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an attribute or method declaration"));
                recovery();
            }
        }
    }

    private void constructorOrMember(Token classIdToken, Token visibility) throws LexicalException, SemanticException {
        if (currentToken.getTokenType().equals(Token.TokenType.METVARID) || currentToken.getTokenType().equals(Token.TokenType.LESS_THAN)) {
            Type type = new ClassType(classIdToken, optionalGenerics());
            var metVarIdToken = currentToken;
            match(Token.TokenType.METVARID);
            closingAttributeMethod(type, metVarIdToken, visibility);
        } else if (first(NonTerminal.FORMAL_ARGS).contains(currentToken.getTokenType())) {
            MethodEntry constructor = new MethodEntry(classIdToken);
            symbolTable.getCurrentClass().addConstructor(constructor);
            formalArgs();
            block();
        } else {
            if(!panicMode){
                exceptions.add(new SyntacticException(currentToken, "constructor arguments or a method declaration"));
                recovery();
            }
        }
    }

  /*  private void attributeOrMethod() throws LexicalException {
        if (first(NonTerminal.TYPE).contains(currentToken.getTokenType())) {
            type();
            match(Token.TokenType.METVARID);
            closingAttributeMethod();
        } else if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            modifier();
            methodType();
            match(Token.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        } else if (currentToken.getTokenType().equals(Token.TokenType.VOID_WORD)) {
            retrieveNextToken();
            match(Token.TokenType.METVARID);
            formalArgsAndOptionalBlock();
        }
    }*/

    private void closingAttributeMethod(Type classId, Token metVarIdToken, Token visibility) throws LexicalException, SemanticException {
        if (first(NonTerminal.FORMAL_ARGS_AND_OPTIONAL_BLOCK).contains(currentToken.getTokenType())) {
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(classId);
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentClass().addMethod(methodEntry);
            formalArgsAndOptionalBlock();
        } else {
            AttributeEntry attributeEntry = new AttributeEntry(metVarIdToken, classId);
            attributeEntry.setVisibility(visibility);
            symbolTable.getCurrentClass().addAttribute(attributeEntry);
            optionalAssignment();
            match(Token.TokenType.SEMICOLON);
        }
    }

    private void closingAttributeMethodInterface(Token metVarIdToken, Type type, Token visibility) throws LexicalException, SemanticException {
        if (currentToken.getTokenType().equals(Token.TokenType.EQUAL)) {
            match(Token.TokenType.EQUAL);
            compoundExpression();
            optionalTernaryOperator();
            match(Token.TokenType.SEMICOLON);

            AttributeEntry attributeEntry = new AttributeEntry(metVarIdToken, type);
            attributeEntry.setVisibility(visibility);
            symbolTable.getCurrentInterface().addAttribute(attributeEntry);
        } else if(first(NonTerminal.FORMAL_ARGS_AND_OPTIONAL_BLOCK).contains(currentToken.getTokenType())) {
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(type);
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentInterface().addMethod(methodEntry);
            formalArgsAndOptionalBlock();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an assignment or a method declaration"));
                recovery();
            }
        }
    }

    private void formalArgsAndOptionalBlock() throws LexicalException {
        formalArgs();
        optionalBlock();
    }

    private void optionalAssignment() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.EQUAL)) {
            retrieveNextToken();
            compoundExpression();
            optionalTernaryOperator();
        } else {
            //Empty production
        }
    }

    private Type methodType() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.VOID_WORD)) {
            match(Token.TokenType.VOID_WORD);
            return null;
        } else if (first(NonTerminal.TYPE).contains(currentToken.getTokenType())) {
            return type();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "a method type"));
                recovery();
            }
        }
        return null;
    }

    private Type type() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.CLASSID)) {
            Token classIdToken = currentToken;
            match(Token.TokenType.CLASSID);
            return new ClassType(classIdToken, optionalGenerics());
        } else if (first(NonTerminal.PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            return primitiveType();
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "a type"));
                recovery();
            }
        }
        return null;
    }

    private Type primitiveType() throws LexicalException {
        Token token = currentToken;
        switch (currentToken.getTokenType()) {
            case Token.TokenType.INT_WORD:
                match(Token.TokenType.INT_WORD);
                return new PrimitiveType(token);
            case Token.TokenType.BOOLEAN_WORD:
                match(Token.TokenType.BOOLEAN_WORD);
                return new PrimitiveType(token);
            case Token.TokenType.CHAR_WORD:
                match(Token.TokenType.CHAR_WORD);
                return new PrimitiveType(token);
            default:
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "a primitive type"));
                    recovery();
                }
        }
        return null;
    }

    private void formalArgs() throws LexicalException {
        match(Token.TokenType.OPENING_PAREN);
        optionalFormalArgsList();
        match(Token.TokenType.CLOSING_PAREN);
    }

    private void optionalFormalArgsList() throws LexicalException {
        if (first(NonTerminal.FORMAL_ARGS_LIST).contains(currentToken.getTokenType())) {
            symbolTable.getCurrentClassOrInterface().getCurrentMethod().setParameters(
                formalArgsList()
            );
        } else {
            symbolTable.getCurrentClassOrInterface().getCurrentMethod().setParameters(
                    Collections.emptyList()
            );
            //Empty production
        }
    }

    private List<ParameterEntry> formalArgsList() throws LexicalException {
        List<ParameterEntry> parameterList = new LinkedList<>();
        parameterList.addLast(formalArg());
        formalArgsList2(parameterList);
        return parameterList;
    }

    private void formalArgsList2(List<ParameterEntry> parameterList) throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.COMMA)) {
            match(Token.TokenType.COMMA);
            parameterList.addLast(formalArg());
            formalArgsList2(parameterList);
        } else {
            //Empty production
        }
    }

    private ParameterEntry formalArg() throws LexicalException {
        type();
        match(Token.TokenType.METVARID);
        return null;
    }

    private void optionalBlock() throws LexicalException {
        if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            block();
        } else {
            match(Token.TokenType.SEMICOLON);
        }
    }

    private void block() throws LexicalException {
        match(Token.TokenType.OPENING_BRACE);
        sentenceList();
        match(Token.TokenType.CLOSING_BRACE);
    }

    private void sentenceList() throws LexicalException {
        if (first(NonTerminal.SENTENCE).contains(currentToken.getTokenType())) {
            sentence();
            sentenceList();
        }
    }

    private void sentence() throws LexicalException {
      /*  if (currentToken.getTokenType().equals(Token.TokenType.FOR_WORD)) {
            forSentence();
        } else */if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            block();
        } else if (currentToken.getTokenType().equals(Token.TokenType.WHILE_WORD)) {
            whileSentence();
        } else if (currentToken.getTokenType().equals(Token.TokenType.IF_WORD)) {
            ifSentence();
        } else if (currentToken.getTokenType().equals(Token.TokenType.RETURN_WORD)) {
            returnSentence();
            match(Token.TokenType.SEMICOLON);
        } else if (first(NonTerminal.ASSIGNMENT_CALL_OR_LOCALVAR).contains(currentToken.getTokenType())) {
            assignmentCallOrLocalVar();
            match(Token.TokenType.SEMICOLON);
        } else {
            match(Token.TokenType.SEMICOLON);
        }
    }

    private void assignmentCallOrLocalVar() throws LexicalException {
        if (first(NonTerminal.LOCAL_VAR_WITH_VAR).contains(currentToken.getTokenType())) {
            localVarWithVar();
        } else if (first(NonTerminal.LOCAL_VAR_WITH_PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            localVarWithPrimitiveType();
        } else if (first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
            expressionWOStaticMethodCall();
        } else if (currentToken.getTokenType().equals(Token.TokenType.CLASSID)) {
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
        if (currentToken.getTokenType().equals(Token.TokenType.PERIOD)) {
            retrieveNextToken();
            match(Token.TokenType.METVARID);
            actualArgs();
            reference2();
            compoundExpression2();
            expression2();
        } else {
            optionalGenerics();
            match(Token.TokenType.METVARID);               //Maybe I want to check if the token is metvarid here and throw exception if it's not
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
        match(Token.TokenType.VAR_WORD);
        match((Token.TokenType.METVARID));
        match(Token.TokenType.EQUAL);
        compoundExpression();
        optionalTernaryOperator();
    }

    private void localVarWithPrimitiveType() throws LexicalException {
        primitiveType();
        match(Token.TokenType.METVARID);
        multipleDeclaration();
        optionalAssignment();
    }

 /*   private void forSentence() throws LexicalException {
        match(Token.TokenType.FOR_WORD);
        match(Token.TokenType.OPENING_PAREN);
        forSentence2();
        forCondition();
        match(Token.TokenType.CLOSING_PAREN);
        sentence();
    }

    private void forSentence2() throws LexicalException {
        if (first(NonTerminal.LOCAL_VAR_WITH_VAR).contains(currentToken.getTokenType())) {
            localVarWithVar();
        } else if (first(NonTerminal.PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            primitiveType();
            match(Token.TokenType.METVARID);
            multipleDeclaration();
            optionalAssignment();
        } else if (first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
            expressionWOStaticMethodCall();
        } else if (currentToken.getTokenType().equals(Token.TokenType.CLASSID)) {
            match(Token.TokenType.CLASSID);
            staticMethodOrLocalVar();
        } else {
            //Empty production
        }
    }

    private void forCondition() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case Token.TokenType.SEMICOLON:
                match(Token.TokenType.SEMICOLON);
                optionalCompoundExpression();
                match(Token.TokenType.SEMICOLON);
                optionalExpression();
                break;
            case Token.TokenType.COLON:
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
*/
    private void multipleDeclaration() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.COMMA)) {
            retrieveNextToken();
            match(Token.TokenType.METVARID);
            multipleDeclaration();
        } else {
            //Empty production
        }
    }

    private void returnSentence() throws LexicalException {
        match(Token.TokenType.RETURN_WORD);
        optionalExpression();
    }

    private void optionalExpression() throws LexicalException {
        if (first(NonTerminal.EXPRESSION).contains(currentToken.getTokenType())) {
            expression();
        }
    }

    private void ifSentence() throws LexicalException {
        match(Token.TokenType.IF_WORD);
        match(Token.TokenType.OPENING_PAREN);
        expression();
        match(Token.TokenType.CLOSING_PAREN);
        sentence();
        elseSentence();
    }

    private void elseSentence() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.ELSE_WORD)) {
            retrieveNextToken();
            sentence();
        } else {
            //Empty production
        }
    }

    private void whileSentence() throws LexicalException {
        match(Token.TokenType.WHILE_WORD);
        match(Token.TokenType.OPENING_PAREN);
        expression();
        match(Token.TokenType.CLOSING_PAREN);
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
        if (currentToken.getTokenType().equals(Token.TokenType.QUESTION_MARK)) {
            retrieveNextToken();
            compoundExpression();
            match(Token.TokenType.COLON);
            compoundExpression();
            optionalTernaryOperator();
        } else {
            //Empty produciton
        }
    }

    private void assignmentOperator() throws LexicalException {
        match(Token.TokenType.EQUAL);
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
            case Token.TokenType.OR:
            case Token.TokenType.AND:
            case Token.TokenType.EQUALS_COMPARISON:
            case Token.TokenType.DIFERENT:
            case Token.TokenType.LESS_THAN:
            case Token.TokenType.GREATER_THAN:
            case Token.TokenType.EQUAL_LESS_THAN:
            case Token.TokenType.EQUAL_GREATER_THAN:
            case Token.TokenType.PLUS:
            case Token.TokenType.MINUS:
            case Token.TokenType.MULTIPLY:
            case Token.TokenType.SLASH:
            case Token.TokenType.PERCENT:
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
            case Token.TokenType.PLUS:
            case Token.TokenType.PLUS1:
            case Token.TokenType.MINUS:
            case Token.TokenType.MINUS1:
            case Token.TokenType.EXCLAMATION_POINT:
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
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an operand"));
                recovery();
            }
        }
    }

    private void primitive() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case Token.TokenType.INTLITERAL:
            case Token.TokenType.CHARLITERAL:
            case Token.TokenType.TRUE_WORD:
            case Token.TokenType.FALSE_WORD:
            case Token.TokenType.NULL_WORD:
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
            case Token.TokenType.THIS_WORD:
            case Token.TokenType.STRINGLITERAL:
                retrieveNextToken();
                break;
            case Token.TokenType.OPENING_PAREN:
                parenthesizedExpression();
                break;
            case Token.TokenType.METVARID:
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
        match(Token.TokenType.METVARID);
        optionalActualArgs();
    }

    private void constructorCall() throws LexicalException {
        match(Token.TokenType.NEW_WORD);
        match(Token.TokenType.CLASSID);
        optionalGenericsOrDiamond();
        actualArgs();
    }

    private void parenthesizedExpression() throws LexicalException {
        match(Token.TokenType.OPENING_PAREN);
        expression();
        match(Token.TokenType.CLOSING_PAREN);
    }

    private void staticMethodCall() throws LexicalException {
        match(Token.TokenType.CLASSID);
        match(Token.TokenType.PERIOD);
        match(Token.TokenType.METVARID);
        actualArgs();
    }

    private void actualArgs() throws LexicalException {
        match(Token.TokenType.OPENING_PAREN);
        optionalExpressionList();
        match(Token.TokenType.CLOSING_PAREN);
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
        if (currentToken.getTokenType().equals(Token.TokenType.COMMA)) {
            retrieveNextToken();
            expression();
            expressionList2();
        } else {
            //Empty produciton
        }
    }

    private void chainedVarMethod() throws LexicalException {
        match(Token.TokenType.PERIOD);
        match(Token.TokenType.METVARID);
        optionalActualArgs();
    }

    private void optionalActualArgs() throws LexicalException {
        if (first(NonTerminal.ACTUAL_ARGS).contains(currentToken.getTokenType())) {
            actualArgs();
        } else {
            //Empty production
        }
    }

    private void match(Token.TokenType expectedTokenType) throws LexicalException {
        if (panicMode && (expectedTokenType.equals(Token.TokenType.CLOSING_BRACE) || expectedTokenType.equals(Token.TokenType.SEMICOLON) || expectedTokenType.equals(Token.TokenType.OPENING_BRACE))) {
            panicMode = false;
            while (!(expectedTokenType.equals(currentToken.getTokenType())) && !currentToken.getTokenType().equals(Token.TokenType.EOF)) {
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

    private CustomSet<Token.TokenType> first(NonTerminal nonTerminal) {
        return switch (nonTerminal) {
            case CLASS_AND_INTERFACE_LIST2 ->
                    first(NonTerminal.CLASS_STATEMENT).appendAll(first(NonTerminal.INTERFACE_STATEMENT));
            case CLASS_STATEMENT -> new CustomHashSet<>(List.of(Token.TokenType.CLASS_WORD));
            case INTERFACE_STATEMENT -> new CustomHashSet<>(List.of(Token.TokenType.INTERFACE_WORD));
            case MODIFIER ->
                    new CustomHashSet<>(List.of(Token.TokenType.ABSTRACT_WORD, Token.TokenType.FINAL_WORD, Token.TokenType.STATIC_WORD));
            case ATTRIBUTE_OR_METHOD, INTERFACE_MEMBER ->
                    first(NonTerminal.MODIFIER).appendAll(first(NonTerminal.TYPE)).append(Token.TokenType.VOID_WORD);
            case PRIMITIVE_TYPE ->
                    new CustomHashSet<>(List.of(Token.TokenType.BOOLEAN_WORD, Token.TokenType.CHAR_WORD, Token.TokenType.INT_WORD));
            case TYPE -> first(NonTerminal.PRIMITIVE_TYPE).append(Token.TokenType.CLASSID);
            case FORMAL_ARGS, ACTUAL_ARGS -> new CustomHashSet<>(List.of(Token.TokenType.OPENING_PAREN));
            case FORMAL_ARGS_AND_OPTIONAL_BLOCK -> first(NonTerminal.FORMAL_ARGS);
            case FORMAL_ARGS_LIST -> first(NonTerminal.FORMAL_ARG);
            case FORMAL_ARG -> first(NonTerminal.TYPE);
            case BLOCK -> new CustomHashSet<>(List.of(Token.TokenType.OPENING_BRACE));
            case SENTENCE ->
                    first(NonTerminal.ASSIGNMENT_CALL_OR_LOCALVAR).appendAll(new CustomHashSet<>(List.of(Token.TokenType.RETURN_WORD, Token.TokenType.IF_WORD, Token.TokenType.WHILE_WORD, Token.TokenType.FOR_WORD, Token.TokenType.SEMICOLON, Token.TokenType.OPENING_BRACE)));
            case ASSIGNMENT_CALL_OR_LOCALVAR ->
                    first(NonTerminal.PRIMITIVE_TYPE).appendAll(first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).append(Token.TokenType.VAR_WORD).append(Token.TokenType.CLASSID));
            case EXPRESSION_WO_STATIC_METHOD_CALL, BASIC_EXPRESSION ->
                    first(NonTerminal.UNARY_OPERATOR).appendAll(first(NonTerminal.OPERAND));
            case UNARY_OPERATOR ->
                    new CustomHashSet<>(List.of(Token.TokenType.PLUS, Token.TokenType.MINUS, Token.TokenType.PLUS1, Token.TokenType.MINUS1, Token.TokenType.EXCLAMATION_POINT));
            case OPERAND -> first(NonTerminal.PRIMITIVE).appendAll(first(NonTerminal.REFERENCE));
            case PRIMITIVE ->
                    new CustomHashSet<>(List.of(Token.TokenType.INTLITERAL, Token.TokenType.CHARLITERAL, Token.TokenType.TRUE_WORD, Token.TokenType.FALSE_WORD, Token.TokenType.NULL_WORD));
            case REFERENCE ->
                    new CustomHashSet<>(List.of(Token.TokenType.METVARID, Token.TokenType.THIS_WORD, Token.TokenType.STRINGLITERAL, Token.TokenType.NEW_WORD, Token.TokenType.OPENING_PAREN));
            case LOCAL_VAR_WITH_VAR -> new CustomHashSet<>(List.of(Token.TokenType.VAR_WORD));
            case LOCAL_VAR_WITH_PRIMITIVE_TYPE -> first(NonTerminal.PRIMITIVE_TYPE);
            case EXPRESSION, COMPOUND_EXPRESSION ->
                    first(NonTerminal.BASIC_EXPRESSION).appendAll(first(NonTerminal.STATIC_METHOD_CALL));
            case STATIC_METHOD_CALL -> new CustomHashSet<>(List.of(Token.TokenType.CLASSID));
            case ASSIGNMENT_OPERATOR -> new CustomHashSet<>(List.of(Token.TokenType.EQUAL));
            case BINARY_OPERATOR ->
                    new CustomHashSet<>(List.of(Token.TokenType.OR, Token.TokenType.AND, Token.TokenType.EQUALS_COMPARISON, Token.TokenType.DIFERENT, Token.TokenType.LESS_THAN, Token.TokenType.GREATER_THAN, Token.TokenType.EQUAL_LESS_THAN, Token.TokenType.EQUAL_GREATER_THAN, Token.TokenType.PLUS, Token.TokenType.MINUS, Token.TokenType.MULTIPLY, Token.TokenType.SLASH, Token.TokenType.PERCENT));
            case CHAINED_VAR_METHOD -> new CustomHashSet<>(List.of(Token.TokenType.PERIOD));
            case CONSTRUCTOR_CALL -> new CustomHashSet<>(List.of(Token.TokenType.NEW_WORD));
            case EXPRESSION_LIST -> first(NonTerminal.EXPRESSION);
        };
    }
}
