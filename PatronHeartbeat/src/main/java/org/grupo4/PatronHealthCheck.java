package org.grupo4;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQ.Poller;

public class PatronHealthCheck {
    private static final String DEFAUT_IP = "0.0.0.0";
    private static final String DEFAUT_PORT = "5554";
    private static final String USAGE = "Uso: java PatronHealthCheck <ip> <puerto>";
    private static final int REQUEST_TIMEOUT = 2500; // 2.5 segundos
    private static final int REQUEST_RETRIES = 3; // 3 intentos


    public static void main(String[] args) {
        String conexion = interpreteArgs(args);
        try (ZContext context = new ZContext()) {
            Socket cliente = context.createSocket(ZMQ.REQ);
            cliente.connect(conexion);

            Poller poller = context.createPoller(1);
            poller.register(cliente, Poller.POLLIN);

            while (!Thread.currentThread().isInterrupted()) {
                int retriesLeft = REQUEST_RETRIES;
                while (retriesLeft > 0 && !Thread.currentThread().isInterrupted()) {
                    System.out.println("Enviando PING...");
                    cliente.send("PING".getBytes(ZMQ.CHARSET), 0);

                    int rc = poller.poll(REQUEST_TIMEOUT);
                    if (rc == -1)
                        break; // Interrupted

                    if (poller.pollin(0)) {
                        byte[] reply = cliente.recv(0);
                        if (reply == null)
                            break; // Interrupted
                        System.out.println("Recibido: " + new String(reply, ZMQ.CHARSET));
                        break; // Exit retry loop and send next PING
                    } else if (--retriesLeft == 0) {
                        System.out.println("E: el servidor parece estar offline, abandonando");
                        break;
                    } else {
                        System.out.println("W: no se recibió respuesta, reintentando...");
                        poller.unregister(cliente);
                        context.destroySocket(cliente);
                        System.out.println("I: reconectando al servidor...");
                        cliente = context.createSocket(ZMQ.REQ);
                        cliente.connect(conexion);
                        poller.register(cliente, Poller.POLLIN);
                    }
                }
                // Optional: add a delay between pings
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String interpreteArgs(String[] args){
        String conexion = "tcp://" + DEFAUT_IP + ":" + DEFAUT_PORT;
        if (args.length == 3) {
            String ip = args[1];
            String port = args[2];
            if (ip != null && !ip.isEmpty() && port != null && !port.isEmpty()) {
                conexion = "tcp://" + ip + ":" + port;
            } else {
                System.out.println("Error: IP o puerto no válidos.");
            }
        } else if (args.length > 3) {
            System.out.println("Error: Demasiados argumentos.");
        }
        return conexion;
    }
}