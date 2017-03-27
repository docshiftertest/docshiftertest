package com.docshifter.core.config.service;

import com.docshifter.core.TestController;
import com.docshifter.core.exceptions.DocShifterLicenceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestController.class)
public class NalpeironServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private  NalpeironService nalpeironService;

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Test
    public void initialTest() {

        String moduleId = "PVEE";

        long[] fid = {0L};

        Map<String, Object> testClientData = new HashMap<>();

        testClientData.put("moduleId", moduleId);
        testClientData.put("mapTest", new boolean[]{true, false, true, true});

        try {
            nalpeironService.validateAndStartModule(moduleId, fid);
            nalpeironService.endModule(moduleId, testClientData, fid);
        }catch (DocShifterLicenceException ex) {
            fail("Module could not be accessed: " + ex.getMessage());
        }
    }


    @Test
    public void derpTest() {

    }
}
