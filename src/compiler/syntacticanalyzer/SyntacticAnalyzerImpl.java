package compiler.syntacticanalyzer;

import compiler.exceptions.LexicalException;
import compiler.exceptions.SemanticException;
import compiler.exceptions.SyntacticException;
import compiler.exceptions.SyntacticExceptions;
import compiler.lexicalanalyzer.LexicalAnalyzer;
import compiler.symboltable.*;
import compiler.symboltable.types.ClassType;
import compiler.symboltable.types.PrimitiveType;
import compiler.symboltable.types.Type;
import compiler.token.Token;
import compiler.token.Token.TokenType;
import compiler.token.TokenImpl;
import utils.CustomHashSet;
import utils.CustomSet;

import java.util.*;

public class SyntacticAnalyzerImpl implements SyntacticAnalyzer {
    public static SymbolTable symbolTable;

    private final LexicalAnalyzer lexicalAnalyzer;
    private Token currentToken;
    private Queue<SyntacticException> exceptions;
    private boolean panicMode = false;
    private Set<Token.TokenType> recoveryTokens;

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
        recoveryTokens = new HashSet<>();

        classAndInterfaceList();
        match(TokenType.EOF);
        if (!exceptions.isEmpty()) {
            throw new SyntacticExceptions(exceptions);
        }
    }

    private void classAndInterfaceList() throws LexicalException, SemanticException {
        recoveryTokens.add(TokenType.CLOSING_BRACE);
        Token modifier = optionalModifier();
        if (first(NonTerminal.CLASS_AND_INTERFACE_LIST2).contains(currentToken.getTokenType())) {
            classAndInterfaceList2(modifier);
        } else { //Empty production
            if(currentToken.getTokenType() != TokenType.EOF){
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "eof")); //TODO setup msg
                    recovery();
                }
            }
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
        match(TokenType.CLASS_WORD);
        Token classId = currentToken;
        match(TokenType.CLASSID);

        ClassEntry classEntry = new ClassEntry(classId);
        classEntry.setModifier(modifier);
        symbolTable.putClass(classEntry);
        classEntry.addGenericType(optionalGenerics());
        optionalInheritance();

        match(TokenType.OPENING_BRACE);
        memberList();
        match(TokenType.CLOSING_BRACE);

    }

    private void interfaceStatement(Token modifier) throws LexicalException, SemanticException {
        match(TokenType.INTERFACE_WORD);
        Token classId = currentToken;
        match(TokenType.CLASSID);
        InterfaceEntry interfaceEntry = new InterfaceEntry(classId);
        symbolTable.putInterface(interfaceEntry);
        interfaceEntry.setModifier(modifier);
        interfaceEntry.addGenericType(optionalGenerics());
        optionalExtends();
        match(TokenType.OPENING_BRACE);
        interfaceMemberList();
        match(TokenType.CLOSING_BRACE);
    }

    private Token modifier() throws LexicalException {
        var modifier = currentToken;
        return switch (currentToken.getTokenType()) {
            case TokenType.ABSTRACT_WORD, TokenType.FINAL_WORD, TokenType.STATIC_WORD -> {
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
            switch (currentToken.getTokenType()) {
                case TokenType.CLASS_WORD, TokenType.INTERFACE_WORD:
                    break;  //Empty production
                default:
                    if (!panicMode) {
                        exceptions.add(new SyntacticException(currentToken, "Class or Interface declaration"));
                        recovery();
                    }
                    break;
            }
            return null;
        }
    }

    private Token optionalVisibility() throws LexicalException {
        var visibility = currentToken;
        switch (currentToken.getTokenType()) {
            case TokenType.PUBLIC_WORD, TokenType.PRIVATE_WORD : {
                retrieveNextToken();
                return visibility;
            }
            case TokenType.INT_WORD,         //Empty production
                 TokenType.BOOLEAN_WORD,
                 TokenType.CHAR_WORD,
                 TokenType.ABSTRACT_WORD,
                 TokenType.STATIC_WORD,
                 TokenType.FINAL_WORD,
                 TokenType.VOID_WORD,
                 TokenType.CLASSID :
                return new TokenImpl(TokenType.PUBLIC_WORD, "public", -1);
            default :
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "Member declaration"));
                    recovery();
                }
        }
        return null;
    }

    private void optionalInheritance() throws LexicalException, SemanticException {
        switch (currentToken.getTokenType()) {
            case TokenType.EXTENDS_WORD:
                retrieveNextToken();
                var parentClass = currentToken;
                match(TokenType.CLASSID);
                symbolTable.getCurrentClass().setParent(parentClass);
                symbolTable.getCurrentClass().setParentGenericType(optionalGenerics());
                break;
            case TokenType.IMPLEMENTS_WORD:
                retrieveNextToken();
                var interfaceClass = currentToken;
                match(TokenType.CLASSID);
                symbolTable.getCurrentClass().setImplementedInterface(interfaceClass);
                optionalGenerics();
                break;
            case TokenType.OPENING_BRACE:  //Empty production
                symbolTable.getCurrentClass().setImplementedInterface(null);
                break;
            default:
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "{"));
                    recovery();
                }
        }
    }

    private void optionalExtends() throws LexicalException {
        if (currentToken.getTokenType().equals(TokenType.EXTENDS_WORD)) {
            retrieveNextToken();
            var interfaceClass = currentToken;
            match(TokenType.CLASSID);
            symbolTable.getCurrentInterface().setParent(interfaceClass);
            optionalGenerics();
        } else if(currentToken.getTokenType() == TokenType.OPENING_BRACE) {
            symbolTable.getCurrentInterface().setParent(null);
            //Empty production
        } else{
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "{"));
                recovery();
            }
        }
    }

    private void optionalGenericsOrDiamond() throws LexicalException {
        if (currentToken.getTokenType().equals(TokenType.LESS_THAN)) {
            retrieveNextToken();
            optionalClassId();
            match(TokenType.GREATER_THAN);
        } else if (currentToken.getTokenType() != TokenType.OPENING_PAREN) {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "("));
                recovery();
            }
        } else{
            //Empty production
        }
    }

    private void optionalClassId() throws LexicalException {
        if (currentToken.getTokenType().equals(TokenType.CLASSID))
            retrieveNextToken();
        else if (currentToken.getTokenType() != TokenType.GREATER_THAN) {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, ">"));
                recovery();
            }
        } else{
            //Empty production
        }
    }

    private Token optionalGenerics() throws LexicalException {
        if (currentToken.getTokenType().equals(TokenType.LESS_THAN)) {
            match(TokenType.LESS_THAN);
            var genericType = currentToken;
            match(TokenType.CLASSID);
            match(TokenType.GREATER_THAN);
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
        }else if (currentToken.getTokenType() != TokenType.CLOSING_BRACE) {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "}"));
                recovery();
            }
        }else{
            //Empty production
        }

    }

    private void interfaceMemberList() throws LexicalException, SemanticException {
        Token visibility = optionalVisibility();
        if (first(NonTerminal.INTERFACE_MEMBER).contains(currentToken.getTokenType())) {
            interfaceMember(visibility);
            interfaceMemberList();
        }else if (currentToken.getTokenType() != TokenType.CLOSING_BRACE) {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "}"));
                recovery();
            }
        }else{
            //Empty production
        }
    }

    private void member(Token visibility) throws LexicalException, SemanticException {
        if (first(NonTerminal.PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            Type type = primitiveType();
            Token metVarIdToken = currentToken;
            match(TokenType.METVARID);
            closingAttributeMethod(type, metVarIdToken, visibility);
        } else if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            Token modifier = modifier();
            Type type = methodType();
            Token metVarId = currentToken;
            match(TokenType.METVARID);

            var methodEntry = new MethodEntry(metVarId);
            methodEntry.setReturnType(type);
            methodEntry.setModifier(modifier);
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentClass().setCurrentMethod(methodEntry);
            boolean modifierIsAbstract = (modifier != null && modifier.getTokenType() == TokenType.ABSTRACT_WORD);
            boolean hasBody = formalArgsAndOptionalBlock();
            if(modifierIsAbstract && hasBody)
                throw new SemanticException("Abstract method can't have a body", metVarId);
            if(!modifierIsAbstract && !hasBody)
                throw new SemanticException("Method body expected", metVarId);
            symbolTable.getCurrentClass().addMethod(methodEntry);
        } else if (currentToken.getTokenType().equals(TokenType.CLASSID)) {
            var classIdToken = currentToken;
            match(TokenType.CLASSID);
            constructorOrMember(classIdToken, visibility);
        } else if (currentToken.getTokenType().equals(TokenType.VOID_WORD)) {
            Token voidWord = currentToken;
            match(TokenType.VOID_WORD);
            Token metVarIdToken = currentToken;
            match(TokenType.METVARID);
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(new PrimitiveType(voidWord));
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentClass().setCurrentMethod(methodEntry);
            if(!formalArgsAndOptionalBlock())
                throw new SemanticException("Method body expected", metVarIdToken);
            symbolTable.getCurrentClass().addMethod(methodEntry);
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
            match(TokenType.METVARID);

            closingAttributeMethodInterface(metVarIdToken, type, visibility);
        } else if (first(NonTerminal.MODIFIER).contains(currentToken.getTokenType())) {
            Token modifier = modifier();
            Type type = methodType();
            Token metVarIdToken = currentToken;
            match(TokenType.METVARID);
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(type);
            methodEntry.setVisibility(visibility);
            methodEntry.setModifier(modifier);
            symbolTable.getCurrentInterface().setCurrentMethod(methodEntry);
            boolean modifierIsStatic = (modifier != null && modifier.getTokenType() == TokenType.STATIC_WORD);
            boolean hasBody = formalArgsAndOptionalBlock();
            if(hasBody && !modifierIsStatic)
                throw new SemanticException("Interface abstract methods cannot have body", metVarIdToken);
            if(!hasBody && modifierIsStatic)
                throw new SemanticException("Interface static methods must have body", metVarIdToken);
            symbolTable.getCurrentInterface().addMethod(methodEntry);
        } else if (currentToken.getTokenType().equals(TokenType.VOID_WORD)) {
            Token voidWord = currentToken;
            match(TokenType.VOID_WORD);
            Token metVarIdToken = currentToken;
            match(TokenType.METVARID);

            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(new PrimitiveType(voidWord));
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentInterface().setCurrentMethod(methodEntry);
            if(formalArgsAndOptionalBlock())
                throw new SemanticException("Interface abstract methods cannot have body", metVarIdToken);
            symbolTable.getCurrentInterface().addMethod(methodEntry);
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an attribute or method declaration"));
                recovery();
            }
        }
    }

    private void constructorOrMember(Token classIdToken, Token visibility) throws LexicalException, SemanticException {
        if (currentToken.getTokenType().equals(TokenType.METVARID) || currentToken.getTokenType().equals(TokenType.LESS_THAN)) {
            Type type = new ClassType(classIdToken, optionalGenerics());
            var metVarIdToken = currentToken;
            match(TokenType.METVARID);
            closingAttributeMethod(type, metVarIdToken, visibility);
        } else if (first(NonTerminal.FORMAL_ARGS).contains(currentToken.getTokenType())) {
            MethodEntry constructor = new MethodEntry(classIdToken);
            symbolTable.getCurrentClass().setCurrentMethod(constructor);
            formalArgs();
            symbolTable.getCurrentClass().addConstructor(constructor);
            block();
        } else {
            if(!panicMode){
                exceptions.add(new SyntacticException(currentToken, "constructor arguments or a method declaration"));
                recovery();
            }
        }
    }

    private void closingAttributeMethod(Type classId, Token metVarIdToken, Token visibility) throws LexicalException, SemanticException {
        if (first(NonTerminal.FORMAL_ARGS_AND_OPTIONAL_BLOCK).contains(currentToken.getTokenType())) {
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(classId);
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentClass().setCurrentMethod(methodEntry);
            if(!formalArgsAndOptionalBlock())
                throw new SemanticException("Method body expected", metVarIdToken);         //TODO check this}
            symbolTable.getCurrentClass().addMethod(methodEntry);
        } else {
            AttributeEntry attributeEntry = new AttributeEntry(metVarIdToken, classId);
            attributeEntry.setVisibility(visibility);
            symbolTable.getCurrentClass().addAttribute(attributeEntry);
            optionalAssignment();
            match(TokenType.SEMICOLON);
        }
    }

    private void closingAttributeMethodInterface(Token metVarIdToken, Type type, Token visibility) throws LexicalException, SemanticException {
        if (currentToken.getTokenType().equals(TokenType.EQUAL)) {
            match(TokenType.EQUAL);
            compoundExpression();
            optionalTernaryOperator();
            match(TokenType.SEMICOLON);

            AttributeEntry attributeEntry = new AttributeEntry(metVarIdToken, type);
            attributeEntry.setVisibility(visibility);
            symbolTable.getCurrentInterface().addAttribute(attributeEntry);
        } else if(first(NonTerminal.FORMAL_ARGS_AND_OPTIONAL_BLOCK).contains(currentToken.getTokenType())) {
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(type);
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentInterface().setCurrentMethod(methodEntry);
            if(formalArgsAndOptionalBlock())
                throw new SemanticException("Interface abstract methods cannot have body", metVarIdToken);
            symbolTable.getCurrentInterface().addMethod(methodEntry);
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an assignment or a method declaration"));
                recovery();
            }
        }
    }

    private boolean formalArgsAndOptionalBlock() throws LexicalException, SemanticException {
        formalArgs();
        return optionalBlock();
    }

    private void optionalAssignment() throws LexicalException {
        if (currentToken.getTokenType().equals(TokenType.EQUAL)) {
            retrieveNextToken();
            compoundExpression();
            optionalTernaryOperator();
        } else if(currentToken.getTokenType() != TokenType.SEMICOLON) {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, ";"));
                recovery();
            }
        } else{
            //Empty production
        }
    }

    private Type methodType() throws LexicalException {
        if (currentToken.getTokenType().equals(TokenType.VOID_WORD)) {
            Token voidWord = currentToken;
            match(TokenType.VOID_WORD);
            return new PrimitiveType(voidWord);
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
        if (currentToken.getTokenType().equals(TokenType.CLASSID)) {
            Token classIdToken = currentToken;
            match(TokenType.CLASSID);
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
            case TokenType.INT_WORD:
                match(TokenType.INT_WORD);
                return new PrimitiveType(token);
            case TokenType.BOOLEAN_WORD:
                match(TokenType.BOOLEAN_WORD);
                return new PrimitiveType(token);
            case TokenType.CHAR_WORD:
                match(TokenType.CHAR_WORD);
                return new PrimitiveType(token);
            default:
                if (!panicMode) {
                    exceptions.add(new SyntacticException(currentToken, "a primitive type"));
                    recovery();
                }
        }
        return null;
    }

    private void formalArgs() throws LexicalException, SemanticException {
        match(TokenType.OPENING_PAREN);
        optionalFormalArgsList();
        match(TokenType.CLOSING_PAREN);
    }

    private void optionalFormalArgsList() throws LexicalException, SemanticException {
        if (first(NonTerminal.FORMAL_ARGS_LIST).contains(currentToken.getTokenType())) {
            formalArgsList();
        } else if(currentToken.getTokenType() != TokenType.CLOSING_PAREN) {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, ")"));
                recovery();
            }
        } else {
            //Empty production
        }
    }

    private void formalArgsList() throws LexicalException, SemanticException {
        symbolTable.getCurrentClassOrInterface().getCurrentMethod().addParameter(formalArg());
        formalArgsList2();
    }

    private void formalArgsList2() throws LexicalException, SemanticException {
        if (currentToken.getTokenType().equals(TokenType.COMMA)) {
            match(TokenType.COMMA);
            symbolTable.getCurrentClassOrInterface().getCurrentMethod().addParameter(formalArg());
            formalArgsList2();
        } else if(currentToken.getTokenType() != TokenType.CLOSING_PAREN){
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, ")"));
                recovery();
            }
        }else {
            //Empty production
        }
    }

    private ParameterEntry formalArg() throws LexicalException {
        Type t = type();
        Token metVarIdToken = currentToken;
        match(TokenType.METVARID);
        return new ParameterEntry(t, metVarIdToken);
    }

    private boolean optionalBlock() throws LexicalException {
        if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            block();
            return true;
        } else {
            match(TokenType.SEMICOLON);
            return false;
        }
    }

    private void block() throws LexicalException {
        match(TokenType.OPENING_BRACE);
        sentenceList();
        match(TokenType.CLOSING_BRACE);
    }

    private void sentenceList() throws LexicalException {
        if (first(NonTerminal.SENTENCE).contains(currentToken.getTokenType())) {
            sentence();
            sentenceList();
        } else if(currentToken.getTokenType() != TokenType.CLOSING_BRACE) {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "}"));
                recovery();
            }
        } else{
            //Empty production
        }
    }

    private void sentence() throws LexicalException {
      /*  if (currentToken.getTokenType().equals(Token.TokenType.FOR_WORD)) {
            forSentence();
        } else */if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            block();
        } else if (currentToken.getTokenType().equals(TokenType.WHILE_WORD)) {
            whileSentence();
        } else if (currentToken.getTokenType().equals(TokenType.IF_WORD)) {
            ifSentence();
        } else if (currentToken.getTokenType().equals(TokenType.RETURN_WORD)) {
            returnSentence();
            match(TokenType.SEMICOLON);
        } else if (first(NonTerminal.ASSIGNMENT_CALL_OR_LOCALVAR).contains(currentToken.getTokenType())) {
            assignmentCallOrLocalVar();
            match(TokenType.SEMICOLON);
        } else {
            match(TokenType.SEMICOLON);
        }
    }

    private void assignmentCallOrLocalVar() throws LexicalException {
        if (first(NonTerminal.LOCAL_VAR_WITH_VAR).contains(currentToken.getTokenType())) {
            localVarWithVar();
        } else if (first(NonTerminal.LOCAL_VAR_WITH_PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            localVarWithPrimitiveType();
        } else if (first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
            expressionWOStaticMethodCall();
        } else if (currentToken.getTokenType().equals(TokenType.CLASSID)) {
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
        if (currentToken.getTokenType().equals(TokenType.PERIOD)) {
            retrieveNextToken();
            match(TokenType.METVARID);
            actualArgs();
            reference2();
            compoundExpression2();
            expression2();
        } else {
            optionalGenerics();
            match(TokenType.METVARID);               //Maybe I want to check if the token is metvarid here and throw exception if it's not
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
        match(TokenType.VAR_WORD);
        match((TokenType.METVARID));
        match(TokenType.EQUAL);
        compoundExpression();
        optionalTernaryOperator();
    }

    private void localVarWithPrimitiveType() throws LexicalException {
        primitiveType();
        match(TokenType.METVARID);
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
        if (currentToken.getTokenType().equals(TokenType.COMMA)) {
            retrieveNextToken();
            match(TokenType.METVARID);
            multipleDeclaration();
        } else {
            //Empty production
        }
    }

    private void returnSentence() throws LexicalException {
        match(TokenType.RETURN_WORD);
        optionalExpression();
    }

    private void optionalExpression() throws LexicalException {
        if (first(NonTerminal.EXPRESSION).contains(currentToken.getTokenType())) {
            expression();
        } else if(currentToken.getTokenType() != TokenType.SEMICOLON){
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, ";"));
                recovery();
            }
        } else {
            //Empty expression
        }
    }

    private void ifSentence() throws LexicalException {
        match(TokenType.IF_WORD);
        match(TokenType.OPENING_PAREN);
        expression();
        match(TokenType.CLOSING_PAREN);
        sentence();
        elseSentence();
    }

    private void elseSentence() throws LexicalException {
        if (currentToken.getTokenType().equals(TokenType.ELSE_WORD)) {
            retrieveNextToken();
            sentence();
        } else if(currentToken.getTokenType() != TokenType.CLOSING_BRACE){
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "}"));
                recovery();
            }
        }else {
            //Empty production
        }
    }

    private void whileSentence() throws LexicalException {
        match(TokenType.WHILE_WORD);
        match(TokenType.OPENING_PAREN);
        expression();
        match(TokenType.CLOSING_PAREN);
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
        if (currentToken.getTokenType().equals(TokenType.QUESTION_MARK)) {
            retrieveNextToken();
            compoundExpression();
            match(TokenType.COLON);
            compoundExpression();
            optionalTernaryOperator();
        } else {
            //Empty produciton
        }
    }

    private void assignmentOperator() throws LexicalException {
        match(TokenType.EQUAL);
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

    private void compoundExpression2() throws LexicalException {
        if (first(NonTerminal.BINARY_OPERATOR).contains(currentToken.getTokenType())) {
            binaryOperator();
            compoundExpression();
        } else if(false){//TODO

        }else {
            //Empty production
        }
    }

    private void binaryOperator() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case TokenType.OR:
            case TokenType.AND:
            case TokenType.EQUALS_COMPARISON:
            case TokenType.DIFERENT:
            case TokenType.LESS_THAN:
            case TokenType.GREATER_THAN:
            case TokenType.EQUAL_LESS_THAN:
            case TokenType.EQUAL_GREATER_THAN:
            case TokenType.PLUS:
            case TokenType.MINUS:
            case TokenType.MULTIPLY:
            case TokenType.SLASH:
            case TokenType.PERCENT:
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
            case TokenType.PLUS:
            case TokenType.PLUS1:
            case TokenType.MINUS:
            case TokenType.MINUS1:
            case TokenType.EXCLAMATION_POINT:
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
            case TokenType.INTLITERAL:
            case TokenType.CHARLITERAL:
            case TokenType.TRUE_WORD:
            case TokenType.FALSE_WORD:
            case TokenType.NULL_WORD:
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
            case TokenType.THIS_WORD:
            case TokenType.STRINGLITERAL:
                retrieveNextToken();
                break;
            case TokenType.OPENING_PAREN:
                parenthesizedExpression();
                break;
            case TokenType.METVARID:
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
        match(TokenType.METVARID);
        optionalActualArgs();
    }

    private void constructorCall() throws LexicalException {
        match(TokenType.NEW_WORD);
        match(TokenType.CLASSID);
        optionalGenericsOrDiamond();
        actualArgs();
    }

    private void parenthesizedExpression() throws LexicalException {
        match(TokenType.OPENING_PAREN);
        expression();
        match(TokenType.CLOSING_PAREN);
    }

    private void staticMethodCall() throws LexicalException {
        match(TokenType.CLASSID);
        match(TokenType.PERIOD);
        match(TokenType.METVARID);
        actualArgs();
    }

    private void actualArgs() throws LexicalException {
        match(TokenType.OPENING_PAREN);
        optionalExpressionList();
        match(TokenType.CLOSING_PAREN);
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
        if (currentToken.getTokenType().equals(TokenType.COMMA)) {
            retrieveNextToken();
            expression();
            expressionList2();
        } else {
            //Empty produciton
        }
    }

    private void chainedVarMethod() throws LexicalException {
        match(TokenType.PERIOD);
        match(TokenType.METVARID);
        optionalActualArgs();
    }

    private void optionalActualArgs() throws LexicalException {
        if (first(NonTerminal.ACTUAL_ARGS).contains(currentToken.getTokenType())) {
            actualArgs();
        } else {
            //Empty production
        }
    }

    private void match(TokenType expectedTokenType) throws LexicalException {
        if (false) { //TODO
            panicMode = false;
            while (!(expectedTokenType.equals(currentToken.getTokenType())) && !currentToken.getTokenType().equals(TokenType.EOF)) {
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

    private CustomSet<TokenType> first(NonTerminal nonTerminal) {
        return switch (nonTerminal) {
            case CLASS_AND_INTERFACE_LIST2 ->
                    first(NonTerminal.CLASS_STATEMENT).appendAll(first(NonTerminal.INTERFACE_STATEMENT));
            case CLASS_STATEMENT -> new CustomHashSet<>(List.of(TokenType.CLASS_WORD));
            case INTERFACE_STATEMENT -> new CustomHashSet<>(List.of(TokenType.INTERFACE_WORD));
            case MODIFIER ->
                    new CustomHashSet<>(List.of(TokenType.ABSTRACT_WORD, TokenType.FINAL_WORD, TokenType.STATIC_WORD));
            case ATTRIBUTE_OR_METHOD, INTERFACE_MEMBER ->
                    first(NonTerminal.MODIFIER).appendAll(first(NonTerminal.TYPE)).append(TokenType.VOID_WORD);
            case PRIMITIVE_TYPE ->
                    new CustomHashSet<>(List.of(TokenType.BOOLEAN_WORD, TokenType.CHAR_WORD, TokenType.INT_WORD));
            case TYPE -> first(NonTerminal.PRIMITIVE_TYPE).append(TokenType.CLASSID);
            case FORMAL_ARGS, ACTUAL_ARGS -> new CustomHashSet<>(List.of(TokenType.OPENING_PAREN));
            case FORMAL_ARGS_AND_OPTIONAL_BLOCK -> first(NonTerminal.FORMAL_ARGS);
            case FORMAL_ARGS_LIST -> first(NonTerminal.FORMAL_ARG);
            case FORMAL_ARG -> first(NonTerminal.TYPE);
            case BLOCK -> new CustomHashSet<>(List.of(TokenType.OPENING_BRACE));
            case SENTENCE ->
                    first(NonTerminal.ASSIGNMENT_CALL_OR_LOCALVAR).appendAll(new CustomHashSet<>(List.of(TokenType.RETURN_WORD, TokenType.IF_WORD, TokenType.WHILE_WORD, TokenType.FOR_WORD, TokenType.SEMICOLON, TokenType.OPENING_BRACE)));
            case ASSIGNMENT_CALL_OR_LOCALVAR ->
                    first(NonTerminal.PRIMITIVE_TYPE).appendAll(first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).append(TokenType.VAR_WORD).append(TokenType.CLASSID));
            case EXPRESSION_WO_STATIC_METHOD_CALL, BASIC_EXPRESSION ->
                    first(NonTerminal.UNARY_OPERATOR).appendAll(first(NonTerminal.OPERAND));
            case UNARY_OPERATOR ->
                    new CustomHashSet<>(List.of(TokenType.PLUS, TokenType.MINUS, TokenType.PLUS1, TokenType.MINUS1, TokenType.EXCLAMATION_POINT));
            case OPERAND -> first(NonTerminal.PRIMITIVE).appendAll(first(NonTerminal.REFERENCE));
            case PRIMITIVE ->
                    new CustomHashSet<>(List.of(TokenType.INTLITERAL, TokenType.CHARLITERAL, TokenType.TRUE_WORD, TokenType.FALSE_WORD, TokenType.NULL_WORD));
            case REFERENCE ->
                    new CustomHashSet<>(List.of(TokenType.METVARID, TokenType.THIS_WORD, TokenType.STRINGLITERAL, TokenType.NEW_WORD, TokenType.OPENING_PAREN));
            case LOCAL_VAR_WITH_VAR -> new CustomHashSet<>(List.of(TokenType.VAR_WORD));
            case LOCAL_VAR_WITH_PRIMITIVE_TYPE -> first(NonTerminal.PRIMITIVE_TYPE);
            case EXPRESSION, COMPOUND_EXPRESSION ->
                    first(NonTerminal.BASIC_EXPRESSION).appendAll(first(NonTerminal.STATIC_METHOD_CALL));
            case STATIC_METHOD_CALL -> new CustomHashSet<>(List.of(TokenType.CLASSID));
            case ASSIGNMENT_OPERATOR -> new CustomHashSet<>(List.of(TokenType.EQUAL));
            case BINARY_OPERATOR ->
                    new CustomHashSet<>(List.of(TokenType.OR, TokenType.AND, TokenType.EQUALS_COMPARISON, TokenType.DIFERENT, TokenType.LESS_THAN, TokenType.GREATER_THAN, TokenType.EQUAL_LESS_THAN, TokenType.EQUAL_GREATER_THAN, TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.SLASH, TokenType.PERCENT));
            case CHAINED_VAR_METHOD -> new CustomHashSet<>(List.of(TokenType.PERIOD));
            case CONSTRUCTOR_CALL -> new CustomHashSet<>(List.of(TokenType.NEW_WORD));
            case EXPRESSION_LIST -> first(NonTerminal.EXPRESSION);
        };
    }
}