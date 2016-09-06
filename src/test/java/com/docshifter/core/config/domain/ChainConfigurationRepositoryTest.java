package com.docshifter.core.config.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by michiel.vandriessche@docbyte.com on 9/6/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = com.docshifter.main.DocShifterConfiguration.class)
public class ChainConfigurationRepositoryTest extends DBTests {

	@Test
	public void findRootNodesByEnabled() throws Exception {
		System.out.println("HERE");
		System.out.println("HERE");
		System.out.println("HERE");
		System.out.println("HERE");
		System.out.println("HERE");
		System.out.println("HERE");
		System.out.println("HERE");
		System.out.println("HERE");

		System.out.println(chainConfigurationRepository.findRootNodesByEnabled(true));
		System.out.println("HERE");
		System.out.println("HERE");
		System.out.println("HERE");
		System.out.println("HERE");
		System.out.println("HERE");
	}

	@Test
	public void findByRootNode() throws Exception {

	}

	@Test
	public void findByQueueName() throws Exception {

	}

}