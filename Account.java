
package BankingSystem;

public class Account {
    private String accountNumber;
    private String name;
    private String pin;
    private double balance;
    private double fixedDeposit;

    public Account(String accountNumber, String name, String pin) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.pin = pin;
        this.balance = 0.0;
        this.fixedDeposit = 0.0;
    }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public String getPin() { return pin; }
}
