package utils;

import java.util.HashSet;
import java.util.Collection;
import java.util.Set;

public class CustomHashSet<E> extends HashSet<E> implements CustomSet<E> {
    public CustomHashSet(Collection<E> classWord) {
        super(classWord);
    }

    public CustomSet<E> append(E e) {
        this.add(e);
        return this;
    }
    public CustomSet<E> appendAll(Set<E> set) {
        this.addAll(set);
        return this;
    }

}
