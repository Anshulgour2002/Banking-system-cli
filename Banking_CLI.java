
package BankingSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Banking_CLI {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("\n====== Banking System ======");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Transfer Money");
            System.out.println("6. Change PIN");
            System.out.println("7. Transaction History");
            System.out.println("8. Make Fixed Deposit");
            System.out.println("9. Delete Account");
            System.out.println("10. Exit");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            try {
                switch (choice) {
                    case 1 ->createAccount();
                    case 2 -> depositMoney();
                    case 3 -> withdrawMoney();
                    case 4 -> checkBalance();
                    case 5 -> transferMoney();
                    case 6 -> changePin();
                    case 7 -> transactionHistory();
                    case 8 -> makeFixedDeposit();
                    case 9 -> deleteAccount();
                    case 10 -> System.out.println("Goodbye!");
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } while (choice != 10);
    }

    private static void createAccount() throws Exception {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Set 4-digit PIN: ");
        String pin = scanner.nextLine();

        Connection conn = DBConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement("INSERT INTO accounts (account_number, name, pin) VALUES (?, ?, ?)");
        pst.setString(1, accNo);
        pst.setString(2, name);
        pst.setString(3, pin);
        pst.executeUpdate();
        System.out.println("Account created successfully.");
        conn.close();
    }

    private static void depositMoney() throws Exception {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();
        System.out.print("Enter amount to deposit: ₹");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        Connection conn = DBConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
        pst.setDouble(1, amount);
        pst.setString(2, accNo);
        int rows = pst.executeUpdate();

        if (rows > 0) {
            PreparedStatement trans = conn.prepareStatement("INSERT INTO transactions (account_number, type, amount, note) VALUES (?, ?, ?, ?)");
            trans.setString(1, accNo);
            trans.setString(2, "Deposit");
            trans.setDouble(3, amount);
            trans.setString(4, "Cash Deposit");
            trans.executeUpdate();
            System.out.println("Deposit successful.");
        } else {
            System.out.println("Account not found.");
        }
        conn.close();
    }

    private static void withdrawMoney() throws Exception {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();
        System.out.print("Enter amount to withdraw: ₹");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        Connection conn = DBConnection.getConnection();
        PreparedStatement check = conn.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?");
        check.setString(1, accNo);
        ResultSet rs = check.executeQuery();
        if (rs.next()) {
            double balance = rs.getDouble("balance");
            if (balance >= amount) {
                PreparedStatement pst = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
                pst.setDouble(1, amount);
                pst.setString(2, accNo);
                pst.executeUpdate();

                PreparedStatement trans = conn.prepareStatement("INSERT INTO transactions (account_number, type, amount, note) VALUES (?, ?, ?, ?)");
                trans.setString(1, accNo);
                trans.setString(2, "Withdraw");
                trans.setDouble(3, amount);
                trans.setString(4, "Cash Withdrawal");
                trans.executeUpdate();
                System.out.println("Withdrawal successful.");
            } else {
                System.out.println("Insufficient balance.");
            }
        } else {
            System.out.println("Account not found.");
        }
        conn.close();
    }

    private static void checkBalance() throws Exception {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();

        Connection conn = DBConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement("SELECT name, balance, fixed_deposit FROM accounts WHERE account_number = ?");
        pst.setString(1, accNo);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            System.out.println("Name: " + rs.getString("name"));
            System.out.println("Balance: ₹" + rs.getDouble("balance"));
            System.out.println("Fixed Deposit: ₹" + rs.getDouble("fixed_deposit"));
        } else {
            System.out.println("Account not found.");
        }
        conn.close();
    }

    private static void transferMoney() throws Exception {
        System.out.print("Enter your account number: ");
        String fromAcc = scanner.nextLine();
        System.out.print("Enter recipient account number: ");
        String toAcc = scanner.nextLine();
        System.out.print("Enter amount to transfer: ₹");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            PreparedStatement check = conn.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?");
            check.setString(1, fromAcc);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getDouble("balance") >= amount) {
                PreparedStatement withdraw = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
                withdraw.setDouble(1, amount);
                withdraw.setString(2, fromAcc);
                withdraw.executeUpdate();

                PreparedStatement deposit = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
                deposit.setDouble(1, amount);
                deposit.setString(2, toAcc);
                deposit.executeUpdate();

                PreparedStatement trans = conn.prepareStatement("INSERT INTO transactions (account_number, type, amount, note) VALUES (?, ?, ?, ?), (?, ?, ?, ?)");
                trans.setString(1, fromAcc);
                trans.setString(2, "Transfer Sent");
                trans.setDouble(3, amount);
                trans.setString(4, "To: " + toAcc);
                trans.setString(5, toAcc);
                trans.setString(6, "Transfer Received");
                trans.setDouble(7, amount);
                trans.setString(8, "From: " + fromAcc);
                trans.executeUpdate();

                conn.commit();
                System.out.println("Transfer successful.");
            } else {
                System.out.println("Insufficient balance or sender account not found.");
            }
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private static void changePin() throws Exception {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();
        System.out.print("Enter new 4-digit PIN: ");
        String newPin = scanner.nextLine();

        Connection conn = DBConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement("UPDATE accounts SET pin = ? WHERE account_number = ?");
        pst.setString(1, newPin);
        pst.setString(2, accNo);
        int rows = pst.executeUpdate();

        if (rows > 0) {
            System.out.println("PIN changed successfully.");
        } else {
            System.out.println("Account not found.");
        }
        conn.close();
    }

    private static void transactionHistory() throws Exception {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();

        Connection conn = DBConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement("SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp DESC");
        pst.setString(1, accNo);
        ResultSet rs = pst.executeQuery();

        System.out.println("\n--- Transaction History ---");
        while (rs.next()) {
            System.out.println(rs.getTimestamp("timestamp") + " | " + rs.getString("type") + " | ₹" + rs.getDouble("amount") + " | " + rs.getString("note"));
        }
        conn.close();
    }

    private static void makeFixedDeposit() throws Exception {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();
        System.out.print("Enter FD amount: ₹");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        Connection conn = DBConnection.getConnection();
        PreparedStatement check = conn.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?");
        check.setString(1, accNo);
        ResultSet rs = check.executeQuery();
        if (rs.next() && rs.getDouble("balance") >= amount) {
            PreparedStatement pst = conn.prepareStatement("UPDATE accounts SET balance = balance - ?, fixed_deposit = fixed_deposit + ? WHERE account_number = ?");
            pst.setDouble(1, amount);
            pst.setDouble(2, amount);
            pst.setString(3, accNo);
            pst.executeUpdate();

            PreparedStatement trans = conn.prepareStatement("INSERT INTO transactions (account_number, type, amount, note) VALUES (?, ?, ?, ?)");
            trans.setString(1, accNo);
            trans.setString(2, "Fixed Deposit");
            trans.setDouble(3, amount);
            trans.setString(4, "FD Created");
            trans.executeUpdate();

            System.out.println("FD created successfully.");
        } else {
            System.out.println("Insufficient balance or account not found.");
        }
        conn.close();
    }

    private static void deleteAccount() throws Exception {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();

        Connection conn = DBConnection.getConnection();
        PreparedStatement deleteTransactions = conn.prepareStatement("DELETE FROM transactions WHERE account_number = ?");
        deleteTransactions.setString(1, accNo);
        deleteTransactions.executeUpdate();

        PreparedStatement deleteAccount = conn.prepareStatement("DELETE FROM accounts WHERE account_number = ?");
        deleteAccount.setString(1, accNo);
        int rows = deleteAccount.executeUpdate();

        if (rows > 0) {
            System.out.println("Account deleted successfully.");
        } else {
            System.out.println("Account not found.");
        }
        conn.close();
    }
}
