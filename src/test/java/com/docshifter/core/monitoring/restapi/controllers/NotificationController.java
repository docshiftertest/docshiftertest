package com.docshifter.core.monitoring.restapi.controllers;

import com.docshifter.core.monitoring.model.NotificationRequest;
import com.docshifter.core.monitoring.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by blazejm on 11.05.2017.
 */
@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping(path = "/notification",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void sendNotification(
            @RequestBody NotificationRequest body
    ) {
        notificationService.sendNotification(
                body.getConfigurationId(),
                body.getLevel(),
                body.getTaskId(),
                body.getMessage());
    }

}
