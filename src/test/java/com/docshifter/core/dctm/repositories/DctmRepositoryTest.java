package com.docshifter.core.dctm.repositories;

import com.docshifter.core.utils.dctm.DctmConnectionDetails;
import com.docshifter.core.utils.dctm.DctmSession;
import com.docshifter.core.utils.dctm.DctmSessionUtils;
import com.docshifter.core.utils.dctm.MetadataConsts;
import com.docshifter.core.utils.dctm.MetadataUtils;
import com.docshifter.core.utils.dctm.repositories.DctmRepository;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import java.util.Arrays;
import java.util.HashSet;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Log4j2
public class DctmRepositoryTest {

	@Disabled("There's no Dctm repo to talk to right now")
	@Test
	public void findDocbaseWithEmptyQuery() throws Exception {
		try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {
			
			DctmRepository<Docbase> repo = new DctmRepository<>(Docbase.class, session);
			
			Docbase docbase = repo.findWithQuery("");
			
			Assertions.assertNotNull(docbase);
			log.debug(docbase);
		}
	}

	@Disabled("There's no Dctm repo to talk to right now")
	@Test
	public void findByName() throws Exception {
		findDocumentByName("TestDoc" + System.currentTimeMillis());
	}

	@Disabled("There's no Dctm repo to talk to right now")
	@Test
	public void findByNameWithQuotes() throws Exception {
		findDocumentByName("Test'Doc" + System.currentTimeMillis());
	}
	
	private void findDocumentByName(String name) throws Exception {
		IDfPersistentObject po= null;

		try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {

			//create object
			
			po = session.newObject("dm_document");
			po.setString("object_name", name);
			po.save();


			//actual test
			DctmRepository<Document> repo = new DctmRepository<>(Document.class, session);

			Document document = repo.findByName(name);

			//asserts
			Assertions.assertNotNull(document);
			Assertions.assertEquals(name, document.getName());
			log.debug(document);


		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}

	@Disabled("There's no Dctm repo to talk to right now")
	@Test
	public void updateDocument() throws Exception {
		IDfPersistentObject po= null;

		try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {

			//create object
			String name = "TestDoc" + System.currentTimeMillis();
			po = session.newObject("dm_document");
			po.setString(MetadataConsts.OBJECT_NAME, name);
			po.save();


			//actual test
			DctmRepository<Document> repo = new DctmRepository<>(Document.class, session);
			Document document = repo.findByName(name);

			String newName = "TestDoc" + System.currentTimeMillis();
			document.setName(newName);

			repo.update(document);

			po.fetch(null);


			//asserts
			Assertions.assertNotSame(name, newName, "System is to fast for new name generation");
			Assertions.assertNotNull(document);
			Assertions.assertEquals(newName, po.getString(MetadataConsts.OBJECT_NAME));
			log.debug(document);


		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}

	@Disabled("There's no Dctm repo to talk to right now")
	@Test
	public void standardInsert() throws Exception  {
		insertDocument("TestDoc" + System.currentTimeMillis());
	}

	@Disabled("There's no Dctm repo to talk to right now")
	@Test
	public void insertWithQuote() throws Exception  {
		insertDocument("Test'Doc" + System.currentTimeMillis());
	}

	@Disabled("There's no Dctm repo to talk to right now")
	private void insertDocument(String name) throws Exception {
		IDfPersistentObject po= null;

		try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {

			

			Document document = new Document();
			document.setName(name);
			document.setLocation(new HashSet<>(Arrays.asList("/Temp")));


			//actual test
			DctmRepository<Document> repo = new DctmRepository<>(Document.class, session);
			repo.insert(document);

			Assertions.assertNotNull(document);
			Assertions.assertNotNull(document.getId());


			po = session.getObject(new DfId(document.getId()));
			Assertions.assertEquals(document.getName(), po.getString(MetadataConsts.OBJECT_NAME));
			Assertions.assertEquals(document.getLocation(), MetadataUtils.getPaths((IDfSysObject) po));
			

			log.debug(document);

		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}

	@Disabled("There's no Dctm repo to talk to right now")
	@Test
	public void updateDocumentWithUpsert() throws Exception {
		IDfPersistentObject po= null;
		
		try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {
			
			//create object
			String name = "TestDoc" + System.currentTimeMillis();
			String title = name;
			po = session.newObject("dm_document");
			po.setString(MetadataConsts.OBJECT_NAME, name);
			po.setString(MetadataConsts.TITLE, title);
			po.save();
			
			
			//actual test
			DctmRepository<Document> repo = new DctmRepository<>(Document.class, session);
			
			Document document = new Document();
			document.setName(name);
			
			String newTitle = "TestDoc" + System.currentTimeMillis();
			document.setTitle(newTitle);
			
			repo.upsertByName(document);
			
			po.fetch(null);
			
			
			//asserts
			Assertions.assertNotSame(title, newTitle, "System is to fast for new title generation");
			Assertions.assertNotNull(document);
			Assertions.assertEquals(name, po.getString(MetadataConsts.OBJECT_NAME));
			Assertions.assertEquals(newTitle, po.getString(MetadataConsts.TITLE));
			log.debug(document);
			
			
		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}

	@Disabled("There's no Dctm repo to talk to right now")
	@Test
	public void insertDocumentWithUpsert() throws Exception {
		IDfPersistentObject po= null;
		
		try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {
			
			//create object
			String name = "TestDoc" + System.currentTimeMillis();
			
			Document document = new Document();
			document.setName(name);
			
			
			//actual test
			DctmRepository<Document> repo = new DctmRepository<>(Document.class, session);
			repo.upsertByName(document);
			
			Assertions.assertNotNull(document);
			Assertions.assertNotNull(document.getId());
			
			
			po = session.getObject(new DfId(document.getId()));
			Assertions.assertEquals(document.getName(), po.getString(MetadataConsts.OBJECT_NAME));
			
			
			log.debug(document);
			
		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}
}
