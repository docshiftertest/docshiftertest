package com.docshifter.core.logging.appenders;


import com.docshifter.core.task.Task;
import com.docshifter.core.work.WorkFolder;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Log4j2
public class TaskMessageAppenderTest {

	private Task task;

	@BeforeEach
	public void before() {
		WorkFolder workFolder = new WorkFolder();
		workFolder.setFolder(Paths.get("target/test-classes/work"));
		task = new Task();
		task.setId("00590175-a659-4da0-91eb-8b5a7412eea1");
		task.setWorkFolder(workFolder);
	}

	@AfterEach
	public void after() {
		TaskMessageAppender.untrackTask();
	}

	@Test
	public void trackTask_appendsTaskMessages() {
		TaskMessageAppender.trackTask(task);

		log.error("Oh no, we got an error!");
		log.error("==> FAILED to fully finish processing task (status: HORRIBLE FAILURE, took 11 ms, with Configuration " +
				"Id: 1) for file: some-kind-of-file.docx");
		log.warn("This is a warning with {} formatted parameter", 1);

		assertEquals("00590175-a659-4da0-91eb-8b5a7412eea1", ThreadContext.get("taskId"));
		assertThat(task.getMessages(), containsInAnyOrder("ERROR: Oh no, we got an error!; (status: HORRIBLE FAILURE, took 11 ms, with Configuration Id: 1) for file: some-kind-of-file.docx",
				"WARNING: This is a warning with 1 formatted parameter"));
	}

	@Test
	public void trackTask_appendsException() {
		TaskMessageAppender.trackTask(task);

		log.error("Oh no, we got the following error:", new IllegalArgumentException("Sorocaba"));

		assertEquals("00590175-a659-4da0-91eb-8b5a7412eea1", ThreadContext.get("taskId"));
		assertThat(task.getMessages(), containsInAnyOrder("ERROR: Oh no, we got the following error: Sorocaba"));
	}

	@Test
	public void untrackTask_doesNotappendTaskMessage() {
		TaskMessageAppender.trackTask(task);
		TaskMessageAppender.untrackTask();

		log.error("Oh no, we got an error!");

		assertFalse(ThreadContext.containsKey("taskId"));
		assertThat(task.getMessages(), empty());
	}

	@Test
	public void registerCurrentThread_threaded_appendsTaskMessages() throws InterruptedException {
		TaskMessageAppender.trackTask(task);

		log.error("Oh no, we got an error!");
		Thread t = new Thread(() -> {
			TaskMessageAppender.registerCurrentThread(task);
			assertEquals("00590175-a659-4da0-91eb-8b5a7412eea1", ThreadContext.get("taskId"));
			log.warn("This is a warning with {} formatted parameter", 1);
		});
		t.start();
		t.join();

		assertEquals("00590175-a659-4da0-91eb-8b5a7412eea1", ThreadContext.get("taskId"));
		assertThat(task.getMessages(), containsInAnyOrder("ERROR: Oh no, we got an error!",
				"WARNING: This is a warning with 1 formatted parameter"));
	}
}
