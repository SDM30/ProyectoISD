package org.grupo4proyecto;


import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.Solicitud;
import org.grupo4proyecto.repositorio.ContenedorDatos;
import org.grupo4proyecto.repositorio.RepositorioPrograma;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main (String[] args) {
        ContenedorDatos datos = new ContenedorDatos();

        if (interpreteArgumentos (args, datos)) {
            Facultad facultad = datos.facultad;
            List<Solicitud> solicitudes = datos.solicitudes;

            System.out.println (facultad.toString());
            System.out.println (solicitudes.toString());
        }
    }

    public static boolean interpreteArgumentos(String[] args, ContenedorDatos datos) {
        if (args.length >= 2) {
            try {
                int puerto = Integer.parseInt(args[1]);
                if (validarInfoServidor(args[0], puerto)) {
                    InetAddress dirIP = InetAddress.getByName(args[0]);
                    datos.facultad.setDirServidorCentral(dirIP);
                    datos.facultad.setPuertoServidorCentral(puerto);
                    RepositorioPrograma.inicializarCliente(datos, null);

                    if (args.length == 3) {
                        RepositorioPrograma.inicializarCliente(datos, args[2]);
                    }
                    return true;

                } else {
                    System.out.println ("El formato de los parametros es: <Direccion IPv4> <Puerto Servidor> <(OPCIONAL) archivo con programas>");
                    System.out.println ("Si se deja vacio se carga la configuracion por defecto");
                }

            } catch (NumberFormatException e) {
                System.err.println("Error: Puerto debe ser numérico");
            } catch (UnknownHostException e) {
                System.err.println("Error: Dirección IP no válida");
            }
            return false;

        } else if (args.length == 0) {
            RepositorioPrograma.inicializarCliente(datos, null);
            return true;
        }
        return false;
    }

    public static boolean validarInfoServidor(String dirIP, int numeroPuerto) {
        try {
            // Validación de IP
            InetAddress direccion = InetAddress.getByName(dirIP);
            if (!(direccion instanceof Inet4Address)) {
                System.err.println("Error: Se requiere dirección IPv4");
                return false;
            }

            // Validación de puerto
            if (numeroPuerto < 1 || numeroPuerto > 65535) {
                System.err.println("Error: Puerto inválido (1-65535)");
                return false;
            }

            return true;

        } catch (UnknownHostException e) {
            System.err.println("Error: Formato de IP inválido");
            return false;
        }
    }

}