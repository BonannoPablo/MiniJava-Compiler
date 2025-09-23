package utils;

import java.util.HashSet;
import java.util.Set;

public class customHashSet<E> extends HashSet<E> implements customSet<E>{
    public customSet<E> append(E e) {
        this.add(e);
        return this;
    }
    public customSet<E> appendAll(Set<E> set) {
        this.addAll(set);
        return this;
    }

}
