package sk.mvp.multiservice.notifyservice.dto;

import java.util.List;

public record SendGridRequest(
        List<Personalization> personalizations,
        From from,
        List<Content> content
) {
    // Adpater/ Mappers between DTO obejects
    public static SendGridRequest fromDomain(EmailRequest domainRequest,
                                             String htmlContent,
                                             String fromEmail,
                                             String fromName) {
        return new SendGridRequest(
                List.of(Personalization.fromDomain(domainRequest)),
                new From(fromEmail, fromName),
                List.of(new Content("text/html", htmlContent))
        );

    }
    public static SendGridRequest fromDomain(String toEmail,
                                             String subject,
                                             String htmlContent,
                                             String fromEmail,
                                             String fromName) {
        return new SendGridRequest(
                List.of(new Personalization(List.of(new To(toEmail)), subject)),
                new From(fromEmail, fromName),
                List.of(new Content("text/html", htmlContent))
        );
    }

    // Nested definitions
    public record Personalization(List<To> to, String subject) {
        public static Personalization fromDomain(EmailRequest domainRequest) {
            return new Personalization(
                    List.of(new To(domainRequest.to())),
                    domainRequest.subject()
            );
        }
    }
    public record To(String email) {}
    public record From(String email, String name) {}
    public record Content(String type, String value) {}
}

