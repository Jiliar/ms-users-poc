# ms-users-poc

Microservicio de gestión de usuarios con GraphQL y Spring Boot

## Descripción
Este proyecto es un microservicio para la gestión de usuarios, desarrollado con Spring Boot, GraphQL y PostgreSQL. Permite crear, consultar, actualizar y eliminar usuarios de tipo administrador y regular, usando un esquema flexible y seguro.

## Tecnologías principales
- Java 21
- Spring Boot 3.2.x
- Spring Boot Starter GraphQL
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- PostgreSQL
- Lombok
- Gradle

## Estructura del proyecto
```
ms-users-poc/
├── src/main/java/bizz/addonai/users/msuserspoc/
│   ├── controllers/
│   ├── dtos/
│   ├── exceptions/
│   ├── models/
│   ├── repositories/
│   ├── services/
│   └── config/
├── src/main/resources/
│   ├── application.yml
│   ├── graphql/
│   │   └── schema.graphqls
│   └── static/
│       └── graphiql.html
├── build.gradle
├── README.md
└── ...
```

## Instalación y ejecución

1. **Configura la base de datos PostgreSQL**
   - Edita `src/main/resources/application.yml` con tus credenciales de PostgreSQL.
   - Asegúrate de que el servicio de PostgreSQL esté corriendo.

2. **Compila el proyecto**
   ```bash
   ./gradlew clean build
   ```

3. **Ejecuta la aplicación**
   ```bash
   ./gradlew bootRun
   ```

4. **Accede a la interfaz GraphiQL**
   - [http://localhost:8080/graphiql](http://localhost:8080/graphiql) (si tienes la dependencia embebida)
   - O usa [Altair](https://altairgraphql.dev/) o [GraphQL Playground](https://github.com/graphql/graphql-playground) apuntando a `http://localhost:8080/graphql`

## Endpoints principales
- `/graphql` : Endpoint principal para consultas y mutaciones GraphQL (POST)
- `/graphiql` : Interfaz web para pruebas (si está habilitada)

## Esquema GraphQL
El esquema se encuentra en `src/main/resources/graphql/schema.graphqls` e incluye:
- Tipos: `AdminUser`, `RegularUser`, `UserResult` (union)
- Inputs: `CreateUserInput`, `UpdateUserInput`
- Queries:
  - `allUsers`: Lista todos los usuarios
  - `userById(id: ID!)`: Consulta usuario por ID
- Mutations:
  - `createUser(input: CreateUserInput!)`: Crea usuario
  - `updateUser(id: ID!, input: UpdateUserInput!)`: Actualiza usuario
  - `deleteUser(id: ID!): Boolean!`: Elimina usuario

## Ejemplos de consultas y mutaciones

### Query: Obtener todos los usuarios
```graphql
query {
  allUsers {
    id
    username
    email
    userType
    permissions
    dashboardUrl
    createdAt
    adminLevel
    department
    subscriptionType
    newsletterSubscribed
  }
}
```

### Mutation: Crear usuario administrador
```graphql
mutation {
  createUser(input: {
    username: "adminuser"
    email: "admin@correo.com"
    password: "MiPasswordSegura"
    userType: "ADMIN"
    adminLevel: "1"
    department: "IT"
  }) {
    id
    username
    email
    userType
    adminLevel
    department
  }
}
```

### Mutation: Crear usuario regular
```graphql
mutation {
  createUser(input: {
    username: "regularuser"
    email: "regular@correo.com"
    password: "MiPasswordSegura"
    userType: "REGULAR"
    subscriptionType: "PREMIUM"
    newsletterSubscribed: true
  }) {
    id
    username
    email
    userType
    subscriptionType
    newsletterSubscribed
  }
}
```

### Mutation: Actualizar usuario
```graphql
mutation {
  updateUser(id: 1, input: {
    username: "usuarioActualizado"
    email: "nuevo@correo.com"
    adminLevel: "2"
    department: "Recursos Humanos"
    subscriptionType: "BASIC"
    newsletterSubscribed: false
  }) {
    id
    username
    email
    userType
    adminLevel
    department
    subscriptionType
    newsletterSubscribed
  }
}
```

### Mutation: Eliminar usuario
```graphql
mutation {
  deleteUser(id: 1)
}
```

## Seguridad
- El acceso a `/graphql` y `/graphiql` está permitido sin autenticación por configuración en `SecurityConfig.java`.
- Puedes personalizar usuarios y contraseñas en `application.yml` si habilitas autenticación.

## Testing
Ejecuta los tests con:
```bash
./gradlew test
```

## Recomendaciones
- Usa Altair o GraphQL Playground para pruebas avanzadas.
- Revisa el log de arranque para errores de base de datos o configuración.
- Si tienes problemas de acceso, revisa la configuración de seguridad y CORS.

## Autor
Proyecto POC desarrollado por el equipo AddonAI.

