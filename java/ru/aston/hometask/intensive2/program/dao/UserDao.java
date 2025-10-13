package ru.aston.hometask.intensive2.program.dao;

import ru.aston.hometask.intensive2.program.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findById(Long id);
    List<User> findAll();
    User save(User user);
    User update(User user);
    void delete(Long id);
    Optional<User> findByEmail(String email);
}