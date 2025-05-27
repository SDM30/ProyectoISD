package org.grupo4proyecto.redes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo4proyecto.entidades.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import zmq.ZMQ;

import java.net.InetAddress;

public class ServidorFacultad implements AutoCloseable {
    private final ZContext context;
    private Socket servidor;
    private final ObjectMapper json = new ObjectMapper();
    private final String ipServidor;
    private final int puertoServidor;
    private volatile boolean ejecutando = false;
    private ClienteFacultad clienteFacultad;

    public ServidorFacultad(String ipServidor, int puertoServidor, ZContext context) {
        this.context = context;
        this.ipServidor = ipServidor;
        this.puertoServidor = puertoServidor;

        this.servidor = context.createSocket(SocketType.REP);

        // Configurar timeouts
        servidor.setReceiveTimeOut(1000); // 1 segundo timeout para recibir
        servidor.setSendTimeOut(1000);    // 1 segundo timeout para enviar
        servidor.setLinger(0);            // No esperar al cerrar el socket

        try {
            String endpoint = "tcp://" + ipServidor + ":" + puertoServidor;
            servidor.bind(endpoint);
            System.out.println("[SERVIDOR FACULTAD] Servidor iniciado en: " + endpoint);
        } catch (Exception e) {
            System.err.println("[SERVIDOR FACULTAD] Error al iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setClienteFacultad(ClienteFacultad clienteFacultad) {
        this.clienteFacultad = clienteFacultad;
    }

    public void iniciarServidor() {
        ejecutando = true;
        System.out.println("[SERVIDOR FACULTAD] Esperando solicitudes de programas...");

        while (ejecutando) {
            try {
                // Recibir solicitud de programa
                String mensajeRecibido = servidor.recvStr(ZMQ.ZMQ_DONTWAIT);

                if (mensajeRecibido != null) {
                    System.out.println("[SERVIDOR FACULTAD] Solicitud recibida: " + mensajeRecibido);

                    try {
                        // Deserializar la solicitud
                        Solicitud solicitud = json.readValue(mensajeRecibido, Solicitud.class);

                        // Procesar la solicitud a través del cliente
                        ResultadoEnvio resultado = procesarSolicitud(solicitud);

                        // Enviar respuesta al programa
                        String respuesta;
                        if (resultado != null) {
                            respuesta = json.writeValueAsString(resultado);
                            System.out.println("[SERVIDOR FACULTAD] Enviando respuesta exitosa al programa");
                        } else {
                            // Crear respuesta de error
                            ResultadoEnvio errorResult = new ResultadoEnvio();
                            errorResult.setInfoGeneral("[ERROR] No se pudo procesar la solicitud");
                            respuesta = json.writeValueAsString(errorResult);
                            System.out.println("[SERVIDOR FACULTAD] Enviando respuesta de error al programa");
                        }

                        boolean enviado = servidor.send(respuesta);
                        if (!enviado) {
                            System.err.println("[SERVIDOR FACULTAD] Error al enviar respuesta");
                        }

                    } catch (Exception e) {
                        System.err.println("[SERVIDOR FACULTAD] Error procesando solicitud: " + e.getMessage());
                        e.printStackTrace();

                        // Enviar respuesta de error
                        try {
                            ResultadoEnvio errorResult = new ResultadoEnvio();
                            errorResult.setInfoGeneral("[ERROR] Error interno del servidor: " + e.getMessage());
                            String errorResponse = json.writeValueAsString(errorResult);
                            servidor.send(errorResponse);
                        } catch (Exception sendError) {
                            System.err.println("[SERVIDOR FACULTAD] Error enviando respuesta de error: " + sendError.getMessage());
                        }
                    }
                } else {
                    // No hay mensajes, esperar un poco
                    Thread.sleep(100);
                }

            } catch (InterruptedException e) {
                System.out.println("[SERVIDOR FACULTAD] Servidor interrumpido");
                break;
            } catch (Exception e) {
                if (ejecutando) {
                    System.err.println("[SERVIDOR FACULTAD] Error en el bucle del servidor: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("[SERVIDOR FACULTAD] Servidor detenido");
    }

    private ResultadoEnvio procesarSolicitud(Solicitud solicitud) {
        if (clienteFacultad == null) {
            System.err.println("[SERVIDOR FACULTAD] ClienteFacultad no configurado");
            return null;
        }

        try {
            System.out.println("[SERVIDOR FACULTAD] Reenviando solicitud al servidor central: " + solicitud.getPrograma());

            // Enviar solicitud al servidor central a través del cliente
            ResultadoEnvio resultado = clienteFacultad.enviarSolicitudServidor(solicitud);

            if (resultado == null) {
                System.out.println("[SERVIDOR FACULTAD] No se recibió respuesta del servidor central");
                return null;
            }

            if (resultado.getInfoGeneral().contains("[ALERTA]")) {
                System.out.println("[SERVIDOR FACULTAD] Alerta del servidor central: " + resultado.getInfoGeneral());
                return resultado;
            }

            // Confirmar la asignación automáticamente
            String confirmacion = clienteFacultad.confirmarAsignacion(solicitud, resultado, true);
            System.out.println("[SERVIDOR FACULTAD] Confirmación enviada: " + confirmacion);

            return resultado;

        } catch (Exception e) {
            System.err.println("[SERVIDOR FACULTAD] Error procesando solicitud: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void detenerServidor() {
        ejecutando = false;
        System.out.println("[SERVIDOR FACULTAD] Deteniendo servidor...");
    }

    public boolean isEjecutando() {
        return ejecutando;
    }

    @Override
    public void close() {
        detenerServidor();
        if (servidor != null) {
            servidor.close();
        }
    }
}