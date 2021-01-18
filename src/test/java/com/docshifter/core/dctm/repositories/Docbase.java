package com.docshifter.core.dctm.repositories;


import com.docshifter.core.utils.dctm.annotations.DctmAttribute;
import com.docshifter.core.utils.dctm.annotations.DctmId;
import com.docshifter.core.utils.dctm.annotations.DctmObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@NoArgsConstructor
@DctmObject("dm_docbase_config")
public class Docbase {
	
	
	@DctmAttribute("object_name")
	private String name;
	
	@DctmId
	private String id;
	

}