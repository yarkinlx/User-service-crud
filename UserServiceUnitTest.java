package ru.aston.hometask.intensive2.program.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.aston.hometask.intensive2.program.dao.UserDao;
import ru.aston.hometask.intensive2.program.model.User;

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
    void testCreateUser_Success() {
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;
        User expectedUser = new User(name, email, age);
        expectedUser.setId(1L);

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.createUser(name, email, age);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(age, result.getAge());
        verify(userDao).findByEmail(email);
        verify(userDao).save(any(User.class));
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
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
    void testCreateUser_InvalidName() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("", "test@example.com", 30));

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(null, "test@example.com", 30));

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_InvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "", 30));

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", null, 30));

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "invalid-email", 30));

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_InvalidAge() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "test@example.com", -5));

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "test@example.com", 200));

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_NullAge() {
        String name = "John Doe";
        String email = "john@example.com";
        User expectedUser = new User(name, email, null);
        expectedUser.setId(1L);

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.createUser(name, email, null);

        assertNotNull(result);
        assertNull(result.getAge());
        verify(userDao).findByEmail(email);
        verify(userDao).save(any(User.class));
    }

    @Test
    void testGetUserById_Found() {
        Long userId = 1L;
        User expectedUser = new User("Test User", "test@example.com", 25);
        expectedUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userDao).findById(userId);
    }

    @Test
    void testGetUserById_NotFound() {
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(userId);

        assertFalse(result.isPresent());
        verify(userDao).findById(userId);
    }

    @Test
    void testGetUserById_InvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(null));

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(0L));

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(-1L));

        verify(userDao, never()).findById(any());
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User("User1", "user1@example.com", 25);
        User user2 = new User("User2", "user2@example.com", 30);
        List<User> expectedUsers = Arrays.asList(user1, user2);

        when(userDao.findAll()).thenReturn(expectedUsers);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
        verify(userDao).findAll();
    }

    @Test
    void testUpdateUser_Success() {
        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.isEmailExistsForOtherUser("new@example.com", userId)).thenReturn(false);
        when(userDao.update(existingUser)).thenReturn(existingUser);

        User result = userService.updateUser(userId, "New Name", "new@example.com", 30);

        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(30, result.getAge());
        verify(userDao).findById(userId);
        verify(userDao).isEmailExistsForOtherUser("new@example.com", userId);
        verify(userDao).update(existingUser);
    }

    @Test
    void testUpdateUser_PartialUpdate() {
        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.update(existingUser)).thenReturn(existingUser);

        User result = userService.updateUser(userId, "New Name", "", null);

        assertEquals("New Name", result.getName());
        assertEquals("old@example.com", result.getEmail());
        assertEquals(25, result.getAge());
        verify(userDao).findById(userId);
        verify(userDao, never()).isEmailExistsForOtherUser(anyString(), any());
        verify(userDao).update(existingUser);
    }

    @Test
    void testUpdateUser_EmailAlreadyExists() {
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
    void testUpdateUser_UserNotFound() {
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userId, "New Name", "new@example.com", 30));

        verify(userDao).findById(userId);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        Long userId = 1L;
        User existingUser = new User("To Delete", "delete@example.com", 40);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(userId);

        verify(userDao).findById(userId);
        verify(userDao).delete(userId);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(userId));

        verify(userDao).findById(userId);
        verify(userDao, never()).delete(userId);
    }

    @Test
    void testGetUserByEmail() {
        String email = "test@example.com";
        User expectedUser = new User("Test User", email, 25);

        when(userDao.findByEmail(email)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.getUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userDao).findByEmail(email);
    }

    @Test
    void testGetUserByEmail_InvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByEmail(""));

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByEmail(null));

        verify(userDao, never()).findByEmail(anyString());
    }

    @Test
    void testIsEmailUnique() {
        String uniqueEmail = "unique@example.com";
        String existingEmail = "existing@example.com";

        when(userDao.findByEmail(uniqueEmail)).thenReturn(Optional.empty());
        when(userDao.findByEmail(existingEmail)).thenReturn(Optional.of(new User()));

        assertTrue(userService.isEmailUnique(uniqueEmail));
        assertFalse(userService.isEmailUnique(existingEmail));
    }
}