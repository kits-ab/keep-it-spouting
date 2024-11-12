package se.kits.awslog;

public record EventRow(String message, int colorIndex) {
    @Override
    public String toString() {
        return message;
    }
}
