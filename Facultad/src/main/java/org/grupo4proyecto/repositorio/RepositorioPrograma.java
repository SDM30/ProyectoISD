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

    public static void inicializarCliente(ContenedorDatos contenedor, String ubicacionArchivo) {
        File archivo;
        if (ubicacionArchivo != null) {
            archivo = new File(ubicacionArchivo);
        } else {
            archivo = new File(ubicacionDefecto);
        }

        if (!archivo.exists()) {
            System.err.println("Error: el archivo no existe");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;

            System.out.println("Leyendo archivo " + ubicacionDefecto);
            System.out.println("------------------------------------");

            // Reiniciamos los datos
            contenedor.facultad = new Facultad();
            contenedor.solicitudes.clear();

            try {
                String nombreFacultad = reader.readLine();
                if (nombreFacultad == null || nombreFacultad.isEmpty()) {
                    throw new IllegalArgumentException("Formato Inválido: Falta nombre de facultad");
                }

                contenedor.facultad.setNombre(nombreFacultad);

                for (int i = 0; i < 5; i++) {
                    linea = reader.readLine();
                    if (linea == null || linea.isEmpty()) {
                        throw new IllegalArgumentException("Formato Inválido: Faltan programas académicos");
                    }

                    String[] partes = linea.split(",");
                    if (partes.length != 4) {
                        throw new IllegalArgumentException("Formato Inválido en línea: " + linea);
                    }

                    String nombrePrograma = partes[0].trim();
                    int numSalon = Integer.parseInt(partes[1].trim());
                    int numLab = Integer.parseInt(partes[2].trim());
                    int semestre = Integer.parseInt(partes[3].trim());

                    contenedor.facultad.getProgramas().add(new Programa(nombrePrograma));
                    contenedor.solicitudes.add(new Solicitud(
                            nombreFacultad,
                            nombrePrograma,
                            semestre,
                            numSalon,
                            numLab
                    ));
                }

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                // Reiniciamos en caso de error
                contenedor.facultad = null;
                contenedor.solicitudes.clear();
            }
        } catch (IOException e) {
            System.err.println("Error leyendo archivo: " + e.getMessage());
        }
    }
}