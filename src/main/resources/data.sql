-- =============================================
-- SEED DATA - BOOKSTORE API
-- =============================================
-- Password de todos los usuarios seed: Admin@2024
-- Hash BCrypt generado con strength 10
-- Para produccion regenerar hashes con BCryptPasswordEncoder
--
-- Usuarios seed:
--   1  admin@bookstore.com      → ADMIN
--   2  quijote@libreria.com     → STORE  (dueño de Libreria El Quijote)
--   3  cervantes@libreria.com   → STORE  (dueño de Libreria Cervantes)
--   4  cliente@example.com      → CUSTOMER
-- =============================================

-- USUARIOS (status: ACTIVE = cuenta normal | PENDING = solicitud libreria | DISABLED = suspendido)
INSERT INTO users (id, email, password, role, status)
OVERRIDING SYSTEM VALUE VALUES
(1, 'admin@bookstore.com',     '$2b$10$WyvFQy01LN.NZ2QeHnxJzu99hZRZ.STRRnrn8DdVz5eK5QaHOk2I.', 'ADMIN',    'ACTIVE'),
(2, 'quijote@libreria.com',    '$2b$10$WyvFQy01LN.NZ2QeHnxJzu99hZRZ.STRRnrn8DdVz5eK5QaHOk2I.', 'STORE',    'ACTIVE'),
(3, 'cervantes@libreria.com',  '$2b$10$WyvFQy01LN.NZ2QeHnxJzu99hZRZ.STRRnrn8DdVz5eK5QaHOk2I.', 'STORE',    'ACTIVE'),
(4, 'cliente@example.com',     '$2b$10$WyvFQy01LN.NZ2QeHnxJzu99hZRZ.STRRnrn8DdVz5eK5QaHOk2I.', 'CUSTOMER', 'ACTIVE')
ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password, status = EXCLUDED.status;

-- PERFILES DE USUARIO
INSERT INTO user_profiles (user_id, first_name, last_name, phone, address)
VALUES
(1, 'Admin',   'Sistema',    '+51999000001', 'Lima, Peru'),
(2, 'Carlos',  'Rodriguez',  '+51999000002', 'Miraflores, Lima'),
(3, 'Sofia',   'Mendoza',    '+51999000003', 'Surco, Lima'),
(4, 'Ana',     'Torres',     '+51999000004', 'San Isidro, Lima')
ON CONFLICT (user_id) DO NOTHING;

-- EDITORIALES
INSERT INTO editorials (id, name, country)
OVERRIDING SYSTEM VALUE VALUES
(1, 'Planeta',         'España'),
(2, 'Santillana',      'España'),
(3, 'O''Reilly',       'USA'),
(4, 'Addison-Wesley',  'USA')
ON CONFLICT (name) DO NOTHING;

-- AUTORES
INSERT INTO authors (id, first_name, last_name, nationality)
OVERRIDING SYSTEM VALUE VALUES
(1, 'Gabriel', 'Garcia Marquez', 'Colombiana'),
(2, 'Mario',   'Vargas Llosa',   'Peruana'),
(3, 'Robert',  'Martin',         'Americana'),
(4, 'Martin',  'Fowler',         'Britanica')
ON CONFLICT DO NOTHING;

-- LIBROS (catalogo global, sin precio ni stock)
INSERT INTO books (id, title, image_url, editorial_id, author_id)
OVERRIDING SYSTEM VALUE VALUES
(1, 'Cien anos de soledad',   'https://example.com/cien-anos.jpg',    1, 1),
(2, 'La ciudad y los perros', 'https://example.com/ciudad.jpg',        1, 2),
(3, 'Clean Code',             'https://example.com/clean-code.jpg',    3, 3),
(4, 'Refactoring',            'https://example.com/refactoring.jpg',   4, 4)
ON CONFLICT DO NOTHING;

-- LIBRERIAS
INSERT INTO stores (id, name, ruc, owner_id, active)
OVERRIDING SYSTEM VALUE VALUES
(1, 'Libreria El Quijote', '20123456789', 2, true),
(2, 'Libreria Cervantes',  '20987654321', 3, true)
ON CONFLICT (name) DO NOTHING;

-- INVENTARIO POR LIBRERIA
-- Libreria 1 (El Quijote): los 4 libros
-- Libreria 2 (Cervantes): solo los 2 libros literarios, precio diferente
INSERT INTO store_books (id, store_id, book_id, price, stock, active, version)
OVERRIDING SYSTEM VALUE VALUES
(1, 1, 1, 29.99, 50, true, 0),
(2, 1, 2, 24.99, 30, true, 0),
(3, 1, 3, 49.99, 20, true, 0),
(4, 1, 4, 44.99, 15, true, 0),
(5, 2, 1, 27.50, 40, true, 0),
(6, 2, 2, 22.00, 25, true, 0)
ON CONFLICT (store_id, book_id) DO NOTHING;

-- COMPRAS DE PRUEBA (cliente ID 4, status COMPLETED para que aparezcan en reportes)
-- Compra 1: Clean Code x1 (49.99) + Cien anos x2 (59.98) = 109.97
-- Compra 2: Refactoring x1 (44.99)
INSERT INTO purchases (id, user_id, total, status, purchase_date)
OVERRIDING SYSTEM VALUE VALUES
(1, 4, 109.97, 'COMPLETED', '2024-06-15 10:30:00'),
(2, 4,  44.99, 'COMPLETED', '2024-09-20 15:00:00')
ON CONFLICT DO NOTHING;

-- ITEMS DE COMPRA
INSERT INTO purchase_items (purchase_id, store_book_id, quantity, unit_price, subtotal)
VALUES
(1, 3,  1, 49.99,  49.99),
(1, 1,  2, 29.99,  59.98),
(2, 4,  1, 44.99,  44.99)
ON CONFLICT DO NOTHING;

-- Resincronizar secuencias
SELECT setval('users_id_seq',      (SELECT MAX(id) FROM users));
SELECT setval('editorials_id_seq', (SELECT MAX(id) FROM editorials));
SELECT setval('authors_id_seq',    (SELECT MAX(id) FROM authors));
SELECT setval('books_id_seq',      (SELECT MAX(id) FROM books));
SELECT setval('stores_id_seq',     (SELECT MAX(id) FROM stores));
SELECT setval('store_books_id_seq',(SELECT MAX(id) FROM store_books));
SELECT setval('purchases_id_seq',  (SELECT MAX(id) FROM purchases));
