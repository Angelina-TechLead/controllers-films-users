package test;

import aplication.controller.UserController;
import aplication.exception.ValidationException;
import aplication.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {

    private final UserController userController = new UserController();

    @Test
    public void testValidateUserWithInvalidEmail() {
        User user = new User();
        user.setEmail("invalidEmail");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    public void testValidateUserWithEmptyLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    public void testValidateUserWithSpacesInLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("invalid login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    public void testValidateUserWithFutureBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    public void testValidateUserWithValidData() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertDoesNotThrow(() -> userController.addUser(user));
    }

    @Test
    public void testAddUserSetsNameToLoginIfEmpty() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("");

        User addedUser = userController.addUser(user);

        assertEquals(user.getLogin(), addedUser.getName());
    }

    @Test
    public void testUpdateNonExistentUserThrowsException() {
        User user = new User();
        user.setId(999L); // Несуществующий ID
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }

    @Test
    public void testUpdateUserWithValidData() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User addedUser = userController.addUser(user);

        addedUser.setLogin("updatedLogin");

        User updatedUser = userController.updateUser(addedUser);

        assertEquals("updatedLogin", updatedUser.getLogin());
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("validLogin1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("validLogin2");
        user2.setBirthday(LocalDate.of(1990, 1, 1));

        userController.addUser(user1);
        userController.addUser(user2);

        List<User> users = userController.getAllUsers();

        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }
}
