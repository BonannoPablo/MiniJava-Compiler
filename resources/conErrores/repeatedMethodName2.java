///[Error:f|7]
// 
class A {
    int f() { return 1; }
}
class B extends A {
    void f() { }             // Error: return type mismatch
}