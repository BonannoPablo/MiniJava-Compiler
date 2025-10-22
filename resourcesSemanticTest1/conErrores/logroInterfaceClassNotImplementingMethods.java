///[Error:B|7]
//
interface A {
    void f();
}

class B implements A {
    // Error: must implement method f()
}