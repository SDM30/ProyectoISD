package org.grupo4proyecto.entidades;

import org.grupo4proyecto.redes.SuscriptorFacultad;
import org.zeromq.ZContext;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Facultad {
    private String nombre;
    private List<Programa> programas;
    private InetAddress dirServidorCentral;
    private int puertoServidorCentral;
    private SuscriptorFacultad suscriptor;

    public Facultad (String nombre, List<Programa> programas, InetAddress dirServidorCentral, int puertoServidorCentral) {
        this.nombre = nombre;
        this.programas = programas;
        this.dirServidorCentral = dirServidorCentral;
        this.puertoServidorCentral = puertoServidorCentral;
    }

    public Facultad () {
        this.nombre = null;
        this.programas = new ArrayList<Programa> ();
        this.dirServidorCentral = null;
        puertoServidorCentral = 0;
        this.suscriptor = new SuscriptorFacultad("","");
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
                '}';
    }

    public void setIpHealthcheck(String ip) {
        suscriptor.setIpHealthcheck(ip);
    }

    public void setPuertoHealthcheck(int puerto) {
        suscriptor.setPuertoHealthcheck(String.valueOf(puerto));
    }

    public void iniciarSuscriptor(ZContext context) {
        Thread subThread = new Thread(() -> suscriptor.recibirMensajes(context));
        subThread.setDaemon(true);
        subThread.start();
    }

    public SuscriptorFacultad getSuscriptor() {
        return suscriptor;
    }
}
