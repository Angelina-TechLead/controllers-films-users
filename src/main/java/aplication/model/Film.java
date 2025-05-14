package aplication.model;

import aplication.validators.film.ValidReleaseDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotNull
    @ValidReleaseDate
    private LocalDate releaseDate;

    @Positive
    @NotNull
    private double duration;

    private Set<Long> likes = new HashSet<>();

    private Set<Genre> genres = new HashSet<>();

    private Set<Director> directors = new HashSet<>();

    private Mpa mpa;
}
