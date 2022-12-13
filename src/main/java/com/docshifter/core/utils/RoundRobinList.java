package com.docshifter.core.utils;

import java.util.Iterator;
import java.util.List;

public class RoundRobinList<T> implements Iterable<T> {

    private final List<T> inputList;
    private int index = 0;

    public RoundRobinList(List<T> inputList) {
        this.inputList = inputList;
    }

    public Iterator<T> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                if (index >= inputList.size()) {
                    index = 0;
                }
                return inputList.get(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
