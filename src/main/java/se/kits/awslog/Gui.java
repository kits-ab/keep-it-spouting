package se.kits.awslog;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Gui {

    private static List<KitsLogGroup> kitsLogGroups;
    private static String logGroupsPattern = "";

    public static void startGui() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Keep it Streaming");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400);
            frame.setLayout(new GridBagLayout());

            DefaultListModel<String> logStreamsModel = new DefaultListModel<>();
            logStreamsModel.addElement("Select log group");
            JPanel mainArea = createStreamPanel(logStreamsModel);

            JPanel sideBar = createGroupPanel(logStreamsModel);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideBar, mainArea);
            splitPane.setOneTouchExpandable(true);
            splitPane.setDividerLocation(200);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.BLUE);

            // Add buttons to the button panel
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(new JButton("Button 1"));
            buttonPanel.add(new JButton("Button 2"));
            buttonPanel.add(new JButton("Button 3"));

            GridBagConstraints gbc = new GridBagConstraints();

            // Add the main area, taking most of the space
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            frame.add(splitPane, gbc);

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

    private static JPanel createGroupPanel(DefaultListModel<String> logStreamsModel) {
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setBackground(Color.ORANGE);
        groupPanel.setPreferredSize(new Dimension(200, 0)); // Fixed width


        DefaultListModel<String> logGroupsModel = new DefaultListModel<>();
        JList<String> logGroups = new JList<>(logGroupsModel);
        logGroups.setAlignmentX(Component.CENTER_ALIGNMENT);
        logGroups.setAlignmentY(Component.CENTER_ALIGNMENT);
        try (ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                .profileName(App.profileName)
                .build()) {
            kitsLogGroups = CloudWatch.getLogGroups(App.region, profileCredentialsProvider, logGroupsPattern);
            for (KitsLogGroup log : kitsLogGroups) {
                logGroupsModel.addElement(log.logGroupName() + " " + log.creationTime());
            }
        }
        logGroups.addListSelectionListener(e -> {
            String key = kitsLogGroups.get(e.getFirstIndex()).logGroupName();
            logStreamsModel.clear();
            try (ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                    .profileName(App.profileName)
                    .build()) {
                List<KitsLogStream> logStreams = CloudWatch.getLogStreams(App.region, profileCredentialsProvider, key);
                for (KitsLogStream stream : logStreams) {
                    logStreamsModel.addElement(stream.name() + " " + stream.lastEventTime());
                }
            }
        });
        JScrollPane sideScrollPane = new JScrollPane(logGroups);
        groupPanel.add(sideScrollPane);
        return groupPanel;
    }

    private static JPanel createStreamPanel(DefaultListModel<String> logStreamsModel) {
        JPanel streamsArea = new JPanel();
        JList<String> logStreams = new JList<>(logStreamsModel);
        JScrollPane mainScrollPane = new JScrollPane(logStreams);
        streamsArea.add(mainScrollPane);
        streamsArea.setLayout(new BoxLayout(streamsArea, BoxLayout.Y_AXIS));
        streamsArea.setBackground(Color.LIGHT_GRAY);
        logStreams.setAlignmentX(Component.CENTER_ALIGNMENT);
        logStreams.setAlignmentY(Component.CENTER_ALIGNMENT);
        return streamsArea;
    }
}
