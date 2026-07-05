package sk.mvp.multiservice.notifyservice.dto;

public record EmailApiRequest(String to,
                              String subject,
                              String htmlContent) {
}
