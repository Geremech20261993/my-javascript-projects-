import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Geremech20261993
 * Project: Advanced Inventory Management System
 * Level: Enterprise Standard (Thread-safe, File Persistence)
 */

// 1. Product Model (Serializable for file storage)
class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private int quantity;

    public Product(String id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public synchronized void updateStock(int amount) {
        this.quantity += amount;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Stock: %d", id, name, quantity);
    }
}

// 2. Warehouse Management Logic
class WarehouseManager {
    // ConcurrentHashMap ensures the system doesn't crash during multiple simultaneous orders
    private final Map<String, Product> inventory = new ConcurrentHashMap<>();
    private final String DATA_FILE = "warehouse_data.dat";

    // Register a new product in the system
    public void addProduct(Product p) {
        inventory.put(p.getId(), p);
        saveData();
    }

    // Process orders using synchronized method to prevent stock errors
    public synchronized void processOrder(String productId, int qty) {
        Product p = inventory.get(productId);
        if (p != null) {
            p.updateStock(-qty);
            System.out.println("[Order] Successfully processed " + qty + " units for Product ID: " + productId);
        } else {
            System.err.println("[Error] Product ID " + productId + " not found in inventory!");
        }
        saveData();
    }

    // Persist data to a local file (Data Persistence)
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(new ArrayList<>(inventory.values()));
        } catch (IOException e) {
            System.err.println("[Critical] Data persistence error: " + e.getMessage());
        }
    }

    public void displayInventory() {
        System.out.println("\n--- Current Warehouse Inventory Status ---");
        inventory.values().forEach(System.out::println);
    }
}

// 3. Main Execution Class
public class GlobalSupplyChainSystem {
    public static void main(String[] args) {
        WarehouseManager manager = new WarehouseManager();

        // Adding initial stock
        manager.addProduct(new Product("P101", "High-End Laptop", 50));
        manager.addProduct(new Product("P102", "Ultra Smartphone", 100));

        // Simulate multiple customers ordering at the same time using Thread Pool
        ExecutorService executor = Executors.newFixedThreadPool(3);

        System.out.println("[System] Initializing global order processing...");

        executor.execute(() -> manager.processOrder("P101", 5));
        executor.execute(() -> manager.processOrder("P102", 10));
        executor.execute(() -> manager.processOrder("P101", 2));

        executor.shutdown();
        try {
            // Wait for all orders to complete before showing final report
            if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                manager.displayInventory();
                System.out.println("\n[System] Inventory successfully updated and saved to: " + "warehouse_data.dat");
            }
        } catch (InterruptedException e) {
            System.err.println("[System Error] Execution interrupted: " + e.getMessage());
        }
    }
}
