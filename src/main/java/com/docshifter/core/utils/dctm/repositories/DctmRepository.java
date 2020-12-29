package com.docshifter.core.utils.dctm.repositories;

import com.docshifter.core.utils.dctm.DataUtils;
import com.docshifter.core.utils.dctm.MetadataConsts;
import com.docshifter.core.utils.dctm.MetadataUtils;
import com.docshifter.core.utils.dctm.annotations.DctmObject;
import com.docshifter.core.utils.dctm.dto.DctmDTO;
import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

@Log4j2
public class DctmRepository<T extends Object> {
	
	private final Class<T> clazz;
	
	private final String type;
	private final DctmDTO<T> dto;
	
	private IDfSession session;
	
	public DctmRepository(Class<T> clazz, IDfSession session) {
		this.clazz = clazz;
		this.session = session;
		this.type = clazz.getAnnotation(DctmObject.class).value();
		this.dto = new DctmDTO<>(this.clazz);
	}
	

	
	public T findByName(String name) throws DfException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ParseException {
		IDfPersistentObject po = session.getObjectByQualification(String.format("%s where object_name = '%s'", type, DataUtils.escapeForDql(name)));
		if (po == null) {
			return null;
		}
		return dto.poToPOJO(po);
	}
	
	public T findWithQuery(String whereClause) throws DfException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ParseException {
		String query = String.format("%s %s", type, whereClause);
		
		log.debug(query);
		
		IDfPersistentObject po = session.getObjectByQualification(query);
		if (po == null) {
			return null;
		}
		return dto.poToPOJO(po);
	}

	public void insert(T object) throws DfException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		IDfPersistentObject po = session.newObject(clazz.getAnnotation(DctmObject.class).value());

		MetadataUtils.setAttributes(po, dto.getAttributeMap(object));
		
		Set<String> paths = dto.getPaths(object);
		
		if (paths != null) {
			IDfSysObject so = (IDfSysObject) po;
			unlinkAll(so);
			for (String path : paths) {
				so.link(path);
			}
		}
		
		po.save();
		dto.setDctmId(object, po.getObjectId().getId());
	}
	
	private void unlinkAll(IDfSysObject so) throws DfException {
		Set<String> paths = MetadataUtils.getPaths(so);
		
		
		for (String path: paths) {
			so.unlink(path);
		}
		
		
	}
	
	public void update(T object) throws DfException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String id = dto.getDctmId(object);

		Map<String, Object> attributesMap = dto.getAttributeMap(object);

		IDfPersistentObject po = session.getObject(new DfId(id));

		MetadataUtils.setAttributes(po, attributesMap);
		po.save();

	}
	
	public void upsertByName(T object) throws DfException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ParseException {
		
		T repoObject = this.findByName(dto.getDctmAttribute(object, MetadataConsts.OBJECT_NAME));
		
		
		if (repoObject == null) {
			this.insert(object);
			
		} else if (!repoObject.equals(object)) {
			dto.setDctmId(object, dto.getDctmId(repoObject));
			update(object);
			
			
		}
		
	}
	
	public void deleteAll() throws DfException {
		IDfQuery query = new DfQuery(String.format("delete %s (all) objects", type));
		IDfCollection coll = null;
		try {
			coll = query.execute(session, IDfQuery.DF_EXEC_QUERY);
		} finally {
			if (coll!=null ) {
				coll.close();
			}
		}
	}
	
}
