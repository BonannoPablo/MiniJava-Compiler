package compiler.syntacticanalyzer;

import compiler.exceptions.LexicalException;
import compiler.exceptions.SyntacticException;
import compiler.lexicalanalyzer.ILexicalAnalyzer;
import compiler.token.IToken;

import java.util.*;

public class SyntacticAnalyzer{
    private Map<String, Set<IToken.TokenType>> firstMap;
    private final ILexicalAnalyzer lexicalAnalyzer;
    private IToken currentToken;

    public SyntacticAnalyzer(ILexicalAnalyzer lexicalAnalyzer){
        this.lexicalAnalyzer = lexicalAnalyzer;
        LoadFirstMap();
    }

    public void start() throws LexicalException, SyntacticException {
        retrieveNextToken();
        if (first("classList").contains(currentToken.getTokenType())) {
            classList();
        }
        match(IToken.TokenType.EOF);
    }

    private void classList() throws LexicalException, SyntacticException {
        if(first("classStatement").contains(currentToken.getTokenType())){
            classStatement();
            classList();
        } else {
            //Empty production
        }
    }

    private void classStatement() throws LexicalException, SyntacticException {
        optionalModifier();
        match(IToken.TokenType.CLASS_WORD);
        match(IToken.TokenType.CLASSID);
        optionalInheritance();
        match(IToken.TokenType.OPENING_BRACE);
        memberList();
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
        if(first("optionalModifier").contains(currentToken.getTokenType())){
            modifier();
        } else {
            //Empty production
        }
    }

    private void optionalInheritance() throws LexicalException, SyntacticException {
        if(currentToken.getTokenType().equals(IToken.TokenType.EXTENDS_WORD)) {
            retrieveNextToken();
            match(IToken.TokenType.CLASSID);
        }
    }

    private void memberList() throws LexicalException, SyntacticException {
        if(first("member").contains(currentToken.getTokenType())) {
            member();
            memberList();
        }
    }

    private void member() throws LexicalException, SyntacticException {
        if(currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
            method();
        }
        else if(first("type").contains(currentToken.getTokenType())) {
            type();
            match(IToken.TokenType.METVARID);
            attributeMethod();
        }
        else if(first("modifier").contains(currentToken.getTokenType())) {
            modifier();
            methodType();
            method();
        } else if(first("constructor").contains(currentToken.getTokenType())){
            constructor();
        }
    }

    private void attributeMethod() throws LexicalException, SyntacticException {
        if(first("formalArgs").contains(currentToken.getTokenType())) {
            formalArgs();
            optionalBlock();
        } else{
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void method() throws LexicalException, SyntacticException {
        match(IToken.TokenType.METVARID);
        formalArgs();
        optionalBlock();
    }

    private void constructor() throws LexicalException, SyntacticException {
        match(IToken.TokenType.PUBLIC_WORD);
        match(IToken.TokenType.CLASSID);
        formalArgs();
        block();
    }

    private void methodType() throws LexicalException, SyntacticException {
        if(currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
        } else{
            type();
        }
    }

    private void type() throws LexicalException, SyntacticException {
        if(currentToken.getTokenType().equals(IToken.TokenType.CLASSID)) {
            retrieveNextToken();
        } else {
            primitiveType();
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
        if(first("formalArgsList").contains(currentToken.getTokenType())){
            formalArgsList();
        }
    }

    private void formalArgsList() throws LexicalException, SyntacticException {
        formalArg();
        formalArgsList2();
    }

    private void formalArgsList2() throws LexicalException, SyntacticException {
        if(currentToken.getTokenType().equals(IToken.TokenType.COMMA)){
            retrieveNextToken();
            formalArg();
            formalArgsList2();
        }
    }

    private void formalArg() throws LexicalException, SyntacticException {
        type();
        match(IToken.TokenType.METVARID);
    }

    private void optionalBlock() throws LexicalException, SyntacticException {
        if(first("block").contains(currentToken.getTokenType())){
            block();
        } else{
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void block() throws LexicalException, SyntacticException {
        match(IToken.TokenType.OPENING_BRACE);
        sentenceList();
        match(IToken.TokenType.CLOSING_BRACE);
    }

    private void sentenceList() throws LexicalException, SyntacticException {
        if(first("sentence").contains(currentToken.getTokenType())){
            sentence();
            sentenceList();
        }
    }

    private void sentence() throws LexicalException, SyntacticException {
        if(first("block").contains(currentToken.getTokenType())){
            block();
        } else if(first("whileSentence").contains(currentToken.getTokenType())){
            whileSentence();
        } else if(first("ifSentence").contains(currentToken.getTokenType())){
            ifSentence();
        } else if(first("returnSentence").contains(currentToken.getTokenType())){
            returnSentence();
            match(IToken.TokenType.SEMICOLON);
        } else if(first("localVar").contains(currentToken.getTokenType())){
            localVar();
            match(IToken.TokenType.SEMICOLON);
        } else if(first("assignmentCall").contains(currentToken.getTokenType())){
            assignmentCall();
            match(IToken.TokenType.SEMICOLON);
        } else {
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void assignmentCall() throws LexicalException, SyntacticException {
        expression();
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
        if(first("expression").contains(currentToken.getTokenType())){
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
        if(currentToken.getTokenType().equals(IToken.TokenType.ELSE_WORD)){
            retrieveNextToken();
            sentence();
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
        assignmentExpression();
    }

    private void assignmentExpression() throws LexicalException, SyntacticException {
        if(first("assignmentOperator").contains(currentToken.getTokenType())){
            assignmentOperator();
            compoundExpression();
        }
    }

    private void assignmentOperator() throws LexicalException, SyntacticException {
        match(IToken.TokenType.EQUAL);
    }

    private void compoundExpression() throws LexicalException, SyntacticException {
        basicExpression();
        compoundExpression2();
    }

    private void compoundExpression2() throws LexicalException, SyntacticException {
        if(first("binaryOperator").contains(currentToken.getTokenType())){
            binaryOperator();
            basicExpression();
            compoundExpression2();
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
        if(first("unaryOperator").contains(currentToken.getTokenType())){
            unaryOperator();
            operand();
        } else {
            operand();
        }
    }

    private void unaryOperator() throws LexicalException, SyntacticException {
        switch (currentToken.getTokenType()){
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
        if (first("primitive").contains(currentToken.getTokenType())) {
            primitive();
        } else{
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
        if(first("chainedVarMethod").contains(currentToken.getTokenType())){
            chainedVarMethod();
            reference2();
        }
    }

    private void primary() throws LexicalException, SyntacticException {
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
                if(first("constructorCall").contains(currentToken.getTokenType())){
                    constructorCall();
                } else if(first("staticMethodCall").contains(currentToken.getTokenType())){
                    staticMethodCall();
                } else {
                    parenthesizedExpression();
                }
        }
    }

    private void varAccessMethodCall() throws LexicalException, SyntacticException {
        if(first("actualArgs").contains(currentToken.getTokenType())){
            actualArgs();
        }
    }

    private void constructorCall() throws LexicalException, SyntacticException {
        match(IToken.TokenType.NEW_WORD);
        match(IToken.TokenType.CLASSID);
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
        if(first("expressionList").contains(currentToken.getTokenType())){
            expressionList();
        }
    }

    private void expressionList() throws LexicalException, SyntacticException {
        expression();
        expressionList2();
    }

    private void expressionList2() throws LexicalException, SyntacticException {
        if(currentToken.getTokenType().equals(IToken.TokenType.COMMA)){
            retrieveNextToken();
            expression();
            expressionList2();
        }
    }

    private void chainedVarMethod() throws LexicalException, SyntacticException {
        match(IToken.TokenType.PERIOD);
        match(IToken.TokenType.METVARID);
        optionalActualArgs();
    }

    private void optionalActualArgs() throws LexicalException, SyntacticException {
        if(first("actualArgs").contains(currentToken.getTokenType())) {
            actualArgs();
        } else{
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

    private void retrieveNextToken()throws LexicalException{
        currentToken = lexicalAnalyzer.nextToken();
    }

    private void LoadFirstMap() {
        firstMap = new HashMap<>();
    }

    private Set<IToken.TokenType> first(String productionName) {
        Set < IToken.TokenType> set = null;
        //set = first(productionName);
        if (set != null)
            return set;
        else {
            switch (productionName) {
                case "classList":
                    return first("classStatement");
                case "classStatement":
                    set = first("optionalModifier");
                    set.add((IToken.TokenType.CLASS_WORD));
                    return set;
                case "modifier":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.ABSTRACT_WORD, IToken.TokenType.FINAL_WORD, IToken.TokenType.STATIC_WORD));
                case "optionalModifier":
                    return first("modifier");
                case "optionalInheritance":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.EXTENDS_WORD));
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
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.METVARID));
                case "constructor":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.PUBLIC_WORD));
                case "methodType":
                    set = first("type");
                    set.add(IToken.TokenType.VOID_WORD);
                    return set;
                case "type":
                    set = first("primitiveType");
                    set.add(IToken.TokenType.CLASSID);
                    return set;
                case "primitiveType":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.INT_WORD, IToken.TokenType.BOOLEAN_WORD, IToken.TokenType.CHAR_WORD));
                case "formalArgs":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.OPENING_PAREN));
                case "optionalFormalArgsList":
                    return first("formalArgsList");
                case "formalArgsList":
                    return first("formalArg");
                case "formalArgsList2":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.COMMA));
                case "formalArg":
                    return first("type");
                case "optionalBlock":
                    set = first("block");
                    set.add(IToken.TokenType.SEMICOLON);
                    return set;
                case "block":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.OPENING_BRACE));
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
                case "assignmentCall":
                    return first("expression");
                case "localVar":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.VAR_WORD));
                case "returnSentence":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.RETURN_WORD));
                case "optionalExpression":
                    return first("expression");
                case "ifSentence":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.IF_WORD));
                case "elseSentence":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.ELSE_WORD));
                case "whileSentence":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.WHILE_WORD));
                case "expression":
                    return first("compoundExpression");
                case "assignmentExpression":
                    return first("assignmentOperator");
                case "assignmentOperator":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.EQUAL));
                case "compoundExpression":
                    return first("basicExpression");
                case "compoundExpression2":
                    return first("binaryOperator");
                case "binaryOperator":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.OR, IToken.TokenType.AND, IToken.TokenType.EQUALS_COMPARISON, IToken.TokenType.DIFERENT, IToken.TokenType.LESS_THAN,
                            IToken.TokenType.GREATER_THAN, IToken.TokenType.EQUAL_LESS_THAN, IToken.TokenType.EQUAL_GREATER_THAN, IToken.TokenType.PLUS, IToken.TokenType.MINUS,
                            IToken.TokenType.MULTIPLY, IToken.TokenType.SLASH, IToken.TokenType.PERCENT));
                case "basicExpression":
                    set = first("unaryOperator");
                    set.addAll(first("operand"));
                    return set;
                case "unaryOperator":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.PLUS, IToken.TokenType.PLUS1, IToken.TokenType.MINUS, IToken.TokenType.MINUS1, IToken.TokenType.EXCLAMATION_POINT));
                case "operand":
                    set = first("primitive");
                    set.addAll(first("reference"));
                    return set;
                case "primitive":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.INTLITERAL, IToken.TokenType.CHARLITERAL, IToken.TokenType.TRUE_WORD, IToken.TokenType.FALSE_WORD, IToken.TokenType.NULL_WORD));
                case "reference":
                    return first("primary");
                case "reference2":
                    return first("chainedVarMethod");
                case "primary":
                    set =  new HashSet<>(Arrays.asList(IToken.TokenType.THIS_WORD, IToken.TokenType.STRINGLITERAL, IToken.TokenType.METVARID));
                    set.addAll(first("staticMethodCall"));
                    set.addAll(first("constructorCall"));
                    set.addAll(first("parenthesizedExpression"));
                    return set;
                case "varAccessMethodCall":
                    return first("actualArgs");
                case "constructorCall":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.NEW_WORD));
                case "parenthesizedExpression":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.OPENING_PAREN));
                case "staticMethodCall":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.CLASSID));
                case "actualArgs":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.OPENING_PAREN));
                case "expressionList":
                    return first("expression");
                case "expressionList2":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.COMMA));
                case "chainedVarMethod":
                    return  new HashSet<>(Arrays.asList(IToken.TokenType.PERIOD));
                case "optionalActualArgs":
                    return first("actualArgs");
                default:
                    System.out.println("Error: production not found");
                    return null;
            }
        }
    }


}
