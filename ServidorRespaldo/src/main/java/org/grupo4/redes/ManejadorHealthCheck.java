package org.grupo4.redes;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ManejadorHealthCheck {
    private final ZContext context;
    private final String bindAddress;

    public ManejadorHealthCheck(ZContext context, String ip, String port) {
        this.context = context;
        this.bindAddress = "tcp://" + ip + ":" + port;
    }

    public boolean esperarActivacion() {
        try (ZMQ.Socket socket = context.createSocket(ZMQ.REP)) {
            socket.bind(this.bindAddress);
            System.out.println("[HEALTHCHECK] HealthCheck escuchando en: " + bindAddress);

            byte[] ping = socket.recv(0);
            if (new String(ping).equals("ACTIVACION")) {
                socket.send("ACK".getBytes(), 0);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error en HealthCheck: " + e.getMessage());
            return false;
        }
    }
}