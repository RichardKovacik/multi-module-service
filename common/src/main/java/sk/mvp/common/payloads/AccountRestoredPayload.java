package sk.mvp.common.payloads;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record AccountRestoredPayload(
        String email,
        String link
) {
}
