package se.kits.awslog;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Gui {

    private static List<KitsLogGroup> kitsLogGroups;

    public static void startGui() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Keep it Streaming");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new GridBagLayout());

        JList<String> mainList = new JList<>();
        // Create components with different background colors
        JPanel mainArea = new JPanel();
//        mainArea.setLayout(new BoxLayout(mainArea, BoxLayout.X_AXIS));
        mainArea.setLayout(new CardLayout());
        mainArea.setBackground(Color.LIGHT_GRAY);
//        StringBuilder text = new StringBuilder();
        try (ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                .profileName(App.profileName)
                .build()) {
            List<KitsLogGroup> logs = CloudWatch.getLogs(App.region, profileCredentialsProvider, "Konpro-Audit");
            for (KitsLogGroup log : logs) {
                mainList.add(new JLabel(log.logGroupName()));
//                text.append(log.logGroupName()).append(" ").append(log.creationTime()).append("\n");
            }
//            mainList.setText(text.toString());
        }
//        textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
//        textArea.setAlignmentY(Component.CENTER_ALIGNMENT);
        mainArea.add(mainList);

        JPanel sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBackground(Color.ORANGE);
        sideBar.setPreferredSize(new Dimension(200, 0)); // Fixed width
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideBar, mainArea);

        DefaultListModel<String> logStreamsModel = new DefaultListModel<>();
        JList<String> logStreams = new JList<>(logStreamsModel);
        logStreams.setAlignmentX(Component.CENTER_ALIGNMENT);
        logStreams.setAlignmentY(Component.CENTER_ALIGNMENT);
        logStreamsModel.addElement("Hej");
        try (ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                .profileName(App.profileName)
                .build()) {
            kitsLogGroups = CloudWatch.getLogs(App.region, profileCredentialsProvider, "Konpro-Audit");
            for (KitsLogGroup log : kitsLogGroups) {
                logStreamsModel.addElement(log.logGroupName() + " " + log.creationTime());
            }
        }
        JScrollPane scrollPane = new JScrollPane(logStreams);
        sideBar.add(scrollPane);
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
}
