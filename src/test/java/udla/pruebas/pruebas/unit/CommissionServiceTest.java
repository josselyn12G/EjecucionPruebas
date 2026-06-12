package udla.pruebas.pruebas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommissionServiceTest {

    @Mock
    private SalesRepository salesRepository;

    @InjectMocks
    private CommissionService commissionService;

    private final Long employeeId = 1L;

    @Test
    public void testCalculateCommission_ZeroOrNegativeSales_ReturnsZero() {
        // Caso: Ventas en 0
        when(salesRepository.getTotalSalesByEmployee(employeeId)).thenReturn(BigDecimal.ZERO);
        BigDecimal commission = commissionService.calculateCommission(employeeId);
        assertEquals(new BigDecimal("0.00"), commission);

        // Caso: Ventas nulas
        when(salesRepository.getTotalSalesByEmployee(employeeId)).thenReturn(null);
        commission = commissionService.calculateCommission(employeeId);
        assertEquals(new BigDecimal("0.00"), commission);
    }

    @Test
    public void testCalculateCommission_Tier1_BoundaryExact() {
        // Límite exacto de Tier 1: $10,000.00 -> Debería aplicar el 5% = $500.00
        when(salesRepository.getTotalSalesByEmployee(employeeId)).thenReturn(new BigDecimal("10000.00"));
        
        BigDecimal commission = commissionService.calculateCommission(employeeId);
        
        assertEquals(new BigDecimal("500.00"), commission);
    }

    @Test
    public void testCalculateCommission_Tier2_BoundaryLower() {
        // Justo arriba de Tier 1: $10,000.01 -> Debería aplicar el 10% = $1,000.00 (Redondeado HALF_UP)
        when(salesRepository.getTotalSalesByEmployee(employeeId)).thenReturn(new BigDecimal("10000.01"));
        
        BigDecimal commission = commissionService.calculateCommission(employeeId);
        
        // 10000.01 * 0.10 = 1000.001 -> Redondeado a 2 decimales es 1000.00
        assertEquals(new BigDecimal("1000.00"), commission);
    }

    @Test
    public void testCalculateCommission_Tier2_BoundaryUpper() {
        // Límite exacto superior de Tier 2: $20,000.00 -> Debería aplicar el 10% = $2,000.00
        when(salesRepository.getTotalSalesByEmployee(employeeId)).thenReturn(new BigDecimal("20000.00"));
        
        BigDecimal commission = commissionService.calculateCommission(employeeId);
        
        assertEquals(new BigDecimal("20000.00").multiply(new BigDecimal("0.10")).setScale(2), commission);
    }

    @Test
    public void testCalculateCommission_Tier3_BoundaryLower() {
        // Justo arriba de Tier 2: $20,000.01 -> Debería aplicar el 15% = $3,000.00
        when(salesRepository.getTotalSalesByEmployee(employeeId)).thenReturn(new BigDecimal("20000.01"));
        
        BigDecimal commission = commissionService.calculateCommission(employeeId);
        
        // 20000.01 * 0.15 = 3000.0015 -> Redondeado HALF_UP es 3000.00
        assertEquals(new BigDecimal("3000.00"), commission);
    }

    @Test
    public void testCalculateCommission_RoundingHalfUp_TriggersCorrectly() {
        // Forzar un redondeo comercial hacia arriba: $10,005.50 * 10% = 1000.550
        // Usemos un valor intermedio para ver el comportamiento del tercer decimal:
        // Ventas: $10,005.55 -> * 10% = 1000.555 -> Redondea a 1000.56
        when(salesRepository.getTotalSalesByEmployee(employeeId)).thenReturn(new BigDecimal("10005.55"));
        
        BigDecimal commission = commissionService.calculateCommission(employeeId);
        
        assertEquals(new BigDecimal("1000.56"), commission);
    }
}