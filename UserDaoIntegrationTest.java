package ru.aston.hometask.intensive2.program.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.aston.hometask.intensive2.program.model.User;
import ru.aston.hometask.intensive2.program.service.UserService;
import ru.aston.hometask.intensive2.program.service.UserServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao);
    }

    @Test
    void shouldCreateUserSuccessfully() {

        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;
        User expectedUser = new User(name, email, age);
        expectedUser.setId(1L);

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(expectedUser);


        User result = userService.createUser(name, email, age);


        assertNotNull(result, "Result should not be null");
        assertEquals(name, result.getName(), "Name should match");
        assertEquals(email, result.getEmail(), "Email should match");
        assertEquals(age, result.getAge(), "Age should match");
        verify(userDao).findByEmail(email);
        verify(userDao).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {

        String name = "John Doe";
        String email = "existing@example.com";
        Integer age = 30;
        User existingUser = new User("Existing User", email, 25);

        when(userDao.findByEmail(email)).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(name, email, age));

        assertEquals("User with this email already exists: " + email, exception.getMessage());
        verify(userDao).findByEmail(email);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithInvalidName() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("", "test@example.com", 30),
                "Should throw exception for empty name");

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(null, "test@example.com", 30),
                "Should throw exception for null name");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithInvalidEmail() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "", 30),
                "Should throw exception for empty email");

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", null, 30),
                "Should throw exception for null email");

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "invalid-email", 30),
                "Should throw exception for invalid email format");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithInvalidAge() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "test@example.com", -5),
                "Should throw exception for negative age");

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "test@example.com", 200),
                "Should throw exception for age exceeding limit");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void shouldCreateUserWithNullAge() {

        String name = "John Doe";
        String email = "john@example.com";
        User expectedUser = new User(name, email, null);
        expectedUser.setId(1L);

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(expectedUser);


        User result = userService.createUser(name, email, null);

        assertNotNull(result, "Result should not be null");
        assertNull(result.getAge(), "Age should be null");
        verify(userDao).findByEmail(email);
        verify(userDao).save(any(User.class));
    }

    @Test
    void shouldReturnUserWhenFoundById() {

        Long userId = 1L;
        User expectedUser = new User("Test User", "test@example.com", 25);
        expectedUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(expectedUser));


        Optional<User> result = userService.getUserById(userId);


        assertTrue(result.isPresent(), "User should be found");
        assertEquals(expectedUser, result.get(), "User should match expected");
        verify(userDao).findById(userId);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundById() {

        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());


        Optional<User> result = userService.getUserById(userId);


        assertFalse(result.isPresent(), "User should not be found");
        verify(userDao).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenGettingUserWithInvalidId() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(null),
                "Should throw exception for null ID");

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(0L),
                "Should throw exception for zero ID");

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(-1L),
                "Should throw exception for negative ID");

        verify(userDao, never()).findById(any());
    }

    @Test
    void shouldReturnAllUsers() {

        User user1 = new User("User1", "user1@example.com", 25);
        User user2 = new User("User2", "user2@example.com", 30);
        List<User> expectedUsers = Arrays.asList(user1, user2);

        when(userDao.findAll()).thenReturn(expectedUsers);


        List<User> result = userService.getAllUsers();


        assertEquals(2, result.size(), "Should return 2 users");
        assertEquals(expectedUsers, result, "Users should match expected");
        verify(userDao).findAll();
    }

    @Test
    void shouldUpdateUserSuccessfully() {

        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.isEmailExistsForOtherUser("new@example.com", userId)).thenReturn(false);
        when(userDao.update(existingUser)).thenReturn(existingUser);


        User result = userService.updateUser(userId, "New Name", "new@example.com", 30);


        assertEquals("New Name", result.getName(), "Name should be updated");
        assertEquals("new@example.com", result.getEmail(), "Email should be updated");
        assertEquals(30, result.getAge(), "Age should be updated");
        verify(userDao).findById(userId);
        verify(userDao).isEmailExistsForOtherUser("new@example.com", userId);
        verify(userDao).update(existingUser);
    }

    @Test
    void shouldPerformPartialUpdate() {

        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.update(existingUser)).thenReturn(existingUser);


        User result = userService.updateUser(userId, "New Name", "", null);


        assertEquals("New Name", result.getName(), "Name should be updated");
        assertEquals("old@example.com", result.getEmail(), "Email should remain unchanged");
        assertEquals(25, result.getAge(), "Age should remain unchanged");
        verify(userDao).findById(userId);
        verify(userDao, never()).isEmailExistsForOtherUser(anyString(), any());
        verify(userDao).update(existingUser);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {

        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.isEmailExistsForOtherUser("existing@example.com", userId)).thenReturn(true);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userId, "New Name", "existing@example.com", 30));

        assertEquals("Another user with this email already exists: existing@example.com",
                exception.getMessage());
        verify(userDao).findById(userId);
        verify(userDao).isEmailExistsForOtherUser("existing@example.com", userId);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {

        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userId, "New Name", "new@example.com", 30),
                "Should throw exception for non-existing user");

        verify(userDao).findById(userId);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        Long userId = 1L;
        User existingUser = new User("To Delete", "delete@example.com", 40);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(userId);

        verify(userDao).findById(userId);
        verify(userDao).delete(userId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingUser() {

        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(userId),
                "Should throw exception for non-existing user");

        verify(userDao).findById(userId);
        verify(userDao, never()).delete(userId);
    }

    @Test
    void shouldFindUserByEmail() {

        String email = "test@example.com";
        User expectedUser = new User("Test User", email, 25);

        when(userDao.findByEmail(email)).thenReturn(Optional.of(expectedUser));


        Optional<User> result = userService.getUserByEmail(email);


        assertTrue(result.isPresent(), "User should be found");
        assertEquals(expectedUser, result.get(), "User should match expected");
        verify(userDao).findByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenFindingUserWithInvalidEmail() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByEmail(""),
                "Should throw exception for empty email");

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByEmail(null),
                "Should throw exception for null email");

        verify(userDao, never()).findByEmail(anyString());
    }

    @Test
    void shouldCheckEmailUniqueness() {

        String uniqueEmail = "unique@example.com";
        String existingEmail = "existing@example.com";

        when(userDao.findByEmail(uniqueEmail)).thenReturn(Optional.empty());
        when(userDao.findByEmail(existingEmail)).thenReturn(Optional.of(new User()));

        // When & Then
        assertTrue(userService.isEmailUnique(uniqueEmail), "Unique email should return true");
        assertFalse(userService.isEmailUnique(existingEmail), "Existing email should return false");
    }
}