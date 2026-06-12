package udla.pruebas.pruebas.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import udla.pruebas.pruebas.repository.TransactionRepository;
import udla.pruebas.pruebas.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
// Evita que Spring reemplace Postgres por la base de datos H2 en memoria
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TransactionRepositoryTest {

    // Define el contenedor de PostgreSQL que se descargará y levantará solo para el test
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldFindTransactionsByDateRange() {
        // 1. Arrange: Guardar un par de transacciones en la base de datos real del contenedor
        LocalDateTime now = LocalDateTime.now();
        
        Transaction t1 = new Transaction(100.0, now.minusDays(2));
        Transaction t2 = new Transaction(250.0, now);
        
        transactionRepository.save(t1);
        transactionRepository.save(t2);

        // 2. Act: Llamar al método de rango de fechas
        List<Transaction> result = transactionRepository.findByCreatedAtBetween(now.minusDays(1), now.plusDays(1));

        // 3. Assert: Verificar que solo traiga la transacción que cae en el rango (t2)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(250.0);
    }
}