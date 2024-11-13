package se.kits.awslog;

import org.slf4j.Logger;
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
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);
    public static DateTimeFormatter awsDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static Region region;
    public static String profileName;

    public static void main( String[] args ) {
        // Set the native look and feel
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        // Set the AWS profile name
        profileName = args.length > 0 ? args[0]:"konpro-admin";

        region = args.length > 1 ? Region.of(args[1]):Region.EU_WEST_1;

        Gui.startGui();
        // Create a ProfileCredentialsProvider with the specified profile
        try (ProfileCredentialsProvider profileCredentialsProvider = ProfileCredentialsProvider.builder()
                .profileName(profileName)
                .build()) {
          CloudWatch.tailLatest(region, profileCredentialsProvider, "/aws/lambda/Konpro-AuditLoggerD252FC81-05YD2GBS78C0");
//            CloudWatch.getLogGroups(region, profileCredentialsProvider, "Konpro-Audit");
        } catch (software.amazon.awssdk.core.exception.SdkClientException e) {
            logger.error("SSO login maybe? {}", e.getMessage());
            System.exit(-1);
        }
    }

}
