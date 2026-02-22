import java.util.*;
import java.math.BigDecimal; // For precise financial calculations
import java.time.LocalDateTime;

/**
 * @author Geremech20261993
 * Project: Secure Enterprise Banking System
 * Features: Thread-safety, Audit Logging, Custom Exceptions, and Financial Precision.
 */

// 1. Custom Exception for Banking Errors
class BankingException extends Exception {
    public BankingException(String message) {
        super(message);
    }
}

// 2. Service Interface for Abstracting Logic
interface IBankService {
    void transfer(BankAccount target, BigDecimal amount) throws BankingException;
    BigDecimal getBalance();
}

// 3. Robust Bank Account Implementation
class BankAccount implements IBankService {
    private final String accountId;
    private final String ownerName;
    private BigDecimal balance; 
    private final List<String> auditLog = new ArrayList<>();

    public BankAccount(String accountId, String ownerName, BigDecimal initialBalance) {
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.balance = initialBalance;
        logAction("Account created with initial balance: $" + initialBalance);
    }

    /**
     * Executes a secure transfer between accounts.
     * Synchronized keyword ensures thread-safety during concurrent transactions.
     */
    @Override
    public synchronized void transfer(BankAccount target, BigDecimal amount) throws BankingException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Transfer amount must be greater than zero.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new BankingException("Insufficient funds in account: " + accountId);
        }

        this.balance = this.balance.subtract(amount);
        target.receive(amount);
        
        logAction("Transferred $" + amount + " to " + target.accountId);
    }

    public synchronized void receive(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        logAction("Received incoming transfer of $" + amount);
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    private void logAction(String message) {
        auditLog.add(LocalDateTime.now() + " [AUDIT]: " + message);
    }

    public void printFullStatement() {
        System.out.println("\n============================================");
        System.out.println("OFFICIAL STATEMENT: " + ownerName + " (" + accountId + ")");
        System.out.println("============================================");
        auditLog.forEach(System.out::println);
        System.out.println("--------------------------------------------");
        System.out.println("Final Ledger Balance: $" + balance);
        System.out.println("============================================\n");
    }
}

// 4. Main Application Entry Point
public class GlobalBankingSystem {
    public static void main(String[] args) {
        // Initializing accounts with BigDecimal values
        BankAccount accountA = new BankAccount("GLOBAL-001", "Geremech", new BigDecimal("10000.00"));
        BankAccount accountB = new BankAccount("GLOBAL-002", "Obsa", new BigDecimal("500.00"));

        try {
            System.out.println("[System] Initiating secure transaction sequence...");
            
            // Transaction 1: Successful transfer
            accountA.transfer(accountB, new BigDecimal("2500.00"));
            
            // Transaction 2: Testing exception handling (Attempting to overdraw)
            System.out.println("[System] Attempting high-value transfer...");
            accountA.transfer(accountB, new BigDecimal("9000.00")); 

        } catch (BankingException e) {
            System.err.println("[Transaction Error]: " + e.getMessage());
        } finally {
            // Print final states regardless of transaction success
            accountA.printFullStatement();
            accountB.printFullStatement();
            System.out.println("[System] All processes finalized.");
        }
    }
}
