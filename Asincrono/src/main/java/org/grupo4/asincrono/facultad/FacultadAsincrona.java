package org.grupo4.asincrono.facultad;

import org.grupo4.asincrono.modelo.ResultadoAsignacion;
import org.grupo4.asincrono.modelo.Solicitud;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FacultadAsincrona {
    public void ejecutar(String nombre, int salones, int labs) throws Exception {
        Solicitud solicitud = new Solicitud(nombre, salones, labs);
        ObjectMapper mapper = new ObjectMapper();

        try (ZContext context = new ZContext()) {
            ZMQ.Socket dealer = context.createSocket(ZMQ.DEALER);
            dealer.setIdentity(nombre.getBytes(ZMQ.CHARSET));
            dealer.connect("tcp://localhost:5555");
            System.out.println("[" + nombre + "] Conectado al servidor");

            String jsonSolicitud = mapper.writeValueAsString(solicitud);
            ZMsg msg = new ZMsg();
            msg.add("");
            msg.add(jsonSolicitud);
            msg.send(dealer);

            ZMsg respuesta = ZMsg.recvMsg(dealer);
            respuesta.pop(); // frame vacío
            
            // Cambio aquí: obtener el frame y convertirlo correctamente
            ZFrame frame = respuesta.pop();
            String jsonResultado;
            
            if (frame != null) {
                byte[] data = frame.getData();
                jsonResultado = new String(data, ZMQ.CHARSET);
                
                // Debug: imprimir lo que recibimos
                System.out.println("[DEBUG] Datos recibidos: " + jsonResultado);
                
                // Si aún viene en hexadecimal, decodificar
                if (jsonResultado.matches("^[0-9A-Fa-f]+$")) {
                    jsonResultado = hexToString(jsonResultado);
                    System.out.println("[DEBUG] Después de decodificar hex: " + jsonResultado);
                }
            } else {
                throw new Exception("No se recibió respuesta del servidor");
            }
            
            ResultadoAsignacion resultado = mapper.readValue(jsonResultado, ResultadoAsignacion.class);

            System.out.println("[" + nombre + "] Respuesta: " + resultado.getMensaje());
            System.out.println("Salones: " + resultado.getSalonesAsignados() + ", Labs: " + resultado.getLaboratoriosAsignados());
        }
    }
    
    // Método auxiliar para convertir hexadecimal a string
    private String hexToString(String hex) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            result.append((char) Integer.parseInt(str, 16));
        }
        return result.toString();
    }
}