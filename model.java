package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String authorEmail;
    private final String authorName;
    private final String message;
    private final LocalDateTime timestamp;

    public Comment(String authorEmail, String authorName, String message) {
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getAuthorEmail() { return authorEmail; }
    public String getAuthorName() { return authorName; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
