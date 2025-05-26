package org.grupo4.asincrono.servidor;

import java.util.HashMap;
import java.util.Map;

import org.grupo4.asincrono.modelo.ResultadoAsignacion;
import org.grupo4.asincrono.modelo.Solicitud;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ServidorAsincrono {
    private final Map<String, Integer> aulasDisponibles = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ServidorAsincrono() {
        aulasDisponibles.put("salones", 380);
        aulasDisponibles.put("laboratorios", 60);
    }

    public void iniciar() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket router = context.createSocket(ZMQ.ROUTER);
            router.bind("tcp://*:5555");
            System.out.println("[SERVIDOR] Iniciado en puerto 5555 (ROUTER)");

            while (!Thread.currentThread().isInterrupted()) {
                ZMsg msg = ZMsg.recvMsg(router);
                if (msg == null) break;

                String identidad = msg.popString();
                msg.pop(); // frame vacío
                String jsonSolicitud = msg.popString();

                Solicitud solicitud = mapper.readValue(jsonSolicitud, Solicitud.class);
                System.out.println("[SERVIDOR] Solicitud de " + solicitud.getFacultad());

                        new Thread(() -> {
    try {
        ResultadoAsignacion resultado = procesarSolicitud(solicitud);
        String jsonRespuesta = mapper.writeValueAsString(resultado);

        ZMsg respuesta = new ZMsg();
        respuesta.add(identidad);
        respuesta.add("");
        respuesta.add(jsonRespuesta.getBytes(ZMQ.CHARSET));
        respuesta.send(router);

        System.out.println("[SERVIDOR] Solicitud de " + solicitud.getFacultad() +
            " procesada en hilo: " + Thread.currentThread().getName());

    } catch (Exception e) {
        e.printStackTrace();
    }
}).start();

                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ResultadoAsignacion procesarSolicitud(Solicitud solicitud) {
        ResultadoAsignacion resultado = new ResultadoAsignacion();
        synchronized (aulasDisponibles) {
            boolean asignado = true;

            if (aulasDisponibles.get("salones") >= solicitud.getSalones()) {
                resultado.setSalonesAsignados(solicitud.getSalones());
                aulasDisponibles.put("salones", aulasDisponibles.get("salones") - solicitud.getSalones());
            } else {
                asignado = false;
            }

            if (aulasDisponibles.get("laboratorios") >= solicitud.getLaboratorios()) {
                resultado.setLaboratoriosAsignados(solicitud.getLaboratorios());
                aulasDisponibles.put("laboratorios", aulasDisponibles.get("laboratorios") - solicitud.getLaboratorios());
            } else {
                asignado = false;
            }

            if (!asignado) {
                resultado.setMensaje("[ALERTA] No hay suficientes recursos para " + solicitud.getFacultad());
            } else {
                resultado.setMensaje("Asignación exitosa a " + solicitud.getFacultad());
            }

             System.out.println("[SERVIDOR] Recursos restantes: " +
            aulasDisponibles.get("salones") + " salones, " +
            aulasDisponibles.get("laboratorios") + " laboratorios");

        }
        return resultado;
    }
}
