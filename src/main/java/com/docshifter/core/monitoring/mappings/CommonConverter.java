package com.docshifter.core.monitoring.mappings;

import com.aspose.slides.internal.og.add;
import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.KeyValuePair;
import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.entities.AbstractConfigurationItem;
import com.docshifter.core.monitoring.entities.MailConfigurationItem;
import com.docshifter.core.monitoring.entities.MonitoringFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                .collect(Collectors.toMap(KeyValuePair::getKey, KeyValuePair::getValue));
    }

    public static void convertFilterToEntity(AbstractConfigurationItemDto dto, AbstractConfigurationItem entity) {
        if (!dto.getSnippets().isBlank()) {
            if (entity.getMonitoringFilter() == null) {
                entity.setMonitoringFilter(MonitoringFilter.builder()
                        .operator(dto.getOperator())
                        .snippets(dto.getSnippets())
                        .snippetsCombination(dto.getSnippetsCombination())
                        .configurationItem(entity)
                        .build());
            } else {
                entity.setMonitoringFilter(MonitoringFilter.builder()
                        .operator(dto.getOperator())
                        .snippets(dto.getSnippets())
                        .id(entity.getMonitoringFilter().getId())
                        .snippetsCombination(dto.getSnippetsCombination())
                        .configurationItem(entity)
                        .build());
            }
        }
    }
}
