package udla.pruebas.pruebas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double amount;
    private LocalDateTime createdAt;

    // Constructor vacío requerido por JPA
    public Transaction() {}

    // Constructor útil para los tests
    public Transaction(Double amount, LocalDateTime createdAt) {
        this.amount = amount;
        this.createdAt = createdAt;
    }

    // Getters y Setters (o usa las anotaciones de Lombok si lo prefieres)
    public Long getId() { return id; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}