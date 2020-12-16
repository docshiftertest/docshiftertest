package com.docbyte.utils.dctm.repositories;

import com.docshifter.core.utils.dctm.MetadataConsts;
import com.docshifter.core.utils.dctm.annotations.DctmAttribute;
import com.docshifter.core.utils.dctm.annotations.DctmId;
import com.docshifter.core.utils.dctm.annotations.DctmLocation;
import com.docshifter.core.utils.dctm.annotations.DctmObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@ToString
@Setter
@Getter
@NoArgsConstructor
@DctmObject("dm_document")
public class Document {


    @DctmAttribute(MetadataConsts.OBJECT_NAME)
    private String name;

    @DctmId
    private String id;
    
    
    @DctmAttribute(MetadataConsts.TITLE)
    private String title;

    @DctmLocation
    private Set<String> location;
}