# Technology Stack

## Backend Stack

### Core Framework
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **Java** | 17 (LTS) | Language | Long-term support, modern features (records, pattern matching), performance |
| **Spring Boot** | 3.2.5 | Application framework | Convention over configuration, production-ready, huge ecosystem |
| **Spring Data JPA** | 3.2.5 | Data access | Repository abstraction, query derivation, pagination, audit |
| **Spring Security** | 6.2.4 | Authentication/Authorization | Industry standard, JWT support, method security, CSRF protection |
| **Spring Validation** | 3.2.5 | Bean Validation | JSR-380, declarative validation on DTOs |

### Database
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **MySQL** | 8.0+ (XAMPP) | Relational database | ACID, mature, free, widely supported, JSON support |
| **HikariCP** | 5.0.1 | Connection pool | Fast, lightweight, default in Spring Boot 3 |
| **Flyway** | 9.22.3 | Database migrations | Version-controlled SQL, repeatable, baseline support |

### Security & Auth
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **JJWT** | 0.12.3 | JWT handling | RFC 7519 compliant, key management, Jackson integration |
| **BCrypt** | (Spring Security) | Password hashing | Adaptive cost, salt included, industry standard |

### Utilities
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **Lombok** | Latest | Boilerplate reduction | @Getter, @Setter, @Builder, @RequiredArgsConstructor, @Slf4j |
| **Jackson** | (Spring Boot) | JSON serialization | Default, configurable, Java 8 time module |

### Build & Dependency Management
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **Maven** | 3.9+ | Build tool | Standard for Java, dependency management, plugins |
| **Spring Boot Maven Plugin** | 3.2.5 | Executable JAR | Fat JAR, devtools, repackage |

## Frontend Stack

### Core Framework
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **React** | 19.3.1 | UI library | Component-based, hooks, concurrent features, huge ecosystem |
| **TypeScript** | ~6.0.3 | Type safety | Catch bugs early, better IDE support, self-documenting |
| **Vite** | 8.3.4 | Build tool / Dev server | Instant HMR, optimized builds, ES modules native |

### Routing & State
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **React Router DOM** | 7.18.4 | Client-side routing | Nested routes, loaders/actions, TypeScript support |
| **React Context** | Built-in | Global state (Auth) | No external dependency, sufficient for auth state |

### HTTP Client
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **Axios** | 1.20.0 | HTTP client | Interceptors, request/response transforms, TypeScript, cancellation |

### Styling
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **Tailwind CSS** | 4.3.3 | Utility-first CSS | No context switching, small bundle, responsive design, dark mode |
| **PostCSS** | 8.5.31 | CSS processing | Tailwind v4 requirement, autoprefixer |

### Code Quality
| Technology | Version | Purpose | Justification |
|------------|---------|---------|---------------|
| **oxlint** | 1.88.0 | Linting | Fast, Rust-based, TypeScript/React support |
| **TypeScript** | ~6.0.3 | Type checking | `tsc -b` for build-time verification |

## Development Tools

| Tool | Version | Purpose |
|------|---------|---------|
| **Git** | Latest | Version control |
| **VS Code** | Latest | IDE with Java/TS extensions |
| **MySQL Workbench** | 8.0+ | Database GUI |
| **Postman/curl** | Latest | API testing |
| **PowerShell/Bash** | Native | Shell scripting |

## Runtime Environment

### Development
| Component | Specification |
|-----------|---------------|
| **OS** | Windows 10/11 (PowerShell) |
| **Java** | JDK 17 (Eclipse Temurin or Oracle) |
| **Maven** | 3.9.15 |
| **Node.js** | 18+ (for Vite) |
| **npm** | 9+ |
| **MySQL** | XAMPP MySQL 8.0 (port 3306) |
| **Backend Port** | 8080 |
| **Frontend Port** | 5173 (Vite dev server) |

### Production (Recommended)
| Component | Specification |
|-----------|---------------|
| **OS** | Ubuntu 22.04 LTS / Amazon Linux 2023 |
| **Java** | JDK 17 (Eclipse Temurin) |
| **Application Server** | Spring Boot fat JAR (embedded Tomcat) |
| **Reverse Proxy** | NGINX (SSL termination, static files) |
| **Database** | MySQL 8.0 (managed: RDS/CloudSQL) or PostgreSQL |
| **Process Manager** | systemd or Docker |
| **SSL** | Let's Encrypt / Certbot |
| **Monitoring** | Prometheus + Grafana (Micrometer) |
| **Logging** | Loki / ELK / CloudWatch |
| **CI/CD** | GitHub Actions / GitLab CI |

## Architecture Decisions

### Why Spring Boot over Node.js/Go?
- **Team expertise** - Java/Spring standard in enterprise
- **Type safety** - Compile-time checks reduce runtime errors
- **Ecosystem** - Spring Data, Security, Batch, Cloud all integrated
- **Long-term maintainability** - Clear patterns, extensive documentation

### Why React over Vue/Svelte?
- **Market demand** - Most job postings require React
- **Ecosystem** - Largest component library ecosystem
- **TypeScript support** - First-class TS support
- **Concurrent features** - React 18/19 concurrent rendering

### Why MySQL over PostgreSQL/MongoDB?
- **Relational data** - Orders, users, menu have strong relationships
- **ACID compliance** - Critical for financial transactions
- **Team familiarity** - XAMPP includes MySQL
- **JSON support** - MySQL 8 has JSON columns for flexible data

### Why Flyway over Liquibase/Hibernate DDL?
- **SQL-first** - Write actual SQL, full control
- **Version control** - Migrations are code, reviewable
- **Repeatable** - Clean + migrate = identical schema
- **Baseline** - Can adopt existing database

### Why JWT over Session/Cookie?
- **Stateless** - Horizontal scaling without session store
- **Mobile-friendly** - Works with native apps
- **Cross-domain** - No CORS cookie issues
- **Standard** - RFC 7519, widely supported

### Why Paise (Minor Units) over Decimal?
- **Precision** - Floating point errors impossible
- **Performance** - Integer arithmetic faster
- **Simplicity** - No rounding logic needed
- **Audit** - Exact amounts stored

## Version Compatibility Matrix

| Component | Version | Compatible With |
|-----------|---------|-----------------|
| Spring Boot | 3.2.5 | Java 17+, Spring Framework 6.1.x |
| Spring Data JPA | 3.2.5 | Hibernate 6.4.x |
| Spring Security | 6.2.4 | Spring Boot 3.2.x |
| MySQL Connector/J | 8.3.0 | MySQL 8.0+, Java 17+ |
| Flyway | 9.22.3 | MySQL 8.0+, Java 17+ |
| JJWT | 0.12.3 | Java 8+, Jackson 2.15+ |
| React | 19.3.1 | Node 18+, React Router 7 |
| Vite | 8.3.4 | Node 18+ |
| Tailwind CSS | 4.3.3 | Node 18+, PostCSS 8 |

## Dependency Tree (Key)

```
spring-boot-starter-parent:3.2.5
├── spring-boot-starter-web
│   ├── spring-webmvc
│   ├── jackson-databind
│   ├── tomcat-embed-core
│   └── spring-web
├── spring-boot-starter-data-jpa
│   ├── spring-data-jpa
│   ├── hibernate-core
│   └── hikari-cp
├── spring-boot-starter-security
│   ├── spring-security-web
│   ├── spring-security-config
│   └── spring-security-crypto
├── spring-boot-starter-validation
│   └── hibernate-validator
├── mysql-connector-j:8.3.0
├── flyway-core:9.22.3
├── flyway-mysql:9.22.3
├── jjwt-api:0.12.3
├── jjwt-impl:0.12.3
├── jjwt-jackson:0.12.3
├── lombok
└── spring-boot-starter-test (test scope)
    ├── junit-jupiter
    ├── mockito
    ├── assertj
    └── spring-test
```

## Security Considerations

| Aspect | Implementation |
|--------|----------------|
| **Password Hashing** | BCrypt cost 10 (Spring Security default) |
| **JWT Algorithm** | HS256 (configurable to RS256) |
| **JWT Expiry** | 24 hours (configurable) |
| **CORS** | Configured for `http://localhost:5173` |
| **CSRF** | Disabled (stateless JWT) |
| **Headers** | Security headers via Spring Security defaults |
| **SQL Injection** | Prevented by JPA parameter binding |
| **XSS** | React auto-escapes, JSON API |
| **Rate Limiting** | Not implemented (future: Bucket4j) |

## Performance Baselines (Development)

| Metric | Target | Measurement |
|--------|--------|-------------|
| **API Latency (p95)** | < 500ms | Manual curl tests |
| **DB Query Time** | < 100ms | Hibernate SQL logging |
| **Frontend Bundle** | < 500KB gzipped | `npm run build` |
| **First Contentful Paint** | < 1.5s | Lighthouse |
| **Time to Interactive** | < 3s | Lighthouse |

## Upgrade Path

| Component | Current | Next Major | Notes |
|-----------|---------|------------|-------|
| Java | 17 | 21 (LTS) | Test with JDK 21 |
| Spring Boot | 3.2.x | 3.3.x | Check migration guide |
| React | 19 | 20 | Codemods available |
| Vite | 8 | 9 | Config changes likely |
| MySQL | 8.0 | 8.4/9.0 | Test compatibility |
| Flyway | 9.x | 10.x | Schema history table changes |