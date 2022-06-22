package com.docshifter.core.metrics.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
