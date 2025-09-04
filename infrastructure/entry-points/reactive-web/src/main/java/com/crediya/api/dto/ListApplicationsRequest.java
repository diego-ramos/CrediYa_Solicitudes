package com.crediya.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ListApplicationsRequest {

    @Min(value=0, message = "page min value is 0")
    private int page = 0;

    @Min(value=1, message = "page min value is 1")
    private int size = 10;

    @NotEmpty(message = "statusIds is required")
    private List<Integer> statusIds;
}
