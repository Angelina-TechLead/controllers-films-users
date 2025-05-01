package aplication.controller;

import aplication.exception.NotFoundException;
import aplication.model.User;
import aplication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashSet;
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
        return userService.getAll();
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
    public Set<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId).stream()
                .sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.add(user));
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.update(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable long id, @Valid @RequestBody User user) {
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User[] addFriend(@PathVariable long id, @PathVariable long friendId) {
        userService.addFriend(id, friendId);
        var user = userService.getById(id);
        return new User[]{ user };
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        userService.removeFriend(id, friendId);
    }

    @DeleteMapping("/{id}")
    public User deleteUserById(@PathVariable Long id) {
        return userService.deleteUserById(id);
    }
}