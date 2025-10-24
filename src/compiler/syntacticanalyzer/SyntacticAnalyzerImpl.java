package compiler.syntacticanalyzer;

import compiler.ast.*;
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
            default -> new TokenImpl(Token.TokenType.PUBLIC_WORD, "public", -1);
            //Empty production
        };
    }

    private void optionalInheritance() throws LexicalException, SemanticException {
        switch (currentToken.getTokenType()) {
            case Token.TokenType.EXTENDS_WORD:
                retrieveNextToken();
                var parentClass = currentToken;
                match(Token.TokenType.CLASSID);
                symbolTable.getCurrentClass().setParent(parentClass);
                symbolTable.getCurrentClass().setParentGenericType(optionalGenerics());
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
            symbolTable.getCurrentClass().setCurrentMethod(methodEntry);
            boolean modifierIsAbstract = (modifier != null && modifier.getTokenType() == Token.TokenType.ABSTRACT_WORD);
            var block = formalArgsAndOptionalBlock();
            if(modifierIsAbstract && block != null)
                throw new SemanticException("Abstract method can't have a body", metVarId);
            if(!modifierIsAbstract && block == null)
                throw new SemanticException("Method body expected", metVarId);
            methodEntry.addBlock(block);
            symbolTable.getCurrentClass().addMethod(methodEntry);
        } else if (currentToken.getTokenType().equals(Token.TokenType.CLASSID)) {
            var classIdToken = currentToken;
            match(Token.TokenType.CLASSID);
            constructorOrMember(classIdToken, visibility);
        } else if (currentToken.getTokenType().equals(Token.TokenType.VOID_WORD)) {
            Token voidWord = currentToken;
            match(Token.TokenType.VOID_WORD);
            Token metVarIdToken = currentToken;
            match(Token.TokenType.METVARID);
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(new PrimitiveType(voidWord));
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentClass().setCurrentMethod(methodEntry);
            var block = formalArgsAndOptionalBlock();
            if(block == null)
                throw new SemanticException("Method body expected", metVarIdToken);
            methodEntry.addBlock(block);
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
            symbolTable.getCurrentInterface().setCurrentMethod(methodEntry);
            boolean modifierIsStatic = (modifier != null && modifier.getTokenType() == Token.TokenType.STATIC_WORD);
            var block = formalArgsAndOptionalBlock();
            if(block != null && !modifierIsStatic)
                throw new SemanticException("Interface abstract methods cannot have body", metVarIdToken);
            if(block == null && modifierIsStatic)
                throw new SemanticException("Interface static methods must have body", metVarIdToken);
            methodEntry.addBlock(block);
            symbolTable.getCurrentInterface().addMethod(methodEntry);
        } else if (currentToken.getTokenType().equals(Token.TokenType.VOID_WORD)) {
            Token voidWord = currentToken;
            match(Token.TokenType.VOID_WORD);
            Token metVarIdToken = currentToken;
            match(Token.TokenType.METVARID);

            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(new PrimitiveType(voidWord));
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentInterface().setCurrentMethod(methodEntry);
            var block = formalArgsAndOptionalBlock();
            if(block != null)
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
        if (currentToken.getTokenType().equals(Token.TokenType.METVARID) || currentToken.getTokenType().equals(Token.TokenType.LESS_THAN)) {
            Type type = new ClassType(classIdToken, optionalGenerics());
            var metVarIdToken = currentToken;
            match(Token.TokenType.METVARID);
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
            var block = formalArgsAndOptionalBlock();
            if(block == null)
                throw new SemanticException("Method body expected", metVarIdToken);         //TODO check this}
            methodEntry.addBlock(block);
            symbolTable.getCurrentClass().addMethod(methodEntry);
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
            var exp = compoundExpression();
            optionalTernaryOperator(exp);
            match(Token.TokenType.SEMICOLON);

            AttributeEntry attributeEntry = new AttributeEntry(metVarIdToken, type);
            attributeEntry.setVisibility(visibility);
            symbolTable.getCurrentInterface().addAttribute(attributeEntry);
        } else if(first(NonTerminal.FORMAL_ARGS_AND_OPTIONAL_BLOCK).contains(currentToken.getTokenType())) {
            MethodEntry methodEntry = new MethodEntry(metVarIdToken);
            methodEntry.setReturnType(type);
            methodEntry.setVisibility(visibility);
            symbolTable.getCurrentInterface().setCurrentMethod(methodEntry);
            var block = formalArgsAndOptionalBlock();
            if(block == null)
                throw new SemanticException("Interface abstract methods cannot have body", metVarIdToken);
            symbolTable.getCurrentInterface().addMethod(methodEntry);
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an assignment or a method declaration"));
                recovery();
            }
        }
    }

    private BlockNode formalArgsAndOptionalBlock() throws LexicalException, SemanticException {
        formalArgs();
        return optionalBlock();
    }

    private void optionalAssignment() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.EQUAL)) {
            retrieveNextToken();
            var exp = compoundExpression();
            optionalTernaryOperator(exp);
        } else {
            //Empty production
        }
    }

    private Type methodType() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.VOID_WORD)) {
            Token voidWord = currentToken;
            match(Token.TokenType.VOID_WORD);
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

    private void formalArgs() throws LexicalException, SemanticException {
        match(Token.TokenType.OPENING_PAREN);
        optionalFormalArgsList();
        match(Token.TokenType.CLOSING_PAREN);
    }

    private void optionalFormalArgsList() throws LexicalException, SemanticException {
        if (first(NonTerminal.FORMAL_ARGS_LIST).contains(currentToken.getTokenType())) {
            formalArgsList();
        } else {
            //Empty production
        }
    }

    private void formalArgsList() throws LexicalException, SemanticException {
        symbolTable.getCurrentClassOrInterface().getCurrentMethod().addParameter(formalArg());
        formalArgsList2();
    }

    private void formalArgsList2() throws LexicalException, SemanticException {
        if (currentToken.getTokenType().equals(Token.TokenType.COMMA)) {
            match(Token.TokenType.COMMA);
            symbolTable.getCurrentClassOrInterface().getCurrentMethod().addParameter(formalArg());
            formalArgsList2();
        } else {
            //Empty production
        }
    }

    private ParameterEntry formalArg() throws LexicalException {
        Type t = type();
        Token metVarIdToken = currentToken;
        match(Token.TokenType.METVARID);
        return new ParameterEntry(t, metVarIdToken);
    }

    private BlockNode optionalBlock() throws LexicalException {
        if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            return block();
        } else {
            match(Token.TokenType.SEMICOLON);
            return null;
        }
    }

    private BlockNode block() throws LexicalException {
        BlockNode block = new BlockNode();

        match(Token.TokenType.OPENING_BRACE);
        sentenceList(block);
        match(Token.TokenType.CLOSING_BRACE);
        return block;
    }

    private void sentenceList(BlockNode block) throws LexicalException {
        if (first(NonTerminal.SENTENCE).contains(currentToken.getTokenType())) {
            block.addSentence(sentence());
            sentenceList(block);
        }
    }

    private SentenceNode sentence() throws LexicalException {
      /*  if (currentToken.getTokenType().equals(Token.TokenType.FOR_WORD)) {
            forSentence();
        } else */if (first(NonTerminal.BLOCK).contains(currentToken.getTokenType())) {
            return block();
        } else if (currentToken.getTokenType().equals(Token.TokenType.WHILE_WORD)) {
            return whileSentence();
        } else if (currentToken.getTokenType().equals(Token.TokenType.IF_WORD)) {
            return ifSentence();
        } else if (currentToken.getTokenType().equals(Token.TokenType.RETURN_WORD)) {
            var returnNode = returnSentence();
            match(Token.TokenType.SEMICOLON);
            return returnNode;
        } else if (first(NonTerminal.ASSIGNMENT_CALL_OR_LOCALVAR).contains(currentToken.getTokenType())) {
            var node = assignmentCallOrLocalVar();
            match(Token.TokenType.SEMICOLON);
            return node;
        } else {
            match(Token.TokenType.SEMICOLON);
        }
        return null;
    }

    private SentenceNode assignmentCallOrLocalVar() throws LexicalException {
        if (first(NonTerminal.LOCAL_VAR_WITH_VAR).contains(currentToken.getTokenType())) {
            return localVarWithVar();
        } else if (first(NonTerminal.LOCAL_VAR_WITH_PRIMITIVE_TYPE).contains(currentToken.getTokenType())) {
            localVarWithPrimitiveType(); //TODO implement classic variable declaration in AST
        } else if (first(NonTerminal.EXPRESSION_WO_STATIC_METHOD_CALL).contains(currentToken.getTokenType())) {
            return expressionWOStaticMethodCall();
        } else if (currentToken.getTokenType().equals(Token.TokenType.CLASSID)) {
            var classIdToken = currentToken;
            match(Token.TokenType.CLASSID);
            return staticMethodOrLocalVar(classIdToken);
        } else {
            if (!panicMode) {
                exceptions.add(new SyntacticException(currentToken, "an assignment, call or local variable declaration"));
                recovery();
            }
        }
        return null;
    }

    private SentenceNode staticMethodOrLocalVar(Token classIdtoken) throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.PERIOD)) {
            var staticMethodCall = new StaticCallExpressionNode(classIdtoken);
            match(Token.TokenType.PERIOD);
            var methodToken = currentToken;
            match(Token.TokenType.METVARID);
            staticMethodCall.addMethodCalled(methodToken);
            staticMethodCall.addArguments(actualArgs());
            staticMethodCall.addChain(reference2());     //TODO finish this. Add chained calls to static method call
            //compoundExpression2();     //ASK if I can remove this
            var exp = expression2();
            var assignmentExp = optionalTernaryOperator(exp);

            if(exp != null){ //If this is an assignment
                var assignment = new AssignmentNode();
                assignment.addLeftSide(staticMethodCall);
                assignment.addRightSide(assignmentExp);
                return assignment;
            } else{ //If this is a call
                //TODO throw exception if assignmentExp != null (Ternary operator appllied over staitc call, not a statement)
                return new StaticCallSentenceNode(staticMethodCall);
            }


        } else { //TODO implement classic variable declaration in AST
            optionalGenerics();
            match(Token.TokenType.METVARID);
            multipleDeclaration();
            optionalAssignment();
        }
        return null;
    }

    private AssignmentNode expressionWOStaticMethodCall() throws LexicalException {
        var assignment = new AssignmentNode();
        assignment.addLeftSide(compoundExpressionWOStaticMethodCall());
        var exp = expression2();
        var rightSide = optionalTernaryOperator(exp);
        assignment.addRightSide(rightSide);
        return assignment;
    }

    private ExpressionNode compoundExpressionWOStaticMethodCall() throws LexicalException {
        basicExpression();
        compoundExpression2();
        return new MockExpressionNode(); //TODO
    }

    private VarInitNode localVarWithVar() throws LexicalException {
        var varInitNode = new VarInitNode();
        match(Token.TokenType.VAR_WORD);
        Token token = currentToken;
        match((Token.TokenType.METVARID));
        varInitNode.addToken(token);
        match(Token.TokenType.EQUAL);
        var exp = compoundExpression();
        var ternaryExp = optionalTernaryOperator(exp);
        varInitNode.addExpression(ternaryExp);
        return varInitNode;
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

    private ReturnNode returnSentence() throws LexicalException {
        var node = new ReturnNode();
        match(Token.TokenType.RETURN_WORD);
        node.addReturnExpression(optionalExpression());
        return node;
    }

    private ExpressionNode optionalExpression() throws LexicalException {
        if (first(NonTerminal.EXPRESSION).contains(currentToken.getTokenType())) {
            expression();
        }
        return new MockExpressionNode(); //TODO
    }

    private IfNode ifSentence() throws LexicalException {
        IfNode node = new IfNode();
        match(Token.TokenType.IF_WORD);
        match(Token.TokenType.OPENING_PAREN);
        node.addCondition(expression());
        match(Token.TokenType.CLOSING_PAREN);
        node.addThenSentence(sentence());
        node.addElseSentence(elseSentence());
        return node;
    }

    private SentenceNode elseSentence() throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.ELSE_WORD)) {
            retrieveNextToken();
            return sentence();
        } else {
            //Empty production
        }
        return null;
    }

    private WhileNode whileSentence() throws LexicalException {
        var node = new WhileNode();
        match(Token.TokenType.WHILE_WORD);
        match(Token.TokenType.OPENING_PAREN);
        node.addCondition(expression());
        match(Token.TokenType.CLOSING_PAREN);
        node.addSentence(sentence());
        return node;
    }

    private ExpressionNode expression() throws LexicalException {
        compoundExpression();
        var exp = expression2();
        optionalTernaryOperator(exp);
        return new MockExpressionNode(); //TODO
    }

    private ExpressionNode expression2() throws LexicalException {
        if (first(NonTerminal.ASSIGNMENT_OPERATOR).contains(currentToken.getTokenType())) {
            assignmentOperator();
            compoundExpression();
        } else {
            return null;
            //Empty production
        }
        return new MockExpressionNode();
    }

    private ExpressionNode optionalTernaryOperator(ExpressionNode condition) throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.QUESTION_MARK)) {
            match(Token.TokenType.QUESTION_MARK);
            var conditionalExpression = new ConditionalExpression();
            conditionalExpression.addCondition(condition);

            var trueExpression = compoundExpression();
            var ternaryTrueExp = optionalTernaryOperator(trueExpression);
            conditionalExpression.addTrueExpression(ternaryTrueExp);

            match(Token.TokenType.COLON);

            var falseExpression = compoundExpression();
            var ternaryFalseExp = optionalTernaryOperator(falseExpression);
            conditionalExpression.addFalseExpression(ternaryFalseExp);

            return conditionalExpression;
        } else {
            return condition;
            //Empty produciton
        }
    }

    private void assignmentOperator() throws LexicalException {
        match(Token.TokenType.EQUAL);
    }

    private ExpressionNode compoundExpression() throws LexicalException {
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
        return new MockExpressionNode(); //TODO
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

    private Chained reference2() throws LexicalException {
        if (first(NonTerminal.CHAINED_VAR_METHOD).contains(currentToken.getTokenType())) {
            var chain = chainedVarMethod();
            reference2();
            //chain.addChain(reference2()); //TODO reference2() should be called like this. chanedVarMethod return null atm, when the method is finished I should change this call
            return chain;
        } else {
            return null;
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

    private List<ExpressionNode> actualArgs() throws LexicalException {
        match(Token.TokenType.OPENING_PAREN);
        var expressionList = optionalExpressionList();
        match(Token.TokenType.CLOSING_PAREN);
        return expressionList;
    }

    private List<ExpressionNode> optionalExpressionList() throws LexicalException {
        if (first(NonTerminal.EXPRESSION_LIST).contains(currentToken.getTokenType())) {
            return expressionList();
        } else {
            return new ArrayList<>();
            //Empty production
        }
    }

    private List<ExpressionNode> expressionList() throws LexicalException {
        var expressionList = new LinkedList<ExpressionNode>();
        expressionList.addLast(expression());
        expressionList2(expressionList);
        return expressionList;
    }

    private List<ExpressionNode> expressionList2(List<ExpressionNode> expressionList) throws LexicalException {
        if (currentToken.getTokenType().equals(Token.TokenType.COMMA)) {
            retrieveNextToken();
            expressionList.addLast(expression());
            return expressionList2(expressionList);
        } else {
            return expressionList;
            //Empty produciton
        }
    }

    private Chained chainedVarMethod() throws LexicalException {
        match(Token.TokenType.PERIOD);
        match(Token.TokenType.METVARID);
        optionalActualArgs();
        return null; //TODO implement chained methods and attributes
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