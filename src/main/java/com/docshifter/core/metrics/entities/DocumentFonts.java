package com.docshifter.core.metrics.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter // these do Gets and Sets automatically
@AllArgsConstructor
@NoArgsConstructor
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
