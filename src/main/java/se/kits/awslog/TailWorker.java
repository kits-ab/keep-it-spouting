package se.kits.awslog;

import org.slf4j.Logger;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;

import javax.swing.*;

import java.time.Instant;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

public class TailWorker extends SwingWorker<Void, EventRow> {
    private static final Logger logger = getLogger(TailWorker.class);
    private final List<KitsLogStream> logStreams;
    private final String logGroup;
    private final DefaultListModel<EventRow> tailModel;

    public TailWorker(List<KitsLogStream> logStreams, String logGroup, DefaultListModel<EventRow> tailModel) {
        this.logStreams = logStreams;
        this.logGroup = logGroup;
        this.tailModel = tailModel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        try (ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                .profileName(App.profileName)
                .build();
             CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                     .region(App.region)
                     .credentialsProvider(profileCredentialsProvider)
                     .build()
        ) {
            long startTime = System.currentTimeMillis();
            long timelimit = startTime + 60 * 60 * 100L;
            String nextToken = null;
            List<String> logStreamStrings = logStreams.stream().map(KitsLogStream::logStreamName).toList();
            while (timelimit > System.currentTimeMillis()) {
                try {
                    FilterLogEventsRequest request = FilterLogEventsRequest.builder()
                            .logGroupName(logGroup)
                            .logStreamNames(logStreamStrings)
                            .nextToken(nextToken)
                            .startTime(Instant.now().minusSeconds(60).toEpochMilli()) // Adjust as necessary
                            .limit(10).build();
                    KitsTailResponse result = CloudWatch.tail(client, nextToken, logStreamStrings, request.builder());
                    nextToken = result.nextToken();
                    List<String> events = result.eventMessages();
                    for (String event : events) {
                        publish(new EventRow(event, 0));
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        } catch (software.amazon.awssdk.core.exception.SdkClientException e) {
            logger.error("SSO login maybe? {}", e.getMessage());
            System.exit(-1);
        }
        return null;
    }

    @Override
    public void process(List<EventRow> events) {
        events.forEach(tailModel::addElement);
    }
}
