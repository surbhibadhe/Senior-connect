# SeniorConnect 🎓

SeniorConnect is a Spring Boot-based academic resource-sharing platform designed to help college students share and access study materials, including notes, assignments, lab manuals, and previous question papers.

## 🚀 Features

- **Google OAuth 2.0 Authentication** — Secure login using verified college email accounts (`@despu.edu.in`).
- **Role-Based Access** — Supports Junior, Senior, and Admin roles.
- **Resource Management** — Create and retrieve academic resources through REST APIs.
- **Category-Based Filtering** — Browse resources by category.
- **PostgreSQL Integration** — Store user and academic resource data.
- **CSRF Protection** — Protect state-changing requests.

## 🛠️ Tech Stack

- **Backend:** Java, Spring Boot
- **Security:** Spring Security, Google OAuth 2.0
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA, Hibernate
- **API:** REST APIs
- **Build Tool:** Maven
- **Testing:** Postman
- **IDE:** IntelliJ IDEA

## 📚 Resource Categories

- Notes
- Lab Manuals
- Assignments
- Question Papers
- Reference Materials
- Textbook Links

## ⚙️ Getting Started

### Prerequisites

- Java 25 or a compatible configured JDK
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

6. Run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows, use:

   ```bash
   mvnw.cmd spring-boot:run
   ```

7. Open `http://localhost:8080` and sign in with your verified college account.

## 🔗 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/csrf` | Retrieve a CSRF token |
| GET | `/api/resources` | Retrieve all resources |
| GET | `/api/resources?category=NOTES` | Filter resources by category |
| GET | `/api/resources/{id}` | Retrieve a resource by ID |
| POST | `/api/resources` | Create a text or link resource |

**Note:** Creating resources requires authentication, appropriate user permissions, and a valid CSRF token.

## 🔐 Security

- Google OAuth 2.0 authentication
- Verified `@despu.edu.in` email restriction
- Role-based resource creation permissions
- CSRF protection for state-changing requests
- Keep OAuth secrets and database credentials out of version control.

## 🗺️ Future Enhancements

- PDF, DOC, and DOCX file uploads
- Resource download functionality
- Search by title and subject
- Frontend integration
- Improved resource organization and user profiles

## 👩‍💻 Author

**Surbhi Badhe**

- GitHub: [@surbhibadhe](https://github.com/surbhibadhe)

---

*SeniorConnect — Sharing knowledge, connecting seniors and juniors.*