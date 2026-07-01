# bookstore api

api rest para una plataforma saas de venta de libros. permite que multiples librerias publiquen su catalogo, gestionen inventario y ventas, mientras los clientes navegan y compran desde un catalogo unificado.

---

## tabla de contenidos

1. [enunciado del proyecto](#1-enunciado-del-proyecto)
2. [tecnologias](#2-tecnologias)
3. [ejecucion local](#3-ejecucion-local)
4. [estructura del proyecto](#4-estructura-del-proyecto)
5. [modelo de datos](#5-modelo-de-datos)
6. [endpoints de la api](#6-endpoints-de-la-api)
7. [seguridad](#7-seguridad)
8. [configuracion](#8-configuracion)
9. [variables de entorno](#9-variables-de-entorno)
10. [base de datos](#10-base-de-datos)
11. [flujos de uso](#11-flujos-de-uso)
12. [pruebas](#12-pruebas)
13. [user stories](#13-user-stories)
14. [criterios de aceptacion](#14-criterios-de-aceptacion)

---

## 1. enunciado del proyecto

**bookstore** es una api rest construida con spring boot que modela una plataforma saas multi-tenant para librerias. el sistema distingue tres tipos de actores:

| rol | descripcion |
|---|---|
| `ADMIN` | administra la plataforma: usuarios, editoriales, catalogos globales y reportes completos |
| `STORE` | dueno de una libreria: publica su inventario con precio y stock, ve sus propios reportes |
| `CUSTOMER` | comprador: navega el catalogo publico, crea una cuenta y realiza compras |

**reglas de negocio principales:**
- un libro existe una sola vez en el catalogo global (`books`) sin precio ni stock
- cada libreria define su propio precio y stock para ese libro en `store_books`
- una compra descuenta stock de la libreria correspondiente con control de concurrencia optimista (`@Version`)
- cancelar una compra restaura el stock automaticamente (solo si esta en estado `PENDING`)
- los reportes de ventas filtran por libreria segun el rol: `STORE` solo ve sus datos, `ADMIN` ve todo

---

## 2. tecnologias

| tecnologia | version | uso |
|---|---|---|
| java | 21 | lenguaje |
| spring boot | 4.0.2 | framework principal |
| spring security | managed | autenticacion y autorizacion |
| spring data jpa | managed | acceso a datos |
| hibernate | managed | orm |
| postgresql | managed | base de datos |
| jjwt | 0.12.6 | generacion y validacion de jwt |
| mapstruct | 1.6.3 | mapeo entity ↔ dto |
| lombok | 1.18.42 | reduccion de boilerplate |
| springdoc openapi | 2.8.6 | documentacion swagger ui |
| jacoco | 0.8.12 | cobertura de pruebas |

---

## 3. ejecucion local

### requisitos

- java 21
- maven 3.9+
- postgresql 14+

### pasos

```bash
# 1. clonar el repositorio
git clone <url-del-repo>
cd bookstore_v5_test_completo

# 2. crear la base de datos en postgresql
psql -U postgres -c "CREATE DATABASE bookstore_db;"

# 3. aplicar stored procedures
psql -U postgres -d bookstore_db -f src/main/resources/stored_procedures.sql

# 4. configurar variables (opcional, tiene valores por defecto)
cp .env.example .env

# 5. compilar
./mvnw clean compile

# 6. ejecutar
./mvnw spring-boot:run
```

el servidor inicia en `http://localhost:8083`

la documentacion swagger esta disponible en: `http://localhost:8083/swagger-ui/index.html`

---

## 4. estructura del proyecto

```
src/
├── main/
│   ├── java/com/example/bookstore/
│   │   ├── BookstoreApplication.java
│   │   │
│   │   ├── config/
│   │   │   ├── CorsConfig.java          # origenes permitidos, metodos, headers
│   │   │   ├── JacksonConfig.java       # ObjectMapper bean (LocalDateTime support)
│   │   │   ├── JwtConfig.java           # lectura de jwt.secret y jwt.expiration
│   │   │   └── SecurityConfig.java      # filterchain, rutas publicas, stateless
│   │   │
│   │   ├── controller/
│   │   │   ├── AuthController.java      # /api/auth/**
│   │   │   ├── AuthorController.java    # /api/authors/**   (GET publico)
│   │   │   ├── BookController.java      # /api/books/**     (ADMIN, STORE)
│   │   │   ├── CatalogController.java   # /api/catalog/**   (publico)
│   │   │   ├── EditorialController.java # /api/editorials/**
│   │   │   ├── PurchaseController.java  # /api/purchases/**
│   │   │   ├── ReportController.java    # /api/reports/**   (ADMIN, STORE)
│   │   │   ├── StoreBookController.java # /api/store-books/** (ADMIN, STORE)
│   │   │   ├── StoreController.java     # /api/stores/**
│   │   │   └── UserProfileController.java # /api/profile
│   │   │
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java       # solo email + password
│   │   │   │   ├── RegisterStoreRequest.java  # email + password + nombre + telefono
│   │   │   │   ├── UserProfileRequest.java
│   │   │   │   ├── PurchaseRequest.java
│   │   │   │   └── PurchaseItemRequest.java
│   │   │   └── response/
│   │   │       ├── TokenResponse.java         # token + id + email + role + status
│   │   │       ├── UserSummaryResponse.java   # id + email + role + status (para admin)
│   │   │       ├── BookResponse.java
│   │   │       ├── AuthorResponse.java
│   │   │       ├── AuthorWithBookCountResponse.java
│   │   │       ├── EditorialResponse.java
│   │   │       ├── StoreResponse.java
│   │   │       ├── StoreBookResponse.java
│   │   │       ├── PurchaseResponse.java
│   │   │       ├── PurchaseItemResponse.java
│   │   │       ├── UserProfileResponse.java
│   │   │       ├── BookSalesReportResponse.java
│   │   │       └── SalesSummaryResponse.java
│   │   │
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java    # manejo centralizado de errores
│   │   │   ├── ResourceNotFoundException.java # 404
│   │   │   ├── DuplicateResourceException.java # 409
│   │   │   └── InsufficientStockException.java # 400
│   │   │
│   │   ├── mapper/
│   │   │   ├── AuthorMapper.java       # mapeo directo, sin relaciones
│   │   │   ├── BookMapper.java         # resuelve author + editorial
│   │   │   ├── EditorialMapper.java    # mapeo directo, sin relaciones
│   │   │   ├── PurchaseMapper.java     # resuelve items + storeBook
│   │   │   ├── StoreBookMapper.java    # resuelve store + book
│   │   │   ├── StoreMapper.java        # resuelve owner (User)
│   │   │   └── UserProfileMapper.java  # incluye email del User
│   │   │
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── UserProfile.java        # PK compartida con User (@MapsId)
│   │   │   ├── Author.java
│   │   │   ├── Book.java               # catalogo global, sin precio ni stock
│   │   │   ├── Editorial.java
│   │   │   ├── Store.java
│   │   │   ├── StoreBook.java          # precio + stock por libreria + @Version
│   │   │   ├── Purchase.java
│   │   │   ├── PurchaseItem.java       # clave compuesta @EmbeddedId
│   │   │   ├── PurchaseItemId.java
│   │   │   └── enums/
│   │   │       ├── Role.java           # ADMIN, STORE, CUSTOMER
│   │   │       ├── PurchaseStatus.java # PENDING, PAID, COMPLETED, CANCELLED, REFUNDED
│   │   │       └── UserStatus.java     # ACTIVE, PENDING, DISABLED
│   │   │
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── UserProfileRepository.java
│   │   │   ├── AuthorRepository.java
│   │   │   ├── BookRepository.java
│   │   │   ├── EditorialRepository.java
│   │   │   ├── StoreRepository.java
│   │   │   ├── StoreBookRepository.java
│   │   │   ├── PurchaseRepository.java
│   │   │   └── ReportRepository.java   # EntityManager manual (stored procedures)
│   │   │
│   │   └── security/
│   │       ├── JwtFilter.java                  # intercepta requests y valida token
│   │       ├── TokenProvider.java              # crea y valida jwt
│   │       ├── TokenBlacklist.java             # tokens revocados en memoria
│   │       ├── UserDetailsServiceImpl.java     # carga usuario por email
│   │       ├── JwtAuthenticationEntryPoint.java # 401 en formato ErrorResponse
│   │       └── JwtAccessDeniedHandler.java      # 403 en formato ErrorResponse
│
└── resources/
│       ├── application.yaml
│       ├── application-prod.yaml       # sobreescribe para produccion
│       ├── data.sql                    # seed data (requiere sql.init.mode: always)
│       └── stored_procedures.sql      # ejecutar manualmente en postgresql
│
└── test/
    └── java/com/example/bookstore/
        └── service/
            ├── BookServiceTest.java
            ├── PurchaseServiceTest.java
            └── UserServiceTest.java
```

---

## 5. modelo de datos

### diagrama de relaciones

```
users (1) ──────── (1) user_profiles
  │
  └─ (1) ──── (0..1) stores
                │
                └─ (1) ──── (N) store_books ──── (N) books ──── (1) authors
                                  │                                    │
                                  │                               (N) ──── (1) editorials
                                  │
                            (N) purchase_items
                                  │
                            (N) ──── (1) purchases ──── (N) users
```

### entidades principales

**`users`** — credenciales, rol y estado

| columna | tipo | descripcion |
|---|---|---|
| id | bigserial PK | autogenerado |
| email | varchar unique | credencial de acceso |
| password | varchar | hash bcrypt |
| role | varchar | ADMIN, STORE, CUSTOMER |
| status | varchar | ACTIVE · PENDING · DISABLED |

| status | descripcion |
|---|---|
| `ACTIVE` | cuenta normal, puede iniciar sesion |
| `PENDING` | dueno de libreria esperando aprobacion del admin. puede iniciar sesion como CUSTOMER mientras espera. **trabajo futuro:** la verificacion se realizara via correo electronico — el solicitante recibira un email con enlace de confirmacion en lugar de requerir aprobacion manual |
| `DISABLED` | cuenta suspendida por el admin — bloquea el login |

**`user_profiles`** — datos personales

| columna | tipo | descripcion |
|---|---|---|
| user_id | bigint PK + FK | comparte PK con users |
| first_name | varchar | nombre |
| last_name | varchar | apellido |
| phone | varchar | telefono |
| address | varchar | direccion |

> el perfil se crea vacio al registrarse como cliente y con datos de contacto al registrarse como libreria. se actualiza con `PUT /api/profile`

**`store_books`** — inventario por libreria

| columna | tipo | descripcion |
|---|---|---|
| id | bigserial PK | clave surrogada |
| store_id | bigint FK | libreria |
| book_id | bigint FK | libro del catalogo global |
| price | numeric | precio en esa libreria |
| stock | int | unidades disponibles |
| active | boolean | visible en catalogo |
| version | bigint | control optimista de concurrencia |

> `store_id + book_id` tiene restriccion UNIQUE — un libro no se repite en la misma tienda

**`purchase_items`** — items de cada compra

| columna | tipo | descripcion |
|---|---|---|
| purchase_id | bigint PK (parte) | FK a purchases |
| store_book_id | bigint PK (parte) | FK a store_books |
| quantity | int | unidades compradas |
| unit_price | numeric | precio al momento de la compra |
| subtotal | numeric | quantity × unit_price |

> clave compuesta `(purchase_id, store_book_id)` — no se puede comprar el mismo storeBook dos veces en la misma orden

---

## 6. endpoints de la api

### autenticacion — `/api/auth`

| metodo | path | acceso | descripcion |
|---|---|---|---|
| POST | `/api/auth/register` | publico | registro de cliente (status: ACTIVE) |
| POST | `/api/auth/register/store` | publico | registro de dueno de libreria (status: PENDING) |
| POST | `/api/auth/login` | publico | login → devuelve jwt |
| POST | `/api/auth/logout` | autenticado | invalida el token actual |
| PATCH | `/api/auth/users/{id}/role` | ADMIN | cambiar rol (promueve a STORE → activa cuenta) |
| PATCH | `/api/auth/users/{id}/status` | ADMIN | cambiar status (ACTIVE / DISABLED) |
| GET | `/api/auth/users/pending-stores` | ADMIN | listar solicitudes de libreria pendientes |

**body de registro de cliente:**
```json
{
  "email": "cliente@example.com",
  "password": "Cliente@1234"
}
```

**body de registro de libreria:**
```json
{
  "email": "libreria@example.com",
  "password": "Libreria@1234",
  "firstName": "Carlos",
  "lastName": "Quispe",
  "phone": "+51999888777"
}
```

**politica de password** — debe cumplir todos:
- minimo 8 caracteres
- al menos una letra mayuscula
- al menos una letra minuscula
- al menos un numero
- al menos un caracter especial (`@$!%*?&._-`)

**respuesta de login / registro:**
```json
{
  "token": "eyJhbGci...",
  "id": 4,
  "email": "cliente@example.com",
  "role": "CUSTOMER",
  "status": "ACTIVE"
}
```

---

### catalogo publico — `/api/catalog`

| metodo | path | acceso | descripcion |
|---|---|---|---|
| GET | `/api/catalog` | publico | todos los store_books activos |
| GET | `/api/catalog/{id}` | publico | detalle de un store_book |
| GET | `/api/catalog/store/{storeId}` | publico | libros de una libreria especifica |

---

### autores — `/api/authors`

| metodo | path | acceso | descripcion |
|---|---|---|---|
| GET | `/api/authors` | publico | listar todos |
| GET | `/api/authors/{id}` | publico | obtener por id |
| GET | `/api/authors/by-nationality?nationality=` | publico | filtrar por nacionalidad |
| GET | `/api/authors/search?lastName=` | publico | buscar por apellido |
| GET | `/api/authors/by-book-count` | publico | ordenados por cantidad de libros |
| POST | `/api/authors` | autenticado | crear autor |
| PUT | `/api/authors/{id}` | autenticado | actualizar autor |
| DELETE | `/api/authors/{id}` | autenticado | eliminar autor |

---

### libros — `/api/books`

> catalogo global. precio y stock no viven aqui, viven en store_books.

| metodo | path | acceso | descripcion |
|---|---|---|---|
| GET | `/api/books` | ADMIN, STORE | listar catalogo global |
| GET | `/api/books/{id}` | ADMIN, STORE | obtener libro por id |
| POST | `/api/books` | ADMIN, STORE | agregar libro al catalogo |
| PUT | `/api/books/{id}` | ADMIN, STORE | actualizar datos del libro |
| DELETE | `/api/books/{id}` | ADMIN | eliminar libro |

---

### editoriales — `/api/editorials`

| metodo | path | acceso | descripcion |
|---|---|---|---|
| GET | `/api/editorials` | autenticado | listar editoriales |
| GET | `/api/editorials/{id}` | autenticado | obtener por id |
| POST | `/api/editorials` | ADMIN | crear editorial |
| PUT | `/api/editorials/{id}` | ADMIN | actualizar editorial |
| DELETE | `/api/editorials/{id}` | ADMIN | eliminar editorial |

---

### librerias — `/api/stores`

| metodo | path | acceso | descripcion |
|---|---|---|---|
| GET | `/api/stores` | ADMIN | listar todas las librerias |
| GET | `/api/stores/{id}` | autenticado | ver detalle de una libreria |
| POST | `/api/stores` | ADMIN, STORE | crear tienda |
| PUT | `/api/stores/{id}` | ADMIN, STORE | actualizar datos de la tienda |
| DELETE | `/api/stores/{id}` | ADMIN | eliminar tienda |

> cuando un usuario STORE crea una tienda no envía `ownerId` — se auto-asigna su propio id. solo puede tener una tienda.

---

### inventario por libreria — `/api/store-books`

| metodo | path | acceso | descripcion |
|---|---|---|---|
| GET | `/api/store-books` | ADMIN, STORE | listar todo el inventario |
| GET | `/api/store-books/{id}` | ADMIN, STORE | obtener item por id |
| GET | `/api/store-books/by-store/{storeId}` | ADMIN, STORE | inventario de una tienda |
| POST | `/api/store-books` | ADMIN, STORE | agregar libro a la tienda con precio y stock |
| PUT | `/api/store-books/{id}` | ADMIN, STORE | actualizar precio o stock |
| DELETE | `/api/store-books/{id}` | ADMIN, STORE | quitar libro de la tienda |

---

### compras — `/api/purchases`

| metodo | path | acceso | descripcion |
|---|---|---|---|
| POST | `/api/purchases` | autenticado | crear compra (→ PENDING, stock reservado) |
| POST | `/api/purchases/{id}/pay` | autenticado | confirmar pago (PENDING → PAID) |
| GET | `/api/purchases` | autenticado | historial de compras |
| GET | `/api/purchases/{id}` | autenticado | detalle de una compra |
| PUT | `/api/purchases/{id}/cancel` | autenticado | cancelar compra (solo si esta PENDING) |

**estados de una compra:**

| estado | descripcion |
|---|---|
| `PENDING` | creada, stock reservado, esperando confirmacion de pago |
| `PAID` | pago confirmado por la pasarela (webhook en produccion) |
| `COMPLETED` | pedido entregado / procesado |
| `CANCELLED` | cancelado antes del pago — stock restaurado |
| `REFUNDED` | reembolso post-pago (para integracion futura con pasarela) |

> en produccion, el endpoint `/pay` seria reemplazado por el webhook del proveedor de pagos.

**body de compra:**
```json
{
  "items": [
    { "storeBookId": 3, "quantity": 1 },
    { "storeBookId": 1, "quantity": 2 }
  ]
}
```

---

### reportes — `/api/reports`

> requiere rol ADMIN o STORE. la respuesta se filtra automaticamente segun el rol.

| metodo | path | acceso | descripcion |
|---|---|---|---|
| GET | `/api/reports/sales-by-book` | ADMIN, STORE | ventas por libro |
| GET | `/api/reports/summary` | ADMIN, STORE | resumen global de ventas |

**parametros opcionales (ambos endpoints):**
```
?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
```

**respuesta de sales-by-book (ADMIN ve todas las librerias):**
```json
[
  {
    "bookId": 3,
    "bookTitle": "Clean Code",
    "storeId": 1,
    "storeName": "Libreria El Quijote",
    "totalQuantity": 5,
    "totalRevenue": 249.95,
    "totalPurchases": 3
  }
]
```

**respuesta de summary:**
```json
{
  "totalSales": 294.94,
  "totalPurchases": 2,
  "averagePerPurchase": 147.47,
  "bestSellerId": 3,
  "bestSellerTitle": "Clean Code",
  "bestSellerQuantity": 5
}
```

---

### perfil — `/api/profile`

| metodo | path | acceso | descripcion |
|---|---|---|---|
| GET | `/api/profile` | autenticado | ver perfil del usuario actual |
| PUT | `/api/profile` | autenticado | crear o actualizar perfil |

---

## 7. seguridad

### arquitectura jwt

```
1. cliente → POST /api/auth/login
2. servidor valida credenciales con BCryptPasswordEncoder
3. servidor genera JWT firmado con HS256 (SecretKey pre-calculada)
4. cliente incluye en cada request: Authorization: Bearer <token>
5. JwtFilter intercepta → valida firma → carga SecurityContext
6. SecurityConfig + @PreAuthorize controlan acceso por rol
```

### componentes de seguridad

| clase | responsabilidad |
|---|---|
| `TokenProvider` | crea y valida jwt. la `SecretKey` se inicializa una vez en el constructor, no en cada llamada |
| `TokenBlacklist` | set en memoria de tokens revocados. `logout` agrega el token aqui. `JwtFilter` verifica antes de autenticar |
| `JwtFilter` | `OncePerRequestFilter`. extrae el token del header, verifica blacklist, valida firma, carga el `SecurityContext` |
| `UserDetailsServiceImpl` | carga el usuario por email desde la bd. bloquea el login si `status == DISABLED` |
| `JwtAuthenticationEntryPoint` | devuelve `401` con `ErrorResponse` en json cuando el token falta o es invalido |
| `JwtAccessDeniedHandler` | devuelve `403` con `ErrorResponse` en json cuando el usuario no tiene el rol necesario |
| `GlobalExceptionHandler` | maneja `BadCredentialsException` (401), `AccessDeniedException` (403), errores de validacion (400) y errores genericos (500) |

### rutas publicas (sin token)

```
POST  /api/auth/**           → login, registro, logout
GET   /api/catalog/**        → catalogo de libros disponibles
GET   /api/authors/**        → autores
GET   /swagger-ui/**         → documentacion
GET   /v3/api-docs/**        → openapi json
```

### headers de seguridad http

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

### autorizacion por rol

| recurso | CUSTOMER | STORE | ADMIN |
|---|---|---|---|
| ver catalogo publico | ✓ (sin token) | ✓ | ✓ |
| comprar | ✓ | — | — |
| ver/editar perfil propio | ✓ | ✓ | ✓ |
| gestionar libros globales | — | ✓ | ✓ |
| ver reportes | — | ✓ (solo su tienda) | ✓ (todas) |
| cambiar roles / status | — | — | ✓ |
| eliminar recursos | — | — | ✓ |

---

## 8. configuracion

### application.yaml

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/bookstore_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:adminadmin}

  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}    # usar 'validate' en produccion
    show-sql: ${JPA_SHOW_SQL:true}        # desactivar en produccion
    defer-datasource-initialization: true  # hibernate crea tablas antes de data.sql
    properties:
      hibernate:
        jdbc:
          "batch_size": 20               # agrupa hasta 20 inserts/updates por batch
        "order_inserts": true            # ordena inserts del mismo tipo juntos
        "order_updates": true            # ordena updates del mismo tipo juntos

server:
  port: ${SERVER_PORT:8083}

jwt:
  secret: ${JWT_SECRET:...}
  expiration: ${JWT_EXPIRATION:86400000} # 24 horas en ms

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:4200}
```

### batch_size, order_inserts, order_updates

estas tres propiedades trabajan juntas para optimizar escrituras masivas en la base de datos. se aplican de forma global a todas las entidades.

**batch_size: 20** — sin batch, hibernate envia una query sql por cada entidad al hacer `save()` en un bucle:
```sql
INSERT INTO purchase_items VALUES (1, 3, 1, 49.99, 49.99);  -- query 1
INSERT INTO purchase_items VALUES (1, 1, 2, 29.99, 59.98);  -- query 2
INSERT INTO purchase_items VALUES (2, 4, 1, 44.99, 44.99);  -- query 3
```
con `batch_size: 20` se acumulan hasta 20 sentencias y se envian en un solo round-trip al driver jdbc:
```sql
INSERT INTO purchase_items VALUES (?,...),(?,...),(?,...);  -- 1 round-trip
```

**order_inserts / order_updates** — garantizan que hibernate agrupe las sentencias del mismo tipo antes de enviarlas, lo que permite que el batch sea efectivo. sin estas opciones, los inserts de distintas tablas podrian intercalarse y romper el batch. como beneficio adicional, `order_updates` previene deadlocks al garantizar que multiples transacciones actualizan filas en el mismo orden.

**donde aplica en este proyecto:**
- `POST /api/purchases` — crea 1 `Purchase` + N `PurchaseItem` + actualiza stock de N `StoreBook`
- `PUT /api/purchases/{id}/cancel` — actualiza 1 `Purchase` + restaura stock de N `StoreBook`

> **nota:** con `GenerationType.IDENTITY` (que usa este proyecto), postgresql no puede ejecutar batch nativo porque cada INSERT necesita retornar el ID generado. hibernate lo detecta y el batch actua como optimizacion del driver. para activar batch completo se necesitaria cambiar a `GenerationType.SEQUENCE` y agregar `reWriteBatchedInserts=true` a la url jdbc — cambio que requiere reestructurar el modelo y esta fuera del alcance actual.

### cors (CorsConfig.java)

| propiedad | valor |
|---|---|
| origenes permitidos | `http://localhost:3000`, `http://localhost:4200` (configurable) |
| metodos | GET, POST, PUT, DELETE, OPTIONS |
| headers permitidos | Authorization, Content-Type, Accept |
| header expuesto | Authorization |
| credentials | true |
| preflight cache | 3600 segundos |

### jackson (JacksonConfig.java)

registra el modulo `JavaTimeModule` para serializar `LocalDateTime` como string iso en lugar de array `[2024, 6, 15, 10, 30, 0]`. sin este bean, las fechas de `PurchaseResponse` y los parametros `from`/`to` de los reportes no funcionarian correctamente.

---

## 9. variables de entorno

todas las variables tienen valor por defecto para desarrollo local. en produccion deben sobreescribirse:

| variable | default | descripcion |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/bookstore_db` | url de conexion a postgresql |
| `DB_USERNAME` | `postgres` | usuario de la bd |
| `DB_PASSWORD` | `adminadmin` | password de la bd |
| `JPA_DDL_AUTO` | `update` | usar `validate` en produccion |
| `JPA_SHOW_SQL` | `true` | usar `false` en produccion |
| `SERVER_PORT` | `8083` | puerto del servidor |
| `JWT_SECRET` | *(valor de desarrollo)* | minimo 256 bits. **cambiar en produccion** |
| `JWT_EXPIRATION` | `86400000` | duracion del token en ms (24h) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,...` | origenes permitidos separados por coma |

crea un archivo `.env` a partir de `.env.example`:
```bash
cp .env.example .env
# editar .env con tus valores reales
```

---

## 10. base de datos

### seed data (data.sql)

el archivo `data.sql` carga datos de prueba con cuatro usuarios, dos librerias, cuatro autores, cuatro libros y dos compras completadas para probar los reportes.

para activar la carga automatica, descomentar en `application.yaml`:
```yaml
sql:
  init:
    mode: always
jpa:
  defer-datasource-initialization: true
```

usuarios seed (password: `Admin@2024`):

| id | email | rol | status | libreria |
|---|---|---|---|---|
| 1 | admin@bookstore.com | ADMIN | ACTIVE | — |
| 2 | quijote@libreria.com | STORE | ACTIVE | Libreria El Quijote |
| 3 | cervantes@libreria.com | STORE | ACTIVE | Libreria Cervantes |
| 4 | cliente@example.com | CUSTOMER | ACTIVE | — |

### stored procedures

las funciones `fn_sales_report_by_book` y `fn_sales_summary` viven en postgresql y **no se ejecutan automaticamente**. deben aplicarse una sola vez de forma manual:

```bash
psql -U postgres -d bookstore_db -f src/main/resources/stored_procedures.sql
```

ambas funciones aceptan tres parametros opcionales:

| parametro | tipo | descripcion |
|---|---|---|
| `p_date_from` | TIMESTAMP | inicio del rango (null = sin limite) |
| `p_date_to` | TIMESTAMP | fin del rango (null = sin limite) |
| `p_store_id` | BIGINT | filtrar por libreria (null = todas) |

el servicio `ReportServiceImpl` aplica `p_store_id` automaticamente segun el rol del usuario autenticado.

---

## 11. flujos de uso

### flujo a — cliente compra un libro

```
1. GET  /api/catalog                          → ver libros disponibles (sin token)
2. POST /api/auth/register                    → crear cuenta
3. POST /api/auth/login                       → obtener token
4. POST /api/purchases                        → comprar (→ status PENDING, stock reservado)
5. POST /api/purchases/{id}/pay              → confirmar pago (→ status PAID)
6. GET  /api/purchases/{id}                   → ver detalle de la compra
```

### flujo b — registro y activacion de libreria

```
1. POST /api/auth/register/store              → registrar con datos de contacto (status: PENDING)
                                                respuesta incluye id del nuevo usuario
2. [admin] POST /api/auth/login               → admin obtiene su token
3. [admin] GET  /api/auth/users/pending-stores → admin consulta solicitudes pendientes
4. [admin] PATCH /api/auth/users/{id}/role    → promover a STORE (activa cuenta automaticamente)
5. POST /api/auth/login                       → dueno obtiene su token como STORE (status: ACTIVE)
6. POST /api/stores                           → crear su libreria (sin ownerId, auto-asignado)
7. POST /api/store-books                      → agregar libros con precio y stock
```

> **mejora futura:** el paso 3-4 sera reemplazado por verificacion via correo electronico. al registrarse, el solicitante recibira un email con un enlace de activacion. cuando lo confirme, su cuenta pasara automaticamente de `PENDING` a `ACTIVE` con rol `STORE`, sin intervencion manual del admin.

### flujo c — dueno de libreria ve sus reportes

```
1. POST /api/auth/login                       → token del STORE
2. GET  /api/reports/sales-by-book            → ventas filtradas automaticamente por su libreria
3. GET  /api/reports/summary                  → resumen de su libreria
4. GET  /api/reports/sales-by-book?from=...&to=... → ventas en rango de fechas
```

---

## 12. pruebas

### ejecutar pruebas unitarias

```bash
./mvnw test
```

### cobertura (jacoco)

```bash
./mvnw verify
# reporte en: target/site/jacoco/index.html
```

### casos de prueba cubiertos

> | rango | clase de test | area |
> |---|---|---|
> | CP-01 a CP-07 | `BookServiceTest` | gestion del catalogo global de libros |
> | CP-08 a CP-16 | `PurchaseServiceTest` | compras, confirmacion de pago y cancelaciones |
> | CP-17 a CP-25 | `UserServiceTest` | registro, estados, roles y sesion |

| id | descripcion | clase | metodo |
|---|---|---|---|
| CP-01 | el admin agrega un libro con datos validos | `BookServiceTest` | `saveValidBook` |
| CP-02 | no se puede agregar un libro si el autor no existe | `BookServiceTest` | `saveBookAuthorNotFound` |
| CP-03 | no se puede agregar un libro si la editorial no existe | `BookServiceTest` | `saveBookEditorialNotFound` |
| CP-04 | no se puede duplicar un titulo para el mismo autor | `BookServiceTest` | `saveBookDuplicateTitle` |
| CP-05 | se pueden listar todos los libros del catalogo | `BookServiceTest` | `findAllBooks` |
| CP-06 | se puede ver el detalle de un libro por su id | `BookServiceTest` | `findBookById` |
| CP-07 | buscar un libro que no existe devuelve error | `BookServiceTest` | `findBookByIdNotFound` |
| CP-08 | el cliente crea una compra y queda en estado PENDING | `PurchaseServiceTest` | `purchaseWithStock` |
| CP-09 | el cliente no puede comprar si el stock es insuficiente | `PurchaseServiceTest` | `purchaseWithoutStock` |
| CP-10 | el total de una compra con varios libros es correcto | `PurchaseServiceTest` | `purchaseMultipleBooks` |
| CP-11 | el stock se descuenta al crear la compra | `PurchaseServiceTest` | `purchaseReducesStock` |
| CP-12 | confirmar el pago cambia la compra de PENDING a PAID | `PurchaseServiceTest` | `confirmPaymentChangesPendingToPaid` |
| CP-13 | no se puede confirmar el pago de una compra ya pagada | `PurchaseServiceTest` | `confirmPaymentAlreadyPaidThrowsException` |
| CP-14 | cancelar una compra PENDING restaura el stock | `PurchaseServiceTest` | `cancelPendingPurchaseRestoresStock` |
| CP-15 | no se puede cancelar una compra ya pagada | `PurchaseServiceTest` | `cancelPaidPurchase` |
| CP-16 | cancelar una compra inexistente devuelve error | `PurchaseServiceTest` | `cancelPurchaseNotFound` |
| CP-17 | el cliente se registra con status ACTIVE y perfil creado | `UserServiceTest` | `registerCustomerCreatesUserAndEmptyProfile` |
| CP-18 | no es posible registrarse con un correo que ya esta en uso | `UserServiceTest` | `registerDuplicateEmailThrowsException` |
| CP-19 | el dueno de libreria se registra con status PENDING y datos de contacto | `UserServiceTest` | `registerStoreOwnerCreatesProfileWithContactData` |
| CP-20 | la libreria no puede registrarse con un correo ya existente | `UserServiceTest` | `registerStoreDuplicateEmailThrowsException` |
| CP-21 | el admin aprueba a un usuario: rol STORE + status ACTIVE | `UserServiceTest` | `adminPromotesUserToStore` |
| CP-22 | el admin no puede cambiar el rol de un usuario que no existe | `UserServiceTest` | `changeRoleUserNotFoundThrowsException` |
| CP-23 | el admin suspende una cuenta (status DISABLED) | `UserServiceTest` | `adminDisablesUser` |
| CP-24 | el admin consulta las solicitudes de libreria pendientes | `UserServiceTest` | `getPendingStoreApplications` |
| CP-25 | el usuario cierra sesion y su token queda invalidado | `UserServiceTest` | `logoutRevokesToken` |

### probar con postman

importa el archivo `bookstore.postman_collection.json` en postman.

variables de la coleccion:

| variable | descripcion |
|---|---|
| `base_url` | `http://localhost:8083` |
| `token` | token del admin (se guarda automaticamente al hacer login as admin) |
| `store_token` | token del dueno de libreria (se guarda automaticamente) |
| `new_user_id` | id del usuario recien registrado (se guarda automaticamente al registrar store owner) |

**orden de ejecucion para el flujo de libreria:**
1. `auth → register store owner (paso 1/3)` — guarda `{{new_user_id}}`
2. `auth → login as admin` — guarda `{{token}}`
3. `auth → change role to STORE (paso 2/3)` — usa `{{token}}` y `{{new_user_id}}`
4. `auth → login as store owner (paso 3a/3)` — guarda `{{store_token}}`
5. `stores → create my store (paso 3b/3)` — usa `{{store_token}}`

---

## 13. user stories

### autenticacion

| id | historia |
|---|---|
| US-01 | como visitante quiero registrarme con email y password para comprar libros |
| US-02 | como dueno de libreria quiero registrarme con mis datos de contacto para solicitar acceso a la plataforma |
| US-03 | como usuario registrado quiero iniciar sesion y recibir un token para usar la api |
| US-04 | como usuario autenticado quiero cerrar sesion para invalidar mi token |
| US-05 | como admin quiero gestionar el estado de las cuentas para aprobar librerias o suspender usuarios |

### catalogo publico

| id | historia |
|---|---|
| US-06 | como visitante quiero ver todos los libros disponibles en todas las librerias sin necesidad de cuenta |
| US-07 | como visitante quiero ver el catalogo de una libreria especifica para comparar precios |
| US-08 | como visitante quiero ver los detalles de un libro (precio, stock, libreria) |

### gestion de inventario (store)

| id | historia |
|---|---|
| US-09 | como dueno de libreria quiero agregar libros a mi inventario con precio y stock |
| US-10 | como dueno de libreria quiero actualizar el precio o stock de un libro en mi tienda |
| US-11 | como dueno de libreria quiero crear mi perfil de tienda despues de ser aprobado por el admin |

### compras (customer)

| id | historia |
|---|---|
| US-12 | como cliente quiero comprar uno o varios libros en una sola transaccion |
| US-13 | como cliente quiero confirmar el pago de mi compra para que sea procesada |
| US-14 | como cliente quiero cancelar una compra pendiente para recuperar el stock |

### reportes (admin / store)

| id | historia |
|---|---|
| US-15 | como admin quiero ver ventas por libro en todas las librerias con filtro de fechas |
| US-16 | como admin quiero ver un resumen global de ventas: total, promedio y libro mas vendido |
| US-17 | como dueno de libreria quiero ver las ventas de mi propia libreria sin acceder a datos de otras |

### perfil

| id | historia |
|---|---|
| US-18 | como usuario autenticado quiero ver y actualizar mi perfil (nombre, telefono, direccion) |

---

## 14. criterios de aceptacion

los criterios describen el comportamiento esperado desde el punto de vista del usuario. cada criterio esta escrito en formato **dado — cuando — entonces** e indica el caso de prueba automatizado que lo valida.

---

### US-01 — registro de cliente

**CP-17** — el cliente se registra exitosamente

> **dado** que soy una persona nueva en la plataforma
> **cuando** me registro con un correo valido y una contrasena que cumple los requisitos de seguridad
> **entonces** accedo de inmediato con mi cuenta creada (status `ACTIVE`) y mi perfil queda listo para completar mis datos personales cuando quiera

**CP-18** — el correo ya pertenece a otra cuenta

> **dado** que ya tengo una cuenta en la plataforma
> **cuando** intento crear otra cuenta con el mismo correo
> **entonces** la plataforma me avisa que ese correo ya esta en uso y no se crea ninguna cuenta nueva

---

### US-02 — registro de dueno de libreria

**CP-19** — el dueno de libreria se registra con sus datos de contacto

> **dado** que soy el dueno de una libreria y quiero unirme a la plataforma
> **cuando** me registro proporcionando mi nombre, apellido y telefono ademas de mis credenciales
> **entonces** se crea mi cuenta con mis datos de contacto guardados, quedo en estado `PENDING` y puedo iniciar sesion mientras espero la aprobacion

> **nota — mejora futura:** la aprobacion actual es manual (el admin promueve al usuario). en una version posterior, el solicitante recibira un correo electronico con un enlace de activacion que completara el proceso automaticamente sin intervencion del admin.

**CP-20** — no es posible registrar dos librerias con el mismo correo

> **dado** que ya existe una cuenta con mi correo
> **cuando** intento registrarme como dueno de libreria con ese mismo correo
> **entonces** la plataforma me informa que ese correo ya esta registrado y no crea la cuenta

---

### US-03 — inicio de sesion

> **dado** que tengo una cuenta activa
> **cuando** ingreso mi correo y contrasena correctamente
> **entonces** obtengo acceso a la plataforma y el sistema reconoce mi rol para mostrarme las funciones que me corresponden

> **dado** que ingreso credenciales incorrectas
> **cuando** intento iniciar sesion
> **entonces** la plataforma me informa que las credenciales son invalidas y no se concede acceso

---

### US-04 — cierre de sesion

**CP-25** — el usuario cierra sesion y su acceso queda revocado

> **dado** que estoy con sesion activa
> **cuando** cierro sesion
> **entonces** mi acceso queda invalidado de inmediato y no puedo realizar mas acciones con la misma sesion

---

### US-05 — aprobacion y gestion de cuentas por el admin

**CP-21** — el admin aprueba al dueno de una libreria

> **dado** que soy el administrador de la plataforma y hay un usuario con status `PENDING`
> **cuando** le asigno el rol de dueno de libreria
> **entonces** ese usuario pasa a rol `STORE` con status `ACTIVE` y puede crear su tienda en la plataforma

**CP-22** — el admin intenta aprobar un usuario que no existe

> **dado** que soy el administrador
> **cuando** intento cambiar el rol de un usuario que no existe en el sistema
> **entonces** la plataforma me informa que ese usuario no fue encontrado

**CP-23** — el admin suspende una cuenta

> **dado** que soy el administrador
> **cuando** cambio el status de un usuario a `DISABLED`
> **entonces** ese usuario no puede iniciar sesion hasta que el admin reactive su cuenta

**CP-24** — el admin consulta solicitudes de libreria pendientes

> **dado** que soy el administrador
> **cuando** consulto la lista de solicitudes pendientes
> **entonces** veo todos los usuarios con status `PENDING` que esperan ser aprobados como duenos de libreria

---

### US-06 — explorar el catalogo sin cuenta

> **dado** que soy un visitante sin cuenta
> **cuando** navego el catalogo de libros disponibles
> **entonces** veo todos los libros activos de todas las librerias con su precio y disponibilidad, sin necesidad de registrarme

---

### US-09 a US-11 — gestion del catalogo de libros (admin / libreria)

**CP-01** — agregar un libro nuevo al catalogo

> **dado** que soy administrador o dueno de libreria
> **cuando** registro un libro con titulo, autor y editorial que existen
> **entonces** el libro queda disponible en el catalogo global con todos sus datos

**CP-02** — el autor del libro no existe

> **dado** que intento registrar un libro
> **cuando** el autor indicado no esta registrado en la plataforma
> **entonces** la plataforma me avisa que el autor no fue encontrado y no guarda el libro

**CP-03** — la editorial del libro no existe

> **dado** que intento registrar un libro
> **cuando** la editorial indicada no esta registrada en la plataforma
> **entonces** la plataforma me avisa que la editorial no fue encontrada y no guarda el libro

**CP-04** — el titulo del libro ya existe para ese autor

> **dado** que intento registrar un libro con un titulo que ya tiene ese autor en el catalogo
> **cuando** confirmo el registro
> **entonces** la plataforma me avisa que ese libro ya existe para ese autor y no lo duplica

**CP-05** — listar el catalogo de libros

> **dado** que soy administrador o dueno de libreria
> **cuando** consulto el listado de libros
> **entonces** veo todos los libros registrados en el catalogo global con su autor y editorial

**CP-06** — ver el detalle de un libro

> **dado** que conozco el identificador de un libro
> **cuando** consulto su detalle
> **entonces** veo toda la informacion del libro incluyendo autor y editorial

**CP-07** — el libro buscado no existe

> **dado** que busco un libro por un identificador
> **cuando** ese identificador no corresponde a ningun libro
> **entonces** la plataforma me informa que el libro no fue encontrado

---

### US-12 — realizar una compra

**CP-08** — el cliente crea una compra exitosamente

> **dado** que soy un cliente con sesion iniciada
> **cuando** selecciono un libro con unidades disponibles y confirmo la compra
> **entonces** mi compra queda registrada en estado `PENDING` con el stock reservado, y recibo el detalle con el total a pagar

**CP-09** — el stock no alcanza para la cantidad solicitada

> **dado** que quiero comprar mas unidades de las que hay disponibles
> **cuando** confirmo la compra
> **entonces** la plataforma me avisa que no hay stock suficiente y la compra no se realiza

**CP-10** — compra de varios libros con total correcto

> **dado** que agrego varios libros al pedido con distintas cantidades
> **cuando** confirmo la compra
> **entonces** el total refleja correctamente la suma de todos los articulos

**CP-11** — el stock se descuenta correctamente despues de comprar

> **dado** que una libreria tiene 10 unidades de un libro
> **cuando** compro 3 unidades
> **entonces** la libreria queda con 7 unidades disponibles para otros compradores

---

### US-13 — confirmar el pago de una compra

**CP-12** — el cliente confirma el pago y la compra pasa a PAID

> **dado** que tengo una compra en estado `PENDING`
> **cuando** confirmo el pago (o el sistema recibe la confirmacion de la pasarela)
> **entonces** mi compra pasa a estado `PAID` y queda lista para ser procesada

**CP-13** — no se puede confirmar el pago de una compra ya pagada

> **dado** que una compra ya tiene estado `PAID`
> **cuando** intento confirmar el pago nuevamente
> **entonces** la plataforma me informa que solo se puede pagar una compra en estado `PENDING`

---

### US-14 — cancelar una compra

**CP-14** — cancelar una compra pendiente y recuperar el stock

> **dado** que tengo una compra en estado `PENDING` (aun no pagada)
> **cuando** la cancelo
> **entonces** mi compra queda como cancelada y las unidades vuelven a estar disponibles en la libreria

**CP-15** — no se puede cancelar una compra ya pagada

> **dado** que una compra ya fue pagada (estado `PAID` o superior)
> **cuando** intento cancelarla
> **entonces** la plataforma me informa que solo se puede cancelar una compra en estado `PENDING`

**CP-16** — la compra a cancelar no existe

> **dado** que intento cancelar una compra
> **cuando** el identificador de la compra no existe
> **entonces** la plataforma me informa que la compra no fue encontrada

---

### US-15 a US-17 — reportes de ventas

> **dado** que soy administrador
> **cuando** consulto el reporte de ventas
> **entonces** veo las ventas de todas las librerias con cantidad vendida, ingresos y numero de compras por libro

> **dado** que soy dueno de una libreria
> **cuando** consulto el reporte de ventas
> **entonces** veo unicamente las ventas de mi propia libreria, sin acceso a datos de otras tiendas

> **dado** que soy cliente
> **cuando** intento acceder a los reportes
> **entonces** la plataforma me indica que no tengo permiso para ver esa informacion

> **dado** que soy administrador o dueno de libreria
> **cuando** filtro el reporte por un rango de fechas
> **entonces** veo unicamente las ventas realizadas dentro de ese periodo

---

### politica de contrasena

> **dado** que me registro en la plataforma
> **cuando** mi contrasena no cumple los requisitos minimos de seguridad
> **entonces** la plataforma me indica exactamente que requisito no se cumple y no crea la cuenta hasta que la contrasena sea segura

requisitos minimos:
- al menos 8 caracteres
- al menos una letra mayuscula y una minuscula
- al menos un numero
- al menos un caracter especial (`@`, `$`, `!`, `%`, `*`, `?`, `&`, `.`, `_`, `-`)

### CODIGOS DOCKER
> **docker compose up --build** inicializa todas las imagenes
> **docker container ls** lista todas las imagenes
> **docker exec -i bookstore_db psql -U postgres -d bookstore_db < src/main/resources/data.sql** ejecuta un script SQL desde un archivo especifico
> **docker exec -it bookstore_db psql -U postgres -d bookstore_db** ingresar a la base de datos desde docker
> **Get-Content src/main/resources/data.sql | docker exec -i bookstore_db psql -U postgres -d bookstore_db** igual al comando anterior pero para el Shell de Windows
> **SELECT * FROM books LIMIT 5;** muestra los registros de las tablas desde DOCKER
> **docker exec -it bookstore_db psql -U postgres -d bookstore_db** ingresar a la base de datos desde docker
> **Get-Content src/main/resources/data.sql | docker exec -i bookstore_db psql -U postgres -d bookstore_db** igual al comando anterior pero para el Shell de Windows
> **SELECT * FROM books LIMIT 5;** muestra los registros de las tablas desde DOCKER
> **&admin123456#$** contraseña de BD en SUPABASE
> **bookstore-AI** nombre proyecto en SUPABASE
<<<<<<< HEAD

### PLANTILLA CARGA MASIVA
> **Recomendación para la plantilla**
> **Para que tus proveedores no se equivoquen, te sugiero crear un archivo Excel de ejemplo (una plantilla) con estos datos de prueba y enviárselo:**
> **Fila 1 (Encabezados): Título | Autor | Nacionalidad | Género | Precio | Stock | Descripción**
> **Fila 2 (Ejemplo): Cien años de soledad | Gabriel García Márquez | Peruano | Novela | 25.50 | 10 | Obra maestra del realismo mágico**

> **docker compose stop "api"** detener un servicio especifico en docker"** iniciar un servicio especifico en docker
> **docker compose logs -f "api"** ver los logs de un servicio especifico en docker
> **docker compose up -d --build "api"** reconstruir la imagen para que el cambio surta efecto dentro del contenedor
> **docker compose down** Detiene y elimina los contenedores y las redes virtuales

