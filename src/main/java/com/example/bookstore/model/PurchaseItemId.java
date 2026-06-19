package com.example.bookstore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Clave compuesta de PurchaseItem: (purchase_id, store_book_id)
// Una compra no puede tener el mismo StoreBook dos veces — se incrementa quantity.
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PurchaseItemId implements Serializable {

    @Column(name = "purchase_id")
    private Long purchaseId;

    @Column(name = "store_book_id")
    private Long storeBookId;
}
