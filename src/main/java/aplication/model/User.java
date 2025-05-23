package aplication.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class User {
    private Long id;

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
