package se.kits.awslog.eventstore;

import org.slf4j.Logger;
import se.kits.awslog.KitsLogEvent;

import static org.slf4j.LoggerFactory.getLogger;

public class EventLogger {
    private static final Logger logger = getLogger(EventLogger.class);
    public static void logEvent(KitsLogEvent event) {
        logger.trace("{}", event.content());
    }
}
