package com.docshifter.core;

import com.docshifter.core.audit.repositories.ModuleConfigurationVersionRepository;
import com.docshifter.core.config.services.ILicensingService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Created by blazejm on 29.05.2017.
 */

@SpringBootTest(classes = BeansApplication.class)
public abstract class AbstractSpringTest {
    @MockBean
    protected ILicensingService nalpeironService;

    @MockBean
    private ModuleConfigurationVersionRepository moduleConfigurationVersionRepository;
}
