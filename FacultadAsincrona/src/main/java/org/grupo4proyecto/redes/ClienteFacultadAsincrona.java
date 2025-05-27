package org.grupo4proyecto.redes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import zmq.ZMQ;

import java.util.ArrayList;
import java.util.List;

public class ClienteFacultadAsincrona implements AutoCloseable, IPChangeListener {
    private final ZContext context;
    private Socket cliente;
    private final Facultad facultad;
    private final ObjectMapper json = new ObjectMapper();
    private List<Solicitud> solicitudesPendientes;

    public ClienteFacultadAsincrona(Facultad facultad, ZContext context) {
        this.context = context;
        this.facultad = facultad;
        this.solicitudesPendientes = new ArrayList<>();

        // Cambio de REQ a DEALER
        this.cliente = context.createSocket(SocketType.DEALER);

        String idCliente = facultad.getNombre();
        cliente.setIdentity(idCliente.getBytes(ZMQ.CHARSET));

        // Misma configuración de timeouts
        cliente.setReceiveTimeOut(1000);
        cliente.setSendTimeOut(1000);
        cliente.setLinger(0);

        System.out.println("[CLIENTE " + idCliente + "] Conectando al servidor...");
        System.out.println("[CLIENTE " + idCliente + "] Conectando a servidor central: "
                + facultad.getDirServidorCentral().getHostAddress() + ":" + facultad.getPuertoServidorCentral());
        cliente.connect(
                "tcp://" + facultad.getDirServidorCentral().getHostAddress()
                        + ":" + facultad.getPuertoServidorCentral());
    }

    public List<Solicitud> getSolicitudesPendientes() {
        return solicitudesPendientes;
    }

    public ResultadoEnvio enviarSolicitudServidor(Solicitud solicitud) {
        try {
            String payload = json.writeValueAsString(solicitud);
            System.out.println("[CLIENTE " + facultad.getNombre() + "] Enviando solicitud: " + payload);

            // En DEALER, necesitamos enviar un frame vacío primero
            cliente.sendMore("");
            boolean sent = cliente.send(payload, ZMQ.ZMQ_DONTWAIT);
            if (!sent) {
                System.out.println("[CLIENTE] No se pudo enviar la solicitud (DONTWAIT)");
                return null;
            }

            System.out.println("[CLIENTE] Solicitud enviada, esperando respuesta...");

            // Esperar respuesta con timeout
            String respuesta = null;
            int intentos = 0;
            while (respuesta == null && intentos < 3) {
                // Primero recibimos el frame vacío
                String empty = cliente.recvStr(ZMQ.ZMQ_DONTWAIT);
                if (empty != null) {
                    // Luego recibimos el payload real
                    respuesta = cliente.recvStr(ZMQ.ZMQ_DONTWAIT);
                }
                if (respuesta == null) {
                    Thread.sleep(500);
                    intentos++;
                    System.out.println("[CLIENTE] Intento " + intentos + " de recibir respuesta...");
                }
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

            // En DEALER, enviar frame vacío primero
            cliente.sendMore("");
            cliente.send(payload);

            System.out.println("[CLIENTE " + facultad.getNombre() + "] Enviando confirmación: " + payload);

            cliente.setReceiveTimeOut(1000);
            // Recibir frame vacío primero
            String empty = cliente.recvStr();
            if (empty == null) {
                System.out.println("[CLIENTE] Timeout esperando confirmación del servidor.");
                return "[CLIENTE] Timeout en la recepción de la confirmación";
            }
            // Recibir respuesta real
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

            cliente.close();
            Thread.sleep(1000);

            // Crear nuevo socket DEALER
            Socket nuevoCliente = context.createSocket(SocketType.DEALER);
            String idCliente = facultad.getNombre();
            nuevoCliente.setIdentity(idCliente.getBytes(ZMQ.CHARSET));
            nuevoCliente.setReceiveTimeOut(1000);
            nuevoCliente.setSendTimeOut(1000);
            nuevoCliente.setLinger(0);

            String newEndpoint = "tcp://" + newIP + ":" + facultad.getPuertoServidorCentral();
            System.out.println("[CLIENTE] Conectando a nuevo endpoint: " + newEndpoint);
            nuevoCliente.connect(newEndpoint);

            Thread.sleep(500);

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
