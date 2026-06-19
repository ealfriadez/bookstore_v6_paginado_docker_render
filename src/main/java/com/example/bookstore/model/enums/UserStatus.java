package com.example.bookstore.model.enums;

public enum UserStatus {
    ACTIVE,   // usuario normal o libreria aprobada
    PENDING,  // dueno de libreria esperando aprobacion del admin
    DISABLED  // cuenta suspendida — no puede iniciar sesion
}
