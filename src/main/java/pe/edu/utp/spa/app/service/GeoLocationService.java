package pe.edu.utp.spa.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pe.edu.utp.spa.app.util.GeoLocationInfo;

import java.time.Duration;

@Service
public class GeoLocationService {

    private static final Logger logger = LoggerFactory.getLogger(GeoLocationService.class);
    private static final String IP_API_URL = "http://ip-api.com/json/";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeoLocationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public GeoLocationInfo getGeoLocation(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank() || isLocalOrPrivateIp(ipAddress)) {
            return new GeoLocationInfo("Local", "Local");
        }

        try {
            String url = IP_API_URL + ipAddress + "?fields=country,city,status";
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "fail";

                if ("success".equals(status)) {
                    String pais = jsonNode.has("country") ? jsonNode.get("country").asText() : "Desconocido";
                    String ciudad = jsonNode.has("city") ? jsonNode.get("city").asText() : "Desconocido";
                    return new GeoLocationInfo(pais, ciudad);
                }
            }
        } catch (Exception e) {
            logger.warn("Error al obtener geolocalización para IP {}: {}", ipAddress, e.getMessage());
        }

        return new GeoLocationInfo("Desconocido", "Desconocido");
    }

    private boolean isLocalOrPrivateIp(String ip) {
        return ip.equals("127.0.0.1") ||
               ip.equals("0:0:0:0:0:0:0:1") ||
               ip.equals("::1") ||
               ip.startsWith("192.168.") ||
               ip.startsWith("10.") ||
               ip.startsWith("172.16.") ||
               ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") ||
               ip.startsWith("172.19.") ||
               ip.startsWith("172.20.") ||
               ip.startsWith("172.21.") ||
               ip.startsWith("172.22.") ||
               ip.startsWith("172.23.") ||
               ip.startsWith("172.24.") ||
               ip.startsWith("172.25.") ||
               ip.startsWith("172.26.") ||
               ip.startsWith("172.27.") ||
               ip.startsWith("172.28.") ||
               ip.startsWith("172.29.") ||
               ip.startsWith("172.30.") ||
               ip.startsWith("172.31.");
    }
}
