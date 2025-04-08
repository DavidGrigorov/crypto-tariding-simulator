package com.crypto.repository;

import com.crypto.models.User;

public interface UserRepository {

    User createUser(User user);
    User findById(int id);
    User findByUsername(String username);
    User save (User save);
    void updateBalance(int userId, double newBalance);
}
