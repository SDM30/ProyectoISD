package org.grupo4proyecto.repositorio;

import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.Programa;
import org.grupo4proyecto.entidades.Solicitud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RepositorioPrograma {
    private static final String ubicacionDefecto = "src/main/resources/programasDefecto.txt";

    public static void inicializarCliente(Facultad facultad, List<Solicitud> solicitudes) {
        File archivo = new File (ubicacionDefecto);

        facultad = new Facultad ();
        solicitudes = new ArrayList<Solicitud> ();

        if (!archivo.exists()) {
            System.err.println ("Error: el archivo no existe");
            return;
        }

        try (BufferedReader reader = new BufferedReader (new FileReader (archivo))) {
            String linea;

            System.out.println ("Leyendo archivo " + ubicacionDefecto);
            System.out.println ("------------------------------------");

            try {
                //Crear facultades, programas y solicitudes
                String nombreFacultad;
                linea = reader.readLine ();

                if (linea != null) {
                    nombreFacultad = linea;
                } else {
                    throw new IllegalArgumentException ("Formato Invalido: No hay un nombre para la facultad en el archivo");
                }

                //Crear facultad
                facultad.setNombre(nombreFacultad);

                for (int i = 0; i < 5; i++) {
                    linea = reader.readLine ();
                    if (linea.isEmpty()) {
                        throw new IllegalArgumentException ("Formato Invalido: No hay registros para 5 programas academicos");
                    }

                    String[] partes = linea.split (",");

                    if (partes.length != 3) {
                        throw new IllegalArgumentException ("Formato Invalido: la solicitud del programa no es valida");
                    }

                    String nombrePrograma = partes[0].trim();
                    int solicitudNumSalon = Integer.parseInt(partes[1].trim());
                    int solicitudNumLab = Integer.parseInt(partes[2].trim());

                    //Crear programa y agregarlo a la facultad
                    facultad.getProgramas().add(new Programa(nombrePrograma));

                    //Crear solicitudes
                    solicitudes.add(new Solicitud (nombreFacultad, nombrePrograma, solicitudNumSalon, solicitudNumLab));
                }

            } catch (Exception e) {
                System.err.printf (e.getMessage ());
            }
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
        }

        // Imprimir informacion de facultad
        System.out.println (facultad.toString());
        System.out.println (solicitudes.toString ());
    }
}
