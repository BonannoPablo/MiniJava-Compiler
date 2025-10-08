package compiler.symboltable;

import compiler.token.Token;

public class AttributeEntry {
    private final Token token;
    String name;
    Type type;
    Token visibility;

    public AttributeEntry(Token token, Type type) {
        this.token = token;
        this.name = token.getLexeme();
        this.type = type;
    }

    public void setVisibility(Token visibility) {
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }
}
