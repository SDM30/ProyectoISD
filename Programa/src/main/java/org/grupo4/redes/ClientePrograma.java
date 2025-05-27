package org.grupo4.redes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo4.entidades.Facultad;
import org.grupo4.entidades.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.FileWriter;
import java.io.IOException;

public class ClientePrograma {
    private final Facultad facultad;
    private final ObjectMapper json;

    public ClientePrograma(Facultad facultad) {
        this.facultad = facultad;
        this.json = new ObjectMapper();
    }

    public void enviarSolicitud(Solicitud solicitud) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);

            // Configure socket timeouts
            socket.setReceiveTimeOut(5000);  // 5 seconds
            socket.setSendTimeOut(5000);     // 5 seconds

            String endpoint = "tcp://" + facultad.getDirFacultad().getHostAddress() + ":" + facultad.getPuertoFacutad();
            socket.connect(endpoint);

            // Convert solicitud to JSON
            String jsonSolicitud = json.writeValueAsString(solicitud);

            // Send request
            boolean sent = socket.send(jsonSolicitud.getBytes(ZMQ.CHARSET), 0);
            if (!sent) {
                throw new IOException("Failed to send request");
            }

            // Receive response
            byte[] reply = socket.recv(0);
            if (reply != null) {
                String jsonResponse = new String(reply, ZMQ.CHARSET);
                ResultadoEnvio resultado = json.readValue(jsonResponse, ResultadoEnvio.class);
                guardarRespuesta(resultado, solicitud.getPrograma());
            } else {
                throw new IOException("No response received from server");
            }

        } catch (Exception e) {
            System.err.println("Error en la comunicación: " + e.getMessage());
        }
    }

    private void guardarRespuesta(ResultadoEnvio resultado, String nombrePrograma) {
        try (FileWriter writer = new FileWriter(nombrePrograma + ".txt")) {
            writer.write(resultado.toString());
        } catch (IOException e) {
            System.err.println("Error al guardar la respuesta: " + e.getMessage());
        }
    }
}