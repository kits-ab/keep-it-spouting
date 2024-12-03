package se.kits.awslog;

import se.kits.awslog.eventstore.EventLogger;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static se.kits.awslog.App.profileName;

public class Gui {

    public static String color1 = "ERROR";
    public static String color2 = "SUCCESS";
    private static List<KitsLogGroup> kitsLogGroups;
    private static List<KitsLogStream> kitsLogStreams;
    private static String logGroupsPattern = "";
    private static String selectedLogGroup = null;

    public static void startGui() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Keep it Streaming");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new GridBagLayout());

            DefaultListModel<String> logStreamsModel = new DefaultListModel<>();
            logStreamsModel.addElement("Select log group");
            DefaultListModel<EventRow> logModel = new DefaultListModel<>();
            DefaultListModel<EventRow> tailModel = new DefaultListModel<>();
            logModel.addElement(new EventRow("Select log stream", 1));

            DefaultListModel<String> logGroupsModel = new DefaultListModel<>();
            JPanel groupPanel = createGroupPanel(logGroupsModel, logStreamsModel);
            JPanel streamPanel = createStreamPanel(logStreamsModel, logModel);
            JPanel logPanel = createLogPanel(logModel);
            JPanel tailPanel = createTailPanel(tailModel);

            JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groupPanel, streamPanel);
            topSplitPane.setOneTouchExpandable(true);
            topSplitPane.setDividerLocation(200);
            JSplitPane bottomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logPanel, tailPanel);
            topSplitPane.setOneTouchExpandable(true);
            topSplitPane.setDividerLocation(200);
            JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplitPane, bottomSplitPane);
            topSplitPane.setOneTouchExpandable(true);
            topSplitPane.setDividerLocation(200);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.BLUE);

            // Add buttons to the button panel
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            JTextField inputField = new JTextField("", 20);
            buttonPanel.add(inputField);
            JButton setLogGroupPatternButton = new JButton("Set Log Group Pattern");
            setLogGroupPatternButton.addActionListener(e -> {
                logGroupsPattern = inputField.getText().trim();
                logGroupsModel.clear();
                fetchLogGroups(logGroupsModel);
            });
            buttonPanel.add(setLogGroupPatternButton);

            JButton color1Button = new JButton("Color 1 pattern");
            buttonPanel.add(color1Button);
            color1Button.addActionListener(e -> color1 = inputField.getText().trim());

            JButton color2Button = new JButton("Color 2 pattern");
            buttonPanel.add(color2Button);
            color2Button.addActionListener(e -> color2 = inputField.getText().trim());

            JButton tailButton = new JButton("Tail");
            buttonPanel.add(tailButton);
            tailButton.addActionListener(e -> new TailWorker(kitsLogStreams, selectedLogGroup, tailModel));

            GridBagConstraints gbc = new GridBagConstraints();

            // Add the main area, taking most of the space
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            frame.add(mainSplitPane, gbc);

            // Add the button panel at the bottom
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            frame.add(buttonPanel, gbc);

            // Display the frame
            frame.setLocationRelativeTo(null); // Center the frame
            frame.setVisible(true);
        });
    }

    private static JPanel createGroupPanel(DefaultListModel<String> logGroupsModel, DefaultListModel<String> logStreamsModel) {
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setBackground(Color.ORANGE);
        groupPanel.setPreferredSize(new Dimension(200, 0)); // Fixed width


        JList<String> logGroups = new JList<>(logGroupsModel);
        logGroups.setAlignmentX(Component.CENTER_ALIGNMENT);
        logGroups.setAlignmentY(Component.CENTER_ALIGNMENT);
        fetchLogGroups(logGroupsModel);
        logGroups.addListSelectionListener(e -> {
            selectedLogGroup = kitsLogGroups.get(e.getFirstIndex()).logGroupName();
            logStreamsModel.clear();
            try (
                    ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                            .profileName(profileName)
                            .build();
                    CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                            .region(App.region)
                            .credentialsProvider(profileCredentialsProvider)
                            .build()) {
                kitsLogStreams = CloudWatch.getLogStreams(client, selectedLogGroup);
                for (KitsLogStream stream : kitsLogStreams) {
                    logStreamsModel.addElement(stream.logStreamName() + " " + stream.lastEventTime());
                }
            }
        });
        JScrollPane sideScrollPane = new JScrollPane(logGroups);
        groupPanel.add(sideScrollPane);
        return groupPanel;
    }

    private static void fetchLogGroups(DefaultListModel<String> logGroupsModel) {
        try (ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                .profileName(profileName)
                .build()) {
            kitsLogGroups = CloudWatch.getLogGroups(App.region, profileCredentialsProvider, logGroupsPattern);
            for (KitsLogGroup log : kitsLogGroups) {
                logGroupsModel.addElement(log.logGroupName() + " " + log.creationTime());
            }
        }
    }

    private static JPanel createStreamPanel(DefaultListModel<String> logStreamsModel, DefaultListModel<EventRow> logModel) {
        JPanel streamsArea = new JPanel();
        JList<String> logStreamsList = new JList<>(logStreamsModel);
        JScrollPane mainScrollPane = new JScrollPane(logStreamsList);
        streamsArea.add(mainScrollPane);
        streamsArea.setLayout(new BoxLayout(streamsArea, BoxLayout.Y_AXIS));
        streamsArea.setBackground(Color.LIGHT_GRAY);
        logStreamsList.setAlignmentX(Component.CENTER_ALIGNMENT);
        logStreamsList.setAlignmentY(Component.CENTER_ALIGNMENT);
        logStreamsList.addListSelectionListener(e -> {
            KitsLogStream key = kitsLogStreams.get(e.getFirstIndex());
            logModel.clear();
            try (ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                    .profileName(profileName)
                    .build()) {
                List<KitsLogEvent> logStreams = CloudWatch.getLogEvents(App.region, profileCredentialsProvider, key);
                for (KitsLogEvent stream : logStreams) {
                    int colorIndex = 0;
                    EventLogger.logEvent(new KitsLogEvent(stream.content(), stream.eventTime()));
                    String message = stream.content() + " " + stream.eventTime();
                    if (message.contains(color1)) {
                        colorIndex = 1;
                    }
                    if (message.contains(color2)) {
                        colorIndex = 2;
                    }
                    logModel.addElement(new EventRow(message, colorIndex));
                }
            }
        });
        return streamsArea;
    }

    private static JPanel createTailPanel(DefaultListModel<EventRow> tailModel) {
        JPanel tailPanel = new JPanel();
        JList<EventRow> logs = new JList<>(tailModel);
        logs.setCellRenderer(new LogCellRenderer());
        JScrollPane logScrollPane = new JScrollPane(logs);
        tailPanel.add(logScrollPane);
        tailPanel.setLayout(new BoxLayout(tailPanel, BoxLayout.Y_AXIS));
        tailPanel.setBackground(Color.GREEN);
        logs.setAlignmentX(Component.CENTER_ALIGNMENT);
        logs.setAlignmentY(Component.CENTER_ALIGNMENT);
        return tailPanel;
    }

    private static JPanel createLogPanel(DefaultListModel<EventRow> logModel) {
        JPanel logPanel = new JPanel();
        JList<EventRow> logs = new JList<>(logModel);
        logs.setCellRenderer(new LogCellRenderer());
        JScrollPane logScrollPane = new JScrollPane(logs);
        logPanel.add(logScrollPane);
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(Color.LIGHT_GRAY);
        logs.setAlignmentX(Component.CENTER_ALIGNMENT);
        logs.setAlignmentY(Component.CENTER_ALIGNMENT);
        return logPanel;
    }
}
