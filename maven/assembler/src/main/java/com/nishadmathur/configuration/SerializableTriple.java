package com.nishadmathur.configuration;

import kotlin.Triple;

import java.io.Serializable;

/**
 * User: nishad
 * Date: 16/10/2015
 * Time: 17:55
 */
public class SerializableTriple<T, U, V> implements Serializable {
    private T first;
    private U second;
    private V third;

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

    public V getThird() {
        return third;
    }

    public void setThird(V third) {
        this.third = third;
    }

    public Triple<T, U, V> getTriple() {
        return new Triple<T, U, V>(first, second, third);
    }
}
