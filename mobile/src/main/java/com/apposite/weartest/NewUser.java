package com.apposite.weartest;

class NewUser {

    private String email;
    private String name;
    private String accountNumber;
    private String branchCode;

    public NewUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public NewUser(String email, String name, String accountNumber, String branchCode) {
        this.email = email;
        this.name = name;
        this.accountNumber = accountNumber;
        this.branchCode = branchCode;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBranchCode() {
        return branchCode;
    }
}
