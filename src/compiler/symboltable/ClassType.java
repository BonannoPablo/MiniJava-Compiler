package compiler.symboltable;

public class ClassType extends Type{
    String genericType;

    public ClassType(String name, String genericType) {
        super.name = name;
        this.genericType = genericType;
    }

    public String getGenericType() {
        return genericType;
    }
}
