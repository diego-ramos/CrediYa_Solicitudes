package com.crediya.model.applicationstatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ApplicationStatus {
    private Integer id;
    private String name;
    private String description;
}
