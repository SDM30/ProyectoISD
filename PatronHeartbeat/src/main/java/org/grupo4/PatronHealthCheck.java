package org.grupo4;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQException;


public class PatronHealthCheck {
    private static final String DEFAULT_IP = "0.0.0.0";
    private static final String DEFAULT_PORT = "5554";
    private static final String DEFAULT_PORT_PUBLISHER = "5553";
    private static String PORT_BACKUP = "5552";
    private static final String USAGE = "Uso: java PatronHealthCheck <ip> <puerto> <backup_ip> <puerto_publicador>";
    private static String BACKUP_IP = "0.0.0.0";
    private static final int REQUEST_TIMEOUT = 2500;
    private static final int REQUEST_RETRIES = 3;
    private static boolean DEBUG = true;
    private static boolean backupMode = false;

    public static void main(String[] args) {
        String conexion = interpreteArgs(args);
        String conexionBackup = getBackupConnectionString();
        try (ZContext context = new ZContext()) {
            Socket cliente = context.createSocket(ZMQ.REQ);
            cliente.connect(conexion);

            Socket publisher = initPublisher(context);
            Socket backupReq = context.createSocket(ZMQ.REQ);
            backupReq.connect(conexionBackup);
            if (DEBUG) System.out.println("[HEALTHCHECK] Conectado a servidor de respaldo: " + conexionBackup);

            Poller poller = context.createPoller(1);
            poller.register(cliente, Poller.POLLIN);

            while (!Thread.currentThread().isInterrupted()) {
                int retriesLeft = REQUEST_RETRIES;
                while (retriesLeft > 0 && !Thread.currentThread().isInterrupted()) {
                    if (DEBUG) System.out.println("Enviando PING...");
                    cliente.send("PING".getBytes(ZMQ.CHARSET), 0);

                    int rc = poller.poll(REQUEST_TIMEOUT);
                    if (rc == -1)
                        break;

                    if (poller.pollin(0)) {
                        byte[] reply = cliente.recv(0);
                        if (reply == null)
                            break;
                        if (DEBUG) System.out.println("Recibido: " + new String(reply, ZMQ.CHARSET));
                        break;
                    } else if (--retriesLeft == 0) {
                        System.out.println("[HEALTHCHECK] el servidor parece estar offline, abandonando");
                        backupMode = true;
                        if (activateBackup(context, backupReq)) {
                            sendBackupIP(publisher);
                        } else {
                            //Prueba publicador
                            System.out.println("[HEALTHCHECK] Enviando Mensaje de respaldo...");
                            sendBackupIP(publisher);
                            System.out.println("[HEALTHCHECK] No se pudo activar el respaldo.");
                        }
                        break;
                    } else {
                        System.out.println("[HEALTHCHECK] no se recibió respuesta, reintentando...");
                        poller.unregister(cliente);
                        context.destroySocket(cliente);
                        System.out.println("[HEALTHCHECK] reconectando al servidor...");
                        cliente = context.createSocket(ZMQ.REQ);
                        cliente.connect(conexion);
                        poller.register(cliente, Poller.POLLIN);
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            if (e instanceof ZMQException) {
                int code = ((ZMQException) e).getErrorCode();
                System.err.println("Error: " + code);
            } else {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static Socket initPublisher(ZContext context) {
        Socket publisher = context.createSocket(ZMQ.PUB);
        publisher.bind("tcp://*:" + DEFAULT_PORT_PUBLISHER);
        if (DEBUG) System.out.println("[HEALTHCHECK] Publicador iniciado en: " + DEFAULT_PORT_PUBLISHER);
        return publisher;
    }

    private static void sendBackupIP(Socket publisher) {
        String topic = "BACKUP";
        String message = topic + " " + BACKUP_IP;
        if (DEBUG) System.out.println("[HEALTHCHECK] Publicado BACKUP IP: " + BACKUP_IP);
        publisher.send(message.getBytes(ZMQ.CHARSET), 0);
    }

    private static boolean activateBackup(ZContext context, Socket backupReq) {
        int retriesLeft = REQUEST_RETRIES;
        while (retriesLeft-- > 0) {
            try {
                if (DEBUG) System.out.println("[HEALTHCHECK] Activando respaldo...");
                backupReq.send("ACTIVACION".getBytes(ZMQ.CHARSET), 0);

                ZMQ.Poller poller = context.createPoller(1);
                poller.register(backupReq, ZMQ.Poller.POLLIN);
                int rc = poller.poll(REQUEST_TIMEOUT);
                if (rc > 0 && poller.pollin(0)) {
                    byte[] reply = backupReq.recv(0);
                    if (reply != null) {
                        if (DEBUG) System.out.println("[HEALTHCHECK] Respaldo respondió a la activación.");
                        poller.close();
                        return true;
                    }
                } else {
                    if (DEBUG) System.out.println("[HEALTHCHECK] No se recibió respuesta de activación, reintentando...");
                }
                poller.close();
            } catch (Exception e) {
                if (DEBUG) System.out.println("[HEALTHCHECK] No se recibió respuesta de activación, reintentando...");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    private static String getBackupConnectionString() {
        return "tcp://" + BACKUP_IP + ":" + PORT_BACKUP;
    }

    /**
     * Interpreta los argumentos de la línea de comandos.
     * @param args Argumentos de la línea de comandos.
     * @return Cadena de conexión en formato "tcp://ip:puerto".
     */
    private static String interpreteArgs(String[] args){
        String conexion = "tcp://" + DEFAULT_IP + ":" + DEFAULT_PORT;
        if (args.length == 5) {
            String ip = args[1];
            String port = args[2];
            String backupIp = args[3];
            String pubPort = args[4];
            if (ip != null && !ip.isEmpty() && port != null && !port.isEmpty() &&
                    backupIp != null && !backupIp.isEmpty() && pubPort != null && !pubPort.isEmpty()) {
                conexion = "tcp://" + ip + ":" + port;
                BACKUP_IP = backupIp;
                PORT_BACKUP = pubPort;
            } else {
                System.out.println("Error: IP, puerto, backup_ip o puerto_publicador no válidos.");
                System.out.println(USAGE);
            }
        } else if (args.length > 5) {
            System.out.println("Error: Demasiados argumentos.");
        }
        return conexion;
    }
}