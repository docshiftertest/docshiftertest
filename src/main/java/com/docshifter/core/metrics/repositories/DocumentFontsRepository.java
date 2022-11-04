package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.samples.DocumentFontsSample;
import com.docshifter.core.metrics.entities.DocumentFonts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DocumentFontsRepository extends JpaRepository<DocumentFonts, String> {
    @Query("""
SELECT coalesce(df.fontName, df.altFontName) as fontName, count(df)
FROM DocumentFonts df
INNER JOIN df.dashboard ds
WHERE ds.isLicensed = true
       AND (ds.workflowName in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND :isInput is NULL OR :isInput = df.input
GROUP BY fontName""")
    List<DocumentFontsSample> findAllDocumentFonts(@Param("workflowNameList") Set<String> workflowNameList,
                                                   @Param("isInput") Boolean isInput);

    @Query("""
SELECT coalesce(df.fontName, df.altFontName) as fontName, count(df)
FROM DocumentFonts df
INNER JOIN df.dashboard ds
WHERE ds.isLicensed = true
       AND (ds.workflowName in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND :isInput is NULL OR :isInput = df.input
       AND ds.onMessageHit BETWEEN :startDate AND :endDate
GROUP BY fontName""")
    List<DocumentFontsSample> findAllDocumentFonts(@Param("workflowNameList") Set<String> workflowNameList,
                                                   @Param("isInput") Boolean isInput,
                                                   @Param("startDate") Long startDate,
                                                   @Param("endDate") Long endDate);
}
