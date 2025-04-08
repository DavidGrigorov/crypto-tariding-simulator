package com.crypto.repository.impl;

import com.crypto.models.Transaction;
import com.crypto.repository.TransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    private final JdbcTemplate jdbc;

    public TransactionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    @Override
    public Transaction findById(int id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        return jdbc.queryForObject(sql, new TransactionRowMapper(), id);
    }

    @Override
    public List<Transaction> findByUser(int userId) {
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY timestamp DESC";
        return jdbc.query(sql, new TransactionRowMapper(), userId);
    }

    @Override
    public Transaction save(Transaction transaction) {
        String sql = "INSERT INTO transactions(user_id, symbol, transaction_type, amount, price, total_value, timestamp) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?) RETURNING id";

        Integer id = jdbc.queryForObject(sql, Integer.class,
                transaction.getUserId(),
                transaction.getSymbol(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getPrice(),
                transaction.getTotalValue(),
                transaction.getTimestamp());

        transaction.setId(id);
        return transaction;
    }

    @Override
    public void delete(int transactionId) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        jdbc.update(sql, transactionId);

    }

    @Override
    public void deleteAllByUser(int userId) {
        String sql = "DELETE FROM transactions WHERE user_id = ?";
        jdbc.update(sql, userId);
    }

    private static class TransactionRowMapper implements RowMapper<Transaction> {
        @Override
        public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
            Transaction transaction = new Transaction();
            transaction.setId(rs.getInt("id"));
            transaction.setUserId(rs.getInt("user_id"));
            transaction.setSymbol(rs.getString("symbol"));
            transaction.setTransactionType(rs.getString("transaction_type"));
            transaction.setAmount(rs.getDouble("amount"));
            transaction.setPrice(rs.getDouble("price"));
            transaction.setTotalValue(rs.getDouble("total_value"));
            transaction.setTimestamp(rs.getTimestamp("timestamp"));
            return transaction;
        }

    }
}
