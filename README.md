# TaskFlow — Full Stack Team Task Tracker

TaskFlow is an end-to-end task management web application featuring a **Spring Boot 3.x + Kotlin** REST API and a modern **React 18 + TypeScript (Vite)** frontend. It provides Kanban-style project boards with real-time status transitions, overdue tracking, dynamic filtering/sorting, strict validation, and stateless JWT authentication.

---

## 🚀 Quick Start (Zero-Config Local Run)

The application is pre-configured with **H2 in PostgreSQL-compatibility mode** for local development, allowing instant startup without manual database installation. A demo account and sample tasks (including an overdue task) are automatically seeded on initial boot.

### 1. Start the Backend API (Spring Boot + Kotlin)
```bash
cd backend
# On Linux / macOS:
./gradlew bootRun

# On Windows:
.\gradlew.bat bootRun
```
* Backend starts at: `http://localhost:8080`
* Swagger / OpenAPI Interactive Documentation: `http://localhost:8080/swagger-ui.html`
* In-memory H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:taskflowdb;MODE=PostgreSQL`)

### 2. Start the Frontend (React + TypeScript + Vite)
In a separate terminal:
```bash
cd frontend
npm install
npm run dev
```
* Frontend starts at: `http://localhost:5173`

---

## 🔑 Pre-Seeded Demo Credentials

The backend automatically seeds a demo user and sample tasks on startup. You can also click the **"Evaluator Demo Account: Click to prefill"** button on the login screen:

* **Email:** `demo@taskflow.com`
* **Password:** `Password123!`

---

## 🐳 Running with Docker Compose (PostgreSQL + Backend + Frontend)

To run the entire full stack with a production-grade **PostgreSQL 16** database container:

```bash
docker compose up --build
```
* **Frontend:** `http://localhost`
* **Backend:** `http://localhost:8080`
* **PostgreSQL:** `localhost:5432` (database: `taskflow`, user: `taskflow`)

---

## 🧪 Running Automated Tests

### Backend Tests (JUnit 5 + MockK + SpringBootTest)
```bash
cd backend
./gradlew test
```
* **Unit Tests (`MockK` + JUnit 5):**
  * `TaskServiceTest`: Validates that tasks with a past `dueDate` throw `400 Bad Request`, verifies valid future task creation, enforces cross-user project ownership security (`403 Forbidden`), and verifies status mutation.
  * `ProjectServiceTest`: Validates correct aggregation of task counts (`TODO`, `IN_PROGRESS`, `DONE`, and overdue count) and verifies ownership isolation.
* **Integration Tests (`@SpringBootTest` + `MockMvc`):**
  * `TaskFlowIntegrationTest`: Full end-to-end user journey (User registration $\rightarrow$ Login $\rightarrow$ JWT authorization checks $\rightarrow$ Project creation $\rightarrow$ Past-date validation check $\rightarrow$ Task creation $\rightarrow$ Summary aggregation).

### Frontend Tests (Vitest + React Testing Library)
```bash
cd frontend
npm test
```
* `TaskCard.test.tsx`: Tests priority tags, overdue badge rendering when `isOverdue` is true, and the status change dropdown callback.
* `SummaryBar.test.tsx`: Tests aggregated counter display and visual overdue alert highlights.

---

## 🏛 Architecture & Design Patterns

### Layered Architecture
```
[ Frontend: React + TypeScript ]
         │ (HTTP / JSON / JWT Bearer)
         ▼
[ Controller Layer ]      (@RestController, input validation, HTTP mappings)
         │ (DTOs)
         ▼
[ Service Layer ]         (Business rules, ownership checks, date constraints)
         │ (Entities)
         ▼
[ Repository Layer ]      (Spring Data JPA + JpaSpecificationExecutor)
         │
         ▼
[ Database: PostgreSQL / H2 ]
```

### Design Patterns Used

1. **Factory / Mapper Pattern (`ProjectMapper.kt`, `TaskMapper.kt`)**:
   * **Purpose:** Shields the internal JPA database structure from API consumers, eliminating entity leakage, circular references, and unintended updates.
   * **Implementation:** Dedicated mapper components safely transform entities to `ResponseDto` instances and dynamically compute transient fields (e.g. `isOverdue = dueDate < today && status != DONE`).

2. **Strategy Pattern (`TaskFilterStrategy.kt`, `TaskSortStrategy.kt`)**:
   * **Purpose:** Eliminates brittle conditional logic for querying tasks by dynamically composing database specifications and sorting algorithms.
   * **Implementation:** `TaskFilterStrategy` constructs pluggable JPA Specifications for status, priority, and assignee filters; `TaskSortStrategy` resolves deterministic orderings by due date, priority, or creation timestamp.

3. **Repository Pattern (`UserRepository`, `ProjectRepository`, `TaskRepository`)**:
   * **Purpose:** Encapsulates persistence logic and hides underlying storage engines behind clean domain interfaces.

---

## 📦 State Management Choice

We selected **TanStack React Query v5** paired with a lightweight **React Context API (`AuthContext`)**:
* **Why React Query?** Task trackers are server-state-heavy applications. React Query provides declarative caching, automatic cache invalidation on mutations (e.g., updating a task's status instantly updates the column view and summary bar), built-in background re-fetching, and first-class loading/error states without boilerplate reducers or manual action creators.
* **Why React Context for Auth?** Authentication state (`token`, `user`) changes infrequently and needs to be accessible globally to guard routes and attach auth headers via Axios interceptors.

---

## 🛡 Security & Error Handling

* **Stateless JWT Authentication:** Access tokens signed using HMAC-SHA256 (`jjwt`) with configurable secret and expiration via environment variables.
* **Resource Ownership Authorization:** Users can only access, view, modify, or delete projects and tasks that belong to them. Unauthorized operations throw custom exceptions mapped to `403 Forbidden`.
* **Global Exception Handler (`@RestControllerAdvice`):** Returns standardized JSON error envelopes:
  * `400 Bad Request` with field-level validation messages (e.g., blank title, title $> 120$ chars, past due date on creation).
  * `401 Unauthorized` for bad credentials or missing/expired tokens.
  * `403 Forbidden` for ownership violations.
  * `404 Not Found` for missing resources.
  * `409 Conflict` for duplicate user email registration.

---

## 📋 API Endpoints Reference

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user account | No |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT token | No |
| `GET` | `/api/projects` | List projects owned by current user | Yes (JWT) |
| `POST` | `/api/projects` | Create a new project | Yes (JWT) |
| `GET` | `/api/projects/{id}/summary` | Status counts and overdue count for project | Yes (JWT) |
| `GET` | `/api/projects/{id}/tasks` | List tasks (filtering, sorting, pagination) | Yes (JWT) |
| `POST` | `/api/projects/{id}/tasks` | Create task (due date cannot be in the past) | Yes (JWT) |
| `PUT` | `/api/tasks/{id}` | Update task details | Yes (JWT) |
| `PATCH` | `/api/tasks/{id}/status` | Move task across Kanban columns | Yes (JWT) |
| `DELETE` | `/api/tasks/{id}` | Delete a task | Yes (JWT) |

---

## ⚖️ Assumptions & Trade-offs

1. **In-Memory H2 default with PostgreSQL dual-support:** Chosen to ensure reviewers can run the application with zero external dependencies, while providing full PostgreSQL readiness via profile and Docker Compose.
2. **Column-Based Board with Quick Status Select & Action Controls:** Designed for fast, touch-friendly, and accessible cross-column task movement across desktop and mobile screens.
3. **Stateless JWTs:** Avoids server-side session stores; token expiration is handled client-side via Axios response interceptors.

---

## 🔮 What We Would Build Next
* **Drag-and-Drop Column Reordering:** Enhance card movement with `@hello-pangea/dnd`.
* **Task Activity History:** Append an audit log entity recording status changes, timestamps, and actors.
* **WebSocket / SSE Live Updates:** Broadcast board changes across multiple concurrent users on the same project.

