///[Error:f|7]
// 
class A {
    static void f() { }
}
class B extends A {
    void f() { }             // Error: cannot override static method with instance method
}