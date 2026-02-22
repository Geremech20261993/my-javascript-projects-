import java.io.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Geremech20261993
 * Project: Security Audit & Transaction Logger
 * Goal: Track all system activities for large enterprises.
 */

class LogEntry implements Serializable {
    private String timestamp;
    private String action;
    private String status;

    public LogEntry(String action, String status) {
        this.timestamp = LocalDateTime.now().toString();
        this.action = action;
        this.status = status;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] ACTION: " + action + " | STATUS: " + status;
    }
}

public class TransactionLogger {
    private final Queue<LogEntry> logQueue = new LinkedList<>();
    private final String LOG_FILE = "system_audit.log";

    // Method to add logs (Used by Banking or Warehouse systems)
    public synchronized void logAction(String action, String status) {
        LogEntry entry = new LogEntry(action, status);
        logQueue.add(entry);
        System.out.println("Log recorded: " + action);
        saveLogsToFile();
    }

    private void saveLogsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logQueue.poll().toString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Audit Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        TransactionLogger logger = new TransactionLogger();
        
        System.out.println("--- Global System Audit Started ---");
        logger.logAction("User Login", "SUCCESS");
        logger.logAction("Warehouse Stock Update", "COMPLETED");
        logger.logAction("Bank Transfer P2P", "PENDING");
        
        System.out.println("All logs have been secured in system_audit.log");
    }
}
