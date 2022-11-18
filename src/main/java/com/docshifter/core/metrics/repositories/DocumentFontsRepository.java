package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.samples.DocumentFontsSample;
import com.docshifter.core.metrics.entities.DocumentFonts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DocumentFontsRepository extends JpaRepository<DocumentFonts, String> {
    @Query(value = """
SELECT coalesce(df.font_name, df.alt_font_name) as fontName, count(*)
FROM metrics.document_fonts df
INNER JOIN metrics.dashboard ds ON df.task_id = ds.task_id
WHERE ds.is_licensed = true
       AND (ds.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND ds.on_message_hit BETWEEN :startDate AND :endDate
GROUP BY fontName""", nativeQuery = true) // Non-native didn't appear to work here for some reason. Count returned null?
    List<DocumentFontsSample> findAllDocumentFonts(@Param("workflowNameList") Set<String> workflowNameList,
                                                   @Param("startDate") long startDate,
                                                   @Param("endDate") long endDate);

    @Query(value = """
SELECT coalesce(df.font_name, df.alt_font_name) as fontName, count(*)
FROM metrics.document_fonts df
INNER JOIN metrics.dashboard ds ON df.task_id = ds.task_id
WHERE ds.is_licensed = true
       AND (ds.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND (:isInput is NULL OR :isInput = df.input)
       AND ds.on_message_hit BETWEEN :startDate AND :endDate
GROUP BY fontName""", nativeQuery = true) // Non-native didn't appear to work here for some reason. Count returned null?
    List<DocumentFontsSample> findAllDocumentFonts(@Param("workflowNameList") Set<String> workflowNameList,
                                                   @Param("isInput") boolean isInput,
                                                   @Param("startDate") long startDate,
                                                   @Param("endDate") long endDate);
}
