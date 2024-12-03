package se.kits.awslog;

import java.util.List;
import java.util.Map;

public record KitsTailResponse(String nextToken, List<String> eventMessages) {
}
