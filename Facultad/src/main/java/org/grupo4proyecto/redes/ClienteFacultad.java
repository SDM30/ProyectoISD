package org.grupo4proyecto.redes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import zmq.ZMQ;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class ClienteFacultad implements AutoCloseable, IPChangeListener {
    private final ZContext context;
    private Socket cliente;
    private final Facultad facultad;
    private final ObjectMapper json = new ObjectMapper();
    private List<Solicitud> solicitudesPendientes;

    public ClienteFacultad(Facultad facultad, ZContext context) {
        this.context = context;
        this.facultad = facultad;
        this.solicitudesPendientes = new ArrayList<>();

        this.cliente = context.createSocket(SocketType.REQ);

        String idCliente = facultad.getNombre();
        cliente.setIdentity(idCliente.getBytes(ZMQ.CHARSET));

        // Configurar timeouts
        cliente.setReceiveTimeOut(1000); // 5 segundos timeout para recibir
        cliente.setSendTimeOut(1000);    // 5 segundos timeout para enviar
        cliente.setLinger(0);            // No esperar al cerrar el socket

        System.out.println("[CLIENTE " + idCliente + "] Conectando a broker...");
        System.out.println("[CLIENTE " + idCliente + "] Conectando a servidor central: "
                + facultad.getDirServidorCentral().getHostAddress() + ":" + facultad.getPuertoServidorCentral());
        cliente.connect(
                "tcp://"+facultad.getDirServidorCentral().getHostAddress()
                        +":"+facultad.getPuertoServidorCentral());
    }

    public List<Solicitud> getSolicitudesPendientes() {
        return solicitudesPendientes;
    }

    //Metodos para comunicarse con el servidor central
    public ResultadoEnvio enviarSolicitudServidor(Solicitud solicitud) {
        try {
            String payload = json.writeValueAsString(solicitud);
            System.out.println("[CLIENTE " + facultad.getNombre() + "] Enviando solicitud: " + payload);

            boolean sent = cliente.send(payload, ZMQ.ZMQ_DONTWAIT);
            if (!sent) {
                System.out.println("[CLIENTE] No se pudo enviar la solicitud (DONTWAIT)");
                return null;
            }

            System.out.println("[CLIENTE] Solicitud enviada, esperando respuesta...");

            // Esperar respuesta con timeout
            String respuesta = cliente.recvStr(ZMQ.ZMQ_DONTWAIT);
            int intentos = 0;
            while (respuesta == null && intentos < 3) {
                Thread.sleep(500);  // Esperar 500ms entre intentos
                respuesta = cliente.recvStr(ZMQ.ZMQ_DONTWAIT);
                intentos++;
                System.out.println("[CLIENTE] Intento " + intentos + " de recibir respuesta...");
            }

            if (respuesta == null) {
                System.out.println("[CLIENTE] Timeout esperando respuesta del servidor después de " + intentos + " intentos.");
                return null;
            }

            System.out.println("[CLIENTE] Respuesta recibida: " + respuesta);
            return json.readValue(respuesta, ResultadoEnvio.class);

        } catch (Exception e) {
            System.out.println("[CLIENTE] Error al enviar solicitud: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String confirmarAsignacion(Solicitud solicitud, ResultadoEnvio resultadoEnvio, boolean aceptado) {
        try {
            String mensaje;
            if (aceptado) {
                mensaje = "CONFIRMAR_ASIGNACION:" + solicitud.getPrograma();
            } else {
                mensaje = "RECHAZAR_ASIGNACION:" + solicitud.getPrograma();
            }

            ConfirmacionAsignacion ack = new ConfirmacionAsignacion(mensaje, resultadoEnvio);
            String payload = json.writeValueAsString(ack);
            cliente.send(payload);

            System.out.println("[CLIENTE " + facultad.getNombre() + "] Enviando confirmación: " + payload);

            cliente.setReceiveTimeOut(1000);
            String respuesta = cliente.recvStr();

            if (respuesta == null) {
                System.out.println("[CLIENTE] Timeout esperando confirmación del servidor.");
                return "[CLIENTE] Timeout en la recepción de la confirmación";
            }

            return respuesta;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "[CLIENTE] Error en la recepcion de la confirmacion";
    }

    @Override
    public void onIPChanged(String newIP, String newPort, ZContext context) {
        try {
            System.out.println("[CLIENTE] Reconectando al nuevo servidor: " + newIP);

            // Close old socket and wait
            cliente.close();
            Thread.sleep(1000);  // Esperar 1 segundo antes de reconectar

            // Create new socket with timeouts
            Socket nuevoCliente = context.createSocket(SocketType.REQ);
            String idCliente = facultad.getNombre();
            nuevoCliente.setIdentity(idCliente.getBytes(ZMQ.CHARSET));
            nuevoCliente.setReceiveTimeOut(1000);
            nuevoCliente.setSendTimeOut(1000);
            nuevoCliente.setLinger(0);

            // Conectar al nuevo endpoint
            String newEndpoint = "tcp://" + newIP + ":" + facultad.getPuertoServidorCentral();
            System.out.println("[CLIENTE] Conectando a nuevo endpoint: " + newEndpoint);
            nuevoCliente.connect(newEndpoint);

            // Esperar por conexion
            Thread.sleep(500);

            // Replace the old socket reference
            cliente = nuevoCliente;

            // Reenviar solicitudes pendientes
            List<Solicitud> solicitudesNoResueltas = new ArrayList<>();
            for (Solicitud solicitud : solicitudesPendientes) {
                ResultadoEnvio resultado = enviarSolicitudServidor(solicitud);
                if (resultado == null || resultado.getInfoGeneral().equals("[ALERTA] No hay suficientes aulas o laboratorios para responder a la demanda")) {
                    solicitudesNoResueltas.add(solicitud);
                } else {
                    confirmarAsignacion(solicitud, resultado, true);
                }
            }

            solicitudesPendientes = solicitudesNoResueltas;
            System.out.println("[CLIENTE] Reconexión completada. Solicitudes pendientes restantes: "
                    + solicitudesPendientes.size());

        } catch (Exception e) {
            System.err.println("[CLIENTE] Error durante la reconexión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        cliente.close();
    }
}
