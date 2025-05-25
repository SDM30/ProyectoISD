package org.grupo4proyecto.entidades;

public record ResultadoAsignacion(
        int labsAsignados,
        int aulaMovilAsignadas,
        int salonesAsignados
) {
    // Inmutabilidad garantizada por el record
    // Métodos autogenerados: toString(), equals(), hashCode()
    public boolean esExitoso() {
        return labsAsignados > 0 || aulaMovilAsignadas > 0 || salonesAsignados > 0;
    }
}