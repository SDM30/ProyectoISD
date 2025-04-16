package org.grupo4proyecto;


import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.Solicitud;
import org.grupo4proyecto.repositorio.RepositorioPrograma;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main (String[] args) {
        Facultad facultad = null;
        List<Solicitud> solicitudes = null;

        RepositorioPrograma.inicializarCliente (facultad, solicitudes);
    }

    public static void interfazFacultad (String[] args) {
        Scanner scan = new Scanner(System.in);
        //Opcion 1: Crear las facultades, desde otro archivo de texto

        //Opcion 2: cargarlas desde el archivo por defecto
        if (args.length == 0) {

        }
    }
}