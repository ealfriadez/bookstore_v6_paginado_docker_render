package com.example.bookstore.security;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Almacena tokens revocados en memoria.
// Limitación: se pierde al reiniciar el servidor.
// En producción se reemplaza por Redis con TTL igual a la expiración del token.
@Component
public class TokenBlacklist {

    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

    public void revoke(String token) {
        revokedTokens.add(token);
    }

    public boolean isRevoked(String token) {
        return revokedTokens.contains(token);
    }
}
