package aplication.controller;

import aplication.model.Film;
import aplication.model.User;
import aplication.model.UserEvent;
import aplication.model.UserEvent.EventType;
import aplication.model.UserEvent.OperationType;
import aplication.service.UserEventService;
import aplication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserEventService userEventService;

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

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long userId) {
        userService.deleteUserById(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User[] addFriend(@PathVariable long id, @PathVariable long friendId) {
        userService.addFriend(id, friendId);
        var user = userService.getById(id);

        UserEvent event = new UserEvent();
        event.setUserId(friendId);
        event.setEventType(EventType.FRIEND);
        event.setOperation(OperationType.ADD);
        event.setEntityId(id);
        event.setTimestamp(System.currentTimeMillis());
        userEventService.create(event);
        
        return new User[] { user };
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        userService.removeFriend(id, friendId);

        UserEvent event = new UserEvent();
        event.setUserId(friendId);
        event.setEventType(EventType.FRIEND);
        event.setOperation(OperationType.REMOVE);
        event.setEntityId(id);
        event.setTimestamp(System.currentTimeMillis());
        userEventService.create(event);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
    }

    @GetMapping("{id}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable Long id,
                                               @RequestParam(required = false, defaultValue = "10") Integer count) {
        return userService.getRecommendations(id, count);
    }
}
