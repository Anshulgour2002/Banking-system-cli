
package BankingSystem;


public class Transaction {
    private String accountNumber;
    private String type;
    private double amount;
    private String note;

    public Transaction(String accountNumber, String type, double amount, String note) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.note = note;
    }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }
}

