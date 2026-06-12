package udla.pruebas.pruebas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udla.pruebas.pruebas.model.Transaction;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}