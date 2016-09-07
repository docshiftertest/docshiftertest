package com.docshifter.main;

import com.docshifter.core.messaging.IDocShifterSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by michiel.vandriessche@docbyte.com on 9/7/16.
 */
@SpringBootApplication(scanBasePackages = {"com.docshifter.main", "com.docshifter.core.config.service"})
public class TestController implements IDocShifterSender {

    @Autowired
    DocShifterConfiguration docShifterConfiguration;

    @Override
    public void restartNotStatic() {

    }
}
