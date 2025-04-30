package aplication.controller;

import aplication.exception.NotFoundException;
import aplication.model.Mpa;
import aplication.storage.dao.MpaDbStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaDbStorage mpaStorage;

    @Autowired
    public MpaController(MpaDbStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    @GetMapping
    public List<Mpa> getAllRatings() {
        var data = mpaStorage.getAll();
        if (data.isEmpty()) {
            throw new NotFoundException("Рейтинги не найдены");
        } else {
            return data;
        }
    }

    @GetMapping("/{id}")
    public Mpa getRatingById(@PathVariable int id) {
        var data = mpaStorage.getById(id);
        if (data == null) {
            throw new NotFoundException("Рейтинги не найдены");
        } else {
            return data;
        }
    }
}
