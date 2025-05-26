package org.grupo4.asincrono.modelo;

public class ResultadoAsignacion {
    private int salonesAsignados;
    private int laboratoriosAsignados;
    private String mensaje;

    public int getSalonesAsignados() { return salonesAsignados; }
    public int getLaboratoriosAsignados() { return laboratoriosAsignados; }
    public String getMensaje() { return mensaje; }

    public void setSalonesAsignados(int salonesAsignados) { this.salonesAsignados = salonesAsignados; }
    public void setLaboratoriosAsignados(int laboratoriosAsignados) { this.laboratoriosAsignados = laboratoriosAsignados; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
