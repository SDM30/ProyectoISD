package org.grupo4proyecto.entidades;

import java.net.InetAddress;
import java.util.List;

public class Facultad {
    private String nombre;
    private List<Programa> programas;
    private InetAddress dirServidorCentral;
    private int puertoServidorCentral;

    public Facultad (String nombre, List<Programa> programas, InetAddress dirServidorCentral, int puertoServidorCentral) {
        this.nombre = nombre;
        this.programas = programas;
        this.dirServidorCentral = dirServidorCentral;
        this.puertoServidorCentral = puertoServidorCentral;
    }

    public String getNombre () {
        return nombre;
    }

    public void setNombre (String nombre) {
        this.nombre = nombre;
    }

    public List<Programa> getProgramas () {
        return programas;
    }

    public void setProgramas (List<Programa> programas) {
        this.programas = programas;
    }

    public InetAddress getDirServidorCentral () {
        return dirServidorCentral;
    }

    public void setDirServidorCentral (InetAddress dirServidorCentral) {
        this.dirServidorCentral = dirServidorCentral;
    }

    public int getPuertoServidorCentral () {
        return puertoServidorCentral;
    }

    public void setPuertoServidorCentral (int puertoServidorCentral) {
        this.puertoServidorCentral = puertoServidorCentral;
    }
}
