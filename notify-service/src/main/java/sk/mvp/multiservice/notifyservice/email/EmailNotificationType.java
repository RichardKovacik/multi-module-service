package sk.mvp.multiservice.notifyservice.email;

public enum EmailNotificationType {
    REGISTRATION_VERIFICATION("registration-verification"),
    PASSWORD_RESET("password-reset");

    private final String templateName;

    EmailNotificationType(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    /**
     *  messageSource (napr. "email.subject.password-reset")
     */
    public String getSubjectKey() {
        return "email.subject." + this.templateName;
    }
}
