package com.example.bookstore.repository;

import com.example.bookstore.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("SELECT DISTINCT p FROM Purchase p JOIN FETCH p.items i JOIN FETCH i.storeBook sb JOIN FETCH sb.book JOIN FETCH sb.store")
    List<Purchase> findAllWithItems();

    @Query("SELECT p FROM Purchase p JOIN FETCH p.items i JOIN FETCH i.storeBook sb JOIN FETCH sb.book JOIN FETCH sb.store WHERE p.id = :id")
    Optional<Purchase> findByIdWithItems(@Param("id") Long id);

    @Query(value = "SELECT * FROM fn_sales_report_by_book(:dateFrom, :dateTo)", nativeQuery = true)
    List<Object[]> getSalesReportByBook(@Param("dateFrom") LocalDateTime dateFrom,
                                        @Param("dateTo") LocalDateTime dateTo);

    @Query(value = "SELECT * FROM fn_sales_summary(:dateFrom, :dateTo)", nativeQuery = true)
    List<Object[]> getSalesSummary(@Param("dateFrom") LocalDateTime dateFrom,
                                   @Param("dateTo") LocalDateTime dateTo);
}
