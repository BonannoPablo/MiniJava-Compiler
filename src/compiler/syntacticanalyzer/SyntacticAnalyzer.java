package compiler.syntacticanalyzer;

import compiler.exceptions.LexicalException;
import compiler.lexicalanalyzer.ILexicalAnalyzer;
import compiler.token.IToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SyntacticAnalyzer{
    private Map<String, Set<IToken.TokenType>> firstMap;
    private final ILexicalAnalyzer lexicalAnalyzer;
    private IToken currentToken;

    public SyntacticAnalyzer(ILexicalAnalyzer lexicalAnalyzer) throws LexicalException {
        this.lexicalAnalyzer = lexicalAnalyzer;
        LoadFirstMap();
        retrieveNextToken();
    }

    public void start() throws LexicalException{
        classList();
        match(IToken.TokenType.EOF);
    }

    private void classList() throws LexicalException{
        if(firstMap.get("classList").contains(currentToken.getTokenType())){
            classNT();
            classList();
        } else {
            match(IToken.TokenType.EOF);
        }
    }

    private void classNT() throws LexicalException {
        optionalModifier();
        match(IToken.TokenType.CLASS_WORD);
        match(IToken.TokenType.CLASSID);
        optionalInheritance();
        match(IToken.TokenType.OPENING_BRACE);
        memberList();
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
                //TODO throw exception
        }
    }

    private void optionalModifier() throws LexicalException {
        if(firstMap.get("optionalModifier").contains(currentToken.getTokenType())){
            modifier();
        } else {
            //TODO throw exception
        }
    }

    private void optionalInheritance() throws LexicalException {
        if(currentToken.getTokenType().equals(IToken.TokenType.EXTENDS_WORD)) {
            retrieveNextToken();
            match(IToken.TokenType.CLASSID);
        }
    }

    private void memberList() throws LexicalException {
        if(firstMap.get("memberList").contains(currentToken.getTokenType())) {
            member();
            memberList();
        }
    }

    private void member() throws LexicalException {
        if(currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
          method();
        }
        else if(firstMap.get("type").contains(currentToken.getTokenType())) {
            type();
            match(IToken.TokenType.METVARID);
            attributeMethod();
        }
        else if(firstMap.get("modifier").contains(currentToken.getTokenType())) {
            modifier();
            methodType();
            method();
        } else if(firstMap.get("constructor").contains(currentToken.getTokenType())){
            constructor();
        }
    }

    private void attributeMethod() throws LexicalException {
        if(firstMap.get("formalArgs").contains(currentToken.getTokenType())) {
            formalArgs();
            optionalBlock();
        } else{
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void method() throws LexicalException {
        match(IToken.TokenType.METVARID);
        formalArgs();
        optionalBlock();
    }

    private void constructor() throws LexicalException {
        match(IToken.TokenType.PUBLIC_WORD);
        match(IToken.TokenType.CLASSID);
        formalArgs();
        block();
    }

    private void methodType() throws LexicalException {
        if(currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
        } else{
            type();
        }
    }

    private void type() throws LexicalException {
        if(currentToken.getTokenType().equals(IToken.TokenType.CLASSID)) {
            retrieveNextToken();
        } else {
            primitiveType();
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
                //TODO throw exception
        }
    }

    private void formalArgs() throws LexicalException {
        match(IToken.TokenType.OPENING_PAREN);
        optionalFormalArgsList();
        match(IToken.TokenType.CLOSING_PAREN);
    }

    private void optionalFormalArgsList() throws LexicalException {
        if(firstMap.get("formalArgsList").contains(currentToken.getTokenType())){
            formalArgsList();
        }
    }

    private void formalArgsList() throws LexicalException {
        formalArg();
        formalArgsList2();
    }

    private void formalArgsList2() throws LexicalException {
        if(firstMap.get("formalArg").contains(currentToken.getTokenType())){
            formalArg();
            formalArgsList2();
        }
    }

    private void formalArg() throws LexicalException {
        type();
        match(IToken.TokenType.METVARID);
    }

    private void optionalBlock() throws LexicalException {
        if(firstMap.get("block").contains(currentToken.getTokenType())){
            block();
        } else{
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void block() throws LexicalException {
        match(IToken.TokenType.OPENING_BRACE);
        sentenceList();
        match(IToken.TokenType.CLOSING_BRACE);
    }

    private void sentenceList() throws LexicalException {
        if(firstMap.get("sentence").contains(currentToken.getTokenType())){
            sentence();
        }
    }

    private void sentence() throws LexicalException {
        if(firstMap.get("block").contains(currentToken.getTokenType())){
            block();
        } else if(firstMap.get("whileSentence").contains(currentToken.getTokenType())){
            whileSentence();
        } else if(firstMap.get("ifSentence").contains(currentToken.getTokenType())){
            ifSentence();
        } else if(firstMap.get("returnSentence").contains(currentToken.getTokenType())){
            returnSentence();
            match(IToken.TokenType.SEMICOLON);
        } else if(firstMap.get("localVar").contains(currentToken.getTokenType())){
            localVar();
            match(IToken.TokenType.SEMICOLON);
        } else if(firstMap.get("assignmentCall").contains(currentToken.getTokenType())){
            assignmentCall();
            match(IToken.TokenType.SEMICOLON);
        } else {
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void assignmentCall() throws LexicalException {
        expression();
    }

    private void localVar() throws LexicalException {
        match(IToken.TokenType.VAR_WORD);
        match((IToken.TokenType.METVARID));
        match(IToken.TokenType.EQUAL);
        compoundExpression();
    }

    private void returnSentence() throws LexicalException {
        match(IToken.TokenType.RETURN_WORD);
        optionalExpression();
    }

    private void optionalExpression() throws LexicalException {
        if(firstMap.get("expression").contains(currentToken.getTokenType())){
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
        if(currentToken.getTokenType().equals(IToken.TokenType.ELSE_WORD)){
            retrieveNextToken();
            sentence();
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
        assignmentExpression();
    }

    private void assignmentExpression() throws LexicalException {
        if(firstMap.get("assignmentOperator").contains(currentToken.getTokenType())){
            assignmentOperator();
            compoundExpression();
        }
    }

    private void assignmentOperator() throws LexicalException {
        match(IToken.TokenType.EQUAL);
    }

    private void compoundExpression() throws LexicalException {
        basicExpression();
        binaryOperator();
        basicExpression();
        compoundExpression2();
    }

    private void compoundExpression2() throws LexicalException {
        if(firstMap.get("binaryOperator").contains(currentToken.getTokenType())){
            binaryOperator();
            basicExpression();
            compoundExpression2();
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
                //TODO throw exception
        }
    }

    private void basicExpression() throws LexicalException {
        if(firstMap.get("unaryOperator").contains(currentToken.getTokenType())){
            unaryOperator();
            operand();
        } else {
            operand();
        }
    }

    private void unaryOperator() throws LexicalException {
        switch (currentToken.getTokenType()){
            case IToken.TokenType.PLUS:
            case IToken.TokenType.PLUS1:
            case IToken.TokenType.MINUS:
            case IToken.TokenType.MINUS1:
            case IToken.TokenType.EXCLAMATION_POINT:
                retrieveNextToken();
                break;
            default:
                //TODO throw exception
        }
    }

    private void operand() throws LexicalException {
        if (firstMap.get("primitive").contains(currentToken.getTokenType())) {
            primitive();
        } else{
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
                //TODO throw exception
        }
    }

    private void reference() throws LexicalException {
        primary();
        reference2();
    }

    private void reference2() throws LexicalException {
        if(firstMap.get("chainedVar").contains(currentToken.getTokenType())){
            chainedVar();
            reference2();
        } else if(firstMap.get("chainedMethod").contains(currentToken.getTokenType())){
            chainedMethod();
            reference2();
        }
    }

    private void primary() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.THIS_WORD:
            case IToken.TokenType.STRINGLITERAL:
                retrieveNextToken();
                break;
            case IToken.TokenType.METVARID:
                retrieveNextToken();
                varAccessMethodCall();
                break;
            default:
                if(firstMap.get("constructorCall").contains(currentToken.getTokenType())){
                    constructorCall();
                } else if(firstMap.get("staticMethodCall").contains(currentToken.getTokenType())){
                    staticMethodCall();
                } else {
                    parenthesizedExpression();
                }
        }
    }

    private void varAccessMethodCall() throws LexicalException {
        if(firstMap.get("actualArgs").contains(currentToken.getTokenType())){
            actualArgs();
        }
    }

    private void constructorCall() throws LexicalException {
        match(IToken.TokenType.NEW_WORD);
        match(IToken.TokenType.CLASSID);
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
        if(firstMap.get("expressionList").contains(currentToken.getTokenType())){
            expressionList();
        }
    }

    private void expressionList() throws LexicalException {
        expression();
        expressionList2();
    }

    private void expressionList2() throws LexicalException {
        if(currentToken.getTokenType().equals(IToken.TokenType.COMMA)){
            retrieveNextToken();
            expression();
            expressionList2();
        }
    }

    private void chainedVar() throws LexicalException {
        match(IToken.TokenType.PERIOD);
        match(IToken.TokenType.METVARID);
    }

    private void chainedMethod() throws LexicalException {
        match(IToken.TokenType.PERIOD);
        match(IToken.TokenType.METVARID);
        actualArgs();
    }

    private void match(IToken.TokenType tokenType) throws LexicalException{
        if (tokenType.equals(currentToken.getTokenType())) {
            retrieveNextToken();
        } else {
            //TODO throw exception
        }
    }

    private void retrieveNextToken()throws LexicalException{
        currentToken = lexicalAnalyzer.nextToken();
    }

    private void LoadFirstMap() {
        firstMap = new HashMap<>();

    }


}
