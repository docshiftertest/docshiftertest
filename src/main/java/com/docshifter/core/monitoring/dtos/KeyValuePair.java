package com.docshifter.core.monitoring.dtos;

public class KeyValuePair<TKey extends Comparable, TValue>
        implements Comparable<com.docshifter.core.monitoring.dtos.KeyValuePair<TKey, TValue>> {
    private TKey key;
    private TValue value;

    public KeyValuePair() {
    }

    public KeyValuePair(TKey key, TValue value) {
        this.key = key;
        this.value = value;
    }

    public TKey getKey() {
        return key;
    }

    public void setKey(TKey key) {
        this.key = key;
    }

    public TValue getValue() {
        return value;
    }

    public void setValue(TValue value) {
        this.value = value;
    }

    //IDEA generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        com.docshifter.core.monitoring.dtos.KeyValuePair<?, ?> that = (com.docshifter.core.monitoring.dtos.KeyValuePair<?, ?>) o;

        if (!key.equals(that.key)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public int compareTo(com.docshifter.core.monitoring.dtos.KeyValuePair<TKey, TValue> o) {
        if (this.getKey() == null || o.getKey() == null) {
            return 0;
        }
        return this.getKey().compareTo(o.getKey());
    }
}
