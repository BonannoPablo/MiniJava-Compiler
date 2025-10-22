///[Error:f|7]
// 
class A {
    void f() { }
}
class B extends A {
    static void f() { }      // Error: cannot hide instance method with static method
}