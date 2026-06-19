package com.example.bookstore.model.enums;

public enum PurchaseStatus {
    PENDING,    // compra creada, stock reservado, esperando pago
    PAID,       // pago confirmado por la pasarela (webhook en produccion)
    COMPLETED,  // pedido entregado / procesado
    CANCELLED,  // cancelado antes del pago — stock restaurado
    REFUNDED    // reembolso realizado post-pago (para integracion futura)
}
