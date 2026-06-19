package com.example.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100)
        String lastName,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone,

        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String address
) {}
