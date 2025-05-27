package org.grupo4.entidades;

import java.net.InetAddress;

public class Facultad {
    private String nombre;
    private InetAddress dirFacultad;
    private int puertoFacutad;

    public Facultad (String nombre, InetAddress dirFacultad, int puertoFacutad) {
        this.nombre = nombre;
        this.dirFacultad = dirFacultad;
        this.puertoFacutad = puertoFacutad;
    }

    public Facultad () {
        this.nombre = null;
        this.dirFacultad = null;
        puertoFacutad = 0;
    }

    public String getNombre () {
        return nombre;
    }

    public void setNombre (String nombre) {
        this.nombre = nombre;
    }

    public InetAddress getDirFacultad() {
        return dirFacultad;
    }

    public void setDirFacultad(InetAddress dirFacultad) {
        this.dirFacultad = dirFacultad;
    }

    public int getPuertoFacutad() {
        return puertoFacutad;
    }

    public void setPuertoFacutad(int puertoFacutad) {
        this.puertoFacutad = puertoFacutad;
    }

    @Override
    public String toString () {
        return "Facultad{" +
                "nombre='" + nombre + '\'' +
                ", dirServidorCentral=" + dirFacultad +
                ", puertoServidorCentral=" + puertoFacutad +
                '}';
    }
}