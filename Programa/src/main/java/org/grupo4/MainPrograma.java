package org.grupo4;

import org.grupo4.entidades.Facultad;
import org.grupo4.entidades.Solicitud;
import org.grupo4.redes.ClientePrograma;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class MainPrograma {
    public static void main(String[] args) {
        String ipFacultad;
        int puertoFacultad;
        int semestre;
        int numSalones;
        int numLaboratorios;
        String nombrePrograma;

        if(args.length == 6) {
            ipFacultad = args[0];
            puertoFacultad = Integer.parseInt(args[1]);
            semestre = Integer.parseInt(args[2]);
            numSalones = Integer.parseInt(args[3]);
            numLaboratorios = Integer.parseInt(args[4]);
            nombrePrograma = args[5];
        } else {
            Properties props = new Properties();
            try (InputStream in = MainPrograma.class.getClassLoader().getResourceAsStream("configPrograma.properties")) {
                if(in == null) {
                    System.err.println("Configuration file not found.");
                    return;
                }
                props.load(in);
            } catch (IOException e) {
                System.err.println("Error reading the configuration: " + e.getMessage());
                return;
            }

            ipFacultad = props.getProperty("ip.facultad");
            puertoFacultad = Integer.parseInt(props.getProperty("puerto.facultad"));
            semestre = Integer.parseInt(props.getProperty("semestre"));
            numSalones = Integer.parseInt(props.getProperty("num.salones"));
            numLaboratorios = Integer.parseInt(props.getProperty("num.laboratorios"));
            nombrePrograma = props.getProperty("nombre.programa", "Ingeniería de Sistemas");
        }

        try {
            InetAddress dirIP = InetAddress.getByName(ipFacultad);
            Facultad servidorFacultad = new Facultad("Facultad Central", dirIP, puertoFacultad);
            System.out.println("Facultad creada: " + servidorFacultad);

            Solicitud solicitud = new Solicitud("Facultad Central", nombrePrograma, semestre, numSalones, numLaboratorios);
            System.out.println("Solicitud creada: " + solicitud);

            ClientePrograma cliente = new ClientePrograma(servidorFacultad);
            cliente.enviarSolicitud(solicitud);

        } catch (UnknownHostException e) {
            System.err.println("Error al crear la dirección IP: " + e.getMessage());
        }
    }
}