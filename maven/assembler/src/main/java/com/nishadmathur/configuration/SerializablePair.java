package com.nishadmathur.configuration;

import kotlin.Pair;

import java.io.Serializable;

/**
 * User: nishad
 * Date: 16/10/2015
 * Time: 18:05
 */
public class SerializablePair<T, U> implements Serializable {
    private T first;
    private U second;

    public T getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public U getSecond() {
        return second;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    public Pair<T, U> getPair() {
        return new Pair<T, U>(first, second);
    }
}
