package com.docshifter.core;

import com.docshifter.core.messaging.IDocShifterSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

/**
 * Created by michiel.vandriessche@docbyte.com on 9/7/16.
 */
@SpringBootApplication
public class TestController implements IDocShifterSender {


    @Override
    public void restartNotStatic() {

    }

    public static void main(String[] args) {
        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "receiver");
        SpringApplication.run(TestController.class, args);
    }
}
