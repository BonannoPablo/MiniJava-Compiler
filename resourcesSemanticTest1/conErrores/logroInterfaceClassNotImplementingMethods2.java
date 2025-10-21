///[Error:B|7]
//
interface A {
    void f(int x);
}

class B implements A {
    void f() { }      // Error: signature mismatch — f(int) expected
}