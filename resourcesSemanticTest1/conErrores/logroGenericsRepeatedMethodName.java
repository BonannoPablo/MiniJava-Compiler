///[Error:get|5]
//
class A<T> { T get(){} }
class B extends A<String> { 
    int get() { }      
}