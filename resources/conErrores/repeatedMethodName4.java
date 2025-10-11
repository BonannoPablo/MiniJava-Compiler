///[Error:f|7]
//
class A {
    void f(int x, int y) { }
}
class B extends A {
    void f(int x) { }     // Error: not a valid override (parameters differ) (puede que este bien)
}