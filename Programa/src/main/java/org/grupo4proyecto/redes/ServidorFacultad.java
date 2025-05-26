package org.grupo4proyecto.redes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo4proyecto.entidades.Solicitud;
import org.grupo4proyecto.entidades.Facultad;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


public class ServidorFacultad implements Runnable {
    private final int puerto;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Facultad facultad;

    public ServidorFacultad(Facultad facultad) {
        this.puerto = facultad.getPuertoParaProgramas();
        this.facultad = facultad;
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext();
             ZMQ.Socket socket = context.createSocket(SocketType.REP);
             ClienteFacultad clienteFacultad = new ClienteFacultad(facultad)) {

            socket.bind("tcp://*:" + puerto);
            System.out.println("[FACULTAD] Esperando solicitudes de programas en puerto " + puerto);

            while (!Thread.currentThread().isInterrupted()) {
                String mensaje = socket.recvStr();
                System.out.println("[FACULTAD] Solicitud recibida de programa: " + mensaje);

                // Parsear solicitud
                Solicitud solicitud = mapper.readValue(mensaje, Solicitud.class);

                // Reenviar solicitud al Servidor Central
                ResultadoEnvio resultado = clienteFacultad.enviarSolicitudServidor(solicitud);

                if (resultado == null) {
                    String error = "[FACULTAD] Error al contactar al servidor central";
                    System.out.println(error);
                    socket.send(error);
                } else {
                    String respuesta = mapper.writeValueAsString(resultado);
                    System.out.println("[FACULTAD] Respuesta del servidor central enviada al programa");
                    socket.send(respuesta);  // Responder al programa
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}