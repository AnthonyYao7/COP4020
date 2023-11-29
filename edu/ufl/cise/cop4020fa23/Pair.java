package edu.ufl.cise.cop4020fa23;

public class Pair<S, T> {
    private S first;
    private T second;

    public Pair(S s, T t) {
        first = s;
        second = t;
    }

    public S getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }
}
