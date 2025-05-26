package org.grupo4.repositorio;

import java.io.FileInputStream;
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
     *   server.inproc = <valor>
     *   server.iphealthcheck = <valor>
     *   server.porthealthcheck = <valor>
     */
    public static List<String> cargarConfiguracionServidor(String rutaConfig) {
        // Valores por defecto
        int maxSalones = 380;
        int maxLabs = 60;
        String ip = "0.0.0.0";
        String port = "5555";
        String inproc = "backend";
        String ipHealthcheck = "0.0.0.0";
        String portHealthcheck = "5554";

        try {
            final InputStream input;
            if (rutaConfig == null) {
                // Cargar archivo por defecto desde resources
                input = Configuracion.class.getClassLoader().getResourceAsStream("configServidor.properties");
                if (input == null) {
                    throw new Exception("No se encontró el archivo de configuración por defecto");
                }
            } else {
                // Intentar primero como recurso
                InputStream resourceInput = Configuracion.class.getClassLoader().getResourceAsStream(rutaConfig);
                if (resourceInput != null) {
                    input = resourceInput;
                } else {
                    // Si no está en resources, intentar como archivo en el sistema
                    input = new FileInputStream(rutaConfig);
                }
            }

            Properties prop = new Properties();
            try (input) {
                prop.load(input);
                maxSalones = Integer.parseInt(prop.getProperty("server.maxSalones", "380"));
                maxLabs = Integer.parseInt(prop.getProperty("server.maxLabs", "60"));
                ip = prop.getProperty("server.ip", "0.0.0.0");
                port = prop.getProperty("server.port", "5555");
                inproc = prop.getProperty("server.inproc", "backend");
                ipHealthcheck = prop.getProperty("server.iphealthcheck", "0.0.0.0");
                portHealthcheck = prop.getProperty("server.porthealthcheck", "5554");
            }
        } catch (Exception e) {
            System.err.println("Error cargando configuración. Usando valores por defecto. Detalle: "
                    + e.getMessage());
        }

        List<String> valores = new ArrayList<>();
        valores.add(String.valueOf(maxSalones));
        valores.add(String.valueOf(maxLabs));
        valores.add(ip);
        valores.add(port);
        valores.add(inproc);
        valores.add(ipHealthcheck);
        valores.add(portHealthcheck);

        return valores;
    }
}