package utils;

import java.util.Set;

public interface CustomSet<E> extends Set<E> {
    public CustomSet<E> append(E e);
    public CustomSet<E> appendAll(Set<E> set);
}
