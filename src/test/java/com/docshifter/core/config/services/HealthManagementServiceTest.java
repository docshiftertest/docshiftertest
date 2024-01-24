package com.docshifter.core.config.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class HealthManagementServiceTest {

	@Mock
	private ApplicationContext appContext;
	@Mock
	private ApplicationEventMulticaster appEventMulticaster;
	private HealthManagementService sut;

	@BeforeEach
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
			throw new RuntimeException("We shouldn't get any InterruptedExceptions during test setup!", ex);
		} catch (ExecutionException ex) {
			throw new RuntimeException("Execution of onAppReady method threw an exception.", ex);
		}
	}

	@Test
	public void onAppReady_correct_setsAppReady() {
		assertFalse(sut.isAppReady());
		fireOnAppReady(LivenessState.CORRECT);
		assertTrue(sut.isAppReady());
	}

	@Test
	public void onAppReady_correct_returnsNewEvent() {
		HealthManagementService.FirstCorrectFiredEvent resultingEvent = fireOnAppReady(LivenessState.CORRECT);
		assertNotNull(resultingEvent);
		assertEquals(appContext, resultingEvent.getApplicationContext());
		assertEquals(appContext, resultingEvent.getSource());
	}

	@Test
	public void onAppReady_broken_appNotReady() {
		fireOnAppReady(LivenessState.BROKEN);
		assertFalse(sut.isAppReady());
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
	public void onAppReady_differentTypes_publishesEarlyEvents() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		fireOnAppReady();

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
						HealthManagementService.Event.MEMORY_SHORTAGE},
				sources.stream().map(HealthManagementService.EventSource::getEventType).toArray(HealthManagementService.Event[]::new));
		assertArrayEquals(new Object[]{null, "some data"},
				sources.stream().map(HealthManagementService.EventSource::getData).toArray());
	}

	@Test
	public void onAppReady_genericEvents_publishesSingleEarlyEvent() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		fireOnAppReady();

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
	public void onAppReady_specificEvents_publishesEarlyEvents() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some other data");
		fireOnAppReady();

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
		assertArrayEquals(new Object[]{"some data", "some other data"},
				sources.stream().map(HealthManagementService.EventSource::getData).toArray());
	}

	@Test
	public void onAppReady_cancelOut_doesNotPublishEarlyEvents() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);
		fireOnAppReady();

		verifyNoInteractions(appContext);
	}

	@Test
	@Disabled("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void onAppReady_cancelOutDistinct_doesNotPublishEarlyEvents() {
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		fireOnAppReady();

		verifyNoInteractions(appContext);
	}

	@Test
	public void onAppReady_cancelOut2_publishesSomeEarlyEvents() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		sut.resolveEvent(HealthManagementService.Event.TASK_STUCK);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		fireOnAppReady();

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
						HealthManagementService.Event.MEMORY_SHORTAGE},
				sources.stream().map(HealthManagementService.EventSource::getEventType).toArray(HealthManagementService.Event[]::new));
		assertArrayEquals(new Object[]{"some data", null},
				sources.stream().map(HealthManagementService.EventSource::getData).toArray());
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
	public void reportEvent_early_doesNotBreakAppState() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);

		verifyNoInteractions(appContext);
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
	@Disabled("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
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
	@Disabled("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
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
	@Disabled("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
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
	public void getEventCount_early_zero() {
		assertEquals(0, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	public void getEventCount_one() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK);
		assertEquals(1, sut.getEventCount(HealthManagementService.Event.TASK_STUCK));
	}

	@Test
	public void getEventCount_early_one() {
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
	public void getEventCount_early_two() {
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
	@Disabled("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
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
	public void containsData_earlyMatch_returnsTrue() {
		sut.reportEvent(HealthManagementService.Event.TASK_STUCK, "some data");
		assertTrue(sut.containsData(HealthManagementService.Event.TASK_STUCK, "some data"));
	}

	@Test
	@Disabled("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void containsData_overwriteWithMatch_returnsTrue() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		assertTrue(sut.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, "some data"));
	}

	@Test
	@Disabled("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void containsData_overwriteWithNoMatch_returnsFalse() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some other data");
		assertFalse(sut.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, "some data"));
	}

	@Test
	@Disabled("No distinct health events exist at the moment (MEMORY_SHORTAGE was changed to non-distinct)")
	public void containsData_overwriteEmpty_returnsFalse() {
		fireOnAppReady();
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, "some data");
		sut.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
		assertFalse(sut.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, "some data"));
	}
}
