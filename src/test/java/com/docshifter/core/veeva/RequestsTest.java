package com.docshifter.core.veeva;

import com.docshifter.core.utils.veeva.VeevaResponse;
import lombok.extern.log4j.Log4j2;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

@Log4j2
class RequestsTest {

	private static final String INVALID_SESSION_RESPONSE = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INVALID_SESSION_ID\",\"message\": \"Invalid or expired session ID.\"}]}";
	private static final String INVALID_DATA_RESPONSE = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INVALID_DATA\",\"message\": \"Requested version not found 30081/1234(0.1)\"}]}";
	private static final String INVALID_DOCUMENT_RESPONSE = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INVALID_DOCUMENT\",\"message\": \"Document not found [12345236236666].\"}]}";
	private static final String INVALID_BINDER_RESPONSE = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INVALID_BINDER\",\"message\": \"Binder not found [1234].\"}]}"; 
	private static final String INVALID_QUERY_RESPONSE = "{\"responseStatus\": \"FAILURE\",\"errors\": [{\"type\": \"INCORRECT_QUERY_SYNTAX\",\"message\": \"Cannot parse the search phrase [SELECT latestversion id, name__v,type__v,subtype__v, lifecycle__v, product__v,clinical_study__v,\\n   SELECT binder__sysr.id, parent_node_id__sys, id, document__sysr.id,document__sysr.major_version_number__v, document__sysr.minor_version_number__v,binder__sysr.clinical_study__v, order__sys\\n   FROM binder_nodes__sysr)  \\nFROM allversions binders \\nWHERE status__v ='Ready to Export']: Unknown token [SELECT] found near position [3] at line [2]\"}]}";
	private static final String EXCEPTION_RESPONSE = "{\"responseStatus\": \"EXCEPTION\",\"errors\": [{\"type\": \"UNEXPECTED_ERROR\",\"message\": \"An unexpected error has occurred.\"}]}";
	private static final Map<String, List<String>> JSON_HEADERS;
	static {
		JSON_HEADERS = new HashMap<>();
		List<String> contentTypes = new ArrayList<>();
		contentTypes.add(VeevaResponse.APP_JSON + ";charset=UTF-8");
		JSON_HEADERS.put(VeevaResponse.CONTENT_TYPE, contentTypes);
	}
	private static final Map<String, List<String>> BINARY_HEADERS;
	static {
		BINARY_HEADERS = new HashMap<>();
		List<String> contentTypes = new ArrayList<>();
		contentTypes.add(VeevaResponse.APP_BINARY + ";charset=UTF-8");
		BINARY_HEADERS.put(VeevaResponse.CONTENT_TYPE, contentTypes);
	}

	/**
	<a href="https://developer.veevavault.com/api/18.3/#submitting-a-query">Veeva</a>
	getBindersToProcess

 	<a href="https://developer.veevavault.com/api/18.3/#bulk-document-state-change">...</a> => but this method is only changing a single document/binder
	changeStatus

	<a href="https://developer.veevavault.com/api/18.3/#retrieve-user-actions">...</a>
	getUserLifecycleActions

	<a href="https://developer.veevavault.com/api/18.3/#retrieve-binder">...</a>
	<a href="https://developer.veevavault.com/api/18.3/#retrieve-document">...</a>
	retrieveMetadata

	<a href="https://developer.veevavault.com/api/18.3/#download-document-file">...</a>
	<a href="https://developer.veevavault.com/api/18.3/#download-document-version-file">...</a>
	getFileContent

 	<a href="https://developer.veevavault.com/api/18.3/#retrieve-document-version-renditions">...</a>
	<a href="https://developer.veevavault.com/api/18.3/#download-document-version-rendition-file">...</a>
	getRenditionContent

	<a href="https://developer.veevavault.com/api/18.3/#retrieve-document-versions">...</a>
	getVersions

	<a href="https://developer.veevavault.com/api/18.3/#create-documents">...</a>
	createDocument
	
	<a href="https://developer.veevavault.com/api/18.3/#update-single-document">...</a>
	updateSingleDocument

	<a href="https://developer.veevavault.com/api/18.3/#create-binder-relationship">...</a>
	<a href="https://developer.veevavault.com/api/18.3/#create-single-document-relationship">...</a>
	createRelations

	<a href="https://developer.veevavault.com/api/18.3/#retrieve-document-relationship">...</a>
	<a href="https://developer.veevavault.com/api/18.3/#retrieve-binder-relationship">...</a>
	retrieveAllRelations

	<a href="https://developer.veevavault.com/api/18.3/#add-document-to-binder">...</a>
	addDocumentToBinder

	<a href="https://developer.veevavault.com/api/18.3/#update-document-version">...</a>
	<a href="https://developer.veevavault.com/api/18.3/#update-binder-version">...</a>
	updateVersionFields
	*/
	private static Stream<Arguments> provideResponseDataForTests() {
		return Stream.of(
				// Get Binders to process
				Arguments.of(new String[] {"{\"responseStatus\": \"SUCCESS\",\"responseDetails\": {\"limit\": 1000,\"offset\": 0,\"size\": 1,\"total\": 1},\"data\": [{\"id\": 2986,\"name__v\": \"TESTBinder\",\"type__v\": \"e-Volume Review\",\"subtype__v\": \"e-Volume Binder\",\"lifecycle__v\": \"DocShifter Lifecycle\",\"product__v\": [\"00P000000000601\"],\"clinical_study__v\": null,\"binder_nodes__sysr\": {\"responseDetails\": {\"limit\": 250,\"offset\": 0,\"size\": 5,\"total\": 5},\"data\": [{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": null,\"id\": 5272,\"document__sysr.id\": null,\"document__sysr.major_version_number__v\": null,\"document__sysr.minor_version_number__v\": null,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 0},{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": 5272,\"id\": 5377,\"document__sysr.id\": 2987,\"document__sysr.major_version_number__v\": 0,\"document__sysr.minor_version_number__v\": 1,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 0},{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": 5272,\"id\": 5378,\"document__sysr.id\": 2988,\"document__sysr.major_version_number__v\": 0,\"document__sysr.minor_version_number__v\": 1,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 100},{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": 5272,\"id\": 5379,\"document__sysr.id\": 2989,\"document__sysr.major_version_number__v\": 0,\"document__sysr.minor_version_number__v\": 1,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 200},{\"binder__sysr.id\": 2986,\"parent_node_id__sys\": 5272,\"id\": 5380,\"document__sysr.id\": 2990,\"document__sysr.major_version_number__v\": 0,\"document__sysr.minor_version_number__v\": 1,\"binder__sysr.clinical_study__v\": null,\"order__sys\": 300}]}}]}",
						INVALID_DATA_RESPONSE, INVALID_QUERY_RESPONSE, INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing getBindersToProcess() because we got an exception"),
				// Change status
				Arguments.of(new String[] {"{\"responseStatus\":\"SUCCESS\",\"id\":2986}",
						INVALID_BINDER_RESPONSE, INVALID_DOCUMENT_RESPONSE, INVALID_DATA_RESPONSE, INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing changeStatus() because we got an exception"),
				// Get User Lifecycle actions
				Arguments.of(new String[] {"{\"responseStatus\":\"SUCCESS\",\"responseMessage\":\"Success\",\"lifecycle_actions__v\":[{\"name__v\":\"LifecycleUserAction1\",\"label__v\":\"Change State to e-Binder Review\",\"lifecycle_action_type__v\":\"stateChange\",\"entry_requirements__v\":\"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/binders/2986/versions/0/1/lifecycle_actions/LifecycleUserAction1/entry_requirements\"},{\"name__v\":\"LifecycleUserAction3\",\"label__v\":\"Change State to Error Occurred\",\"lifecycle_action_type__v\":\"stateChange\",\"entry_requirements__v\":\"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/binders/2986/versions/0/1/lifecycle_actions/LifecycleUserAction3/entry_requirements\"},{\"name__v\":\"LifecycleUserAction\",\"label__v\":\"Change State to Draft\",\"lifecycle_action_type__v\":\"workflow\",\"entry_requirements__v\":\"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/binders/2986/versions/0/1/lifecycle_actions/LifecycleUserAction/entry_requirements\"}]}",
						INVALID_BINDER_RESPONSE, INVALID_DOCUMENT_RESPONSE, INVALID_DATA_RESPONSE, INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing getUserLifecycleActions() because we got an exception"),
				// Retrieve Metadata
				Arguments.of(new String[] {"{\"responseStatus\": \"SUCCESS\",\"document\": { \"id\": 2987, \"version_id\": \"2987_0_1\", \"binder__v\": false, \"coordinator__v\": {\"groups\": [],\"users\": [] }, \"owner__v\": {\"groups\": [],\"users\": [    2480841] }, \"approver__v\": {\"groups\": [],\"users\": [] }, \"reviewer__v\": {\"groups\": [],\"users\": [] }, \"distribution_contacts__v\": {\"groups\": [],\"users\": [] }, \"viewer__v\": {\"groups\": [    1394917493803,    1394917493803],\"users\": [] }, \"consumer__v\": {\"groups\": [],\"users\": [] }, \"editor__v\": {\"groups\": [    1394917494301,    1394917494201,    1394917494201,    1394917494301],\"users\": [] }, \"annotations_all__v\": 0, \"filename__v\": \"TEST1.docx\", \"drug_product__v\": [], \"version_created_by__v\": 2480841, \"application__v\": [], \"lifecycle__v\": \"DocShifter Lifecycle\", \"subtype__v\": \"e-Volume PDF\", \"nonclinical_study__v\": [], \"name__v\": \"TEST1\", \"pages__v\": 1, \"type__v\": \"e-Volume Review\", \"manufacturer__v\": [], \"annotations_unresolved__v\": 0, \"last_modified_by__v\": 2480841, \"version_modified_date__v\": \"2019-08-19T09:04:56.000Z\", \"created_by__v\": 2480841, \"product_detail__v\": [], \"format__v\": \"application/vnd.openxmlformats-officedocument.wordprocessingml.document\", \"version_creation_date__v\": \"2019-05-20T13:46:20.151Z\", \"major_version_number__v\": 0, \"region__v\": [], \"clinical_study__v\": [], \"excipient__v\": [], \"annotations_links__v\": 0, \"status__v\": \"Draft\", \"suppress_rendition__v\": \"false\", \"product__v\": [\"00P000000000601\" ], \"country__v\": [], \"annotations_anchors__v\": 0, \"drug_substance__v\": [], \"document_number__v\": \"02093\", \"minor_version_number__v\": 1, \"clinical_site__v\": [], \"crosslink__v\": false, \"annotations_notes__v\": 0, \"locked__v\": false, \"submission__v\": [], \"size__v\": 11897, \"md5checksum__v\": \"3ac5e0d54d0fc30abd59a9f97bc490a8\", \"document_creation_date__v\": \"2019-05-20T13:46:20.151Z\", \"annotations_resolved__v\": 0, \"annotations_lines__v\": 0},\"renditions\": { \"viewable_rendition__v\": \"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/documents/2987/renditions/viewable_rendition__v\"},\"versions\": [ {\"number\": \"0.1\",\"value\": \"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/documents/2987/versions/0/1\" }]}",
						INVALID_BINDER_RESPONSE, INVALID_DOCUMENT_RESPONSE, INVALID_DATA_RESPONSE, INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing retrieveMetadata() because we got an exception"),
				// Get File content
				Arguments.of(new String[] {"%PDF-1.4 %���� 3 0 obj <</E 674994/H [ 657 137 ]/L 675442/Linearized 1/N 1/O 5/T 675235>> endobj `g``������9�\u0001\u00150\u0003!\u000B\u0003�\u0001�\u0003\u0010\u0001F�\u0004;\u001430�0�0�:��,1\f�\u000B\u0018\u0016.�\u0011`",
						INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						BINARY_HEADERS,
						"Failing getFileContent() because we got an exception"),
				// Get Rendition content
				Arguments.of(new String[] {"%PDF-1.4 %���� 3 0 obj <</E 674994/H [ 657 137 ]/L 675442/Linearized 1/N 1/O 5/T 675235>> endobj `g``������9�\u0001\u00150\u0003!\u000B\u0003�\u0001�\u0003\u0010\u0001F�\u0004;\u001430�0�0�:��,1\f�\u000B\u0018\u0016.�\u0011`",
						INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						BINARY_HEADERS,
						"Failing getRenditionContent() because we got an exception"),
				// Get Versions
				Arguments.of(new String[] {"{\"responseStatus\": \"SUCCESS\",\"versions\": [{\"number\": \"0.1\",\"value\": \"https://sb-glpg-submissions.veevavault.com/api/v18.3/objects/binders/2986/versions/0/1\"}]}",
						INVALID_BINDER_RESPONSE, INVALID_DOCUMENT_RESPONSE, INVALID_DATA_RESPONSE, INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing getVersions() because we got an exception"),
				// Create Document
				Arguments.of(new String[] {"{\"responseStatus\": \"SUCCESS\",\"responseMessage\": \"successfully created document\",\"id\": 3602}",
						INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing createDocument() because we got an exception"),
				// Update single Document
				Arguments.of(new String[] {"{\"responseStatus\": \"SUCCESS\",\"responseMessage\": \"New draft successfully created.\",\"major_version_number__v\": 0,\"minor_version_number__v\": 12}",
						INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing updateSingleDocument() because we got an exception"),
				// Create Relations
				Arguments.of(new String[] {"{\"responseStatus\":\"SUCCESS\",\"responseMessage\":\"Document relationship successfully created.\",\"id\":2584}",
						INVALID_BINDER_RESPONSE, INVALID_DOCUMENT_RESPONSE, INVALID_DATA_RESPONSE, INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing createRelations() because we got an exception"),
				// Retrieve all Relations
				Arguments.of(new String[] {"{\"responseStatus\": \"SUCCESS\",\"responseMessage\": null,\"errorCodes\": null,\"relationships\": [{\"relationship\": {\"source_doc_id__v\": 397,\"relationship_type__v\": \"supporting_documents__c\",\"created_date__v\": \"2019-03-06T15:31:30.000Z\",\"id\": 114,\"target_doc_id__v\": 457,\"created_by__v\": 2480841}}],\"errorType\": null}",
						INVALID_BINDER_RESPONSE, INVALID_DOCUMENT_RESPONSE, INVALID_DATA_RESPONSE, INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing retrieveAllRelations() because we got an exception"),
				// Add Document to Binder
				Arguments.of(new String[] {"{\"responseStatus\": \"SUCCESS\",\"id\": \"1427491342404:-1828014479\"}",
						INVALID_BINDER_RESPONSE, INVALID_DOCUMENT_RESPONSE, INVALID_DATA_RESPONSE, INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing addDocumentToBinder() because we got an exception"),
				// Update Version fields
				Arguments.of(new String[] {"{\"responseStatus\": \"SUCCESS\",\"id\": 534}",
						INVALID_BINDER_RESPONSE, INVALID_DOCUMENT_RESPONSE, INVALID_DATA_RESPONSE, INVALID_SESSION_RESPONSE, EXCEPTION_RESPONSE},
						JSON_HEADERS,
						"Failing updateVersionFields() because we got an exception")

		);
	}

	@ParameterizedTest
	@MethodSource("provideResponseDataForTests")
	void checkResponses(String[] responseDatas, Map<String, List<String>> headers, String failureMessage) {

		for (String responseData : responseDatas) {
			try {
				VeevaResponse veevaResponse = VeevaResponse.getVeevaResponse(responseData.getBytes(), headers);
				VeevaResponse.checkResponse(veevaResponse, "dummy-sessionId", "dummy-host", "v18.3", "dummy-user", "dummy-password");
			}
			catch (JSONException exc) {
				log.info("This is OK, a failure 'JsonMappingException' because we received the content of a file so the response does not start with a squirrel: '{'");
			}
			catch (UnknownHostException exc) {
				if (responseData.equals(INVALID_SESSION_RESPONSE)) {
					log.info("This is OK, a failure 'INVALID_SESSION_ID' because session id is not correct");
				}
				else {
					log.error(exc);
					fail(failureMessage + ": " + exc);
				}
			}
			catch (NullPointerException npe) {
				log.error("Got unexpected NullPointer?");
				npe.printStackTrace();
			}
			catch (SSLHandshakeException shakeOnIt) {
				log.info("This is bad, we can't get an SSL connection to work!", shakeOnIt);
				fail(failureMessage + ": " + shakeOnIt);
			}
			catch (IOException ioe) {
				fail(failureMessage + ": " + ioe);
			}
			catch (Exception exc) {
				if (exc.getMessage().startsWith("Error when doing request") &&
						responseData.equals(INVALID_DATA_RESPONSE)) {
					log.info("This is OK, a failure 'INVALID_DATA' because of unknown field in query");
				}
				else if (exc.getMessage().startsWith("Error when doing request") &&
						responseData.equals(INVALID_QUERY_RESPONSE)) {
					log.info("This is OK, a failure 'INCORRECT_QUERY_SYNTAX' because the sintax is incorrect in query");
				}
				else if (responseData.equals(EXCEPTION_RESPONSE)) {
					log.info("This is OK, a failure 'UNEXPECTED_ERROR'");
				}
				else if (responseData.equals(INVALID_BINDER_RESPONSE) || responseData.equals(INVALID_DOCUMENT_RESPONSE)) {
					log.info("This is OK, a failure 'INVALID_BINDER / INVALID_DOCUMENT' because it does not exist");
				}
				else {
					log.error(exc);
					fail(failureMessage + ": " + exc);
				}
			}
		}
	}
}
