package se.kits.awslog;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import javax.swing.*;
import java.time.format.DateTimeFormatter;

/**
 * Hello world!
 *
 */
public class App 
{
    public static DateTimeFormatter awsDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static Region region;
    public static String profileName;

    public static void main( String[] args ) {
        // Set the native look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set the AWS profile name
        profileName = args.length > 0 ? args[0]:"konpro-admin";

        region = args.length > 1 ? Region.of(args[1]):Region.EU_WEST_1;

        Gui.startGui();
        // Create a ProfileCredentialsProvider with the specified profile
        try (ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                .profileName(profileName)
                .build()) {
            CloudWatch.getLogs(region, profileCredentialsProvider, "Konpro-Audit");
        } catch (software.amazon.awssdk.core.exception.SdkClientException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
