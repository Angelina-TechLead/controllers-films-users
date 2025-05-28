package aplication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {

    @JsonProperty("reviewId")
    private Long id;

    @NotBlank
    private String content;

    @NotNull
    private boolean positive;

    @NotNull
    private Long userId;

    @NotNull
    private Long filmId;

    private int useful;
}
