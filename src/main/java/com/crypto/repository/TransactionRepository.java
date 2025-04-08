package com.crypto.repository;

import com.crypto.models.Transaction;

import java.util.List;

public interface TransactionRepository {
    Transaction findById(int id);
    List<Transaction> findByUser(int userId);
    Transaction save(Transaction transaction);
    void delete(int transactionId);
    void deleteAllByUser(int userId);
}
