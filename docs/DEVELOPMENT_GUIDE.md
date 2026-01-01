# 💻 Development Guide

Guide for setting up your development environment and contributing to the project.

---

## Table of Contents
- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Project Structure](#project-structure)
- [Development Workflow](#development-workflow)
- [Code Style](#code-style)
- [Testing Guidelines](#testing-guidelines)
- [Adding New Features](#adding-new-features)

---

## Prerequisites

### Required Software
| Software | Version | Purpose |
|----------|---------|---------|
| Java | 17+ | Backend services |
| Maven | 3.8+ | Build tool |
| Node.js | 20+ | Frontend |
| Docker | 24+ | Containerization |
| Git | 2.40+ | Version control |

### Recommended IDE
- **IntelliJ IDEA** (Ultimate for Spring support)
- **VS Code** with extensions:
  - Spring Boot Extension Pack
  - Java Extension Pack
  - ESLint, Prettier

---

## Environment Setup

### 1. Clone Repository
```bash
git clone https://github.com/mohamedlandolsi/greenhouse-management-system.git
cd greenhouse-management-system
```

### 2. Start Infrastructure
```bash
# Start only databases, Kafka, Redis
docker compose up -d postgres-env postgres-ctrl zookeeper kafka redis
```

### 3. Build Backend
```bash
mvn clean install -DskipTests
```

### 4. Run Services (Order Matters)

```bash
# Terminal 1: Service Discovery
cd service-discovery && mvn spring-boot:run

# Terminal 2: Config Server
cd config-server && mvn spring-boot:run

# Terminal 3: API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 4: Environnement Service
cd environnement-service && mvn spring-boot:run

# Terminal 5: Controle Service
cd controle-service && mvn spring-boot:run
```

### 5. Run Frontend
```bash
cd greenhouse-dashboard
npm install
npm run dev
```

### 6. Verify Setup
- Dashboard: http://localhost:3000
- Eureka: http://localhost:8761
- API Gateway: http://localhost:8080

---

## Project Structure

### Backend Service Structure
```
service-name/
├── src/
│   ├── main/
│   │   ├── java/com/greenhouse/service/
│   │   │   ├── config/         # Configuration classes
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── exception/      # Custom exceptions
│   │   │   ├── model/          # JPA entities
│   │   │   ├── repository/     # Spring Data repos
│   │   │   └── service/        # Business logic
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-docker.yml
│   └── test/                   # Tests mirror main structure
├── Dockerfile
└── pom.xml
```

### Frontend Structure
```
greenhouse-dashboard/
├── src/
│   ├── app/                    # Next.js App Router
│   ├── components/             # React components
│   ├── hooks/                  # Custom hooks
│   ├── lib/                    # Utilities
│   └── types/                  # TypeScript types
├── public/
├── package.json
└── next.config.js
```

---

## Development Workflow

### Git Branching
```
main
├── develop
│   ├── feature/add-humidity-sensor
│   ├── feature/improve-dashboard
│   └── bugfix/fix-alert-timing
```

### Branch Naming
- `feature/` - New features
- `bugfix/` - Bug fixes
- `hotfix/` - Production fixes
- `refactor/` - Code improvements

### Commit Messages
Follow [Conventional Commits](https://www.conventionalcommits.org/):
```
feat: add humidity sensor support
fix: resolve alert timing issue
docs: update API documentation
refactor: simplify action service logic
test: add integration tests for alerts
```

### Pull Request Process
1. Create feature branch
2. Make changes with tests
3. Ensure all tests pass
4. Update documentation
5. Create PR with description
6. Request review
7. Address feedback
8. Merge after approval

---

## Code Style

### Java
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok to reduce boilerplate
- Max line length: 120 characters
- Use meaningful variable names

```java
// ✅ Good
@Service
@RequiredArgsConstructor
@Slf4j
public class ParametreService {
    private final ParametreRepository parametreRepository;
    
    public ParametreResponse createParametre(ParametreRequest request) {
        log.info("Creating parameter of type: {}", request.getType());
        // ...
    }
}

// ❌ Bad
@Service
public class ParametreService {
    @Autowired
    ParametreRepository repo;
    
    public ParametreResponse create(ParametreRequest r) {
        System.out.println("Creating...");
        // ...
    }
}
```

### TypeScript/React
- Use functional components
- Use TypeScript strictly
- Follow ESLint/Prettier rules

```tsx
// ✅ Good
interface DashboardProps {
  data: MeasurementData[];
  onRefresh: () => void;
}

export function Dashboard({ data, onRefresh }: DashboardProps) {
  return (
    <div className="dashboard">
      {data.map((item) => (
        <MeasurementCard key={item.id} data={item} />
      ))}
    </div>
  );
}

// ❌ Bad
export function Dashboard(props: any) {
  return (
    <div>
      {props.data.map((item: any) => <div>{item.value}</div>)}
    </div>
  );
}
```

---

## Testing Guidelines

### Backend Testing Pyramid
```
        /\
       /  \    E2E Tests (10%)
      /----\
     /      \  Integration Tests (30%)
    /--------\
   /          \ Unit Tests (60%)
  /____________\
```

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class ParametreServiceTest {
    @Mock
    private ParametreRepository repository;
    
    @InjectMocks
    private ParametreService service;
    
    @Test
    @DisplayName("should create parameter when type is unique")
    void shouldCreateParameterWhenTypeIsUnique() {
        // Given
        ParametreRequest request = TestDataBuilder.aParametre().buildRequest();
        when(repository.existsByType(any())).thenReturn(false);
        
        // When
        ParametreResponse response = service.createParametre(request);
        
        // Then
        assertThat(response).isNotNull();
        verify(repository).save(any());
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParametreControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateParameter() throws Exception {
        mockMvc.perform(post("/api/environnement/parametres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{...}"))
            .andExpect(status().isCreated());
    }
}
```

### Running Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# With coverage report
mvn verify -Pcoverage
# View report: target/site/jacoco/index.html
```

---

## Adding New Features

### Example: Adding a New Sensor Type

#### 1. Update Model
```java
// environnement-service/model/ParametreType.java
public enum ParametreType {
    TEMPERATURE,
    HUMIDITY,
    CO2,
    LUMINOSITY,
    SOIL_MOISTURE,
    PH_LEVEL  // New sensor type
}
```

#### 2. Add Tests First (TDD)
```java
@Test
void shouldCreatePhLevelParameter() {
    ParametreRequest request = ParametreRequest.builder()
        .type(ParametreType.PH_LEVEL)
        .seuilMin(6.0)
        .seuilMax(7.5)
        .unite("pH")
        .build();
    
    ParametreResponse response = service.createParametre(request);
    
    assertThat(response.getType()).isEqualTo(ParametreType.PH_LEVEL);
}
```

#### 3. Update Frontend Types
```typescript
// types/index.ts
export type ParametreType = 
  | 'TEMPERATURE'
  | 'HUMIDITY'
  | 'CO2'
  | 'LUMINOSITY'
  | 'SOIL_MOISTURE'
  | 'PH_LEVEL';  // New type
```

#### 4. Add UI Component
```tsx
// components/PhMeter.tsx
export function PhMeter({ value }: { value: number }) {
  return (
    <div className="ph-meter">
      <span>pH: {value}</span>
    </div>
  );
}
```

#### 5. Update Documentation
- Update API docs
- Update README if needed

#### 6. Create PR
```bash
git checkout -b feature/add-ph-sensor
git add .
git commit -m "feat: add pH level sensor support"
git push origin feature/add-ph-sensor
```

---

## Useful Commands

| Command | Purpose |
|---------|---------|
| `mvn clean install` | Build all modules |
| `mvn spring-boot:run` | Run single service |
| `mvn test` | Run unit tests |
| `mvn verify` | Run all tests |
| `docker compose logs -f` | View container logs |
| `npm run dev` | Start frontend dev server |
| `npm run build` | Build frontend for production |
