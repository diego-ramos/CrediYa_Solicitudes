package com.crediya.api.mapper;

import com.crediya.api.dto.ApplicationRequest;
import com.crediya.api.dto.ApplicationResponse;
import com.crediya.model.application.Application;
import com.crediya.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface ApplicationMapper {

    Application toModel(ApplicationRequest dto);

    @Mapping(source = "applicationStatus.name", target = "applicationStatusName")
    @Mapping(source = "loanType.name", target = "loanTypeName")
    @Mapping(source = "loanType.interestRate", target = "interestRate")
    @Mapping(target = "fullName", expression = "java(mapFullName(dto.getUser()))")
    @Mapping(source = "user.baseSalary", target = "baseSalary")
    ApplicationResponse toResponse(Application dto);

    default String mapFullName(User user) {
        if (user == null) {
            return null;
        }
        String first = user.getFirstNames() == null ? "" : user.getFirstNames();
        String last = user.getLastNames() == null ? "" : user.getLastNames();
        return (first + " " + last).trim();
    }
}
