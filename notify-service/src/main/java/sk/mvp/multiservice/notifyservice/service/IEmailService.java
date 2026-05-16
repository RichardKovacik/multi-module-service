package sk.mvp.multiservice.notifyservice.service;

import sk.mvp.multiservice.notifyservice.dto.EmailRequest;

public interface IEmailService {
    void sendEmail(EmailRequest request);
}
