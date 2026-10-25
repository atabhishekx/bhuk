# Development Setup

## Prerequisites

### Required Software

| Tool | Version | Installation |
|------|---------|--------------|
| **Java JDK** | 17 (LTS) | [Eclipse Temurin](https://adoptium.net/temurin/releases/) or Oracle JDK |
| **Apache Maven** | 3.9+ | [Download](https://maven.apache.org/download.cgi) or `winget install Apache.Maven` |
| **Node.js** | 18+ | [Download](https://nodejs.org/) or `winget install OpenJS.NodeJS` |
| **MySQL** | 8.0+ | [XAMPP](https://www.apachefriends.org/) (includes MySQL + phpMyAdmin) |
| **Git** | Latest | [Download](https://git-scm.com/) |

### Verify Installation
```powershell
java -version
# openjdk version "17.0.x" 202x-xx-xx

mvn -version
# Apache Maven 3.9.x

node -version
# v18.x.x or higher

npm -version
# 9.x.x or higher

mysql --version
# mysql  Ver 8.0.x for Win64 on x86_64
```

## Project Structure

```
restru/
├── backend/                 # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/restru/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/     # Flyway SQL files
│   │   └── test/
│   ├── pom.xml
│   └── target/              # Build output
├── frontend/
│   └── frontend/            # React + Vite application
│       ├── src/
│       ├── public/
│       ├── package.json
│       ├── vite.config.ts
│       ├── tailwind.config.js
│       └── tsconfig.json
├── docs/
│   ├── FA1/
│   └── FA2/
├── seed-db.ps1              # Database seed script
├── seed-db.sh               # Database seed script (Linux/Mac)
└── study.md                 # Study guide
```

## Configuration

### Backend Environment Variables

Create a `.env` file in `backend/` or set in shell:

```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "restru_db"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = ""          # XAMPP default: empty password
$env:JWT_SECRET = "dev-secret-key-at-least-32-characters-long"
$env:SERVER_PORT = "8080"
$env:LOG_LEVEL = "DEBUG"
```

```bash
# Linux/Mac (export in ~/.bashrc or shell)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=restru_db
export DB_USERNAME=root
export DB_PASSWORD=
export JWT_SECRET=dev-secret-key-at-least-32-characters-long
export SERVER_PORT=8080
export LOG_LEVEL=DEBUG
```

### application.yml (backend/src/main/resources/application.yml)
```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  application:
    name: restru

  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:restru_db}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate          # Never auto-create; use Flyway
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
    show-sql: false

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false

logging:
  level:
    com.restru: ${LOG_LEVEL:INFO}
    org.springframework.web: INFO
    org.hibernate.SQL: WARN

app:
  jwt:
    secret: ${JWT_SECRET:CHANGE_ME_GENERATE_A_STRONG_RANDOM_SECRET_MIN_32_CHARS}
    expiration-ms: ${JWT_EXPIRATION_MS:86400000}
  razorpay:
    key-id: ${RAZORPAY_KEY_ID:}
    key-secret: ${RAZORPAY_KEY_SECRET:}
    webhook-secret: ${RAZORPAY_WEBHOOK_SECRET:}
```

### Frontend Configuration

#### vite.config.ts (frontend/frontend/vite.config.ts)
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/postcss'

export default defineConfig({
  plugins: [react()],
  css: {
    postcss: {
      plugins: [tailwindcss()],
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

#### API Base URL (frontend/frontend/src/api/axios.ts)
```typescript
const api = axios.create({
  baseURL: '/api/v1',  // Proxied via Vite to http://localhost:8080/api/v1
  headers: {
    'Content-Type': 'application/json',
  },
})
```

## Database Setup

### 1. Start MySQL (XAMPP)
1. Open XAMPP Control Panel
2. Start **MySQL** module
3. Verify: `mysql -u root -p` (press Enter for empty password)

### 2. Create Database (Optional - Flyway creates tables)
```sql
CREATE DATABASE IF NOT EXISTS restru_db 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Run Migrations
```powershell
cd C:\xampp\htdocs\Projects\College\restru\backend
& "C:\Users\abhiv\tools\apache-maven-3.9.15\bin\mvn.cmd" flyway:clean flyway:migrate
```

**Expected Output:**
```
[INFO] Successfully applied 9 migrations to schema `restru_db`, now at version v9
```

### 4. Verify Seed Data
```sql
USE restru_db;
SELECT * FROM users;                    -- 3 users
SELECT * FROM outlets;                  -- 1 outlet
SELECT * FROM menu_categories;          -- 7 categories
SELECT COUNT(*) FROM menu_items;        -- 50 items
SELECT * FROM user_roles;               -- 3 roles assigned
```

## Running the Application

### Backend (Terminal 1)
```powershell
# Set environment variables first (see above)
cd C:\xampp\htdocs\Projects\College\restru\backend
& "C:\Users\abhiv\tools\apache-maven-3.9.15\bin\mvn.cmd" spring-boot:run
```

**Success Indicators:**
```
[INFO] Started RestruApplication in X.XXX seconds
[INFO] Tomcat started on port 8080 (http) with context path ''
```

**Health Check:**
```powershell
Invoke-RestMethod http://localhost:8080/api/v1/health
# Returns: {"success":true,"data":{"status":"UP"}}
```

### Frontend (Terminal 2)
```powershell
cd C:\xampp\htdocs\Projects\College\restru\frontend\frontend
npm install          # First time only
npm run dev
```

**Success Indicators:**
```
VITE v8.x.x  ready in XXX ms
➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

Open browser: **http://localhost:5173**

## Test Credentials (After Seeding)

| Role | Email | Password | Access |
|------|-------|----------|--------|
| **Admin** | admin@restru.local | admin123 | Full admin dashboard |
| **Delivery** | delivery@restru.local | admin123 | Delivery dashboard |
| **Customer** | customer@restru.local | admin123 | Menu, cart, orders |

## Common Commands

### Backend
```powershell
# Clean build
mvn clean compile

# Run tests
mvn test

# Flyway commands
mvn flyway:info           # Migration status
mvn flyway:validate       # Check checksums
mvn flyway:clean          # Drop all tables (DEV ONLY)
mvn flyway:migrate        # Apply pending migrations
mvn flyway:repair         # Fix failed migration

# Package JAR
mvn clean package -DskipTests
# Output: target/restru-backend-0.1.0-SNAPSHOT.jar

# Run JAR
java -jar target/restru-backend-0.1.0-SNAPSHOT.jar
```

### Frontend
```bash
cd frontend/frontend

# Install dependencies
npm install

# Development server
npm run dev

# Production build
npm run build
# Output: dist/

# Preview production build
npm run preview

# Type check
npx tsc -b

# Lint
npm run lint
```

### Database Utilities
```powershell
# Reset database completely
cd backend
mvn flyway:clean flyway:migrate

# Run seed script (PowerShell)
.\seed-db.ps1

# Run seed script (Bash)
./seed-db.sh

# Manual MySQL access
mysql -u root -p restru_db
```

## Troubleshooting

### Port 8080 Already in Use
```powershell
# Find and kill process
$pid = (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue).OwningProcess
if ($pid) { Stop-Process -Id $pid -Force }
```

### Database Connection Failed
1. Verify MySQL is running in XAMPP
2. Check credentials in environment variables
3. Verify database exists: `SHOW DATABASES;`
4. Check firewall/antivirus not blocking 3306

### Flyway Migration Failed
```powershell
# Check status
mvn flyway:info

# Repair checksum mismatch
mvn flyway:repair

# Full reset (loses data)
mvn flyway:clean flyway:migrate
```

### Frontend Build Errors
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install

# TypeScript errors
npx tsc -b --noEmit
```

### JWT Secret Error
```
Error: JWT secret too short
```
**Fix:** Ensure `JWT_SECRET` is at least 32 characters:
```powershell
$env:JWT_SECRET = "dev-secret-key-at-least-32-characters-long"
```

### CORS Errors
**Backend** `WebConfig.java` or `SecurityConfig.java`:
```java
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
```
Or configure in `SecurityConfig`:
```java
http.cors(cors -> cors.configurationSource(request -> {
    var config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));
    config.setAllowedMethods(List.of("*"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    return config;
}))
```

## IDE Setup

### IntelliJ IDEA (Recommended for Backend)
1. Open `backend/` as Maven project
2. Enable **Lombok** plugin: Settings → Plugins → Lombok
3. Enable **Annotation Processing**: Settings → Build → Compiler → Annotation Processors
4. Run Configuration: Spring Boot → `RestruApplication`

### VS Code (Recommended for Frontend)
1. Install extensions:
   - **ES7+ React/Redux/React-Native snippets**
   - **Tailwind CSS IntelliSense**
   - **TypeScript Hero**
   - **Error Lens**
2. Open `frontend/frontend/` folder
3. Terminal → Run Task → `npm: dev`

## Environment-Specific Notes

### Windows
- Use PowerShell for environment variables (`$env:VAR = "value"`)
- Path separator: `\` (but Java/Maven accept `/`)
- Maven wrapper: `mvnw.cmd` if available

### Linux/Mac
- Use `export VAR=value` in shell
- Path separator: `/`
- Maven wrapper: `./mvnw` if available

### Docker (Optional)
```dockerfile
# Backend Dockerfile
FROM eclipse-temurin:17-jre
COPY target/restru-backend-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

```dockerfile
# Frontend Dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY frontend/frontend/package*.json ./
RUN npm ci
COPY frontend/frontend/ ./
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

## Verification Checklist

After setup, verify:

- [ ] Backend starts on port 8080
- [ ] `GET /api/v1/health` returns 200
- [ ] Flyway shows 9 applied migrations
- [ ] Frontend starts on port 5173
- [ ] Login works with test credentials
- [ ] Menu loads with 7 categories, 50 items
- [ ] Cart add/remove works
- [ ] Checkout creates order
- [ ] Order appears in history
- [ ] No console errors in browser
- [ ] No exceptions in backend logs