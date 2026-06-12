# Proyecto `pruebas` — Estrategia y Documentación de Testing

Proyecto **Spring Boot 3.5.15 / Java 21** orientado a demostrar distintos **niveles y técnicas de pruebas** sobre un dominio de pagos y comisiones.

El objetivo no es la lógica de negocio en sí, sino mostrar **cómo se prueba cada capa** con la herramienta adecuada:

| Nivel | Técnica | Herramienta | Qué valida |
|-------|---------|-------------|------------|
| Unitario | Mocking | JUnit 5 + Mockito | Lógica pura, sin dependencias externas |
| Integración (BD) | Base de datos real efímera | Testcontainers + PostgreSQL | Que las consultas JPA funcionen contra Postgres real |
| Contrato | Contract Testing | Pact (consumer) | Que el cliente HTTP respete el contrato del proveedor |
| Arranque | Smoke test | Spring Boot Test | Que el contexto de Spring levante sin errores |

**Resultado de la última ejecución (2026-06-11): 9 pruebas, 0 fallos, 0 errores ✅**
Ver informe visual en [reports/informe-pruebas.html](reports/informe-pruebas.html).

---

## 1. Pruebas creadas

### 1.1 `CommissionServiceTest` — Prueba Unitaria
📁 [src/test/java/udla/pruebas/pruebas/unit/CommissionServiceTest.java](src/test/java/udla/pruebas/pruebas/unit/CommissionServiceTest.java)

Prueba **unitaria pura** del cálculo de comisiones. Usa **Mockito** para simular el `SalesRepository`, de modo que no toca base de datos ni red: solo se evalúa la lógica de `CommissionService`.

Se aplica la técnica de **análisis de valores límite (boundary testing)** sobre los tramos de comisión:

- Tier 1 (≤ $10,000.00) → 5%
- Tier 2 ($10,000.01 – $20,000.00) → 10%
- Tier 3 (> $20,000.00) → 15%

| Test | Qué verifica |
|------|--------------|
| `testCalculateCommission_ZeroOrNegativeSales_ReturnsZero` | Ventas en 0 o nulas devuelven `0.00` (caso defensivo) |
| `testCalculateCommission_Tier1_BoundaryExact` | Límite exacto $10,000.00 aplica 5% = $500.00 |
| `testCalculateCommission_Tier2_BoundaryLower` | Justo encima ($10,000.01) salta a 10% |
| `testCalculateCommission_Tier2_BoundaryUpper` | Límite superior $20,000.00 sigue en 10% |
| `testCalculateCommission_Tier3_BoundaryLower` | Justo encima ($20,000.01) salta a 15% |
| `testCalculateCommission_RoundingHalfUp_TriggersCorrectly` | El redondeo comercial `HALF_UP` redondea el 3er decimal correctamente |

**6 tests.** Es la prueba más rápida porque no levanta nada externo.

---

### 1.2 `TransactionRepositoryTest` — Prueba de Integración con BD
📁 [src/test/java/udla/pruebas/pruebas/integration/TransactionRepositoryTest.java](src/test/java/udla/pruebas/pruebas/integration/TransactionRepositoryTest.java)

Prueba de **integración real contra PostgreSQL** usando **Testcontainers**: levanta un contenedor Docker de `postgres:16-alpine` solo para el test y lo destruye al terminar.

- `@DataJpaTest` carga únicamente la capa JPA (no toda la app).
- `@AutoConfigureTestDatabase(replace = NONE)` impide que Spring sustituya Postgres por H2 en memoria — así probamos contra el motor **real** que se usará en producción.
- `@ServiceConnection` conecta automáticamente Spring al contenedor.

| Test | Qué verifica |
|------|--------------|
| `shouldFindTransactionsByDateRange` | Que `findByCreatedAtBetween(...)` traiga **solo** las transacciones dentro del rango de fechas |

**1 test.** Es la más lenta (~15 s) porque arranca un contenedor de base de datos.

---

### 1.3 `PaymentContractTest` — Prueba de Contrato (Pact)
📁 [src/test/java/udla/pruebas/pruebas/integration/PaymentContractTest.java](src/test/java/udla/pruebas/pruebas/integration/PaymentContractTest.java)

Prueba de **contrato (consumer-driven contract testing)** con **Pact**. Define el "contrato" que nuestro cliente espera del servicio externo de pagos y verifica que el `PaymentClient` real cumpla ese acuerdo, sin depender del proveedor real.

Flujo:
1. **Se define el contrato** (`@Pact createPact`): ante un `POST /payments/authorize` con `{amount:100, currency:USD}`, el proveedor debe responder `200` con `{status:AUTHORIZED, transactionId:TX-9999}`.
2. Pact levanta un **mock server** en el puerto `8089` que cumple ese contrato.
3. **Se ejecuta el cliente real** contra ese mock y se valida que la respuesta mapee con lo pactado.

| Test | Qué verifica |
|------|--------------|
| `shouldAuthorizePaymentAccordingToContract` | Que `PaymentClient.authorizePayment(...)` envíe la petición correcta e interprete bien la respuesta del contrato |

Además, Pact genera el archivo de contrato en `target/pacts/` para poder compartirlo con el equipo del proveedor.

**1 test.**

---

### 1.4 `PruebasApplicationTests` — Smoke Test
📁 [src/test/java/udla/pruebas/pruebas/PruebasApplicationTests.java](src/test/java/udla/pruebas/pruebas/PruebasApplicationTests.java)

Prueba de humo generada por Spring Boot. `@SpringBootTest` levanta el **contexto completo** de la aplicación y el test `contextLoads()` confirma que todos los beans se construyen y cablean sin errores. Si falta una dependencia o una configuración está mal, este test falla.

**1 test.**

---

## 2. Código de producción que se prueba

Estos son los archivos de `src/main` que las pruebas ejercitan:

| Archivo | Rol |
|---------|-----|
| [CommissionService.java](src/main/java/udla/pruebas/pruebas/CommissionService.java) | Lógica de cálculo de comisiones por tramos con redondeo `HALF_UP`. Probado por `CommissionServiceTest`. |
| [SalesRepository.java](src/main/java/udla/pruebas/pruebas/SalesRepository.java) | Interfaz que provee el total de ventas por empleado. Se **mockea** en las pruebas unitarias. |
| [model/Transaction.java](src/main/java/udla/pruebas/pruebas/model/Transaction.java) | Entidad JPA mapeada a la tabla `transactions` (id, amount, createdAt). |
| [repository/TransactionRepository.java](src/main/java/udla/pruebas/pruebas/repository/TransactionRepository.java) | Repositorio Spring Data JPA con la consulta derivada `findByCreatedAtBetween`. Probado por `TransactionRepositoryTest`. |
| [service/PaymentClient.java](src/main/java/udla/pruebas/pruebas/service/PaymentClient.java) | Cliente HTTP (`RestTemplate`) que autoriza pagos contra el servicio externo. Probado por `PaymentContractTest`. |
| [config/AppConfig.java](src/main/java/udla/pruebas/pruebas/config/AppConfig.java) | Configuración que expone el bean `RestTemplate`. |
| [PruebasApplication.java](src/main/java/udla/pruebas/pruebas/PruebasApplication.java) | Punto de entrada de la aplicación Spring Boot. |

---

## 3. Dependencias de testing (en `pom.xml`)

| Dependencia | Para qué |
|-------------|----------|
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ, Spring Test |
| `spring-boot-testcontainers` + `testcontainers/junit-jupiter` + `testcontainers/postgresql` | Levantar PostgreSQL real en Docker durante los tests |
| `org.postgresql:postgresql` | Driver de PostgreSQL |
| `com.h2database:h2` | BD en memoria (alternativa, no usada en el test de integración) |
| `au.com.dius.pact.consumer:junit5` | Contract testing con Pact |
| `org.wiremock:wiremock-standalone` | Mock de servicios HTTP |
| `pitest-maven` | (plugin) Pruebas de mutación para medir la calidad de los tests |

> **Requisito:** las pruebas de integración (`TransactionRepositoryTest`) necesitan **Docker** corriendo en la máquina.

---

## 4. Cómo ejecutar las pruebas

```bash
# Todas las pruebas
mvn test

# Una sola clase
mvn test -Dtest=CommissionServiceTest

# Pruebas de mutación (calidad de los tests)
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

> En este equipo no hay Maven instalado globalmente; usa el wrapper `./mvnw` (Linux/Mac) o `mvnw.cmd` (Windows), que requiere conexión a internet la primera vez para descargar Maven.

### Dónde ver los resultados

- **Resumen por consola** al terminar `mvn test`.
- **Reportes nativos** en `target/surefire-reports/`:
  - `*.txt` → resumen legible por clase
  - `TEST-*.xml` → formato estándar para CI (Jenkins, GitLab, SonarQube)
- **Informe HTML** generado para este proyecto: [reports/informe-pruebas.html](reports/informe-pruebas.html)
- **Contratos Pact** generados en `target/pacts/`

---

## 5. Estructura de carpetas de pruebas

```
src/test/java/udla/pruebas/pruebas/
├── PruebasApplicationTests.java        # Smoke test (contexto Spring)
├── unit/
│   └── CommissionServiceTest.java      # Unitario + Mockito
└── integration/
    ├── PaymentContractTest.java        # Contract testing (Pact)
    └── TransactionRepositoryTest.java  # Integración BD (Testcontainers)
```
