package pe.edu.utp.spa.app.util;

import ua_parser.Client;
import ua_parser.Parser;

public class UserAgentParser {

    private static final Parser UA_PARSER;

    static {
        try {
            UA_PARSER = new Parser();
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar UserAgentParser", e);
        }
    }

    public static UserAgentInfo parse(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return new UserAgentInfo("Desconocido", "Desconocido", "Desconocido");
        }

        try {
            Client client = UA_PARSER.parse(userAgentString);

            String navegador = buildBrowserName(client);
            String sistemaOperativo = buildOsName(client);
            String dispositivo = buildDeviceName(client);

            return new UserAgentInfo(navegador, sistemaOperativo, dispositivo);
        } catch (Exception e) {
            return new UserAgentInfo("Error al parsear", "Error al parsear", "Error al parsear");
        }
    }

    private static String buildBrowserName(Client client) {
        String family = client.userAgent.family;
        String major = client.userAgent.major;
        String minor = client.userAgent.minor;

        if (family == null) return "Desconocido";

        StringBuilder browser = new StringBuilder(family);
        if (major != null) {
            browser.append(" ").append(major);
            if (minor != null) {
                browser.append(".").append(minor);
            }
        }
        return browser.toString();
    }

    private static String buildOsName(Client client) {
        String family = client.os.family;
        String major = client.os.major;
        String minor = client.os.minor;

        if (family == null) return "Desconocido";

        StringBuilder os = new StringBuilder(family);
        if (major != null) {
            os.append(" ").append(major);
            if (minor != null) {
                os.append(".").append(minor);
            }
        }
        return os.toString();
    }

    private static String buildDeviceName(Client client) {
        String family = client.device.family;

        if (family == null || family.equalsIgnoreCase("Other")) {
            return "PC";
        }

        return family;
    }
}
