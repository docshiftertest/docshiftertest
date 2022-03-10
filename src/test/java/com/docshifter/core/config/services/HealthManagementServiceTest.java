package com.docshifter.core.config.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class HealthManagementServiceTest {

	@Mock
	private ApplicationContext appContext;
	@Mock
	private ApplicationEventMulticaster appEventMulticaster;
	private HealthManagementService sut;

	@Before
	public void before() {
		sut = new HealthManagementService(appContext, appEventMulticaster);
	}

	private HealthManagementService.FirstCorrectFiredEvent fireOnAppReady() {
		return fireOnAppReady(LivenessState.CORRECT);
	}

	private HealthManagementService.FirstCorrectFiredEvent fireOnAppReady(LivenessState state) {
		AtomicReference<CompletableFuture<HealthManagementService.FirstCorrectFiredEvent>> result = new AtomicReference<>();
		Thread t = new Thread(() -> result.set(sut.onAppReady(new AvailabilityChangeEvent<>(appContext, state))));
		t.start();
		t.interrupt();
		try {
			t.join();
			CompletableFuture<HealthManagementService.FirstCorrectFiredEvent> resultingEventFuture = result.get();
			assertNotNull(resultingEventFuture);
			return resultingEventFuture.get();
		} catch (InterruptedException ex) {
			fail("We shouldn't get any InterruptedExceptions during test setup! " + ex);
		} catch (ExecutionException ex) {
			fail("Execution of onAppReady method threw an exception: " + ex);
		}

		return null;
	}

	@Test
	public void onAppReady_correct_returnsNewEvent() {
		HealthManagementService.FirstCorrectFiredEvent resultingEvent = fireOnAppReady(LivenessState.CORRECT);
		assertNotNull(resultingEvent);
		assertEquals(appContext, resultingEvent.getApplicationContext());
		assertEquals(appContext, resultingEvent.getSource());
	}

	@Test
	public void onAppReady_broken_returnsNull() {
		HealthManagementService.FirstCorrectFiredEvent resultingEvent = fireOnAppReady(LivenessState.BROKEN);
		assertNull(resultingEvent);
	}

	@Test
	public void onAppReady_twice_returnsNull() {
		fireOnAppReady(LivenessState.CORRECT);
		HealthManagementService.FirstCorrectFiredEvent resultingEvent = fireOnAppReady(LivenessState.CORRECT);
		assertNull(resultingEvent);
	}

	@Test
	public void reportEvent_breaksAppState() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertEquals(LivenessState.BROKEN, captor.getValue().getState());
		assertTrue(captor.getValue().getSource() instanceof HealthManagementService.EventSource);
		HealthManagementService.EventSource source = (HealthManagementService.EventSource) captor.getValue().getSource();
		assertEquals(HealthManagementService.Event.TASK_STUCK, source.getEventType());
		assertNull(source.getData());
	}

	@Test
	public void reportEvent_withData_breaksAppState() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertEquals(LivenessState.BROKEN, captor.getValue().getState());
		assertTrue(captor.getValue().getSource() instanceof HealthManagementService.EventSource);
		HealthManagementService.EventSource source = (HealthManagementService.EventSource) captor.getValue().getSource();
		assertEquals(HealthManagementService.Event.TASK_STUCK, source.getEventType());
		assertEquals("some data", source.getData());
	}

	@Test
	public void resolveEvent_fixesAppState() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext, times(2)).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertArrayEquals(new LivenessState[]{LivenessState.BROKEN, LivenessState.CORRECT},
				captor.getAllValues().stream().map(AvailabilityChangeEvent::getState).toArray(LivenessState[]::new));
		assertTrue(captor.getAllValues().stream().allMatch(value -> value.getSource() instanceof HealthManagementService.EventSource));
		List<HealthManagementService.EventSource> sources = captor.getAllValues().stream()
				.map(value -> (HealthManagementService.EventSource) value.getSource())
				.collect(Collectors.toList());
		assertArrayEquals(new HealthManagementService.Event[]{HealthManagementService.Event.TASK_STUCK, HealthManagementService.Event.TASK_STUCK},
				sources.stream().map(HealthManagementService.EventSource::getEventType).toArray(HealthManagementService.Event[]::new));
		assertArrayEquals(new Object[]{null, null},
				sources.stream().map(HealthManagementService.EventSource::getData).toArray());
	}

	@Test
	public void resolveEvent_withData_fixesAppState() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK, "some data");

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext, times(2)).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertArrayEquals(new LivenessState[]{LivenessState.BROKEN, LivenessState.CORRECT},
				captor.getAllValues().stream().map(AvailabilityChangeEvent::getState).toArray(LivenessState[]::new));
		assertTrue(captor.getAllValues().stream().allMatch(value -> value.getSource() instanceof HealthManagementService.EventSource));
		List<HealthManagementService.EventSource> sources = captor.getAllValues().stream()
				.map(value -> (HealthManagementService.EventSource) value.getSource())
				.collect(Collectors.toList());
		assertArrayEquals(new HealthManagementService.Event[]{HealthManagementService.Event.TASK_STUCK, HealthManagementService.Event.TASK_STUCK},
				sources.stream().map(HealthManagementService.EventSource::getEventType).toArray(HealthManagementService.Event[]::new));
		assertArrayEquals(new Object[]{"some data", "some data"},
				sources.stream().map(HealthManagementService.EventSource::getData).toArray());
	}

	@Test
	public void resolveEvent_differentType_appStateStillBroken() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertEquals(LivenessState.BROKEN, captor.getValue().getState());
		assertTrue(captor.getValue().getSource() instanceof HealthManagementService.EventSource);
		HealthManagementService.EventSource source = (HealthManagementService.EventSource) captor.getValue().getSource();
		assertEquals(HealthManagementService.Event.TASK_STUCK, source.getEventType());
		assertNull(source.getData());
	}

	@Test
	public void resolveEvent_sameTypeNoData_appStateStillBroken() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertEquals(LivenessState.BROKEN, captor.getValue().getState());
		assertTrue(captor.getValue().getSource() instanceof HealthManagementService.EventSource);
		HealthManagementService.EventSource source = (HealthManagementService.EventSource) captor.getValue().getSource();
		assertEquals(HealthManagementService.Event.TASK_STUCK, source.getEventType());
		assertEquals("some data", source.getData());
	}

	@Test
	public void resolveEvent_sameTypeDifferentData_appStateStillBroken() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK, "some other data");

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertEquals(LivenessState.BROKEN, captor.getValue().getState());
		assertTrue(captor.getValue().getSource() instanceof HealthManagementService.EventSource);
		HealthManagementService.EventSource source = (HealthManagementService.EventSource) captor.getValue().getSource();
		assertEquals(HealthManagementService.Event.TASK_STUCK, source.getEventType());
		assertEquals("some data", source.getData());
	}

	@Test
	public void resolveEvent_multipleSameType_appStateStillBroken() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext, times(2)).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertArrayEquals(new LivenessState[]{LivenessState.BROKEN, LivenessState.BROKEN},
				captor.getAllValues().stream().map(AvailabilityChangeEvent::getState).toArray(LivenessState[]::new));
		assertTrue(captor.getAllValues().stream().allMatch(value -> value.getSource() instanceof HealthManagementService.EventSource));
		List<HealthManagementService.EventSource> sources = captor.getAllValues().stream()
				.map(value -> (HealthManagementService.EventSource) value.getSource())
				.collect(Collectors.toList());
		assertArrayEquals(new HealthManagementService.Event[]{HealthManagementService.Event.TASK_STUCK,
						HealthManagementService.Event.TASK_STUCK},
				sources.stream().map(HealthManagementService.EventSource::getEventType).toArray(HealthManagementService.Event[]::new));
		assertArrayEquals(new Object[]{null, "some data"},
				sources.stream().map(HealthManagementService.EventSource::getData).toArray());
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void resolveEvent_multipleDistinct_sameType_fixesAppState() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext, times(2)).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertArrayEquals(new LivenessState[]{LivenessState.BROKEN, LivenessState.CORRECT},
				captor.getAllValues().stream().map(AvailabilityChangeEvent::getState).toArray(LivenessState[]::new));
		assertTrue(captor.getAllValues().stream().allMatch(value -> value.getSource() instanceof HealthManagementService.EventSource));
		List<HealthManagementService.EventSource> sources = captor.getAllValues().stream()
				.map(value -> (HealthManagementService.EventSource) value.getSource())
				.collect(Collectors.toList());
		assertArrayEquals(new HealthManagementService.Event[]{HealthManagementService.Event.MEMORY_SHORTAGE,
						HealthManagementService.Event.MEMORY_SHORTAGE},
				sources.stream().map(HealthManagementService.EventSource::getEventType).toArray(HealthManagementService.Event[]::new));
		assertArrayEquals(new Object[]{null, null},
				sources.stream().map(HealthManagementService.EventSource::getData).toArray());
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void resolveEvent_multipleDistinct_sameTypeNoData_fixesAppState() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext, times(2)).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertArrayEquals(new LivenessState[]{LivenessState.BROKEN, LivenessState.CORRECT},
				captor.getAllValues().stream().map(AvailabilityChangeEvent::getState).toArray(LivenessState[]::new));
		assertTrue(captor.getAllValues().stream().allMatch(value -> value.getSource() instanceof HealthManagementService.EventSource));
		List<HealthManagementService.EventSource> sources = captor.getAllValues().stream()
				.map(value -> (HealthManagementService.EventSource) value.getSource())
				.collect(Collectors.toList());
		assertArrayEquals(new HealthManagementService.Event[]{HealthManagementService.Event.MEMORY_SHORTAGE,
						HealthManagementService.Event.MEMORY_SHORTAGE},
				sources.stream().map(HealthManagementService.EventSource::getEventType).toArray(HealthManagementService.Event[]::new));
		assertArrayEquals(new Object[]{"some data", null},
				sources.stream().map(HealthManagementService.EventSource::getData).toArray());
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void resolveEvent_multipleDistinct_sameTypeDifferentData_fixesAppState() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some other data");

		ArgumentCaptor<AvailabilityChangeEvent<LivenessState>> captor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
		verify(appContext, times(2)).publishEvent(captor.capture());
		verifyNoMoreInteractions(appContext);
		assertArrayEquals(new LivenessState[]{LivenessState.BROKEN, LivenessState.CORRECT},
				captor.getAllValues().stream().map(AvailabilityChangeEvent::getState).toArray(LivenessState[]::new));
		assertTrue(captor.getAllValues().stream().allMatch(value -> value.getSource() instanceof HealthManagementService.EventSource));
		List<HealthManagementService.EventSource> sources = captor.getAllValues().stream()
				.map(value -> (HealthManagementService.EventSource) value.getSource())
				.collect(Collectors.toList());
		assertArrayEquals(new HealthManagementService.Event[]{HealthManagementService.Event.MEMORY_SHORTAGE,
						HealthManagementService.Event.MEMORY_SHORTAGE},
				sources.stream().map(HealthManagementService.EventSource::getEventType).toArray(HealthManagementService.Event[]::new));
		assertArrayEquals(new Object[]{"some data", "some other data"},
				sources.stream().map(HealthManagementService.EventSource::getData).toArray());
	}

	@Test
	public void getEventCount_zero() {
		fireOnAppReady();
		assertEquals(0, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	public void getEventCount_one() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(1, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	public void getEventCount_two() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertEquals(2, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	public void getEventCount_sameData_one() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertEquals(1, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void getEventCount_distinct_one() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		assertEquals(1, sut.getEventCount(HealthManagementService.Event.MEMORY_SHORTAGE));
	}

	@Test
	public void containsData_null_returnsFalse() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		assertFalse(sut.containsData(HealthManagementService.Event.TASK_STUCK, null));
	}

	@Test
	public void containsData_noData_returnsFalse() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		assertFalse(sut.containsData(HealthManagementService.Event.TASK_STUCK, "some data"));
	}

	@Test
	public void containsData_differentType_returnsFalse() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertFalse(sut.containsData(HealthManagementService.Event.CRITICAL_MQ_ERROR, "some data"));
	}

	@Test
	public void containsData_match_returnsTrue() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertTrue(sut.containsData(HealthManagementService.Event.TASK_STUCK, "some data"));
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void containsData_overwriteWithMatch_returnsTrue() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		assertTrue(sut.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, "some data"));
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void containsData_overwriteWithNoMatch_returnsFalse() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some other data");
		assertFalse(sut.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, "some data"));
	}

	@Test
	@Ignore("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void containsData_overwriteEmpty_returnsFalse() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		assertFalse(sut.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, "some data"));
	}
}
