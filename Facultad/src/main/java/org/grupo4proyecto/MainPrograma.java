package org.grupo4proyecto;

import org.grupo4proyecto.entidades.Solicitud;
import org.grupo4proyecto.redes.ClientePrograma;
import org.grupo4proyecto.repositorio.ContenedorDatos;
import org.grupo4proyecto.repositorio.RepositorioPrograma;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class MainPrograma {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Uso: <archivo_programas> <semestre> <ipFacultad> <puertoFacultad>");
            return;
        }

        String archivo = args[0];
        int semestre = Integer.parseInt(args[1]);
        String ipFacultad = args[2];
        int puerto = Integer.parseInt(args[3]);

        try {
            ContenedorDatos datos = new ContenedorDatos();
            InputStream input = new FileInputStream(archivo);

            // Cargar todas las solicitudes desde el archivo
            RepositorioPrograma.inicializarCliente(datos, input, semestre);
            List<Solicitud> solicitudes = datos.solicitudes;

            // Enviar cada solicitud a la facultad
            ClientePrograma cliente = new ClientePrograma();
            for (Solicitud solicitud : solicitudes) {
                cliente.enviarSolicitud(ipFacultad, puerto, solicitud);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
