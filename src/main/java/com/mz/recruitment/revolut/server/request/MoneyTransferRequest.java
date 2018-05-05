package com.mz.recruitment.revolut.server.request;

import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;

public class MoneyTransferRequest {
    public static final String FROM_ACCOUNT_FIELD_NAME = "fromAccount";
    public static final String TO_ACCOUNT_FIELD_NAME = "toAccount";
    public static final String AMOUNT_FILED_NAME = "amount";

    private final String fromAccount;
    private final String toAccount;
    private final BigDecimal amount;

    public MoneyTransferRequest(String fromAccount, String toAccount, BigDecimal amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    public static MoneyTransferRequest fromJson(JsonObject json) {
        String fromAccount = json.getValue(FROM_ACCOUNT_FIELD_NAME).toString();
        String toAccount = json.getValue(TO_ACCOUNT_FIELD_NAME).toString();
        BigDecimal amount = new BigDecimal(json.getValue(AMOUNT_FILED_NAME).toString());
        return new MoneyTransferRequest(fromAccount, toAccount, amount);
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MoneyTransferRequest that = (MoneyTransferRequest) o;

        if (fromAccount != null ? !fromAccount.equals(that.fromAccount) : that.fromAccount != null) {
            return false;
        }
        if (toAccount != null ? !toAccount.equals(that.toAccount) : that.toAccount != null) {
            return false;
        }
        return amount != null ? amount.equals(that.amount) : that.amount == null;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int result = fromAccount != null ? fromAccount.hashCode() : 0;
        result = 31 * result + (toAccount != null ? toAccount.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        return result;
    }
}
