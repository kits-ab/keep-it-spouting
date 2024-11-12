package se.kits.awslog;

import org.slf4j.Logger;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class CloudWatch {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CloudWatch.class);
    public static List<KitsLogGroup> getLogGroups(Region region, ProfileCredentialsProvider profileCredentialsProvider, String logGroupsPattern) {
        try (CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                .region(region)
                .credentialsProvider(profileCredentialsProvider)
                .build()) {
            // Create a request to describe log groups
            DescribeLogGroupsRequest request = logGroupsPattern == null || logGroupsPattern.isEmpty() ?
                    DescribeLogGroupsRequest.builder()
                    .limit(10).build():
                    DescribeLogGroupsRequest.builder()
                    .logGroupNamePattern(logGroupsPattern)
                    .limit(10).build();

            // Send the request and receive the response
            DescribeLogGroupsResponse response = client.describeLogGroups(request);

            List<KitsLogGroup> kitsLogGroups = new ArrayList<>();
            // Process and print the log group names
            for (LogGroup logGroup : response.logGroups()) {
                Instant creationMillis = Instant.ofEpochMilli(logGroup.creationTime());
                LocalDateTime creationTime = LocalDateTime.ofInstant(creationMillis, ZoneId.systemDefault());
                kitsLogGroups.add(new KitsLogGroup(logGroup.logGroupName(), creationTime));
                logger.info("Log Group Name: {} , created: {}", logGroup.logGroupName(), App.awsDateFormat.format(creationTime));
            }
            return kitsLogGroups;
        } catch (CloudWatchLogsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return new ArrayList<>();
    }

    public static List<KitsLogStream> getLogStreams(Region region, ProfileCredentialsProvider profileCredentialsProvider, String logGroupName) {
        try (CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                .region(region)
                .credentialsProvider(profileCredentialsProvider)
                .build()) {

            DescribeLogStreamsRequest request = DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName)
                .descending(true)
                .limit(10)
                .build();
            DescribeLogStreamsResponse response = client.describeLogStreams(request);
            List<KitsLogStream> kitsLogStreams = new ArrayList<>();
            for (LogStream logStream : response.logStreams()) {
                Instant lastEventMillis = Instant.ofEpochMilli(logStream.lastEventTimestamp());
                LocalDateTime lastEventTime = LocalDateTime.ofInstant(lastEventMillis, ZoneId.systemDefault());
                kitsLogStreams.add(new KitsLogStream(logStream.logStreamName(), logGroupName, lastEventTime));
                logger.info("Log Stream Name: {} {}", logStream.logStreamName(), App.awsDateFormat.format(lastEventTime));
            }
            return kitsLogStreams;
        }
    }

    public static List<KitsLogEvent> getLogEvents(Region region, ProfileCredentialsProvider profileCredentialsProvider, KitsLogStream kitsLogStream) {
        try (CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                .region(region)
                .credentialsProvider(profileCredentialsProvider)
                .build()) {

            GetLogEventsRequest getLogEventsRequest = GetLogEventsRequest.builder()
                .logGroupName(kitsLogStream.logGroupName())
                .logStreamName(kitsLogStream.logStreamName())
                .startFromHead(true)
                .limit(100)
                .build();
            GetLogEventsResponse response = client.getLogEvents(getLogEventsRequest);

        // Iterate and print log events
        for (OutputLogEvent logEvent : response.events()) {
            System.out.println("Timestamp: " + logEvent.timestamp());
            System.out.println("Message: " + logEvent.message());
            System.out.println("-----------------------------------");
        }
        List<KitsLogEvent> kitsLogEvents = new ArrayList<>();
            for (OutputLogEvent logEvent : response.events()) {
                Instant eventMillis = Instant.ofEpochMilli(logEvent.timestamp());
                LocalDateTime eventTime = LocalDateTime.ofInstant(eventMillis, ZoneId.systemDefault());
                kitsLogEvents.add(new KitsLogEvent(logEvent.message(), eventTime));
                logger.info("Log Message: {} {}", logEvent.message(), App.awsDateFormat.format(eventTime));
            }
            return kitsLogEvents;
        }
    }
}
