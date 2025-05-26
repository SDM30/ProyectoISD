package org.grupo4proyecto.redes;

import org.grupo4proyecto.entidades.Facultad;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.net.InetAddress;

public class SuscriptorFacultad {
    private static final String TOPIC = "BACKUP";
    private String IP_HEALTHCHECK = "0.0.0.0";
    private String PUERTO_HEALTHCHECK = "5553";
    private volatile boolean activo = true;

    public SuscriptorFacultad(String ip, String puerto) {
        this.IP_HEALTHCHECK = ip;
        this.PUERTO_HEALTHCHECK = puerto;
    }

    public void recibirMensajes(ZContext context, Facultad facultad) {
        ZMQ.Socket socket = context.createSocket(ZMQ.SUB);

        System.out.println("[SUBSCRIBER] Conectando a Publicador "
                + IP_HEALTHCHECK + ":"
                + PUERTO_HEALTHCHECK
                + " con el tema: " + TOPIC);
        socket.connect("tcp://" + IP_HEALTHCHECK + ":" + PUERTO_HEALTHCHECK);
        socket.subscribe(TOPIC.getBytes(ZMQ.CHARSET));

        try {
            while (activo && !Thread.currentThread().isInterrupted()) {
                byte[] mensaje = socket.recv(0);
                if (mensaje == null) break;
                String mensajeStr = new String(mensaje, ZMQ.CHARSET);
                System.out.println("[SUBSCRIBER] Mensaje recibido: " + mensajeStr);

                String[] partes = mensajeStr.split(" ");
                if (partes.length == 2 && "BACKUP".equals(partes[0])) {
                    String backupIp = partes[1];
                    System.out.println("IP del servidor replica: " + backupIp);
                }

                facultad.setDirServidorCentral(InetAddress.getByName(partes[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public void detener() {
        activo = false;
    }


    public void setIpHealthcheck(String ipHealthcheck) {
        IP_HEALTHCHECK = ipHealthcheck;
    }

    public void setPuertoHealthcheck(String puertoHealthcheck) {
        PUERTO_HEALTHCHECK = puertoHealthcheck;
    }

    public String getIpHealthcheck() {
        return IP_HEALTHCHECK;
    }

    public String getPuertoHealthcheck() {
        return PUERTO_HEALTHCHECK;
    }
}
