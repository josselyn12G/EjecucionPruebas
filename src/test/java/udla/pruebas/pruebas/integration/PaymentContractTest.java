package udla.pruebas.pruebas.integration;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.RestTemplate;
import udla.pruebas.pruebas.service.PaymentClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "PaymentProvider", port = "8089")
public class PaymentContractTest {

    // 1. DEFINICIÓN DEL CONTRATO (Pact)
    @Pact(consumer = "PruebasAppConsumer")
    public V4Pact createPact(PactDslWithProvider builder) {
        return builder
            .given("El servicio de pagos está disponible")
            .uponReceiving("Una petición para autorizar un pago de 100 USD")
                .path("/payments/authorize")
                .method("POST")
                .body("{\"amount\": 100.0, \"currency\": \"USD\"}")
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body("{\"status\": \"AUTHORIZED\", \"transactionId\": \"TX-9999\"}")
            .toPact(V4Pact.class);
    }

    // 2. EJECUCIÓN DEL TEST usando el cliente real contra el Mock configurado por Pact
    @Test
    @PactTestFor(pactMethod = "createPact")
    void shouldAuthorizePaymentAccordingToContract() {
        // Inicializamos el cliente apuntando al puerto mock de Pact/WireMock (8089)
        PaymentClient paymentClient = new PaymentClient(new RestTemplate(), "http://localhost:8089");

        // Ejecutamos la acción real
        Map<String, Object> response = paymentClient.authorizePayment(100.0, "USD");

        // Verificamos que la respuesta mapee con lo que pactamos en el contrato
        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo("AUTHORIZED");
        assertThat(response.get("transactionId")).isEqualTo("TX-9999");
    }
}