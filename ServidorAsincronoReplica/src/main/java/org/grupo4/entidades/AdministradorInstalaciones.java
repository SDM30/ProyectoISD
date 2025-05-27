package org.grupo4.entidades;

import org.grupo4.concurrencia.ContadorAtomico;
import org.grupo4.redes.ResultadoEnvio;

public class AdministradorInstalaciones {
    private static volatile AdministradorInstalaciones singleton;
    private final ContadorAtomico salones;
    private final ContadorAtomico labs;
    private final ContadorAtomico aulasMoviles;
    private final ContadorAtomico aulasMovilesAsignadas;

    public AdministradorInstalaciones(int salones, int labs, int aulasMoviles) {
        this.salones = new ContadorAtomico(salones);
        this.labs = new ContadorAtomico(labs);
        this.aulasMoviles = new ContadorAtomico(aulasMoviles);
        this.aulasMovilesAsignadas = new ContadorAtomico(0);
    }

    public AdministradorInstalaciones() {
        this.aulasMoviles = new ContadorAtomico(0);
        this.salones = new ContadorAtomico(0);
        this.labs = new ContadorAtomico(0);
        this.aulasMovilesAsignadas = new ContadorAtomico(0);
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

    public static AdministradorInstalaciones getInstance(int salones, int labs, int aulasMoviles) {
        if (singleton == null) {
            synchronized(AdministradorInstalaciones.class) {
                if(singleton == null) {
                    singleton = new AdministradorInstalaciones(salones, labs, aulasMoviles);
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
                return new ResultadoAsignacion(labsNecesitados, salonesNecesitados, 0);
            }

            // Caso 2: No hay suficientes laboratorios pero hay suficientes aulas móviles disponibles
            int labsFaltantes = labsNecesitados - labs.get();
            if (labsFaltantes > 0 && aulasMoviles.get() >= labsFaltantes && salones.get() >= salonesNecesitados) {
                int labsDisponibles = labs.get();
                labs.decrementar(labsDisponibles);
                salones.decrementar(salonesNecesitados);
                aulasMoviles.decrementar(labsFaltantes);
                return new ResultadoAsignacion(labsDisponibles, salonesNecesitados, labsFaltantes);
            }

            // Caso 3: No hay suficientes laboratorios pero podemos usar salones y/o aulas móviles como labs
            if (labsFaltantes > 0) {
                int labsDisponibles = labs.get();
                int aulasMovilesUsadas = Math.min(aulasMoviles.get(), labsFaltantes);
                int salonesFaltantes = labsFaltantes - aulasMovilesUsadas;

                if (salones.get() >= (salonesNecesitados + salonesFaltantes)) {
                    labs.decrementar(labsDisponibles);
                    aulasMoviles.decrementar(aulasMovilesUsadas);
                    salones.decrementar(salonesNecesitados + salonesFaltantes);
                    return new ResultadoAsignacion(labsDisponibles, salonesNecesitados, labsFaltantes);
                }
            }

            // Caso 4: No hay recursos suficientes
            return new ResultadoAsignacion(0, 0, 0);
        }
    }

    public boolean devolverRecursos(ResultadoEnvio asignacion) {
        // 1. Calcular valores futuros
        int labsFuturos = labs.get() + asignacion.getLabsAsignados();
        int salonesFuturos = salones.get() + asignacion.getSalonesAsignados() + asignacion.getAulaMovilAsignadas();
        int aulasMovilesFuturas = aulasMovilesAsignadas.get() - asignacion.getAulaMovilAsignadas();

        // 2. Validar integridad
        boolean operacionValida =
                labsFuturos >= 0 &&
                        salonesFuturos >= 0 &&
                        aulasMovilesFuturas >= 0 &&
                        asignacion.getAulaMovilAsignadas() <= aulasMovilesAsignadas.get();

        if (!operacionValida) {
            return false;
        }

        // 3. Aplicar cambios con incrementar/decrementar (sin usar set)
        labs.incrementar(asignacion.getLabsAsignados());
        salones.incrementar(asignacion.getSalonesAsignados() + asignacion.getAulaMovilAsignadas());

        if (asignacion.getAulaMovilAsignadas() > 0) {
            aulasMovilesAsignadas.decrementar(asignacion.getAulaMovilAsignadas());
        }

        return true;
    }

    // Método para obtener estadísticas actuales
    public String getEstadisticas() {
        return String.format("Salones disponibles: %d, Laboratorios disponibles: %d, Aulas móviles: %d, Aulas móviles asignadas: %d",
                salones.get(), labs.get(), aulasMoviles.get(), aulasMovilesAsignadas.get());
    }

}
