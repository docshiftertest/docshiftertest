package com.docbyte.utils.veeva;

import com.docshifter.core.utils.veeva.VeevaResponse;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Log4j

public class RequestsTest {

	private String invalidSessionResponse;
	private String invalidDataResponse;
	private String invalidDocumentResponse;
	private String invalidBinderResponse;
	private String invalidQueryResponse;
	private String exceptionResponse;
	private Map<String, List<String>> jsonHeaders;
	private Map<String, List<String>> binaryHeaders;

	public RequestsTest() {

		this.invalidBinderResponse = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INVALID_BINDER\",\"message\": \"Binder not found [1234].\"}]}";
		this.invalidDocumentResponse = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INVALID_DOCUMENT\",\"message\": \"Document not found [12345236236666].\"}]}";
		this.invalidDataResponse = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INVALID_DATA\",\"message\": \"Requested version not found 30081/1234(0.1)\"}]}";
		this.invalidSessionResponse = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INVALID_SESSION_ID\",\"message\": \"Invalid or expired session ID.\"}]}";
		this.exceptionResponse = "{\"responseStatus\": \"EXCEPTION\",\"errors\": [{\"type\": \"UNEXPECTED_ERROR\",\"message\": \"An unexpected error has occurred.\"}]}";
		this.invalidQueryResponse = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INCORRECT_QUERY_SYNTAX\",\"message\": \"Cannot parse the search phrase [SELECT latestversion id, name__v,type__v,subtype__v, lifecycle__v, product__v,clinical_study__v,\\n   SELECT binder__sysr.id, parent_node_id__sys, id, document__sysr.id,document__sysr.major_version_number__v, document__sysr.minor_version_number__v,binder__sysr.clinical_study__v, order__sys\\n   FROM binder_nodes__sysr)  \\nFROM allversions binders \\nWHERE status__v ='Ready to Export']: Unknown token [SELECT] found near position [3] at line [2]\"}]}";

	}

	@Before
	public void setupHeaders() {
		jsonHeaders = new HashMap<String, List<String>>();
		List<String> contentTypes = new ArrayList<>();
		contentTypes.add(VeevaResponse.APP_JSON + ";charset=UTF-8");
		jsonHeaders.put(VeevaResponse.CONTENT_TYPE, contentTypes);
		binaryHeaders = new HashMap<String, List<String>>();
		contentTypes = new ArrayList<>();
		contentTypes.add(VeevaResponse.APP_BINARY + ";charset=UTF-8");
		binaryHeaders.put(VeevaResponse.CONTENT_TYPE, contentTypes);
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#submitting-a-query
	 */
	@Test
	public void getBindersToProcess() {

		String goodResponse = "{\"responseStatus\": \"SUCCESS\",\"responseDetails\": {\"limit\": 1000,\"offset\": 0,\"size\": 1,\"total\": 1},\"data\": [{\"id\": 2986,\"name__v\": \"TESTBinder\",\"type__v\": \"e-Volume Review\",\"subtype__v\": \"e-Volume Binder\",\"lifecycle__v\": \"DocShifter Lifecycle\",\"product__v\": [\"00P000000000601\"],\"clinical_study__v\": null,\"binder_nodes__sysr\": {\"responseDetails\": {\"limit\": 250,\"offset\": 0,\"size\": 5,\"total\": 5},\"data\": [{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": null,\"id\": 5272,\"document__sysr.id\": null,\"document__sysr.major_version_number__v\": null,\"document__sysr.minor_version_number__v\": null,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 0},{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": 5272,\"id\": 5377,\"document__sysr.id\": 2987,\"document__sysr.major_version_number__v\": 0,\"document__sysr.minor_version_number__v\": 1,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 0},{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": 5272,\"id\": 5378,\"document__sysr.id\": 2988,\"document__sysr.major_version_number__v\": 0,\"document__sysr.minor_version_number__v\": 1,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 100},{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": 5272,\"id\": 5379,\"document__sysr.id\": 2989,\"document__sysr.major_version_number__v\": 0,\"document__sysr.minor_version_number__v\": 1,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 200},{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": 5272,\"id\": 5380,\"document__sysr.id\": 2990,\"document__sysr.major_version_number__v\": 0,\"document__sysr.minor_version_number__v\": 1,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 300}]}}]}";

		String[] responseDatas = {goodResponse, this.invalidDataResponse, this.invalidQueryResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing getBindersToProcess() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#bulk-document-state-change => but this method is only changing a single document/binder
	 */
	@Test
	public void changeStatus() {

		String goodResponse = "{\"responseStatus\":\"SUCCESS\",\"id\":2986}";

		String[] responseDatas = {goodResponse, this.invalidBinderResponse, this.invalidDocumentResponse, this.invalidDataResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing changeStatus() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-user-actions
	 */
	@Test
	public void getUserLifecycleActions() {

		String goodResponse = "{\"responseStatus\":\"SUCCESS\",\"responseMessage\":\"Success\",\"lifecycle_actions__v\":[{\"name__v\":\"LifecycleUserAction1\",\"label__v\":\"Change State to e-Binder Review\",\"lifecycle_action_type__v\":\"stateChange\",\"entry_requirements__v\":\"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/binders/2986/versions/0/1/lifecycle_actions/LifecycleUserAction1/entry_requirements\"},{\"name__v\":\"LifecycleUserAction3\",\"label__v\":\"Change State to Error Occurred\",\"lifecycle_action_type__v\":\"stateChange\",\"entry_requirements__v\":\"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/binders/2986/versions/0/1/lifecycle_actions/LifecycleUserAction3/entry_requirements\"},{\"name__v\":\"LifecycleUserAction\",\"label__v\":\"Change State to Draft\",\"lifecycle_action_type__v\":\"workflow\",\"entry_requirements__v\":\"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/binders/2986/versions/0/1/lifecycle_actions/LifecycleUserAction/entry_requirements\"}]}";

		String[] responseDatas = {goodResponse, this.invalidBinderResponse, this.invalidDocumentResponse, this.invalidDataResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing getUserLifecycleActions() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-binder
	 * https://developer.veevavault.com/api/18.3/#retrieve-document
	 */
	@Test
	public void retrieveMetadata() {

		String goodResponse = "{\"responseStatus\": \"SUCCESS\",\"document\": { \"id\": 2987, \"version_id\": \"2987_0_1\", \"binder__v\": false, \"coordinator__v\": {\"groups\": [],\"users\": [] }, \"owner__v\": {\"groups\": [],\"users\": [    2480841] }, \"approver__v\": {\"groups\": [],\"users\": [] }, \"reviewer__v\": {\"groups\": [],\"users\": [] }, \"distribution_contacts__v\": {\"groups\": [],\"users\": [] }, \"viewer__v\": {\"groups\": [    1394917493803,    1394917493803],\"users\": [] }, \"consumer__v\": {\"groups\": [],\"users\": [] }, \"editor__v\": {\"groups\": [    1394917494301,    1394917494201,    1394917494201,    1394917494301],\"users\": [] }, \"annotations_all__v\": 0, \"filename__v\": \"TEST1.docx\", \"drug_product__v\": [], \"version_created_by__v\": 2480841, \"application__v\": [], \"lifecycle__v\": \"DocShifter Lifecycle\", \"subtype__v\": \"e-Volume PDF\", \"nonclinical_study__v\": [], \"name__v\": \"TEST1\", \"pages__v\": 1, \"type__v\": \"e-Volume Review\", \"manufacturer__v\": [], \"annotations_unresolved__v\": 0, \"last_modified_by__v\": 2480841, \"version_modified_date__v\": \"2019-08-19T09:04:56.000Z\", \"created_by__v\": 2480841, \"product_detail__v\": [], \"format__v\": \"application/vnd.openxmlformats-officedocument.wordprocessingml.document\", \"version_creation_date__v\": \"2019-05-20T13:46:20.151Z\", \"major_version_number__v\": 0, \"region__v\": [], \"clinical_study__v\": [], \"excipient__v\": [], \"annotations_links__v\": 0, \"status__v\": \"Draft\", \"suppress_rendition__v\": \"false\", \"product__v\": [\"00P000000000601\" ], \"country__v\": [], \"annotations_anchors__v\": 0, \"drug_substance__v\": [], \"document_number__v\": \"02093\", \"minor_version_number__v\": 1, \"clinical_site__v\": [], \"crosslink__v\": false, \"annotations_notes__v\": 0, \"locked__v\": false, \"submission__v\": [], \"size__v\": 11897, \"md5checksum__v\": \"3ac5e0d54d0fc30abd59a9f97bc490a8\", \"document_creation_date__v\": \"2019-05-20T13:46:20.151Z\", \"annotations_resolved__v\": 0, \"annotations_lines__v\": 0},\"renditions\": { \"viewable_rendition__v\": \"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/documents/2987/renditions/viewable_rendition__v\"},\"versions\": [ {\"number\": \"0.1\",\"value\": \"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/documents/2987/versions/0/1\" }]}";

		String[] responseDatas = {goodResponse, this.invalidBinderResponse, this.invalidDocumentResponse, this.invalidDataResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing retrieveMetadata() because we got an IOException: " + ioe);
		}

	}

	/**
	 * https://developer.veevavault.com/api/18.3/#download-document-file
	 * https://developer.veevavault.com/api/18.3/#download-document-version-file
	 */
	@Test
	public void getFileContent() {

		String goodResponse = "%PDF-1.4 %���� 3 0 obj <</E 674994/H [ 657 137 ]/L 675442/Linearized 1/N 1/O 5/T 675235>> endobj `g``������9�\u0001\u00150\u0003!\u000B\u0003�\u0001�\u0003\u0010\u0001F�\u0004;\u001430�0�0�:��,1\f�\u000B\u0018\u0016.�\u0011`";

		String[] responseDatas = {goodResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, binaryHeaders));
		}
		catch (IOException ioe) {
			fail("Failing getFileContent() because we got an IOException: " + ioe);
		}

	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-document-version-renditions
	 * https://developer.veevavault.com/api/18.3/#download-document-version-rendition-file
	 */
	@Test
	public void getRenditionContent() {

		String goodResponse = "%PDF-1.4 %���� 3 0 obj <</E 674994/H [ 657 137 ]/L 675442/Linearized 1/N 1/O 5/T 675235>> endobj `g``������9�\u0001\u00150\u0003!\u000B\u0003�\u0001�\u0003\u0010\u0001F�\u0004;\u001430�0�0�:��,1\f�\u000B\u0018\u0016.�\u0011`";

		String[] responseDatas = {goodResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, binaryHeaders));
		}
		catch (IOException ioe) {
			fail("Failing getRenditionContent() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-document-versions
	 */
	@Test
	public void getVersions() {

		String goodResponse = "{\"responseStatus\": \"SUCCESS\",\"versions\": [{\"number\": \"0.1\",\"value\": \"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/binders/2986/versions/0/1\"}]}";

		String[] responseDatas = {goodResponse, this.invalidBinderResponse, this.invalidDocumentResponse, this.invalidDataResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing getVersions() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#create-documents
	 */
	@Test
	public void createDocument() {

		String goodResponse = "{\"responseStatus\": \"SUCCESS\",\"responseMessage\": \"successfully created document\",\"id\": 3602}";

		String[] responseDatas = {goodResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing createDocument() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#update-single-document
	 */
	@Test
	public void updateSingleDocument() {

		String goodResponse = "{\"responseStatus\": \"SUCCESS\",\"responseMessage\": \"New draft successfully created.\",\"major_version_number__v\": 0,\"minor_version_number__v\": 12}";

		String[] responseDatas = {goodResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing updateSingleDocument() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#create-binder-relationship
	 * https://developer.veevavault.com/api/18.3/#create-single-document-relationship
	 */
	@Test
	public void createRelations() {

		String goodResponse = "{\"responseStatus\":\"SUCCESS\",\"responseMessage\":\"Document relationship successfully created.\",\"id\":2584}";

		String[] responseDatas = {goodResponse, this.invalidBinderResponse, this.invalidDocumentResponse, this.invalidDataResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing createRelations() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-document-relationship
	 * https://developer.veevavault.com/api/18.3/#retrieve-binder-relationship
	 */
	@Test
	public void retrieveAllRelations() {

		String goodResponse = "{\"responseStatus\": \"SUCCESS\",\"responseMessage\": null,\"errorCodes\": null,\"relationships\": [{\"relationship\": {\"source_doc_id__v\": 397,\"relationship_type__v\": \"supporting_documents__c\",\"created_date__v\": \"2019-03-06T15:31:30.000Z\",\"id\": 114,\"target_doc_id__v\": 457,\"created_by__v\": 2480841}}],\"errorType\": null}";

		String[] responseDatas = {goodResponse, this.invalidBinderResponse, this.invalidDocumentResponse, this.invalidDataResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing retrieveAllRelations() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#add-document-to-binder
	 */
	@Test
	public void addDocumentToBinder() {

		String goodResponse = "{\"responseStatus\": \"SUCCESS\",\"id\": \"1427491342404:-1828014479\"}";

		String[] responseDatas = {goodResponse, this.invalidBinderResponse, this.invalidDocumentResponse, this.invalidDataResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing addDocumentToBinder() because we got an IOException: " + ioe);
		}
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#update-document-version
	 * https://developer.veevavault.com/api/18.3/#update-binder-version
	 */
	@Test
	public void updateVersionFields() {

		String goodResponse = "{\"responseStatus\": \"SUCCESS\",\"id\": 534}";
		String[] responseDatas = {goodResponse, this.invalidBinderResponse, this.invalidDocumentResponse, this.invalidDataResponse, this.invalidSessionResponse, this.exceptionResponse};
		try {
			assertTrue(checkResponses(responseDatas, jsonHeaders));
		}
		catch (IOException ioe) {
			fail("Failing updateVersionFields() because we got an IOException: " + ioe);
		}
	}

	private Boolean checkResponses(String[] responseDatas, Map<String, List<String>> headers) throws IOException {

		for (String responseData : responseDatas) {
			VeevaResponse veevaResponse = VeevaResponse.getVeevaResponse(responseData.getBytes(), headers);
			try {
				VeevaResponse.checkResponse(veevaResponse, "dummy-sessionId", "dummy-host", "v18.3", "dummy-user", "dummy-password");
			} catch (JSONException exc) {
				log.info("This is OK, a failure 'JsonMappingException' because we received the content of a file so the response does not start with a squirrel: '{'");
			} catch (UnknownHostException exc) {
				if (responseData.equals(this.invalidSessionResponse)) {
					log.info("This is OK, a failure 'INVALID_SESSION_ID' because session id is not correct");
				} else {
					log.error(exc);
					fail();
					return false;
				}
			}
			catch (NullPointerException npe) {
				log.error("Got unexpected NullPointer?");
				npe.printStackTrace();
			}
			catch (Exception exc) {
				if (exc.getMessage().startsWith("Error when doing request") &&
						responseData.equals(this.invalidDataResponse)) {
					log.info("This is OK, a failure 'INVALID_DATA' because of unknown field in query");
				} else if (exc.getMessage().startsWith("Error when doing request") &&
						responseData.equals(this.invalidQueryResponse)) {
					log.info("This is OK, a failure 'INCORRECT_QUERY_SYNTAX' because the sintax is incorrect in query");
				} else if (responseData.equals(this.exceptionResponse)) {
					log.info("This is OK, a failure 'UNEXPECTED_ERROR'");
				} else if (responseData.equals(invalidBinderResponse) || responseData.equals(invalidDocumentResponse)) {
					log.info("This is OK, a failure 'INVALID_BINDER / INVALID_DOCUMENT' because it does not exist");
				} else {
					log.error(exc);
					fail();
					return false;

				}
			}
		}

		return true;
	}


}
