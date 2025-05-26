package org.grupo4proyecto.redes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo4proyecto.entidades.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ClientePrograma {
    private final ObjectMapper mapper = new ObjectMapper();

    public String enviarSolicitud(String ipFacultad, int puertoFacultad, Solicitud solicitud) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://" + ipFacultad + ":" + puertoFacultad);

            String payload = mapper.writeValueAsString(solicitud);
            System.out.println("[PROGRAMA] Enviando solicitud: " + payload);
            socket.send(payload);

            String respuesta = socket.recvStr();
            System.out.println("[PROGRAMA] Respuesta de facultad: " + respuesta);

            return respuesta;
        } catch (Exception e) {
            e.printStackTrace();
            return "[PROGRAMA] Error al enviar solicitud";
        }
    }
}
