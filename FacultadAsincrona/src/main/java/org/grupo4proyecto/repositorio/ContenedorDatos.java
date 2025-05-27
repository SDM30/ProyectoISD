package org.grupo4proyecto.repositorio;

import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.Solicitud;

import java.util.ArrayList;
import java.util.List;

public class ContenedorDatos {
    public Facultad facultad;
    public List<Solicitud> solicitudes;

    public String ipServidorFacultad;
    public int puertoServidorFacultad;

    public ContenedorDatos() {
        this.facultad = new Facultad();
        this.solicitudes = new ArrayList<>();

        // Valores por defecto para el servidor de facultad
        this.ipServidorFacultad = "127.0.0.1";
        this.puertoServidorFacultad = 5554;
    }

    public ContenedorDatos(Facultad facultad, List<Solicitud> solicitudes) {
        this.facultad = facultad;
        this.solicitudes = solicitudes;

        // Valores por defecto para el servidor de facultad
        this.ipServidorFacultad = "127.0.0.1";
        this.puertoServidorFacultad = 5554;
    }

    public ContenedorDatos(Facultad facultad, List<Solicitud> solicitudes, String ipServidorFacultad, int puertoServidorFacultad) {
        this.facultad = facultad;
        this.solicitudes = solicitudes;
        this.ipServidorFacultad = ipServidorFacultad;
        this.puertoServidorFacultad = puertoServidorFacultad;
    }

    @Override
    public String toString() {
        return "ContenedorDatos{" +
                "facultad=" + facultad +
                ", solicitudes=" + solicitudes +
                ", ipServidorFacultad='" + ipServidorFacultad + '\'' +
                ", puertoServidorFacultad=" + puertoServidorFacultad +
                '}';
    }
}