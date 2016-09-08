package com.docshifter.core.main;

import com.docshifter.core.config.service.GeneralConfigService;
import com.docshifter.core.messaging.IDocShifterSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by michiel.vandriessche@docbyte.com on 9/7/16.
 */
@SpringBootApplication
public class TestController implements IDocShifterSender {


    @Autowired
    DocShifterConfiguration docShifterConfiguration;

    @Override
    public void restartNotStatic() {

    }

    public static void main(String[] args) {
        SpringApplication.run(TestController.class, args);
    }
}
