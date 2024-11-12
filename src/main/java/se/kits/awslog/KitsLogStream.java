package se.kits.awslog;

import java.time.LocalDateTime;

public record KitsLogStream(String logStreamName, String logGroupName, LocalDateTime lastEventTime) {
}
