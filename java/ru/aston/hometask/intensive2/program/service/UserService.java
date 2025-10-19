package ru.aston.hometask.intensive2.program.service;

import ru.aston.hometask.intensive2.program.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(String name, String email, Integer age);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, String name, String email, Integer age);
    void deleteUser(Long id);
    Optional<User> getUserByEmail(String email);
    boolean isEmailUnique(String email);
}

