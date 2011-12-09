package org.motechproject.sms.http;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.motechproject.model.MotechEvent;
import org.motechproject.server.event.annotations.MotechListener;
import org.motechproject.sms.api.SmsEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static org.motechproject.sms.api.service.SmsService.*;

@Component
public class SmsSendHandler implements SmsEventHandler {
    private static Logger log = LoggerFactory.getLogger(SmsSendHandler.class);
    private SmsSendTemplate template;
    private HttpClient httpClient;

    @Autowired
    public SmsSendHandler(TemplateReader templateReader) {
        String templateFile = "/sms-http-template.json";
        this.template = templateReader.getTemplate(templateFile);
        this.httpClient = new HttpClient();
    }

    @Override
    @MotechListener(subjects = SEND_SMS)
    public void handle(MotechEvent event) throws IOException {
        List<String> recipients = (List<String>) event.getParameters().get(RECIPIENTS);
        String message = (String) event.getParameters().get(MESSAGE);
        HttpMethod httpMethod = template.generateRequestFor(recipients, message);
        int status = httpClient.executeMethod(httpMethod);
        log.info("HTTP Status:"+status + "|Response:" + httpMethod.getResponseBodyAsString());
    }
}