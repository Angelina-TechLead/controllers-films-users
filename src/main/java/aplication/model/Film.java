package aplication.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private double duration;
}
