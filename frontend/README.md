# SeniorConnect — Academic Resource Platform (Frontend)

SeniorConnect is a university resource-sharing platform designed for students of **DES Pune University**. It allows students to discover, bookmark, evaluate, and share lecture notes, lab manuals, assignments, and previous exam question papers.

This frontend is built with **React 18**, **TypeScript**, **Vite**, and **Lucide React**, fully integrated with the Spring Boot 4.x backend.

---

## Aesthetic & Design System

The application features a **minimal, warm, editorial, pale-peach & off-white aesthetic**:

- **Main Page Background:** `#FCFAF8`
- **Secondary Background:** `#F8F0EB`
- **Card Background:** `#FFFFFF`
- **Main Accent:** `#E8C2B2`
- **Soft Accent:** `#F0DCD2`
- **Muted Terracotta (Selected Actions):** `#B97F6B`
- **Main Text:** `#332F2D`
- **Secondary Text:** `#827873`
- **Borders:** `#EBE2DC`
- **Typography:** DM Serif Display for headings, Plus Jakarta Sans for clean body text.

---

## Security & Architecture

1. **Session-Based Authentication:** Uses Spring Security's HTTP-only session cookies (`JSESSIONID`). All API requests include `credentials: 'include'`.
2. **Google OAuth2:** Initiated via `/oauth2/authorization/google`. Restricted by the backend to verified institutional accounts ending with `@despu.edu.in`.
3. **CSRF Protection:** Integrated with Spring Security's `CookieCsrfTokenRepository`. The frontend automatically synchronizes CSRF tokens (via cookie or `/api/csrf`) and attaches the required `X-XSRF-TOKEN` header on all mutating requests (`POST`, `PUT`, `DELETE`).
4. **Zero Mock Mode:** All features connect directly to the real Spring Boot REST API. Real loading skeletons, empty states, and error alerts are displayed.

---

## Getting Started

### Prerequisites

- Node.js (v18 or higher recommended)
- Running Spring Boot backend on `http://localhost:8080`
- PostgreSQL database (`seniorconnect_db`)

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment

Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

By default, leaving `VITE_API_BASE_URL` empty allows Vite's built-in development proxy (`vite.config.ts`) to forward `/api`, `/oauth2`, and `/logout` to `http://localhost:8080` without cross-origin cookie restrictions.

If connecting directly to a remote server, specify the absolute URL:
```env
VITE_API_BASE_URL=http://localhost:8080
```

### 3. Start Development Server

```bash
npm run dev
```

Visit `http://localhost:5173` in your browser.

### 4. Build for Production

```bash
npm run build
npm run preview
```

---

## Backend Endpoint Mapping

| Feature | Method | Endpoint | Description |
|---|---|---|---|
| **Google Sign-In** | `GET` | `/oauth2/authorization/google` | Initiates Google OAuth2 login flow |
| **Current Student** | `GET` | `/api/users/me` | Fetches authenticated user profile & role |
| **Update Profile** | `PUT` | `/api/users/me` | Updates student bio and department |
| **Public Profile** | `GET` | `/api/users/{id}` | Fetches public contributor profile |
| **Contributor Work** | `GET` | `/api/users/{id}/resources` | Paginated resources by contributor |
| **List Resources** | `GET` | `/api/resources` | Query resources (search, category, sort, page) |
| **Resource Detail** | `GET` | `/api/resources/{id}` | Fetches resource by ID |
| **Upload File** | `POST` | `/api/resources/upload` | Multipart file upload (PDF, DOC, DOCX) |
| **Create Text/Link** | `POST` | `/api/resources` | JSON creation for text notes or URLs |
| **Download File** | `GET` | `/api/resources/{id}/download`| Streams file attachment |
| **Delete Resource** | `DELETE`| `/api/resources/{id}` | Removes resource (owner or admin) |
| **Bookmark Toggle** | `POST`/`DELETE` | `/api/resources/{id}/bookmark` | Adds or removes bookmark |
| **List Bookmarks** | `GET` | `/api/bookmarks` | Paginated list of student's saved items |
| **Helpful Feedback** | `POST`/`DELETE` | `/api/resources/{id}/helpful` | Marks or unmarks helpful feedback |
| **Report Resource** | `POST` | `/api/resources/{id}/report` | Submits moderation report |
| **Admin Reports** | `GET` | `/api/admin/reports` | Lists reports for admin moderation |
| **Resolve Report** | `PUT` | `/api/admin/reports/{id}/status` | Updates report status with moderator notes |
| **Remove Reported** | `DELETE`| `/api/admin/reports/{id}/resource`| Removes violating resource via admin |
| **CSRF Token** | `GET` | `/api/csrf` | Retrieves current CSRF token |
| **Sign Out** | `POST` | `/logout` | Invalidates Spring Security session |

---

## Project Structure

```
frontend/
├── index.html               # Main HTML shell with typography & SEO
├── package.json             # Dependencies and build scripts
├── tsconfig.json            # Strict TypeScript configuration
├── vite.config.ts           # Vite bundler & development proxy setup
├── .env.example             # Safe environment variable template
├── src/
│   ├── App.tsx              # Main routing & authentication protection
│   ├── main.tsx             # Application entry point
│   ├── types/
│   │   └── index.ts         # TypeScript interfaces synced with backend DTOs
│   ├── services/
│   │   ├── config.ts        # API configuration
│   │   ├── apiClient.ts     # Centralized fetch wrapper with CSRF & cookies
│   │   ├── authService.ts   # Authentication and user profile endpoints
│   │   └── resourceService.ts # Resources, bookmarks, feedback, and moderation
│   ├── hooks/
│   │   ├── useAuth.tsx      # Auth context provider and hook
│   │   └── useScrollShift.ts # Parallax micro-animation hook
│   ├── components/
│   │   ├── Navbar.tsx       # Navigation bar with user pill & mobile drawer
│   │   ├── ResourceCard.tsx # Resource row with category pills & metadata
│   │   ├── UploadModal.tsx  # Document upload and link creation modal
│   │   ├── ReportModal.tsx  # Resource flagging modal
│   │   └── EditProfileModal.tsx # Student profile editing modal
│   ├── pages/
│   │   ├── Landing.tsx      # Editorial landing hero with category showcase
│   │   ├── SignIn.tsx       # Minimal Google sign-in page
│   │   ├── Home.tsx         # Personalized home dashboard & quick discovery
│   │   ├── Browse.tsx       # Search, filtering, and paginated resource list
│   │   ├── ResourceDetail.tsx # File download, text reader, and interactions
│   │   ├── Bookmarks.tsx    # Saved resources collection
│   │   ├── Profile.tsx      # Student profile & personal contributions
│   │   ├── ContributorProfile.tsx # Public contributor profile page
│   │   └── AdminModeration.tsx # Administrative moderation dashboard
│   └── styles/
│       ├── tokens.css       # Pale-peach & off-white design tokens
│       └── app.css          # Core styles, responsive layout, animations
```
