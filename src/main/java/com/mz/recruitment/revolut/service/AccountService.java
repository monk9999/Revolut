package com.mz.recruitment.revolut.service;

import com.mz.recruitment.revolut.model.transfer.TransferResult;

import java.math.BigDecimal;

public interface AccountService {

    TransferResult makeTransfer(String from, String to, BigDecimal amount);
}
