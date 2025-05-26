package org.grupo4proyecto.entidades;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Facultad {
    private String nombre;
    private List<Programa> programas;
    private InetAddress dirServidorCentral;
    private int puertoServidorCentral;
    private int puertoParaProgramas;



    public Facultad (String nombre, List<Programa> programas, InetAddress dirServidorCentral, int puertoServidorCentral, int puertoParaProgramas) {
        this.nombre = nombre;
        this.programas = programas;
        this.dirServidorCentral = dirServidorCentral;
        this.puertoServidorCentral = puertoServidorCentral;
        this.puertoParaProgramas = puertoParaProgramas;
    }

    public Facultad () {
        this.nombre = null;
        this.programas = new ArrayList<Programa> ();
        this.dirServidorCentral = null;
        puertoServidorCentral = 0;
        puertoParaProgramas = 0;
    }
    public int getPuertoParaProgramas() {
        return puertoParaProgramas;
    }

    public void setPuertoParaProgramas(int puertoParaProgramas) {
        this.puertoParaProgramas = puertoParaProgramas;
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

    @Override
    public String toString () {
        return "Facultad{" +
                "nombre='" + nombre + '\'' +
                ", programas=" + programas +
                ", dirServidorCentral=" + dirServidorCentral +
                ", puertoServidorCentral=" + puertoServidorCentral +
                ", puertoParaPrograma= "+ puertoParaProgramas +
                '}';
    }

}
