package udla.pruebas.pruebas.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import java.util.HashMap;

@Service
public class PaymentClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    // Usamos @Value para que Spring inyecte la URL desde application.properties
    // Si no existe, usamos el valor por defecto "http://localhost:8089"
    public PaymentClient(RestTemplate restTemplate, 
                         @Value("${payment.api.url:http://localhost:8089}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Map<String, Object> authorizePayment(Double amount, String currency) {
        String url = baseUrl + "/payments/authorize";
        
        Map<String, Object> request = new HashMap<>();
        request.put("amount", amount);
        request.put("currency", currency);

        return restTemplate.postForObject(url, request, Map.class);
    }
}