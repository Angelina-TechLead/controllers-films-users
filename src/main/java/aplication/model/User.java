package aplication.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private long id;

    @NotBlank(message = "@Valid: Film name shouldn't be blank")
    @Email
    private String email;

    @NotBlank(message = "@Valid: Film name shouldn't be blank")
    private String login;

    @NotBlank
    private String name;

    @NotNull(message = "@Valid: Film name shouldn't be blank")
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();
}

