package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.Sample.DocumentFontsSample;
import com.docshifter.core.metrics.entities.DocumentFonts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentFontsRepository extends JpaRepository<DocumentFonts, String> {

    @Query(value = "select coalesce(font_name, alt_font_name) AS fontName , " +
            "count(coalesce(font_name, alt_font_name)) AS documentFontCount " +
            "from metrics.document_fonts df " +
            "inner join lateral ( " +
            "select ds.task_id " +
            "from metrics.dashboard ds " +
            "where 1 = 1 " +
            "AND ds.task_id = df.task_id " +
            "AND ds.is_licensed = TRUE " +
            "AND (:overall = true OR (ds.on_message_hit BETWEEN :startDate AND :endDate)) " +
            "AND (ds.workflow_name in (:workflowNameList) or 'ALL' in (:workflowNameList)) " +
            "AND 1 = 1) ds on 1 = 1 " +
            " where 1 = 1 " +
            "AND (cast (cast (:isInput as text) as boolean) is null) OR (cast (cast (:isInput as text) as boolean) is not null AND cast (cast (:isInput as text) as boolean) = df.input) " +
            "AND 1 = 1 " +
            "group by coalesce(font_name, alt_font_name) " +
            "order by coalesce(font_name, alt_font_name) ", nativeQuery = true)
    List<DocumentFontsSample> findAllDocumentFonts(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("overall") boolean overall, @Param("workflowNameList") List<String> workflowNameList, @Param("isInput") Boolean isInput);

}
