package org.grupo4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo4.entidades.AdministradorInstalaciones;
import org.grupo4.entidades.Solicitud;
import org.grupo4.redes.ConfirmacionAsignacion;
import org.grupo4.redes.ResultadoEnvio;
import org.grupo4.redes.TrabajadorPeticion;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Main {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            Socket frontend = context.createSocket(SocketType.ROUTER);
            Socket backend = context.createSocket(SocketType.ROUTER);
            frontend.bind("tcp://*:5555");
            backend.bind("inproc://backend");

            System.out.println("[BROKER] Iniciado en tcp://*:5556");
            System.out.println("[BROKER] Lanzando trabajadores...");

            for (int i = 1; i <= 5; i++) {
                String workerId = String.valueOf(i);
                new Thread(new TrabajadorPeticion(context, workerId)).start();
                System.out.println("[BROKER] Trabajador " + workerId + " iniciado");
            }

            Queue<String> workerQueue = new LinkedList<>();

            while (!Thread.currentThread().isInterrupted()) {
                Poller poller = context.createPoller(2);
                poller.register(backend, Poller.POLLIN);

                if (!workerQueue.isEmpty()) {
                    poller.register(frontend, Poller.POLLIN);
                    System.out.println("[BROKER] Trabajadores disponibles: " + workerQueue.size());
                    System.out.println("[BROKER] Estado recursos: " + AdministradorInstalaciones.getInstance().getEstadisticas());
                }

                int events = poller.poll(1000);
                if (events == 0) continue;

                // Manejar trabajadores
                if (poller.pollin(0)) {
                    String workerAddr = backend.recvStr();
                    backend.recv();
                    String command = backend.recvStr();

                    if ("READY".equals(command)) {
                        workerQueue.add(workerAddr);
                        System.out.println("[BROKER] Trabajador " + workerAddr + " marcado como listo");
                    } else {
                        String clientAddr = command;
                        backend.recv();
                        String response = backend.recvStr();

                        System.out.println("[BROKER] Reenviando respuesta a cliente " + clientAddr
                                + " desde trabajador " + workerAddr);

                        frontend.sendMore(clientAddr);
                        frontend.sendMore("");
                        frontend.send(response);
                        workerQueue.add(workerAddr);
                    }
                }

                // Manejar clientes
                if (poller.pollin(1)) {
                    String clientAddr = frontend.recvStr();
                    frontend.recv();
                    String request = frontend.recvStr();

                    recepcionClienteFacultad(clientAddr, request, backend, frontend, workerQueue);

                }
            }
        }
    }


    public static void recepcionClienteFacultad(String clientAddr, String requestJson,
                                                Socket backend, Socket frontend,
                                                Queue<String> workerQueue) {
        try {

            // Validar que el JSON no sea nulo o vacío
            if (requestJson == null || requestJson.trim().isEmpty()) {
                System.err.println("[BROKER] Error: Mensaje JSON vacío o nulo");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();

            // 1. Primer intento: Deserializar como ConfirmacionAsignacion
            try {
                ConfirmacionAsignacion confirmacion = mapper.readValue(requestJson, ConfirmacionAsignacion.class);

                boolean exito = false;
                String respuesta = null;

                switch(confirmacion.getEncabezado().split(":")[0]) {

                    case "CONFIRMAR_ASIGNACION":
                        System.out.println("[BROKER] Confirmación recibida de " + clientAddr);
                        // Enviar respuesta simple
                        respuesta = "CONFIRMADO ACEPTACION";
                        break;

                    case "RECHAZAR_ASIGNACION":
                        System.out.println("[BROKER] Rechazo recibido de " + clientAddr);
                        exito = AdministradorInstalaciones.getInstance().devolverRecursos(confirmacion.getResEnvio());
                        // Enviar respuesta simple
                        respuesta = "CONFIRMADO RECHAZO";
                        break;
                }

                frontend.sendMore(clientAddr);
                frontend.sendMore("");
                frontend.send(respuesta);
                return;

            } catch (JsonProcessingException e) {
                System.out.println("[DEBUG] No es mensaje de confirmación: " + e.getStackTrace());
            }

            // 2. Segundo intento: Deserializar como Solicitud
            try {
                Solicitud solicitud = mapper.readValue(requestJson, Solicitud.class);

                // Lógica para otras solicitudes
                String workerAddr = workerQueue.poll();
                System.out.println("[BROKER] Solicitud genérica de " + clientAddr + " asignada a " + workerAddr);

                backend.sendMore(workerAddr);
                backend.sendMore("");
                backend.sendMore(clientAddr);
                backend.sendMore("");
                backend.send(requestJson);
                return;
            } catch (JsonProcessingException e) {
                System.out.println("[DEBUG] No es solicitud de recursos: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("[BROKER] Error crítico: " + e.getMessage());
        }
    }
}