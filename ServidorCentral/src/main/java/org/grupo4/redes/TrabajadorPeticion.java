package org.grupo4.redes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo4.entidades.AdministradorInstalaciones;
import org.grupo4.entidades.ResultadoAsignacion;
import org.grupo4.entidades.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.Arrays;

public class TrabajadorPeticion extends Thread{
    private final ZContext contexto;
    private final ZMQ.Socket trabajador;
    private final ObjectMapper json = new ObjectMapper();

    public TrabajadorPeticion(ZContext contexto) {
        this.contexto = contexto;
        this.trabajador = contexto.createSocket(SocketType.REQ);
        trabajador.connect("inproc://backend");
        trabajador.setReceiveTimeOut(1000); // Timeout de 1 segundo
    }


    @Override
    public void run() {
        // 1) Indicar al broker que estoy listo
        trabajador.send("READY");
        System.out.println("[TRABAJADOR] Enviado READY al broker");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 2) Recibir el mensaje multipart del broker:
                // Formato: [workerAddr][empty][clientAddr][empty][request]

                // --- Recepción de frames CON validación ---
                byte[] workerAddr = trabajador.recv();
                if (workerAddr == null) continue; // Timeout: reintentar
                trabajador.recv(); // Frame vacío (ignorar)

                byte[] clientAddr = trabajador.recv();
                if (clientAddr == null) continue;
                trabajador.recv(); // Frame vacío (ignorar)

                byte[] reqBytes = trabajador.recv();
                if (reqBytes == null) continue;

                System.out.println("[TRABAJADOR] Solicitud recibida de cliente: " + Arrays.toString(clientAddr));

                // 3) Procesar la solicitud
                String reqJson = new String(reqBytes, ZMQ.CHARSET);
                Solicitud peticion = json.readValue(reqJson, Solicitud.class);
                String resJson = procesarSolicitud(peticion);

                // 4) Enviar respuesta al broker:
                // Formato: [clientAddr][empty][response]
                trabajador.sendMore(clientAddr);
                trabajador.sendMore("");
                trabajador.send(resJson);

                System.out.println("[TRABAJADOR] Respuesta enviada para cliente: " + Arrays.toString(clientAddr));

            } catch (ZMQException e) {
                if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                    break; // Contexto cerrado, terminar hilo
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String obtenerInfoGeneral (ResultadoAsignacion resultado, Solicitud solicitud) {
        /*
         * Estructura Trama
         * Trama 1: Informacion general
         * Trama 2: Laboratorios asignados
         * Trama 3: Aulas moviles
         * Trama 4: Salones asignados
         */

        String infoGeneral = "[ALERTA] No hay suficientes aulas o laboratorios para responder a la demanda";
        //Caso 1
        if (resultado.esExitoso()) {
            if(resultado.aulaMovilAsignadas() == 0) {
                infoGeneral = String.format("Asignacion exitosa de laboratorios y salones para %s", solicitud.getPrograma());
            } else {
                infoGeneral = String.format("Asignacion exitosa para %s, algunos laboratorios se asignaron como aulas moviles", solicitud.getPrograma());
            }
        }

        return infoGeneral;
    }

    public String procesarSolicitud(Solicitud peticion) {
        try {
            ResultadoAsignacion resultado = AdministradorInstalaciones.getInstance().asignar(
                    peticion.getNumSalones(),
                    peticion.getNumLaboratorios());

            ResultadoEnvio resEnvio = new ResultadoEnvio(
                    obtenerInfoGeneral(resultado, peticion),
                    resultado.labsAsignados(),
                    resultado.aulaMovilAsignadas(),
                    resultado.salonesAsignados());

            return json.writeValueAsString(resEnvio);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
