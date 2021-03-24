package com.docshifter.core.dctm;

import com.docshifter.core.utils.dctm.*;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by michiel.vandriessche on 07/03/17.
 */
public class DataUtilsTest {


    @Test
    public void testQueryInterpretation() {
		FieldInfo fieldInfo = DataUtils.extractFieldInfo("dm_user.description(dm_document.authors=dm_user.user_name)");
        Assert.assertEquals("dm_user.description", fieldInfo.getFieldDefinition());
        Assert.assertEquals("dm_user.description", fieldInfo.getReturnName());
		Assert.assertEquals("dm_document.authors=dm_user.user_name", fieldInfo.getClause());
	
		fieldInfo = DataUtils.extractFieldInfo("object_name");
		Assert.assertEquals("object_name", fieldInfo.getFieldDefinition());
		Assert.assertEquals("object_name", fieldInfo.getReturnName());
		Assert.assertEquals("", fieldInfo.getClause());
		
	
	
	
		Assert.assertEquals("dm_document", DataUtils.getObjectTypeFromFieldDef("dm_document.authors"));
		Assert.assertEquals("dm_user", DataUtils.getObjectTypeFromFieldDef("dm_user.user_name"));

    
    }

    @Test
    public void testQueryInterpretationWithAltName() {

        String field = "dm_user.description|creator.description(dm_document.r_creator_name=dm_user.user_name)";
	
	
		FieldInfo fieldInfo = DataUtils.extractFieldInfo(field);

        Assert.assertEquals("dm_user.description", fieldInfo.getFieldDefinition());
        Assert.assertEquals("creator.description", fieldInfo.getReturnName());
        Assert.assertEquals("dm_document.r_creator_name=dm_user.user_name", fieldInfo.getClause());
	
		Assert.assertEquals("dm_document", DataUtils.getObjectTypeFromFieldDef("dm_document.r_creator_name"));
		Assert.assertEquals("dm_user", DataUtils.getObjectTypeFromFieldDef("dm_user.user_name"));
	
	
	}

	@Ignore("There's no Dctm repo to talk to right now")
    @Test
    public void getValuesByQuery() throws Exception {
        String fieldQuery = "dm_user.user_address|author.mail(dm_document.authors=dm_user.user_name)";

        try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {
        	IDfSysObject sysObject = (IDfSysObject) session.getObject(new DfId("09aaaaaa8005e76f"));
	
	
			FieldInfo fieldInfo = DataUtils.extractFieldInfo(fieldQuery);
        	Object[] result = DataUtils.getFieldWithQuery(sysObject, fieldInfo.getFieldDefinition(), fieldInfo.getClause());
        	System.out.print(Arrays.toString(result));
        }
    }

	@Ignore("There's no Dctm repo to talk to right now")
    @Test
    public void getData() throws Exception {
    	try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {
    		IDfSysObject sysObject = (IDfSysObject) session.getObject(new DfId("09aaaaaa8005e76f"));
    		String[] fields = new String[]{
    				"object_name",
    				"title",
    				"r_creator_name|creator.name",
    				"r_creation_date",
    				"r_version_label",
    				"authors|author.name",
    				"dm_user.user_address|author.mail(dm_document.authors=dm_user.user_name)",
    				"dm_user.description|author.description(dm_document.authors=dm_user.user_name)",
    				"dm_user.description|creator.description(dm_document.r_creator_name=dm_user.user_name)"
    		};
    		Map<String, Object> result = DataUtils.extractData(sysObject, fields);
    		for (Map.Entry<String, Object> entity : result.entrySet()) {
    			System.out.println(entity.getKey());
    			if (entity.getValue() instanceof Object[]) {
    				System.out.println(Arrays.toString((Object[]) entity.getValue()));
    			} else {
    				System.out.println(entity.getValue());
    			}
    		}
    	}
    }
}