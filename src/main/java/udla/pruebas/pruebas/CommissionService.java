package udla.pruebas.pruebas;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CommissionService {

    private final SalesRepository salesRepository;

    public CommissionService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    public BigDecimal calculateCommission(Long employeeId) {
        BigDecimal totalSales = salesRepository.getTotalSalesByEmployee(employeeId);
        
        if (totalSales == null || totalSales.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal rate;

        // Definición de Tiers (Límites de comisión)
        // Tier 1: Hasta $10,000.00 -> 5%
        // Tier 2: De $10,000.01 hasta $20,000.00 -> 10%
        // Tier 3: Más de $20,000.00 -> 15%
        if (totalSales.compareTo(new BigDecimal("10000.00")) <= 0) {
            rate = new BigDecimal("0.05");
        } else if (totalSales.compareTo(new BigDecimal("20000.00")) <= 0) {
            rate = new BigDecimal("0.10");
        } else {
            rate = new BigDecimal("0.15");
        }

        // Cálculo con redondeo comercial estándar (HALF_UP)
        return totalSales.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}