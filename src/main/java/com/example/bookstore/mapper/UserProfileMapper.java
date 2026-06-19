package com.example.bookstore.mapper;

import com.example.bookstore.dto.response.UserProfileResponse;
import com.example.bookstore.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Dos fuentes: profile + email
@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    // email viene de User (LAZY) — se pasa como parámetro separado
    @Mapping(source = "profile.id",        target = "id")
    @Mapping(source = "profile.firstName", target = "firstName")
    @Mapping(source = "profile.lastName",  target = "lastName")
    @Mapping(source = "profile.phone",     target = "phone")
    @Mapping(source = "profile.address",   target = "address")
    @Mapping(source = "email",             target = "email")
    UserProfileResponse toResponse(UserProfile profile, String email);
}
