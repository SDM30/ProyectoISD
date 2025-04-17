package org.grupo4.entidades;

import org.grupo4.concurrencia.ContadorAtomico;

import java.io.InputStream;
import java.util.Properties;

public class AdministradorInstalaciones {
    private static volatile AdministradorInstalaciones singleton;
    private final ContadorAtomico salones;
    private final ContadorAtomico labs;
    private final ContadorAtomico aulasMoviles;

    // Valores maximos parametrizables
    public AdministradorInstalaciones() {
        // Valores por defecto
        int maxSalones = 380;
        int maxLabs = 60;

        // Cargar configuración desde archivo
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("src/main/resources/configServidor.properties")) {

            Properties prop = new Properties();
            prop.load(input);

            maxSalones = Integer.parseInt(
                    prop.getProperty("server.maxSalones", "380"));

            maxLabs = Integer.parseInt(
                    prop.getProperty("server.maxLabs", "60"));

        } catch (Exception e) {
            System.err.println("Error cargando configuración. Usando valores por defecto. Detalle: "
                    + e.getMessage());
        }

        this.salones = new ContadorAtomico(maxSalones);
        this.labs = new ContadorAtomico(maxLabs);
        this.aulasMoviles = new ContadorAtomico(0);
    }

    public static AdministradorInstalaciones getInstance() {
        if (singleton == null) {
            synchronized(AdministradorInstalaciones.class) {
                if(singleton == null) {
                    singleton = new AdministradorInstalaciones();
                }
            }
        }
        return singleton;
    }

    public ResultadoAsignacion asignar(int salonesNecesitados, int labsNecesitados) {
        synchronized(this) {
            // Caso 1: Hay suficientes salones y labs disponibles
            if (labs.get() >= labsNecesitados && salones.get() >= salonesNecesitados) {
                labs.decrementar(labsNecesitados);
                salones.decrementar(salonesNecesitados);
                return new ResultadoAsignacion(labsNecesitados, 0, salonesNecesitados);
            }

            // Caso 2: Faltan labs pero podemos convertir salones en aulas móviles
            int labsDisponibles = labs.get();
            int labsFaltantes = labsNecesitados - labsDisponibles;
            int salonesRequeridos = salonesNecesitados + labsFaltantes;

            if (salones.get() >= salonesRequeridos) {
                labs.decrementar(labsDisponibles);
                salones.decrementar(salonesRequeridos);
                aulasMoviles.incrementar(labsFaltantes);

                return new ResultadoAsignacion(labsDisponibles, salonesNecesitados, labsFaltantes);
            }

            // Caso 3: No hay recursos suficientes
            return new ResultadoAsignacion(0, 0, 0);
        }
    }

    // Método para obtener estadísticas actuales
    public String getEstadisticas() {
        return String.format("Salones disponibles: %d, Laboratorios disponibles: %d, Aulas móviles: %d",
                salones.get(), labs.get(), aulasMoviles.get());
    }
}
