-- =============================================
-- FUNCIONES PARA REPORTES - BOOKSTORE API
-- =============================================
-- Ejecutar este archivo manualmente:
--   psql -U postgres -d bookstore_db -f stored_procedures.sql
--
-- Llamar funciones:
--   SELECT * FROM fn_sales_report_by_book();                         -- Admin: todas las librerías
--   SELECT * FROM fn_sales_report_by_book(NULL, NULL, 1);            -- Solo librería ID 1
--   SELECT * FROM fn_sales_report_by_book('2024-01-01', '2024-12-31');
--   SELECT * FROM fn_sales_report_by_book('2024-01-01', '2024-12-31', 2);
--   SELECT * FROM fn_sales_summary();
--   SELECT * FROM fn_sales_summary(NULL, NULL, 1);
--   SELECT * FROM fn_sales_summary('2024-01-01', '2024-12-31');
--   SELECT * FROM fn_sales_summary('2024-01-01', '2024-12-31', 2);
-- =============================================

-- Reporte: ventas por libro (solo compras COMPLETED)
-- p_store_id = NULL  → ADMIN: todas las librerías
-- p_store_id = N     → STORE: solo su librería
CREATE OR REPLACE FUNCTION fn_sales_report_by_book(
    p_date_from TIMESTAMP DEFAULT NULL,
    p_date_to   TIMESTAMP DEFAULT NULL,
    p_store_id  BIGINT    DEFAULT NULL
)
RETURNS TABLE(
    book_id         BIGINT,
    book_title      VARCHAR,
    store_id        BIGINT,
    store_name      VARCHAR,
    total_quantity  BIGINT,
    total_revenue   NUMERIC,
    total_purchases BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        b.id,
        b.title,
        s.id,
        s.name,
        SUM(pi.quantity)::BIGINT,
        SUM(pi.subtotal),
        COUNT(DISTINCT p.id)::BIGINT
    FROM purchase_items pi
    JOIN store_books  sb ON sb.id  = pi.store_book_id
    JOIN books        b  ON b.id   = sb.book_id
    JOIN stores       s  ON s.id   = sb.store_id
    JOIN purchases    p  ON p.id   = pi.purchase_id
    WHERE p.status = 'COMPLETED'
      AND (p_date_from IS NULL OR p.purchase_date >= p_date_from)
      AND (p_date_to   IS NULL OR p.purchase_date <= p_date_to)
      AND (p_store_id  IS NULL OR sb.store_id      = p_store_id)
    GROUP BY b.id, b.title, s.id, s.name
    ORDER BY SUM(pi.subtotal) DESC;
END;
$$ LANGUAGE plpgsql;


-- Reporte: resumen general de ventas (siempre retorna 1 fila)
-- p_store_id = NULL  → ADMIN: todas las librerías
-- p_store_id = N     → STORE: solo su librería
CREATE OR REPLACE FUNCTION fn_sales_summary(
    p_date_from TIMESTAMP DEFAULT NULL,
    p_date_to   TIMESTAMP DEFAULT NULL,
    p_store_id  BIGINT    DEFAULT NULL
)
RETURNS TABLE(
    total_sales          NUMERIC,
    total_purchases      BIGINT,
    average_per_purchase NUMERIC,
    best_seller_id       BIGINT,
    best_seller_title    VARCHAR,
    best_seller_quantity BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COALESCE(SUM(p.total), 0),
        COUNT(p.id)::BIGINT,
        COALESCE(AVG(p.total), 0),
        bs.book_id,
        bs.title,
        bs.qty
    FROM (SELECT 1) dummy
    LEFT JOIN purchases p
           ON p.status = 'COMPLETED'
          AND (p_date_from IS NULL OR p.purchase_date >= p_date_from)
          AND (p_date_to   IS NULL OR p.purchase_date <= p_date_to)
    LEFT JOIN (
        SELECT b.id AS book_id, b.title, SUM(pi.quantity)::BIGINT AS qty
        FROM purchase_items pi
        JOIN store_books  sb  ON sb.id  = pi.store_book_id
        JOIN books        b   ON b.id   = sb.book_id
        JOIN purchases    p2  ON p2.id  = pi.purchase_id
        WHERE p2.status = 'COMPLETED'
          AND (p_date_from IS NULL OR p2.purchase_date >= p_date_from)
          AND (p_date_to   IS NULL OR p2.purchase_date <= p_date_to)
          AND (p_store_id  IS NULL OR sb.store_id       = p_store_id)
        GROUP BY b.id, b.title
        ORDER BY SUM(pi.quantity) DESC
        LIMIT 1
    ) bs ON TRUE
    GROUP BY bs.book_id, bs.title, bs.qty;
END;
$$ LANGUAGE plpgsql;
