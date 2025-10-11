///[SinErrores]
class Box<T> {
    T value;
    void set(T v) { value = v; }
    T get() { return value; }
}


class StringBox extends Box<String> { }


class Container<T> extends Box<T> {
    void print() { System.println(get()); }
}

class Holder<T> {
    T item;
    void put(T i) { item = i; }
    T take() { return item; }
}

class Pair<T> {
    T first;
    T second;
    void setBoth(T a, T b) { first = a; second = b; }
    T getFirst() { return first; }
}

class IntPair extends Pair<String> { }

class Wrapper<T> {
    Box<T> inner;
    Wrapper(Box<T> b) { inner = b; }
    T unwrap() { return inner.get(); }
}

class AdvancedContainer<T> extends Container<T> {
    void reset(T v) { set(v); }
}

class Example {
    void test() {
        Box<Double> b = new Box<Double>();
        b.set(3);
        Double d = b.get();
    }
}

class GenericPoint<T> {
    T x;
    T y;
    GenericPoint(T a, T b) { x = a; y = b; }
}