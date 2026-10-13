# DigitalHelper — Technology Made Simple for Everyone

A complete, modern, production-oriented full-stack web application designed to solve real everyday digital challenges for ordinary citizens, seniors, and non-technical users.

---

## 🌟 Key Features

1. **AI Everyday Assistant**
   - Natural language questions about smartphones, WhatsApp, Wi-Fi, printers, and payments.
   - Beginner-friendly, numbered step-by-step instructions.
   - Text-to-Speech (read answers aloud) and Speech-to-Text (voice dictation).
   - Rating, bookmarking, and multi-turn conversation memory.

2. **Screenshot & Error Explainer**
   - Drag-and-drop or paste screenshots of cryptic error dialogs.
   - Multimodal Vision AI detects root causes and provides plain-language solutions.
   - Privacy-first warning banner cautioning users to hide passwords, OTPs, and card numbers.

3. **Simple Language & Translation**
   - Translates between Indian languages (Tamil, Hindi, Telugu, Kannada, Malayalam, Bengali, Marathi, Gujarati) and English.
   - "Simplify Jargon" mode rewrites complex tech/legal fine print into 5th-grade words.
   - Plain-language dictionary for tricky terms (KYC, 2FA, Phishing, Cache, Malware).
   - Audio playback and clear non-certification disclaimer.

4. **Scam & Fake Message Checker**
   - Paste suspicious SMS, WhatsApp forwards, or phishing links.
   - Evaluates urgency traps, impersonation, short links, and credential harvesting.
   - Three risk levels: **Low Concern (🟢)**, **Suspicious (🟡)**, and **High Risk (🔴)**.
   - Recommends safe steps and direct access to India's National Cybercrime Helpline (`1930`).

5. **Form & Document Guide**
   - Step-by-step guidance for confusing government and online forms: Passport Application, PAN Card, Aadhaar Update, and Bank KYC.
   - Required documents checklists, photo size rules, and common rejection pitfalls.

6. **Saved Answers & Bookmarks Vault**
   - Bookmark critical rules (e.g. "Never enter UPI PIN to receive money").
   - Filter by categories and search keywords.

7. **Security & REST API Inspector (Swiggy / QuickBite Style)**
   - Built-in floating inspector dock intercepting all `/api/v1/*` network calls.
   - Inspects HTTP methods, endpoint paths, masked JWT authorization headers, latencies, payloads, and security audits.

8. **Admin Operations Hub**
   - Real-time aggregate metrics: user counts, active conversations, high-risk scam counts, and satisfaction percentages.
   - Anonymized feedback reviews respecting user privacy.

---

## 🛠️ Technology Stack

- **Backend**: Java 17/21, Spring Boot 3.2.5, Spring Security, Spring Data JPA, Bean Validation, JJWT (0.12.5)
- **Frontend**: React 19, TypeScript, Vite, Tailwind CSS v4, Lucide Icons
- **Database**: MySQL 8.x (with plug-and-play H2 MySQL-compatibility mode for zero-setup execution)
- **AI Integration**: Configurable LLM client (OpenAI / Gemini REST protocol) + Built-in offline Intelligent Knowledge Engine
- **Testing**: JUnit 5, Mockito, Spring Security Test, Vitest, React Testing Library

---

## 📂 Project Architecture

```
digitalhelper/
├── backend/
│   ├── src/main/java/com/digitalhelper/
│   │   ├── config/          # SecurityConfig, CorsConfig, JwtTokenProvider, DataInitializer
│   │   ├── controller/      # Auth, User, Assistant, Safety, Document, SavedAnswer, Admin
│   │   ├── dto/             # Request & Response contracts + ApiResponse wrapper
│   │   ├── exception/       # GlobalExceptionHandler, Custom exceptions
│   │   ├── model/           # User, Preference, Conversation, Message, SavedAnswer, etc.
│   │   ├── repository/      # Spring Data JPA repositories
│   │   └── service/         # Business services & AiServiceImpl
│   ├── src/main/resources/
│   │   ├── application.properties        # Default in-memory MySQL-compatible profile
│   │   ├── application-mysql.properties  # Production MySQL connection profile
│   │   └── schema.sql                    # Full MySQL DDL creation script
│   ├── src/test/java/com/digitalhelper/  # JUnit 5 & Mockito test suites
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/      # Navbar, AccessibilityBar, NetworkInspectorModal, Modals
│   │   ├── context/         # AuthContext (JWT state, font scaler, high contrast)
│   │   ├── services/        # api.ts (REST client with live network interceptor)
│   │   ├── types/           # TypeScript interfaces
│   │   ├── views/           # HomeDashboard, ChatAssistant, Screenshot, Scam, etc.
│   │   ├── App.tsx
│   │   └── index.css
│   ├── package.json
│   └── vite.config.ts
├── run.bat                  # One-click Windows starter
├── .env.example             # Environment variable template
├── .gitignore
└── README.md
```

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- **Java**: JDK 17 or higher (`java -version`)
- **Maven**: 3.8+ (e.g. `apache-maven-3.9.6`)
- **Node.js**: 18+ and `npm` (`node -v`)
- **MySQL** (Optional for production mode; embedded mode runs out of the box)

---

### 2. Running the Application

#### Option A: Quick Launcher (Windows)
Double-click `run.bat` or run:
```cmd
run.bat
```
This automatically boots:
- Spring Boot backend on **http://localhost:8080**
- Vite React frontend on **http://localhost:5173**

#### Option B: Manual Startup

**Terminal 1 — Backend:**
```cmd
cd backend
mvn spring-boot:run
```
*(Backend starts on port 8080 and pre-seeds demo accounts and sample bookmarks)*

**Terminal 2 — Frontend:**
```cmd
cd frontend
npm install
npm run dev
```
*(Frontend opens on http://localhost:5173)*

---

## 🔑 Pre-Seeded Demo Credentials

The backend seeds test accounts automatically:

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@digitalhelper.com` | `Admin@12345` |
| **User** | `user@digitalhelper.com` | `User@12345` |

*(You can also use the one-click demo login buttons inside the Sign In modal!)*

---

## 🗄️ MySQL Database Setup (Production Mode)

To run against a local or cloud MySQL instance:

1. Open your MySQL client and initialize the database:
```sql
CREATE DATABASE digitalhelper_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Run the provided schema script in `backend/src/main/resources/schema.sql`.

3. Run Spring Boot with the MySQL profile:
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
Or set environment variables:
```cmd
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_DATABASE=digitalhelper_db
set MYSQL_USER=root
set MYSQL_PASSWORD=your_password
```

---

## 🧠 AI Provider Configuration

DigitalHelper is designed with a pluggable AI service abstraction.

### A. Intelligent Local Mode (Default — Zero Setup Needed)
If no external API key is provided, DigitalHelper runs its built-in offline smart knowledge engine with deep rules, multi-language dictionaries, and scam heuristic analyzers.

### B. Configuring Real OpenAI or Gemini API
Set the environment variables before starting:

```cmd
set AI_PROVIDER=openai
set AI_API_KEY=sk-...your-key...
set AI_MODEL=gpt-4o-mini
```

---

## 📡 REST API Reference (`/api/v1`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Register new account | No |
| `POST` | `/api/v1/auth/login` | Login and receive JWT | No |
| `POST` | `/api/v1/auth/logout` | Client token invalidation | No |
| `GET` | `/api/v1/users/me` | Current user profile | Bearer Token |
| `GET` | `/api/v1/users/me/preferences` | Retrieve user preferences | Bearer Token |
| `PATCH` | `/api/v1/users/me/preferences` | Update font/contrast/language | Bearer Token |
| `DELETE` | `/api/v1/users/me` | Right to be forgotten (delete data) | Bearer Token |
| `POST` | `/api/v1/assistant/ask` | Everyday AI troubleshooting | Bearer Token |
| `GET` | `/api/v1/assistant/conversations` | List conversation history | Bearer Token |
| `GET` | `/api/v1/assistant/conversations/{id}` | Get full conversation chat | Bearer Token |
| `DELETE`| `/api/v1/assistant/conversations/{id}` | Delete conversation | Bearer Token |
| `POST` | `/api/v1/assistant/explain-image` | Multimodal error screenshot analysis | Bearer Token |
| `POST` | `/api/v1/assistant/translate` | Indian language & plain jargon translation | Public |
| `POST` | `/api/v1/safety/check-message` | Fraud & scam message risk evaluation | Public / Logged |
| `POST` | `/api/v1/documents/explain` | Online form checklists & guidance | Public |
| `GET` | `/api/v1/saved-answers` | Retrieve bookmarked answers | Bearer Token |
| `POST` | `/api/v1/saved-answers` | Bookmark a useful solution | Bearer Token |
| `PATCH` | `/api/v1/saved-answers/{id}/favorite` | Toggle favorite star | Bearer Token |
| `DELETE`| `/api/v1/saved-answers/{id}` | Remove bookmark | Bearer Token |
| `POST` | `/api/v1/feedback` | Submit answer helpfulness rating | Public / Logged |
| `GET` | `/api/v1/admin/stats` | Aggregate metrics & health | ROLE_ADMIN |
| `GET` | `/api/v1/admin/feedback` | Anonymized user ratings log | ROLE_ADMIN |
| `GET` | `/api/v1/health` | Live service health check | Public |

---

## 🧪 Testing

### Backend Unit & Integration Tests (JUnit 5 + Mockito)
```cmd
cd backend
mvn test
```
Tests cover:
- Authentication & password hashing
- Duplicate email prevention
- Scam message risk heuristics & urgency scoring
- Safe boundary evaluation

### Frontend Unit Tests (Vitest + React Testing Library)
```cmd
cd frontend
npm test
```
Tests cover:
- Navigation header rendering
- Safety banner presence
- Service card interactive links
- Responsive accessibility structure

---

## 🔒 Security Best Practices Implemented

1. **Stateless JWT Security**: Passwords hashed with BCrypt (cost factor 10).
2. **Untrusted Input Isolation**: Scam analysis never executes pasted links or fetches remote binaries.
3. **Sensitive Data Protection**: Passwords, OTPs, PINs, and full card details are never logged in application logs or stored in error reports.
4. **CORS & Header Protection**: Allowed origins strictly bound to client domains.
5. **GDPR / Privacy Compliance**: Users can view their stored preferences and permanently delete their data with one click.
