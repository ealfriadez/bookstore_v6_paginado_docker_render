# Paginación — Fundamentos

---

## ¿Qué problema resuelve?

Sin paginación, `GET /api/books` trae **todos** los registros de una vez:
- 50.000 filas en RAM → 50.000 objetos Java → respuesta de varios MB

Con paginación, trae solo los de esa página:
- 10 filas → 10 objetos → respuesta de kilobytes

---

## ¿Cómo funciona el SQL? — LIMIT y OFFSET

`LIMIT` = cuántos registros devolver  
`OFFSET` = cuántos registros saltarse antes de empezar

```
Fila:  1  2  3  4  5  6  7  8  9  10  11  12  ...  30

Página 0 → LIMIT 10 OFFSET  0 → toma [1  … 10]
Página 1 → LIMIT 10 OFFSET 10 → salta 10, toma [11 … 20]
Página 2 → LIMIT 10 OFFSET 20 → salta 20, toma [21 … 30]
```

Fórmula: `OFFSET = página × tamaño`

Spring calcula esto solo. Tú solo envías `?page=2&size=10`.

Spring ejecuta **dos consultas** automáticamente:
```sql
SELECT * FROM books LIMIT 10 OFFSET 20;  -- los datos de esa página
SELECT COUNT(*) FROM books;              -- el total para calcular totalPages
```

---

## Los 3 tipos de Spring para paginar

| Tipo | Qué es | De dónde viene |
|---|---|---|
| `Pageable` | Los parámetros: página, tamaño, orden | Lo construye Spring MVC desde los query params |
| `Page<T>` | El resultado: datos + metadatos | Lo devuelve el repositorio |
| `PagedResponse<T>` | El JSON que devuelve tu API | Lo defines tú (DTO propio) |

### `Pageable` — parámetros de entrada

```
GET /api/books?page=2&size=10&sort=title,asc
                 ^       ^         ^
               página  tamaño  ordenamiento
```

`@PageableDefault` define los valores cuando el cliente no envía nada:
```java
@PageableDefault(size = 10, sort = "title")
// → page=0, size=10, sort=title,asc por defecto
```

### `Page<T>` — resultado del repositorio

```
Page<Book>
├── content       → [Book1, Book2, ..., Book10]   los datos
├── number        → 2                              página actual
├── size          → 10                             tamaño de página
├── totalElements → 347                            registros totales en la BD
├── totalPages    → 35                             páginas totales
├── first         → false
└── last          → false
```

### `PagedResponse<T>` — por qué un DTO propio

`Page<T>` tiene campos internos de Spring que contaminarían el JSON.  
`PagedResponse<T>` define exactamente lo que el frontend necesita ver.

---

## ¿Qué es `record`?

Forma corta de escribir una clase de datos en Java. El compilador genera automáticamente el constructor, getters, `equals`, `hashCode` y `toString`.

```java
// Sin record — 40 líneas de boilerplate
public class PagedResponse<T> {
    private final List<T> content;
    private final int page;
    public PagedResponse(List<T> content, int page) { ... }
    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    // equals, hashCode, toString...
}

// Con record — 5 líneas, mismo resultado
public record PagedResponse<T>(
        List<T> content,
        int page
) { }
```

Los campos son **inmutables**: una vez creado el objeto, no se pueden cambiar. Perfecto para DTOs de respuesta.

---

## ¿Qué es `static` en `from()`?

Un método `static` pertenece a la **clase**, no a un objeto. Se llama sin crear una instancia:

```java
// Método normal (de instancia) — necesitas un objeto primero
PagedResponse r = new PagedResponse(...);
r.metodo();

// Método static — se llama directo con la clase
PagedResponse.from(page);
```

`from()` tiene que ser `static` porque su trabajo es **crear** el objeto.  
Si no lo fuera, necesitarías un `PagedResponse` para crear un `PagedResponse` — paradoja.

Este patrón se llama **factory method**. Lo usa Java en todas partes:  
`List.of(...)`, `Optional.of(...)`, `LocalDate.now()`.

---

## Flujo completo de una petición paginada

```
GET /api/catalog?page=0&size=5
        ↓
Controller — recibe Pageable (Spring lo construye solo)
        ↓
Service — llama a repository.findAllActive(pageable)
        ↓
Repository — ejecuta:
    SELECT ... WHERE active=true LIMIT 5 OFFSET 0
    SELECT COUNT(*) WHERE active=true
        ↓
Service — Page<StoreBook>.map(mapper) → Page<StoreBookResponse>
        ↓
Controller — PagedResponse.from(page) → empaqueta en tu DTO
        ↓
JSON de respuesta:
{
  "content": [ {...}, {...}, {...}, {...}, {...} ],
  "page": 0,
  "size": 5,
  "totalElements": 48,
  "totalPages": 10,
  "first": true,
  "last": false
}
```

---

## ¿Por qué `countQuery` separado en repos con `JOIN FETCH`?

```java
@Query(
    value = "SELECT b FROM Book b JOIN FETCH b.author JOIN FETCH b.editorial",
    countQuery = "SELECT COUNT(b) FROM Book b"   // ← obligatorio
)
Page<Book> findAllWithDetails(Pageable pageable);
```

Spring necesita hacer `SELECT COUNT(*)` para calcular `totalPages`.  
Intenta reutilizar la query principal, pero `JOIN FETCH` no es válido dentro de un `COUNT` en JPQL → Hibernate lanza error en runtime.

**Regla:** si la query tiene `JOIN FETCH` y devuelve `Page<T>`, siempre declara `countQuery`.

---

## Archivos modificados en este proyecto

| Archivo | Cambio |
|---|---|
| `BookstoreApplication.java` | `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` |
| `dto/response/PagedResponse.java` | **Nuevo** — record con `static from()` |
| `repository/BookRepository.java` | `findAllWithDetails(Pageable)` + `countQuery` |
| `repository/AuthorRepository.java` | `searchByLastName(String, Pageable)` |
| `repository/StoreBookRepository.java` | `findAllActive(Pageable)` + `countQuery` |
| `service/IBookService.java` | `Page<BookResponse> findAll(Pageable)` |
| `service/IAuthorService.java` | `Page<AuthorResponse> findAll(Pageable)` |
| `service/IEditorialService.java` | `Page<EditorialResponse> findAll(Pageable)` |
| `service/IStoreBookService.java` | `Page<StoreBookResponse> findAll(Pageable)` |
| `service/impl/BookServiceImpl.java` | Implementación de `findAll(Pageable)` |
| `service/impl/AuthorServiceImpl.java` | Implementación de `findAll(Pageable)` |
| `service/impl/EditorialServiceImpl.java` | Implementación de `findAll(Pageable)` |
| `service/impl/StoreBookServiceImpl.java` | Implementación de `findAll(Pageable)` |
| `controller/BookController.java` | `getAll()` → `PagedResponse<BookResponse>` |
| `controller/AuthorController.java` | `getAllAuthors()` → `PagedResponse<AuthorResponse>` |
| `controller/EditorialController.java` | `getAll()` → `PagedResponse<EditorialResponse>` |
| `controller/CatalogController.java` | `getAll()` → `PagedResponse<StoreBookResponse>` |
| `controller/StoreBookController.java` | `getAll()` → `PagedResponse<StoreBookResponse>` |
