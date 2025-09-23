package utils;

import java.util.Set;

public interface customSet <E> extends Set<E> {
    public customSet<E> append(E e);
    public customSet<E> appendAll(Set<E> set);
}
