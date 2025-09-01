package com.crediya.api.mapper;

import com.crediya.api.dto.ApplicationRequest;
import com.crediya.api.dto.ApplicationResponse;
import com.crediya.model.application.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface ApplicationMapper {

    Application toModel(ApplicationRequest dto);

    @Mapping(source = "applicationStatus.name", target = "applicationStatus")
    @Mapping(source = "loanType.name", target = "loanType")
    @Mapping(source = "loanType.interestRate", target = "interestRate")
    ApplicationResponse toResponse(Application dto);
}
