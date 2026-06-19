package com.example.bookstore.dto.request;

import jakarta.validation.constraints.*;

public record RegisterStoreRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato valido")
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,}$",
            message = "La contrasena debe contener al menos una mayuscula, una minuscula, un numero y un caracter especial"
        )
        String password,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "El telefono es obligatorio")
        @Size(max = 20)
        String phone
) {}
