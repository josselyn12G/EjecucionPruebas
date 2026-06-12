package udla.pruebas.pruebas;

import java.math.BigDecimal;

public interface SalesRepository {
    BigDecimal getTotalSalesByEmployee(Long employeeId);
}