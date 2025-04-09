package aplication.controller;

import aplication.exception.NotFoundException;
import aplication.model.User;
import aplication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Array;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getAll() {
        if (userService.getAll().isEmpty()) {
            throw new NotFoundException("Пользователи не найдены");
        } else {
            return userService.getAll();
        }
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return userService.getById(id);
    }

    @GetMapping("/{id}/friends")
    public Set<User> getFriends(@PathVariable long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId).stream()
                .sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        var addedUser = userService.add(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedUser);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        if (userService.getById(user.getId()) != null) {
            var updatedUser = userService.update(user);
            return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable long id, @Valid @RequestBody User user) {
        if (id == user.getId()) {
            userService.update(user);
        }

        throw new NotFoundException("Пользователь с ID " + id + " не найден");
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User[]> addFriend(@PathVariable long id, @PathVariable long friendId) {
        var updatedUser = userService.addFriend(id, friendId);
        var response = new User[]{ updatedUser };
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> removeFriend(@PathVariable long id, @PathVariable long friendId) {
        userService.removeFriend(id, friendId);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}