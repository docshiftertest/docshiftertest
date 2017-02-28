package com.docshifter.core.config.service;

import com.docshifter.core.exceptions.DocShifterLicenceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class NalpeironServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void initialTest() {
        NalpeironService nalpeironService = new NalpeironService(applicationContext);

        long[] fid = {0L};

        try {
            nalpeironService.validateAndStartModule("DCTMI", fid);
            nalpeironService.endModule("DCTMI", fid);
        }catch (DocShifterLicenceException ex) {
            fail();
        }

        fail();
    }
}
