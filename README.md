# ms-users-poc

Microservicio de gestión de usuarios con GraphQL y Spring Boot.

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.2.x |
| Spring GraphQL | incluido en Boot |
| Spring Data JPA + JpaSpecificationExecutor | incluido en Boot |
| PostgreSQL | 16 |
| Redis | 7 (caché, opcional) |
| Lombok | latest |
| Gradle | wrapper incluido |

---

## Estructura del proyecto

```
ms-users-poc/
├── cache/
│   └── docker-compose.yml          # Redis + Redis Commander
├── db/
│   └── docker-compose.yml          # PostgreSQL + pgAdmin
├── src/main/java/.../
│   ├── config/                     # ValidationConfig
│   ├── controllers/                # IUserController + UserControllerImpl
│   ├── dtos/                       # DTOs de request, response y paginación
│   ├── exceptions/                 # Jerarquía de excepciones por capa + GlobalExceptionHandler
│   ├── models/
│   │   ├── enums/                  # UserType, SubscriptionType
│   │   ├── AdminUser.java
│   │   ├── RegularUser.java
│   │   └── UserEntity.java
│   ├── repositories/
│   │   ├── IUserRepository.java    # JpaRepository + JpaSpecificationExecutor
│   │   └── specifications/
│   │       └── UserSpecification.java
│   └── services/
│       ├── factories/              # Abstract Factory: AdminUserFactory, RegularUserFactory
│       ├── IUserService.java
│       └── impl/
│           ├── PasswordService.java
│           └── UserServiceImpl.java
└── src/main/resources/
    ├── application.yml
    └── graphql/
        └── schema.graphqls
```

---

## Infraestructura local

### Base de datos (PostgreSQL)

```bash
docker compose -f db/docker-compose.yml up -d
```

| Servicio | URL |
|---|---|
| PostgreSQL | `localhost:5432` |
| pgAdmin | http://localhost:5050 |

### Caché (Redis) — opcional

```bash
docker compose -f cache/docker-compose.yml up -d
```

| Servicio | URL |
|---|---|
| Redis | `localhost:6379` |
| Redis Commander | http://localhost:8081 |

---

## Levantar la aplicación

```bash
./gradlew clean build
./gradlew bootRun
```

- GraphQL endpoint: `POST http://localhost:8080/graphql`
- GraphiQL UI: http://localhost:8080/graphiql

---

## Modelo de dominio

### Tipos de usuario (`UserType`)

| Valor | Descripción |
|---|---|
| `ADMIN` | Usuario administrador con acceso total |
| `REGULAR` | Usuario regular con acceso limitado |

### Tipos de suscripción (`SubscriptionType`)

| Valor | Descripción |
|---|---|
| `FREE` | Plan gratuito |
| `BASIC` | Plan básico |
| `PREMIUM` | Plan premium |
| `ENTERPRISE` | Plan empresarial |

---

## Arquitectura de excepciones

| Capa | Excepciones |
|---|---|
| **Controller** | `NotFoundException`, `ConflictException`, `BadRequestException`, `BadGatewayException` |
| **Service** | `InternalServerErrorException` |

El servicio lanza `InternalServerErrorException` ante errores de base de datos. El controller la captura y relanza como `BadGatewayException`. El `GlobalExceptionHandler` (extiende `DataFetcherExceptionResolverAdapter`) mapea todas las excepciones a errores GraphQL tipificados.

---

## Esquema GraphQL

### Enums

```graphql
enum UserType      { ADMIN, REGULAR }
enum SubscriptionType { FREE, BASIC, PREMIUM, ENTERPRISE }
enum SortDirection { ASC, DESC }
```

### Queries

```graphql
allUsers(filter: UserFilterInput, page: PageInput): UsersPage!
userById(id: UUID!): UserDTO
```

### Mutations

```graphql
createUser(input: CreateUserInput!): UserDTO!
updateUser(id: UUID!, input: UpdateUserInput!): UserDTO!
deleteUser(id: UUID!): Boolean!
```

### Inputs de paginación y filtrado

```graphql
input UserFilterInput {
  startDate: String      # yyyy-MM-dd — filtra por createdAt >=
  endDate: String        # yyyy-MM-dd — filtra por createdAt <=
  userType: UserType     # filtra por tipo de usuario
}

input PageInput {
  page: Int              # índice base 0 (default: 0)
  size: Int              # ítems por página, máx 100 (default: 10)
  sortBy: String         # createdAt | updatedAt | username | email (default: createdAt)
  sortDirection: SortDirection  # ASC | DESC (default: DESC)
}
```

### Respuesta paginada

```graphql
type UsersPage {
  content:  [UserDTO!]!
  pageInfo: PageInfo!
}

type PageInfo {
  page:          Int!
  size:          Int!
  totalElements: Int!
  totalPages:    Int!
  hasNext:       Boolean!
  hasPrevious:   Boolean!
}
```

---

## Ejemplos de consultas

### Listar usuarios (defaults)

```graphql
query {
  allUsers {
    content {
      id username email userType permissions dashboardUrl createdAt
      adminLevel department subscriptionType newsletterSubscribed
    }
    pageInfo {
      page size totalElements totalPages hasNext hasPrevious
    }
  }
}
```

### Listar con paginación y filtros

```graphql
query {
  allUsers(
    filter: { startDate: "2025-01-01", endDate: "2025-12-31", userType: ADMIN }
    page: { page: 0, size: 20, sortBy: "createdAt", sortDirection: DESC }
  ) {
    content { id username email userType adminLevel department }
    pageInfo { page size totalElements totalPages hasNext hasPrevious }
  }
}
```

### Obtener usuario por ID

```graphql
query {
  userById(id: "550e8400-e29b-41d4-a716-446655440000") {
    id username email userType permissions dashboardUrl createdAt
  }
}
```

### Crear usuario administrador

```graphql
mutation {
  createUser(input: {
    username: "john_admin"
    email: "john@example.com"
    password: "Secret@123"
    userType: ADMIN
    adminLevel: "SENIOR"
    department: "IT"
  }) {
    id username email userType adminLevel department createdAt
  }
}
```

### Crear usuario regular

```graphql
mutation {
  createUser(input: {
    username: "jane_user"
    email: "jane@example.com"
    password: "Secret@123"
    userType: REGULAR
    subscriptionType: PREMIUM
    newsletterSubscribed: true
  }) {
    id username email userType subscriptionType newsletterSubscribed createdAt
  }
}
```

### Actualizar usuario

```graphql
mutation {
  updateUser(
    id: "550e8400-e29b-41d4-a716-446655440000"
    input: {
      username: "john_updated"
      email: "john.new@example.com"
      adminLevel: "LEAD"
      department: "Engineering"
    }
  ) {
    id username email adminLevel department
  }
}
```

### Eliminar usuario

```graphql
mutation {
  deleteUser(id: "550e8400-e29b-41d4-a716-446655440000")
}
```

---

## Ejemplos con curl

### Listar usuarios (sin filtros)

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { allUsers { content { id username email userType } pageInfo { page size totalElements totalPages hasNext hasPrevious } } }"
  }'
```

### Listar con variables (paginación + filtros)

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query($filter: UserFilterInput, $page: PageInput) { allUsers(filter: $filter, page: $page) { content { id username email userType } pageInfo { page size totalElements totalPages hasNext hasPrevious } } }",
    "variables": {
      "filter": { "startDate": "2025-01-01", "endDate": "2025-12-31", "userType": "ADMIN" },
      "page": { "page": 0, "size": 5, "sortBy": "createdAt", "sortDirection": "DESC" }
    }
  }'
```

### Crear usuario

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation($input: CreateUserInput!) { createUser(input: $input) { id username email userType } }",
    "variables": {
      "input": {
        "username": "john_admin",
        "email": "john@example.com",
        "password": "Secret@123",
        "userType": "ADMIN",
        "adminLevel": "SENIOR",
        "department": "IT"
      }
    }
  }'
```

### Obtener por ID

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query($id: UUID!) { userById(id: $id) { id username email userType } }",
    "variables": { "id": "550e8400-e29b-41d4-a716-446655440000" }
  }'
```

### Eliminar usuario

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation($id: UUID!) { deleteUser(id: $id) }",
    "variables": { "id": "550e8400-e29b-41d4-a716-446655440000" }
  }'
```

---

## Validaciones de entrada

### `CreateUserInput`

| Campo | Regla |
|---|---|
| `username` | Requerido, 3–50 chars, `[a-zA-Z0-9._-]` |
| `email` | Requerido, formato email válido |
| `password` | Requerido, 8–100 chars, mínimo 1 mayúscula, 1 minúscula, 1 número, 1 especial (`@$!%*?&`) |
| `userType` | Requerido, enum `ADMIN \| REGULAR` |
| `adminLevel` | Opcional, máx 50 chars |
| `department` | Opcional, máx 100 chars |
| `subscriptionType` | Opcional, enum `FREE \| BASIC \| PREMIUM \| ENTERPRISE` |
| `newsletterSubscribed` | Opcional, Boolean |

### `UpdateUserInput`

Todos los campos son opcionales. Solo se actualizan los campos enviados.

| Campo | Regla |
|---|---|
| `username` | 3–50 chars, `[a-zA-Z0-9._-]` |
| `email` | Formato email válido |
| `adminLevel` | Máx 50 chars |
| `department` | Máx 100 chars |
| `subscriptionType` | Enum `FREE \| BASIC \| PREMIUM \| ENTERPRISE` |
| `newsletterSubscribed` | Boolean |

### `PageInput` — reglas del servicio

| Campo | Regla |
|---|---|
| `page` | Mínimo 0 (default: 0) |
| `size` | Entre 1 y 100 (default: 10) |
| `sortBy` | Solo `createdAt`, `updatedAt`, `username`, `email` (default: `createdAt`) |
| `sortDirection` | `ASC` o `DESC` (default: `DESC`) |

---

## Tests

```bash
./gradlew test
```

Cobertura de tests unitarios:

| Clase | Tests |
|---|---|
| `UserServiceImplTest` | createUser, getAllUsers con paginación y filtros, getUserById, updateUser, deleteUser |
| `UserControllerImplTest` | allUsers con/sin filtros, propagación de excepciones por capa |
| `CreateUserRequestValidationTest` | Todas las constraints de entrada |
| `UpdateUserRequestValidationTest` | Constraints de campos opcionales |
| `AdminUserFactoryTest` / `RegularUserFactoryTest` | Creación de entidades por tipo |
| `UserFactoryProviderImplTest` | Resolución de factory por tipo |
| `GlobalExceptionHandlerTest` | Mapeo de excepciones a errores GraphQL |
| `PasswordServiceTest` | Encriptación de contraseña |

---

## Autor

Proyecto POC desarrollado por el equipo AddonAI.
