package sk.mvp.common.event;

public enum EventType {
    USER_REGISTERED,
    PASSWORD_RESET_REQUESTED,
    UNKNOWN_EVENT;

    public EventType fromString(String value) {
        try {
            return EventType.valueOf(value);
        } catch (Exception e) {
            return UNKNOWN_EVENT;
        }
    }
}
