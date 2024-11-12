package se.kits.awslog;

import java.time.LocalDateTime;

public record KitsLogEvent(String content, LocalDateTime eventTime) {
}
