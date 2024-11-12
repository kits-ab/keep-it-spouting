package se.kits.awslog;

import java.time.LocalDateTime;

public record KitsLogStream(String name, LocalDateTime lastEventTime) {
}
