package com.crediya.model.application;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Application {
    private Long id;

    private Integer identificationNumber;
}
