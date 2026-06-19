package com.example.bookstore.repository;

import com.example.bookstore.dto.response.BookSalesReportResponse;
import com.example.bookstore.dto.response.SalesSummaryResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final EntityManager em;

    @SuppressWarnings("unchecked")
    public List<BookSalesReportResponse> findSalesByBook(LocalDateTime from, LocalDateTime to, Long storeId) {
        Query query = em.createNativeQuery(
                "SELECT * FROM fn_sales_report_by_book(" +
                "CAST(:from AS TIMESTAMP), CAST(:to AS TIMESTAMP), CAST(:storeId AS BIGINT))"
        );
        query.setParameter("from", from);
        query.setParameter("to", to);
        query.setParameter("storeId", storeId);

        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(r -> new BookSalesReportResponse(
                        toLong(r[0]),
                        (String) r[1],
                        toLong(r[2]),
                        (String) r[3],
                        toLong(r[4]),
                        toBigDecimal(r[5]),
                        toLong(r[6])
                ))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public SalesSummaryResponse findSummary(LocalDateTime from, LocalDateTime to, Long storeId) {
        Query query = em.createNativeQuery(
                "SELECT * FROM fn_sales_summary(" +
                "CAST(:from AS TIMESTAMP), CAST(:to AS TIMESTAMP), CAST(:storeId AS BIGINT))"
        );
        query.setParameter("from", from);
        query.setParameter("to", to);
        query.setParameter("storeId", storeId);

        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return new SalesSummaryResponse(BigDecimal.ZERO, 0L, BigDecimal.ZERO, null, null, null);
        }
        Object[] r = rows.get(0);
        return new SalesSummaryResponse(
                toBigDecimal(r[0]),
                toLong(r[1]),
                toBigDecimal(r[2]),
                toLong(r[3]),
                (String) r[4],
                toLong(r[5])
        );
    }

    private static Long toLong(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        return new BigDecimal(o.toString());
    }
}
