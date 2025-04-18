package org.grupo4proyecto.redes;

// Clase encargada de la aceptacion de la facultad
public class ConfirmacionAsignacion {
    private String encabezado;
    private ResultadoEnvio resEnvio;

    public ConfirmacionAsignacion(String encabezado, ResultadoEnvio resEnvio) {
        this.encabezado = encabezado;
        this.resEnvio = resEnvio;
    }

    public ConfirmacionAsignacion() {
    }

    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public ResultadoEnvio getResEnvio() {
        return resEnvio;
    }

    public void setResEnvio(ResultadoEnvio resEnvio) {
        this.resEnvio = resEnvio;
    }
}
