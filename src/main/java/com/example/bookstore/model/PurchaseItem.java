package com.example.bookstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
@Getter @Setter
@NoArgsConstructor
public class PurchaseItem {

    // Clave compuesta: (purchase_id, store_book_id)
    @EmbeddedId
    private PurchaseItemId id = new PurchaseItemId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("purchaseId")
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeBookId")
    @JoinColumn(name = "store_book_id", nullable = false)
    private StoreBook storeBook;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal subtotal;
}
