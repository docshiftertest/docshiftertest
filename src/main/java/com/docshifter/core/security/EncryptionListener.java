package com.docshifter.core.security;

import java.util.Map;

import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.springframework.stereotype.Component;

import com.docshifter.core.config.entities.ModuleConfiguration;
import com.docshifter.core.config.entities.Parameter;
import com.docshifter.core.security.utils.SecurityProperties;
import com.docshifter.core.security.utils.SecurityUtils;

/**
 * Encryption event listener.
 * 
 * @author Created by juan.marques on 09/12/2019.
 */
@Component
public class EncryptionListener implements PreInsertEventListener, PostLoadEventListener, PreUpdateEventListener,
		PreCollectionUpdateEventListener , PreLoadEventListener {

	private final FieldEncrypter fieldEncrypter;

	private final FieldDecrypter fieldDecrypter;

	public EncryptionListener(FieldEncrypter fieldEncrypter, FieldDecrypter fieldDecrypter) {
		this.fieldEncrypter = fieldEncrypter;
		this.fieldDecrypter = fieldDecrypter;
	}

	@Override
	public void onPostLoad(PostLoadEvent event) {
		fieldDecrypter.decrypt(event.getEntity());
	}

	@Override
	public void onPreLoad(PreLoadEvent event) {
		fieldDecrypter.decrypt(event.getEntity());
	}

	@Override
	public boolean onPreInsert(PreInsertEvent event) {
		Object[] state = event.getState();
		String[] propertyNames = event.getPersister().getPropertyNames();
		Object entity = event.getEntity();
		fieldEncrypter.encrypt(state, propertyNames, entity);
		return false;
	}

	@Override
	public boolean onPreUpdate(PreUpdateEvent event) {
		Object[] state = event.getState();
		String[] propertyNames = event.getPersister().getPropertyNames();
		Object entity = event.getEntity();
		fieldEncrypter.encrypt(state, propertyNames, entity);
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
		if (event.getCollection().getOwner() instanceof ModuleConfiguration) {
			SecurityUtils.readParametersThenEncrypt((Map<Parameter, String>) event.getCollection().getValue(),
					SecurityProperties.TYPE_PASSWORD.getValue(), event.getCollection().getOwner().getClass());
		}
	}
}
