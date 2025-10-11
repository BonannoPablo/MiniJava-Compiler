///[Error:A|4]
// 
class A {
    void f(int x) { }
}
class B extends A {
    void f() { }             // Error: does not override A.f(int)
}