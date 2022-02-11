package com.docshifter.core.config.services;

import org.junit.Before;
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
	public void resolveEvent_fixesAppState() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(LivenessState.CORRECT, appAvailability.getLivenessState());
	}

	@Test
	public void resolveEvent_differentType_appStateStillBroken() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		assertEquals(LivenessState.BROKEN, appAvailability.getLivenessState());
	}

	@Test
	public void resolveEvent_multipleSameType_appStateStillBroken() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(LivenessState.BROKEN, appAvailability.getLivenessState());
	}

	@Test
	public void resolveEvent_multipleDistinctSameType_fixesAppState() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
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
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(2, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	public void getEventCount_distinct_one() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		assertEquals(1, sut.getEventCount(HealthManagementService.Event.MEMORY_SHORTAGE));
	}
}
