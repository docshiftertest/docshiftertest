package com.docshifter.core.config.services;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
public class NalpeironServiceTest {

    @MockBean
    private  NalpeironService nalpeironService;

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Test
    public void initialTest() {

        String moduleId = "CSTM";

        long[] fid = {0L};

        Map<String, Object> testClientData = new HashMap<>();

        testClientData.put("moduleId", moduleId);
        testClientData.put("mapTest", new boolean[]{true, false, true, true});

        try {
            nalpeironService.validateAndStartModule(moduleId, fid);
            nalpeironService.endModule(moduleId, testClientData, fid);
        }
        catch (DocShifterLicenseException ex) {
            fail("Module could not be accessed: " + ex.getMessage());
        }
    }
}
