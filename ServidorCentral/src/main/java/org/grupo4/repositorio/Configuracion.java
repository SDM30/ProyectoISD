package org.grupo4.repositorio;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Configuracion {
    /**
     * Carga las propiedades del servidor y devuelve una lista de strings:
     *   server.maxSalones = <valor>
     *   server.maxLabs = <valor>
     *   server.ip = <valor>
     *   server.port = <valor>
     */
    public static List<String> cargarConfiguracionServidor() {
        // Valores por defecto
        int maxSalones = 380;
        int maxLabs = 60;
        String ip = "localhost";
        String port = "5555";

        try (InputStream input = Configuracion.class.getClassLoader()
                .getResourceAsStream("configServidor.properties")) {

            Properties prop = new Properties();
            prop.load(input);

            maxSalones = Integer.parseInt(prop.getProperty("server.maxSalones", "380"));
            maxLabs = Integer.parseInt(prop.getProperty("server.maxLabs", "60"));
            ip = prop.getProperty("server.ip", "localhost");
            port = prop.getProperty("server.port", "5555");

        } catch (Exception e) {
            System.err.println("Error cargando configuración. Usando valores por defecto. Detalle: "
                    + e.getMessage());
        }

        List<String> valores = new ArrayList<>();
        valores.add(String.valueOf(maxSalones));
        valores.add(String.valueOf(maxLabs));
        valores.add(ip);
        valores.add(port);

        return valores;
    }
}
