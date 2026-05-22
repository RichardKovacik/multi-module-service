package sk.mvp.multiservice.notifyservice.service;

import sk.mvp.multiservice.notifyservice.dto.EmailRequest;

public interface IEmailProvider {
    void sendEmail(EmailRequest request);
}
