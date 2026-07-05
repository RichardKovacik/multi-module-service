package sk.mvp.multiservice.notifyservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResendEmailApiRequest(
        @JsonProperty("fromz") String fromz,
        @JsonProperty("to") String[] to,
        @JsonProperty("subject") String subject,
        @JsonProperty("html") String html
) {

}
