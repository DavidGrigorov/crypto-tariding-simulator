package com.crypto.repository.impl;

import com.crypto.models.User;
import com.crypto.repository.UserRepository;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public User createUser(User user) {
        return save(user);
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbc.queryForObject(sql, new UserRowMapper(), id);
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return jdbc.queryForObject(sql, new UserRowMapper(), username);
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users(username, balance) VALUES(?, ?) RETURNING id";
        int id = jdbc.queryForObject(sql, int.class, user.getName(), user.getBalance());
        user.setId(id);
        return user;
    }

    @Override
    public void updateBalance(int userId, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE id = ?";
        int updated = jdbc.update(sql, newBalance, userId);
        if (updated != 1) {
            throw new RuntimeException("Failed to update balance for user: " + userId);
        }
        System.out.println("Successfully updated balance for user " + userId + " to " + newBalance);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("username"));
            user.setBalance(rs.getDouble("balance"));
            return user;
        }
    }
}
