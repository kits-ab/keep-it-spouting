package se.kits.awslog;

import java.time.LocalDateTime;

public record KitsLogGroup(String logGroupName, LocalDateTime creationTime) {
}
