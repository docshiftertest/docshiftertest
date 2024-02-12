package com.docshifter.core.metrics.entities;

import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Data
@Table(name = "document_fonts")
public class DocumentFonts {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name="task_id")
    private Dashboard dashboard;

    private String fontName;
    private String altFontName;
    private String documentName;
    private boolean input;

}
