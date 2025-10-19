package ru.aston.hometask.intensive2.program.service;

import ru.aston.hometask.intensive2.program.dao.UserDao;
import ru.aston.hometask.intensive2.program.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User createUser(String name, String email, Integer age) {
        logger.info("Creating new user: {}, {}, {}", name, email, age);


        validateUserData(name, email, age);

        if (!isEmailUnique(email)) {
            throw new IllegalArgumentException("User with this email already exists: " + email);
        }

        User user = new User(name, email, age);
        return userDao.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        logger.info("Getting user by id: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        return userDao.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        logger.info("Getting all users");
        return userDao.findAll();
    }

    @Override
    public User updateUser(Long id, String name, String email, Integer age) {
        logger.info("Updating user with id: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }


        User existingUser = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));


        if (email != null && !email.trim().isEmpty()) {
            validateEmail(email);
            // Используем новый метод из DAO
            if (userDao.isEmailExistsForOtherUser(email, id)) {
                throw new IllegalArgumentException("Another user with this email already exists: " + email);
            }
            existingUser.setEmail(email);
        }

        if (name != null && !name.trim().isEmpty()) {
            validateName(name);
            existingUser.setName(name);
        }

        if (age != null) {
            validateAge(age);
            existingUser.setAge(age);
        }

        return userDao.update(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }

        // Проверяем существование пользователя
        if (!userDao.findById(id).isPresent()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        userDao.delete(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        logger.info("Getting user by email: {}", email);
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        return userDao.findByEmail(email);
    }

    @Override
    public boolean isEmailUnique(String email) {
        return !userDao.findByEmail(email).isPresent();
    }


    private void validateUserData(String name, String email, Integer age) {
        validateName(name);
        validateEmail(email);
        if (age != null) {
            validateAge(age);
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 characters");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (email.length() > 150) {
            throw new IllegalArgumentException("Email cannot exceed 150 characters");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must contain @ symbol");
        }
    }

    private void validateAge(Integer age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        if (age > 150) {
            throw new IllegalArgumentException("Age cannot exceed 150");
        }
    }
}