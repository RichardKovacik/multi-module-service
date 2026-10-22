package sk.mvp.user_service.user.entity;

import java.util.Set;

public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    BLOCKED,
    REMOVED;
    static {
        PENDING_VERIFICATION.nextStates = Set.of(ACTIVE, REMOVED);
        ACTIVE.nextStates = Set.of(BLOCKED, REMOVED);
        BLOCKED.nextStates = Set.of(ACTIVE, REMOVED);
        REMOVED.nextStates = Set.of();
    }
    private Set<UserStatus> nextStates;

    public boolean isNextStateValid(UserStatus nextState) {
        return this.nextStates.contains(nextState);
    }
}
