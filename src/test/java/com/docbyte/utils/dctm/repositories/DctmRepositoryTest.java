package com.docbyte.utils.dctm.repositories;

import com.docshifter.core.utils.dctm.DctmConnectionDetails;
import com.docshifter.core.utils.dctm.DctmSession;
import com.docshifter.core.utils.dctm.DctmSessionUtils;
import com.docshifter.core.utils.dctm.MetadataConsts;
import com.docshifter.core.utils.dctm.MetadataUtils;
import com.docshifter.core.utils.dctm.repositories.DctmRepository;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import lombok.extern.log4j.Log4j;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;

@Log4j
public class DctmRepositoryTest {

	@Test
	public void findDocbaseWithEmptyQuery() throws Exception {
		try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {
			
			DctmRepository<Docbase> repo = new DctmRepository<>(Docbase.class, session);
			
			Docbase docbase = repo.findWithQuery("");
			
			Assert.assertNotNull(docbase);
			log.debug(docbase);
		}
	}

	@Test
	public void findByName() throws Exception {
		findDocumentByName("TestDoc" + System.currentTimeMillis());
	}
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
			Assert.assertNotNull(document);
			Assert.assertEquals(name, document.getName());
			log.debug(document);


		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}

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
			Assert.assertNotSame("System is to fast for new name generation", name, newName);
			Assert.assertNotNull(document);
			Assert.assertEquals(newName, po.getString(MetadataConsts.OBJECT_NAME));
			log.debug(document);


		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}

	@Test
	public void standardInsert() throws Exception  {
		insertDocument("TestDoc" + System.currentTimeMillis());
	}
	
	@Test
	public void insertWithQuote() throws Exception  {
		insertDocument("Test'Doc" + System.currentTimeMillis());
	}
	
	
	private void insertDocument(String name) throws Exception {
		IDfPersistentObject po= null;

		try (DctmSession session = DctmSessionUtils.getInstance().createSession( DctmConnectionDetails.fromProperties("repoTest.properties"))) {

			

			Document document = new Document();
			document.setName(name);
			document.setLocation(new HashSet<>(Arrays.asList("/Temp")));


			//actual test
			DctmRepository<Document> repo = new DctmRepository<>(Document.class, session);
			repo.insert(document);

			Assert.assertNotNull(document);
			Assert.assertNotNull(document.getId());


			po = session.getObject(new DfId(document.getId()));
			Assert.assertEquals(document.getName(), po.getString(MetadataConsts.OBJECT_NAME));
			Assert.assertEquals(document.getLocation(), MetadataUtils.getPaths((IDfSysObject) po));
			

			log.debug(document);

		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}
	
	
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
			Assert.assertNotSame("System is to fast for new title generation", title, newTitle);
			Assert.assertNotNull(document);
			Assert.assertEquals(name, po.getString(MetadataConsts.OBJECT_NAME));
			Assert.assertEquals(newTitle, po.getString(MetadataConsts.TITLE));
			log.debug(document);
			
			
		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}
	
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
			
			Assert.assertNotNull(document);
			Assert.assertNotNull(document.getId());
			
			
			po = session.getObject(new DfId(document.getId()));
			Assert.assertEquals(document.getName(), po.getString(MetadataConsts.OBJECT_NAME));
			
			
			log.debug(document);
			
		} finally {
			//cleanup
			if (po != null)
				po.destroy();
		}
	}
}