# Spring Auto UI

**Spring Auto UI** is a lightweight developer tool that automatically generates an interactive web interface for all REST endpoints in a Spring Boot application.  
It scans controllers at runtime and provides a user-friendly interface for sending requests, viewing responses, and inspecting endpoint details — without requiring manual Swagger configuration or annotations.

---

## Features

- Automatically detects all REST controllers and endpoints.
- Displays request methods, paths, parameters, headers, and body models.
- Provides a ready-to-use web UI for testing endpoints.
- Supports all HTTP methods (`GET`, `POST`, `PUT`, `DELETE`, etc.).
- Built-in token input for authorization headers.
- Works with Spring Boot 3.x and Jakarta EE packages.
- Can be added as a dependency with zero configuration.

---

## Getting Started

### 1. Add the Dependency

After installing the library locally or publishing it to your Maven repository, add the following dependency to your Spring Boot project:

```xml
<dependency>
  <groupId>com.mnf</groupId>
  <artifactId>spring-auto-ui</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

### 2. Run the Application

Start your Spring Boot application as usual.  
Once the application is running, open the following URL in your browser:

```
http://localhost:8080/auto-ui/
```

You will see the automatically generated API testing interface.

---

## How It Works

1. **Controller Scanning**  
   `ControllerMethodDetector` scans all `@RestController` and `@Controller` beans registered in the Spring context.  
   It extracts request mappings, method signatures, parameter sources, headers, and body definitions.

2. **Data Exposure**  
   `AutoUIController` exposes the scanned metadata through an internal JSON API (`/auto-ui/api/endpoints`).

3. **Frontend UI**  
   The bundled frontend (React + Vite) renders the data as an interactive API dashboard.  
   It allows you to:
    - Modify request parameters.
    - Send HTTP requests directly.
    - View formatted responses and cURL commands.



## Building the Project

### 1. Build the Frontend

```bash
cd frontend
npm install
npm run build -- --base=/auto-ui/
cd ..
```

This generates the static UI files under `frontend/dist/`.

### 2. Package the Library

```bash
mvn clean install
```

This will:
- Copy the frontend assets into `src/main/resources/static/auto-ui/`
- Package the project as a reusable `.jar`
- Install it in your local Maven repository

---

## Integration Notes

- The UI automatically excludes controllers annotated with `@ExcludeFromUI`.
- Authorization headers can be added manually in the interface.
- `PropertiesService` and other internal components are auto-configured.
- Compatible with `spring-boot-starter-web` (Jakarta packages).

---

## Example Usage

Once integrated, you can test your endpoints directly from the browser:

```
GET  /api/users
POST /api/orders
PUT  /api/profile/{id}
```

Each endpoint will show all required parameters, headers, and request body templates.

---

## License

This project is licensed under the MIT License.  
See the [LICENSE](LICENSE) file for details.
