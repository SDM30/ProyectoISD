package org.grupo4proyecto;


import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.Solicitud;
import org.grupo4proyecto.redes.ClienteFacultad;
import org.grupo4proyecto.redes.ResultadoEnvio;
import org.grupo4proyecto.repositorio.ContenedorDatos;
import org.grupo4proyecto.repositorio.RepositorioPrograma;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main (String[] args) {
        ContenedorDatos datos = new ContenedorDatos();

        if (interpreteArgumentos (args, datos)) {
            Facultad facultad = datos.facultad;
            List<Solicitud> solicitudes = datos.solicitudes;
            ResultadoEnvio res = null;

            System.out.println (facultad.toString());
            System.out.println (solicitudes.toString());


            try (ClienteFacultad clienteFacultad = new ClienteFacultad(datos.facultad)) {
                   res = clienteFacultad.enviarSolicitudServidor(solicitudes.get(0));
                   System.out.println(res);
            }
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
            cargarConfiguracionServidor(datos.facultad);
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

    public static void cargarConfiguracionServidor(Facultad facultad) {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/configCliente.properties")) {

            // Cargar archivo de propiedades
            prop.load(input);

            // Obtener y validar dirección IP
            String ip = prop.getProperty("server.ip", "localhost");
            try {
                InetAddress direccion = InetAddress.getByName(ip);
                facultad.setDirServidorCentral(direccion);
            } catch (UnknownHostException e) {
                System.err.println("Dirección IP inválida en configuración, usando localhost");
                facultad.setDirServidorCentral(InetAddress.getLoopbackAddress());
            }

            // Obtener y validar puerto
            String puerto = prop.getProperty("server.port", "5555");
            try {
                facultad.setPuertoServidorCentral(Integer.parseInt(puerto));
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido en configuración, usando 5555");
                facultad.setPuertoServidorCentral(5555);
            }

        } catch (IOException e) {
            System.err.println("No se encontró configCliente.properties, usando valores por defecto");
            // Valores por defecto
            facultad.setDirServidorCentral(InetAddress.getLoopbackAddress());
            facultad.setPuertoServidorCentral(5555);
        }
    }

}