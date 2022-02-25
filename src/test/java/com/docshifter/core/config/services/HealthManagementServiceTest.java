package com.docshifter.core.config.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class HealthManagementServiceTest {

	@Autowired
	private ApplicationAvailability appAvailability;
	@Autowired
	private ApplicationContext appContext;
	private HealthManagementService sut;

	@Before
	public void before() {
		sut = new HealthManagementService(appContext);
	}

	@Test
	public void reportEvent_breaksAppState() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(LivenessState.BROKEN, appAvailability.getLivenessState());
	}

	@Test
	public void reportEvent_withData_breaksAppState() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertEquals(LivenessState.BROKEN, appAvailability.getLivenessState());
	}

	@Test
	public void resolveEvent_fixesAppState() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(LivenessState.CORRECT, appAvailability.getLivenessState());
	}

	@Test
	public void resolveEvent_withData_fixesAppState() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertEquals(LivenessState.CORRECT, appAvailability.getLivenessState());
	}

	@Test
	public void resolveEvent_differentType_appStateStillBroken() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		assertEquals(LivenessState.BROKEN, appAvailability.getLivenessState());
	}

	@Test
	public void resolveEvent_sameTypeNoData_appStateStillBroken() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(LivenessState.BROKEN, appAvailability.getLivenessState());
	}

	@Test
	public void resolveEvent_sameTypeDifferentData_appStateStillBroken() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK, "some other data");
		assertEquals(LivenessState.BROKEN, appAvailability.getLivenessState());
	}

	@Test
	public void resolveEvent_multipleSameType_appStateStillBroken() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(LivenessState.BROKEN, appAvailability.getLivenessState());
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void resolveEvent_multipleDistinct_sameType_fixesAppState() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		assertEquals(LivenessState.CORRECT, appAvailability.getLivenessState());
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void resolveEvent_multipleDistinct_sameTypeNoData_fixesAppState() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		assertEquals(LivenessState.CORRECT, appAvailability.getLivenessState());
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void resolveEvent_multipleDistinct_sameTypeDifferentData_fixesAppState() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some other data");
		assertEquals(LivenessState.CORRECT, appAvailability.getLivenessState());
	}

	@Test
	public void getEventCount_zero() {
		assertEquals(0, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	public void getEventCount_one() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(1, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	public void getEventCount_two() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertEquals(2, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	public void getEventCount_sameData_one() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertEquals(1, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void getEventCount_distinct_one() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		assertEquals(1, sut.getEventCount(HealthManagementService.Event.MEMORY_SHORTAGE));
	}

	@Test
	public void containsData_null_returnsFalse() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		assertFalse(sut.containsData(HealthManagementService.Event.TASK_STUCK, null));
	}

	@Test
	public void containsData_noData_returnsFalse() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		assertFalse(sut.containsData(HealthManagementService.Event.TASK_STUCK, "some data"));
	}

	@Test
	public void containsData_differentType_returnsFalse() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertFalse(sut.containsData(HealthManagementService.Event.CRITICAL_MQ_ERROR, "some data"));
	}

	@Test
	public void containsData_match_returnsTrue() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertTrue(sut.containsData(HealthManagementService.Event.TASK_STUCK, "some data"));
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void containsData_overwriteWithMatch_returnsTrue() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		assertTrue(sut.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, "some data"));
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void containsData_overwriteWithNoMatch_returnsFalse() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some other data");
		assertFalse(sut.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, "some data"));
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void containsData_overwriteEmpty_returnsFalse() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		assertFalse(sut.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, "some data"));
	}
}
