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

        String idCliente = facultad.getNombre();
        cliente.setIdentity(idCliente.getBytes(ZMQ.CHARSET));

        System.out.println("[CLIENTE " + idCliente + "] Conectando a broker...");
        cliente.connect("tcp://localhost:5555");
    }

    //Metodos para comunicarse con el servidor central
    public ResultadoEnvio enviarSolicitudServidor(Solicitud solicitud) {
        try {
            String payload = json.writeValueAsString(solicitud);
            System.out.println("[CLIENTE " + facultad.getNombre() + "] Enviando solicitud: " + payload);

            cliente.send(payload);
            System.out.println("[CLIENTE] Solicitud enviada, esperando respuesta...");

            String respuesta = cliente.recvStr();

            System.out.println("[CLIENTE] Respuesta recibida: " + respuesta);

            return json.readValue(respuesta, ResultadoEnvio.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {
        contexto.close();
        cliente.close();
    }
}
