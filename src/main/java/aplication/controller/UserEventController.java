package aplication.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import aplication.model.UserEvent;
import aplication.service.UserEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/feed")
public class UserEventController {
    private final UserEventService userEventService;

    @GetMapping
    public ResponseEntity<List<UserEvent>> getUserEvents(@PathVariable int userId) {
        List<UserEvent> events = userEventService.getFeeds(userId);
        return events.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
                : ResponseEntity.ok(events);
    }
}
