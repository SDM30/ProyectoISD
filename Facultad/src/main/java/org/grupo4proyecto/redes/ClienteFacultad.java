package org.grupo4proyecto.redes;

import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import zmq.ZMQ;

import java.net.InetAddress;


public class ClienteFacultad implements AutoCloseable {
    private final ZContext contexto;
    private final Socket cliente;
    private final Facultad facultad;

    public ClienteFacultad(Facultad facultad) {
        this.contexto = new ZContext();
        this.facultad = facultad;

        InetAddress dirServidor = facultad.getDirServidorCentral();
        int puertoServidor = facultad.getPuertoServidorCentral();

        this.cliente = contexto.createSocket(SocketType.REQ);
        cliente.setIdentity(facultad.getNombre().getBytes(ZMQ.CHARSET));
        cliente.connect("tcp://" + dirServidor.getHostAddress() + ":" + puertoServidor);
    }

    //Metodos para comunicarse con el programa (sincrono)
    public void recibirSolicitudPrograma() {
    }

    public void enviarConfirmacionPrograma() {
    }

    //Metodos para comunicarse con el servidor central (asincrono)
    public void enviarSolicitudServidor(Solicitud solicitud) {
        cliente.sendMore(String.valueOf(solicitud.getSemestre()));
        cliente.sendMore(facultad.getNombre());
        cliente.sendMore(solicitud.getPrograma());
        cliente.sendMore(String.valueOf(solicitud.getNumSalones()));
        cliente.send(String.valueOf(solicitud.getNumLaboratorios()));

        String infoGeneral = cliente.recvStr();
        String labsAsignados = cliente.recvStr();
        String aulasMoviles = cliente.recvStr();
        String salonesAsignados = cliente.recvStr();
        System.out.println("Respuesta del servidor: " + infoGeneral);
    }

    public void recibirRespuestaServidor() {

    }

    public void enviarConfirmacionServidor() {
    }

    @Override
    public void close() {
        if (contexto != null) {
            contexto.close();
        }
    }
}
