package org.grupo4proyecto.entidades;

public class Solicitud {
    private String facultad;
    private String programa;
    private int numSalones;
    private int numLaboratorios;

    public Solicitud (String facultad, String programa, int numSalones, int numLaboratorios) {
        this.facultad = facultad;
        this.programa = programa;
        this.numSalones = numSalones;
        this.numLaboratorios = numLaboratorios;
    }

    public String getFacultad () {
        return facultad;
    }

    public void setFacultad (String facultad) {
        this.facultad = facultad;
    }

    public String getPrograma () {
        return programa;
    }

    public void setPrograma (String programa) {
        this.programa = programa;
    }

    public int getNumSalones () {
        return numSalones;
    }

    public void setNumSalones (int numSalones) {
        this.numSalones = numSalones;
    }

    public int getNumLaboratorios () {
        return numLaboratorios;
    }

    public void setNumLaboratorios (int numLaboratorios) {
        this.numLaboratorios = numLaboratorios;
    }
}
