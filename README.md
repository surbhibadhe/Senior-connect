# SeniorConnect 🎓

SeniorConnect is a Spring Boot-based academic resource-sharing platform designed to help college students share and access study materials, including notes, assignments, lab manuals, and previous question papers.

## 🚀 Features

- **Google OAuth 2.0 Authentication** — Secure login using verified college email accounts (`@despu.edu.in`).
- **Student Reviews & Perspectives Section** — Publicly visible community reviews on the landing page seeded with authentic student stories, with submission restricted to authenticated students.
- **Reddit-Style Discussion Threads** — Live comment section on every resource with nested replies, upvoting, and markdown-friendly notes.
- **Role-Based Access** — Supports Junior, Senior, and Admin roles.
- **Resource Management** — Create, download, bookmark, and mark helpful academic resources through REST APIs.
- **Category-Based Filtering** — Browse resources by category (Lecture Notes, Lab Manuals, PYQs, etc.).
- **Modern University Visual Language** — Rebuilt landing page and portals matching DES Pune University aesthetic design reference.
- **PostgreSQL Integration** — Store user, academic resource, review, and comment data.
- **CSRF Protection** — Protect state-changing requests.

## 🛠️ Tech Stack

- **Frontend:** React 18, TypeScript, Vite, React Router 6, Lucide Icons, Plus Jakarta Sans typography
- **Backend:** Java 25, Spring Boot 4.x
- **Security:** Spring Security 7, Google OAuth 2.0
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA, Hibernate
- **API:** REST APIs with CSRF support
- **Build Tool:** Maven & Vite

## 📚 Resource Categories

- Notes
- Lab Manuals
- Assignments
- Question Papers (PYQ)
- Reference Materials
- Textbook Links

## ⚙️ Getting Started

### Prerequisites

- Java 25 or a compatible configured JDK
- Node.js 18+ and npm
- Maven
- PostgreSQL
- Google OAuth credentials

### Installation

1. Clone the repository:

   ```bash
   git clone <YOUR_GITHUB_REPOSITORY_URL>
   ```

2. Navigate to the project directory:

   ```bash
   cd SeniorConnect
   ```

3. Create a PostgreSQL database named `seniorconnect_db`.

4. Configure your database credentials and Google OAuth settings in `src/main/resources/application.properties` or through environment variables.

5. Set your Google OAuth client credentials, including `GOOGLE_CLIENT_SECRET`, in your environment or IDE run configuration.

6. Run the backend application:

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows, use:

   ```bash
   mvnw.cmd spring-boot:run
   ```

7. In a separate terminal, launch the frontend:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

8. Open `http://localhost:5173` to explore the landing page or sign in with your verified college account.

## 🔗 API Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/api/csrf` | Retrieve a CSRF token | No |
| GET | `/api/reviews` | Retrieve all student reviews | No |
| POST | `/api/reviews` | Submit a new student review | Yes |
| DELETE | `/api/reviews/{id}` | Delete a review (owner or admin) | Yes |
| GET | `/api/resources` | Retrieve paginated resources | No |
| GET | `/api/resources/{id}` | Retrieve a resource by ID | No |
| POST | `/api/resources` | Create a text, file, or link resource | Yes |
| GET | `/api/resources/{id}/comments` | Retrieve Reddit-style discussion comments | No |
| POST | `/api/resources/{id}/comments` | Post a comment or reply | Yes |
| POST | `/api/resources/{id}/comments/{cid}/upvote` | Upvote a comment | Yes |
| DELETE | `/api/resources/{id}/comments/{cid}` | Delete a comment | Yes |

**Note:** State-changing requests (`POST`, `PUT`, `DELETE`) require authentication and a valid CSRF token header (`X-XSRF-TOKEN`).

## 👩‍💻 Author

**Surbhi Badhe**

- GitHub: [@surbhibadhe](https://github.com/surbhibadhe)

---

*SeniorConnect — Sharing knowledge, connecting seniors and juniors.*
