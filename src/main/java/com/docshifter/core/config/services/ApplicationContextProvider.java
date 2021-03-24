package com.docshifter.core.config.services;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Created by samnang.nop on 5/12/2016.
 */
@Service
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext context;

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        context = ctx;
    }
}
