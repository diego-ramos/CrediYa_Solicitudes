package com.crediya.api.mapper;

import com.crediya.api.dto.ApplicationRequest;
import com.crediya.api.dto.ApplicationResponse;
import com.crediya.model.application.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    Application toModel(ApplicationRequest dto);

    @Mapping(source = "applicationStatus.name", target = "applicationStatus")
    @Mapping(source = "loanType.name", target = "loanType")
    ApplicationResponse toResponse(Application dto);
}
