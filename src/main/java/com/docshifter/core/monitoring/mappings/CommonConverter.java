package com.docshifter.core.monitoring.mappings;

import com.docshifter.core.monitoring.dtos.KeyValuePair;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CommonConverter {
    public static <T> List<T> convertToList(Collection<T> collection) {
        if (collection == null) {
            return null;
        }
        return new ArrayList<>(collection);
    }
    
    public static <T> Set<T> convertToSet(List<T> list) {
        if (list == null) {
            return null;
        }
        return new HashSet<>(list);
    }
    
    public static <TKey extends Comparable, TValue> List<KeyValuePair<TKey, TValue>> convertToKeyValuePairs(Map<TKey, TValue> map) {
        if (map == null) {
            return null;
        }
        return map
                .entrySet()
                .stream()
                .map(e -> new KeyValuePair<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
    
    public static <TKey extends Comparable, TValue> Map<TKey, TValue> convertToMap(List<KeyValuePair<TKey, TValue>> pairs) {
        if (pairs == null) {
            return null;
        }
        return pairs
                .stream()
                .filter(p -> p.getKey() != null)
                .sorted()
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }
}
