package com.docshifter.core.veeva;

import com.docshifter.core.utils.veeva.VeevaBadResponse;
import com.docshifter.core.utils.veeva.VeevaLoginSuccess;
import com.docshifter.core.utils.veeva.VeevaResponse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Code moved from main() of VeevaSession
 *
 */
@Log4j2
public class VeevaSessionTest {

	private Map<String, List<String>> jsonHeaders;
	private Map<String, List<String>> binaryHeaders;

	@BeforeEach
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

	@Test
	/**
	 * Test the parsing of a VeevaSession from the response string
	 * TODO: Add Assertions and check stuff works!
	 * @throws Exception
	 */
	public void testLoginResults() throws Exception {
		String goodResponseCredentials = "{\"responseStatus\":\"SUCCESS\",\"sessionId\":\"F1C3850ABE3582CE3F0B45FA1523701BE0DCCC315B11179F3BF4417E46F050CB47C728DF750F9D20B16E0BF1E023AB239C0AD4AA81E1696A1CB2515CBB4AA880\",\"userId\":2480841,\"vaultIds\":[{\"id\":30081,\"name\":\"Submissions Sandbox\",\"url\":\"https://sb-glpg-submissions.veevavault.com/api\"}],\"vaultId\":30081}";
		String badResponseCredentials = "{\"responseStatus\":\"EXCEPTION\",\"errors\":[{\"type\":\"USERNAME_OR_PASSWORD_INCORRECT\",\"message\":\"Authentication failed for user: null.\"}],\"errorType\":\"GENERAL\"}";
		Class<?>[] expectedClasses = {VeevaLoginSuccess.class, VeevaBadResponse.class};
		String[] expectedStatuses = {"SUCCESS", "EXCEPTION"};
		String[] responseDatas = {goodResponseCredentials, badResponseCredentials};
		VeevaResponse response;
		int idx = 0;
		for (String responseData : responseDatas) {
			Class<?> expectedClass = expectedClasses[idx];
			response = VeevaResponse.getLoginResult(responseData);
			assertTrue(expectedClass == response.getClass(), "Expected Classs mismatch");
			String expectedStatus = expectedStatuses[idx];
			assertTrue(expectedStatus.equals(response.getResponseStatus()),
					"Expected status: " + expectedStatus + " but got: " + response.getResponseStatus());
			if (expectedClass == VeevaLoginSuccess.class) {
				String sessionId = ((VeevaLoginSuccess) response).getSessionId();
				assertTrue("F1C3850ABE3582CE3F0B45FA1523701BE0DCCC315B11179F3BF4417E46F050CB47C728DF750F9D20B16E0BF1E023AB239C0AD4AA81E1696A1CB2515CBB4AA880"
						.equals(sessionId),
						"Session Id expected to be: " + 
						"F1C3850ABE3582CE3F0B45FA1523701BE0DCCC315B11179F3BF4417E46F050CB47C728DF750F9D20B16E0BF1E023AB239C0AD4AA81E1696A1CB2515CBB4AA880" + 
						" but is: " + sessionId);
			}
			idx++;
		}
	}

	@Test
	/**
	 * Test the parsing of a VeevaSession from the response string
	 * TODO: Add Assertions and check stuff works!
	 * @throws Exception
	 */
	public void testOtherResults() throws Exception {
		String badResponseInvalidsession = "{\"responseStatus\":\"FAILURE\",\"errors\":[{\"type\":\"INVALID_SESSION_ID\",\"message\":\"Invalid or expired session ID.\"}],\"errorType\":\"GENERAL\"}";
		String badResponseOperation = "{\"responseStatus\":\"FAILURE\",\"responseMessage\":\"The specified lifecycle action cannot be started.\",\"errors\":[{\"type\":\"OPERATION_NOT_ALLOWED\",\"message\":\"Cannot find lifecycle action: LifecycleUserAction2.\"}],\"errorType\":\"GENERAL\"}";
		String goodResponseQuery = "{\"responseStatus\":\"SUCCESS\",\"responseDetails\":{\"limit\":1000,\"offset\":0,\"size\":2,\"total\":2},\"data\":[{\"id\":397,\"name__v\":\"Docshifter Binder1\",\"binder_nodes__sysr\":{\"responseDetails\":{\"limit\":250,\"offset\":0,\"size\":15,\"total\":15},\"data\":[{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Docshifter Binder1\",\"parent_node_id__sys\":2316,\"id\":2179,\"document__sysr.id\":398,\"document__sysr.name__v\":\"TEST1\",\"order__sys\":200},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Docshifter Binder1\",\"parent_node_id__sys\":2316,\"id\":2180,\"document__sysr.id\":399,\"document__sysr.name__v\":\"TEST2\",\"order__sys\":300},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Section1.1\",\"parent_node_id__sys\":2319,\"id\":2181,\"document__sysr.id\":400,\"document__sysr.name__v\":\"TEST3\",\"order__sys\":0},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Docshifter Binder1\",\"parent_node_id__sys\":2316,\"id\":2182,\"document__sysr.id\":409,\"document__sysr.name__v\":\"TEST8\",\"order__sys\":150},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Docshifter Binder1\",\"parent_node_id__sys\":2316,\"id\":2186,\"document__sysr.id\":410,\"document__sysr.name__v\":\"TEST5\",\"order__sys\":600},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Section1.1\",\"parent_node_id__sys\":2319,\"id\":2188,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":100},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"section 1.1.1\",\"parent_node_id__sys\":2188,\"id\":2189,\"document__sysr.id\":415,\"document__sysr.name__v\":\"TEST6\",\"order__sys\":0},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":null,\"parent_node_id__sys\":null,\"id\":2316,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":0},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Docshifter Binder1\",\"parent_node_id__sys\":2316,\"id\":2317,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":175},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Docshifter Binder1\",\"parent_node_id__sys\":2316,\"id\":2318,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":400},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Section1\",\"parent_node_id__sys\":2317,\"id\":2319,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":0},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Section2\",\"parent_node_id__sys\":2318,\"id\":2320,\"document__sysr.id\":401,\"document__sysr.name__v\":\"TEST4\",\"order__sys\":0},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Section2\",\"parent_node_id__sys\":2318,\"id\":2321,\"document__sysr.id\":402,\"document__sysr.name__v\":\"TEST5\",\"order__sys\":100},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"Docshifter Binder1\",\"parent_node_id__sys\":2316,\"id\":2325,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":500},{\"binder__sysr.id\":397,\"parent_node__sysr.name__v\":\"section 1.1.1\",\"parent_node_id__sys\":2188,\"id\":2336,\"document__sysr.id\":420,\"document__sysr.name__v\":\"TEST7\",\"order__sys\":100}]}},{\"id\":416,\"name__v\":\"Docshifter Binder2\",\"binder_nodes__sysr\":{\"responseDetails\":{\"limit\":250,\"offset\":0,\"size\":9,\"total\":9},\"data\":[{\"binder__sysr.id\":416,\"parent_node__sysr.name__v\":\"section3.1\",\"parent_node_id__sys\":2334,\"id\":2190,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":0},{\"binder__sysr.id\":416,\"parent_node__sysr.name__v\":null,\"parent_node_id__sys\":null,\"id\":2328,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":0},{\"binder__sysr.id\":416,\"parent_node__sysr.name__v\":\"Docshifter Binder2\",\"parent_node_id__sys\":2328,\"id\":2329,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":0},{\"binder__sysr.id\":416,\"parent_node__sysr.name__v\":\"Docshifter Binder2\",\"parent_node_id__sys\":2328,\"id\":2330,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":100},{\"binder__sysr.id\":416,\"parent_node__sysr.name__v\":\"Docshifter Binder2\",\"parent_node_id__sys\":2328,\"id\":2331,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":200},{\"binder__sysr.id\":416,\"parent_node__sysr.name__v\":\"section1\",\"parent_node_id__sys\":2329,\"id\":2332,\"document__sysr.id\":417,\"document__sysr.name__v\":\"TEST1\",\"order__sys\":0},{\"binder__sysr.id\":416,\"parent_node__sysr.name__v\":\"section2\",\"parent_node_id__sys\":2330,\"id\":2333,\"document__sysr.id\":418,\"document__sysr.name__v\":\"TEST2\",\"order__sys\":0},{\"binder__sysr.id\":416,\"parent_node__sysr.name__v\":\"section3\",\"parent_node_id__sys\":2331,\"id\":2334,\"document__sysr.id\":null,\"document__sysr.name__v\":null,\"order__sys\":0},{\"binder__sysr.id\":416,\"parent_node__sysr.name__v\":\"section3.1.1\",\"parent_node_id__sys\":2190,\"id\":2335,\"document__sysr.id\":419,\"document__sysr.name__v\":\"TEST5\",\"order__sys\":0}]}}]}";
		String goodResponseDocumentCreation = "{\"responseStatus\":\"SUCCESS\",\"responseMessage\":\"successfully created document\",\"id\":479}";
		String sessionId = "F1C3850ABE3582CE3F0B45FA1523701BE0DCCC315B11179F3BF4417E46F050CB47C728DF750F9D20B16E0BF1E023AB239C0AD4AA81E1696A1CB2515CBB4AA880";
		String[] expectedSessionIds = {null, null, sessionId, sessionId};
		String[] responseDatas = {badResponseInvalidsession, badResponseOperation, goodResponseQuery, goodResponseDocumentCreation};
		String actualSessionId;
		int idx = 0;
		for (String responseData : responseDatas) {
			VeevaResponse veevaResponse = VeevaResponse.getVeevaResponse(responseData.getBytes(), jsonHeaders);
			actualSessionId = null;
			try {
				actualSessionId = VeevaResponse.checkResponse(veevaResponse, sessionId, "dummy-host", "v18.3", "julian.isaac@sb-glpg.com", "*****");
			}
			catch (Exception exc) {
				if (exc instanceof UnknownHostException &&
						responseData.equals(badResponseInvalidsession)) {
					log.info("This is OK, proves we tried to connect to dummy-host!");
				}
				else if (exc.getMessage().startsWith("Error when doing request") &&
						responseData.equals(badResponseOperation)) {
					log.info("This is OK, a failure that's not invalid session shouldn't try to autologin again!");
				}
				else {
					log.error(exc);
					fail();
				}
			}
			//log.info("Got response: " + response.toString());
			String expectedSessionId = expectedSessionIds[idx];
			assertTrue((expectedSessionId == null && actualSessionId == null) ||
					(expectedSessionId != null && expectedSessionId.equals(actualSessionId)),
					"Expected Session Id: " + expectedSessionId + " but got: " + actualSessionId);
			idx++;
		}
	}

}
