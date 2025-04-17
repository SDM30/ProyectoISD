package org.grupo4proyecto.redes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.ResultadoAsignacion;
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
    private final ObjectMapper json = new ObjectMapper();

    public ClienteFacultad(Facultad facultad) {
        this.contexto = new ZContext();
        this.facultad = facultad;

        InetAddress dirServidor = facultad.getDirServidorCentral();
        int puertoServidor = facultad.getPuertoServidorCentral();

        this.cliente = contexto.createSocket(SocketType.REQ);
        cliente.setIdentity(facultad.getNombre().getBytes(ZMQ.CHARSET));
        cliente.connect("tcp://localhost:5555");
        //cliente.setReceiveTimeOut(5000);
    }

    //Metodos para comunicarse con el programa (sincrono)
    public void recibirSolicitudPrograma() {
    }

    public void enviarConfirmacionPrograma() {
    }

    //Metodos para comunicarse con el servidor central (asincrono)
    public ResultadoEnvio enviarSolicitudServidor(Solicitud solicitud) {
        try {
            String payload = json.writeValueAsString(solicitud);
            System.out.println("[CLIENTE] Enviando solicitud: " + payload);

            cliente.send(payload);
            System.out.println("[CLIENTE] Solicitud enviada, esperando respuesta...");

            byte[] respuestaBytes = cliente.recv();
            if (respuestaBytes == null) {
                System.out.println("[CLIENTE] Timeout: No se recibió respuesta.");
                return null;
            }

            System.out.println("[CLIENTE] Respuesta recibida ");

            return json.readValue(respuestaBytes, ResultadoEnvio.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void recibirRespuestaServidor() {

    }

    public void enviarConfirmacionServidor() {
    }

    @Override
    public void close() {
        contexto.close();
        cliente.close();
    }
}
