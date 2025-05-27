package grupo4.repositorio;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Configuracion {
    public static List<String> cargarConfiguracionServidor(String rutaConfig) {
        // Valores por defecto
        int maxSalones = 380;
        int maxLabs = 60;
        int aulasMoviles = 10;
        String ip = "0.0.0.0";
        String port = "5555";
        String inproc = "backend";
        String ipHealthcheck = "0.0.0.0";
        String portHealthcheck = "5554";
        String cassandraIp = "localhost";
        int cassandraPort = 9042;

        try {
            final InputStream input;
            if (rutaConfig == null) {
                input = Configuracion.class.getClassLoader().getResourceAsStream("configServidor.properties");
                if (input == null) {
                    throw new Exception("No se encontró el archivo de configuración por defecto");
                }
            } else {
                InputStream resourceInput = Configuracion.class.getClassLoader().getResourceAsStream(rutaConfig);
                if (resourceInput != null) {
                    input = resourceInput;
                } else {
                    input = new FileInputStream(rutaConfig);
                }
            }

            Properties prop = new Properties();
            try (input) {
                prop.load(input);

                // Cargar propiedades en el orden especificado
                maxSalones = Integer.parseInt(prop.getProperty("server.maxSalones", "380"));
                maxLabs = Integer.parseInt(prop.getProperty("server.maxLabs", "60"));
                aulasMoviles = Integer.parseInt(prop.getProperty("server.aulasMoviles", "10"));
                ip = prop.getProperty("server.ip", "0.0.0.0");
                port = prop.getProperty("server.port", "5555");
                inproc = prop.getProperty("server.inproc", "backend");
                ipHealthcheck = prop.getProperty("server.iphealthcheck", "0.0.0.0");
                portHealthcheck = prop.getProperty("server.porthealthcheck", "5554");
                cassandraIp = prop.getProperty("server.cassandranodeip", "localhost");
                cassandraPort = Integer.parseInt(prop.getProperty("server.cassandranodeport", "9042"));
            }
        } catch (Exception e) {
            System.err.println("Error cargando configuración. Usando valores por defecto. Detalle: " + e.getMessage());
        }

        // Crear lista de valores en el orden especificado
        List<String> valores = new ArrayList<>();
        valores.add(String.valueOf(maxSalones));
        valores.add(String.valueOf(maxLabs));
        valores.add(String.valueOf(aulasMoviles));
        valores.add(ip);
        valores.add(port);
        valores.add(inproc);
        valores.add(ipHealthcheck);
        valores.add(portHealthcheck);
        valores.add(cassandraIp);
        valores.add(String.valueOf(cassandraPort));

        return valores;
    }
}
