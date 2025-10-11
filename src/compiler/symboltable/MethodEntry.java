package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

import java.util.*;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class MethodEntry{
    String name;
    Token token;
    Type returnType;
    Token modifier;
    Token visibility;
    List<ParameterEntry> parameters;
    Set<String> parametersNames;


    public MethodEntry(Token token){
        this.token = token;
        name = token.getLexeme();
        visibility = new TokenImpl(Token.TokenType.PUBLIC_WORD, "public", -1);
        parameters = new LinkedList<>();
        parametersNames = new HashSet<>();
    }

    public void setReturnType(Type type) {
        returnType = type;
    }

    public void setModifier(Token modifier) throws SemanticException {
        if(modifier != null && modifier.getTokenType() == Token.TokenType.ABSTRACT_WORD
         && symbolTable.getCurrentClassOrInterface() instanceof ClassEntry
         && symbolTable.getCurrentClassOrInterface().getModifier() != Token.TokenType.ABSTRACT_WORD)
            throw new SemanticException("Cannot have abstract method in non abstract class", token);
        this.modifier = modifier;
    }

    public void setVisibility(Token visibility) {
        this.visibility = visibility;
    }


    public String getName() {
        return name;
    }


    public void checkDeclaration() throws SemanticException {
        Token genericType = symbolTable.getCurrentClass().getGenericType();
        if(returnType.getToken().getTokenType() == Token.TokenType.CLASSID
            && ! (symbolTable.existsClass(returnType.getName()) || (genericType != null &&  returnType.getName().equals(genericType.getLexeme())))){
            throw new SemanticException("Class does not exist", returnType.getToken());//TODO change msg
        }

        for(ParameterEntry parameter : parameters){
            parameter.checkDeclaration();
        }

    }
    public void checkConstructionDeclaration() throws SemanticException {
        if(!name.equals(symbolTable.getCurrentClass().getName()))
            throw new SemanticException("Constructor should be named after class name", token);

        for(ParameterEntry parameter : parameters){
            parameter.checkDeclaration();
        }
    }

    public String print() {
        String modifierName = modifier == null ? "none" : modifier.getLexeme();
        String returnTypeString = returnType == null ? "none" : returnType.getName();
        StringBuilder s = new StringBuilder(name + " : " + returnTypeString + " " + modifierName + " " + visibility.getLexeme() + "(");
        for(ParameterEntry parameter : parameters){
            s.append(parameter.type.getName()).append(" ").append(parameter.name) .append(", ");
        }
        s.append(")");
        return s.toString();
    }

    public Token getToken() {
        return token;
    }

    public boolean matchSignatures(MethodEntry m) {
        Token[] genericTypeMap = symbolTable.getCurrentClass().getGenericTypeMap();
        boolean match = returnType.getName().equals(m.getReturnType().getName()) ||
                (genericTypeMap != null && returnType.getName().equals(genericTypeMap[0].getLexeme())               //TODO refactor this pls omg
                && m.getReturnType().getName().equals(genericTypeMap[1].getLexeme()) );
        List<ParameterEntry> otherParameters = m.getParameters();
        if(parameters.size() == otherParameters.size()) {
            for (int i = 0; i < parameters.size() && match; i++)
                match = parameters.get(i).getType().getName().equals(otherParameters.get(i).getType().getName())
                        || (genericTypeMap != null && parameters.get(i).getType().getName().equals(genericTypeMap[0].getLexeme())
                && otherParameters.get(i).getType().getName().equals(genericTypeMap[1].getLexeme()));
            return match;
        }else{
            return false;
        }
    }

    public List<ParameterEntry> getParameters() {
        return parameters;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Token getModifier() {
        return modifier;
    }

    public void addParameter(ParameterEntry parameterEntry) throws SemanticException {
        if(!parametersNames.add(parameterEntry.getName()))
            throw new SemanticException("Duplicate parameter name", parameterEntry.getToken());
        parameters.addLast(parameterEntry);
    }
}