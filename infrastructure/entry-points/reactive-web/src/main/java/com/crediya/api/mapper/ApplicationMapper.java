package com.crediya.api.mapper;

import com.crediya.api.dto.ApplicationRequest;
import com.crediya.model.application.Application;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    Application toModel(ApplicationRequest dto);
}
