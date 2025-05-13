package aplication.controller;

import aplication.exception.NotFoundException;
import aplication.model.Director;
import aplication.storage.dao.DirectorDbStorage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorDbStorage directorDbStorage;

    @GetMapping
    public ResponseEntity<List<Director>> getAllDirectors() {
        if (directorDbStorage.getAll() != null) {
            return ResponseEntity.status(HttpStatus.OK).body(directorDbStorage.getAll());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id) {
        var data = directorDbStorage.getById(id);
        if (data == null) {
            throw new NotFoundException("Режиссер не найден");
        } else {
            return data;
        }
    }

    @PostMapping
    public ResponseEntity<Director> create(@Valid @RequestBody Director director) {
        var newDirector = directorDbStorage.create(director);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDirector);
    }

    @PutMapping
    public ResponseEntity<Director> update(@Valid @RequestBody Director newDirector) {
        var updatedDirector = directorDbStorage.update(newDirector);
        return ResponseEntity.status(HttpStatus.OK).body(updatedDirector);
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable int id, @PathVariable int userId) {
        var data = directorDbStorage.getById(id);
        if (data == null) {
            throw new NotFoundException("Режиссер не найден");
        } else {
            directorDbStorage.delete(id);
        }
    }
}
