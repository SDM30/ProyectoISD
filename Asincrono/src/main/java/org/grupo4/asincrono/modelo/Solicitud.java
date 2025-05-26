package org.grupo4.asincrono.modelo;

public class Solicitud {
    private String facultad;
    private int salones;
    private int laboratorios;

    public Solicitud() {}

    public Solicitud(String facultad, int salones, int laboratorios) {
        this.facultad = facultad;
        this.salones = salones;
        this.laboratorios = laboratorios;
    }

    public String getFacultad() { return facultad; }
    public int getSalones() { return salones; }
    public int getLaboratorios() { return laboratorios; }

    public void setFacultad(String facultad) { this.facultad = facultad; }
    public void setSalones(int salones) { this.salones = salones; }
    public void setLaboratorios(int laboratorios) { this.laboratorios = laboratorios; }
}