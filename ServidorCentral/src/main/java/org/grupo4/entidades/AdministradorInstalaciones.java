package org.grupo4.entidades;

import org.grupo4.concurrencia.ContadorAtomico;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.grupo4.repositorio.Configuracion.cargarConfiguracionServidor;

public class AdministradorInstalaciones {
    private static volatile AdministradorInstalaciones singleton;
    private final ContadorAtomico salones;
    private final ContadorAtomico labs;
    private final ContadorAtomico aulasMoviles;

    // Valores maximos parametrizables
    public AdministradorInstalaciones() {
        List<String> valores = cargarConfiguracionServidor();

        int maxSalones = Integer.parseInt(valores.get(0));
        int maxLabs = Integer.parseInt(valores.get(1));

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
