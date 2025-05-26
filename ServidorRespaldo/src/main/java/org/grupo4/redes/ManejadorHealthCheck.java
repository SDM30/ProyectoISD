package org.grupo4.redes;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ManejadorHealthCheck implements Runnable{
    private final ZContext context;
    private final String bindAddress;

    public ManejadorHealthCheck(ZContext context, String ip, String port) {
        this.context = context;
        this.bindAddress = "tcp://" + ip + ":" + port;
    }

    @Override
    public void run() {
        try (ZMQ.Socket socket = context.createSocket(ZMQ.REP)) {
            socket.bind(this.bindAddress);
            System.out.println("[HEALTHCHECK] HealthCheck escuchando en: " + bindAddress);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] ping = socket.recv(0);
                if (new String(ping).equals("PING")) {
                    socket.send("PONG".getBytes(), 0);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en HealthCheck: " + e.getMessage());
        }
    }

}
