package com.docshifter.core;

import com.docshifter.core.TestController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by michiel.vandriessche@docbyte.com on 9/6/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestController.class)
public class DocShifterConfigurationTest {



	@Test
	public void contextLoads() {
	}

}