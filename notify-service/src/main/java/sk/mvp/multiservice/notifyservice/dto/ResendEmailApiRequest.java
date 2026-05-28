package sk.mvp.multiservice.notifyservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResendEmailApiRequest(
        @JsonProperty("from") String from,
        @JsonProperty("to") String[] to,
        @JsonProperty("subject") String subject,
        @JsonProperty("html") String html
) {

}
