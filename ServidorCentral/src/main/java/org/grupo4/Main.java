package org.grupo4;

import org.grupo4.redes.TrabajadorPeticion;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            // 1. Sockets del broker
            Socket frontend = context.createSocket(SocketType.ROUTER);
            Socket backend = context.createSocket(SocketType.ROUTER);
            frontend.bind("tcp://*:5555");     // Clientes se conectan aquí
            backend.bind("inproc://backend");  // Trabajadores internos

            // 2. Iniciar trabajadores
            int NBR_WORKERS = 1;
            for (int i = 0; i < NBR_WORKERS; i++) {
                new Thread(new TrabajadorPeticion(context)).start();
            }

            // 3. Cola de trabajadores disponibles
            Queue<byte[]> workerQueue = new LinkedList<>();

            // 4. Bucle principal del broker
            while (!Thread.currentThread().isInterrupted()) {
                Poller poller = context.createPoller(2);
                poller.register(backend, Poller.POLLIN);  // Siempre escuchar backend

                // Escuchar frontend solo si hay trabajadores disponibles
                if (!workerQueue.isEmpty()) {
                    poller.register(frontend, Poller.POLLIN);
                }

                poller.poll();

                // 5. Manejar mensajes del backend (trabajadores)
                if (poller.pollin(0)) {
                    // Formato: [workerAddr][empty][clientAddr][empty][response]
                    byte[] workerAddr = backend.recv();
                    backend.recv();    // Frame vacío
                    byte[] clientAddr = backend.recv();


                    if ("READY".equals(new String(clientAddr, ZMQ.CHARSET))) {
                        // Registrar trabajador como disponible
                        workerQueue.add(workerAddr);
                        System.out.println("[BROKER] Trabajador registrado: ");
                    } else {
                        // Reenviar respuesta al cliente
                        backend.recv();  // Frame vacío
                        byte[] response = backend.recv();

                        frontend.sendMore(clientAddr);
                        frontend.sendMore("");
                        frontend.send(response);
                        System.out.println("[BROKER] Respuesta enviada a cliente ");
                    }
                }

                // 6. Manejar mensajes del frontend (clientes)
                if (poller.pollin(1)) {
                    // Formato: [clientAddr][empty][request]
                    byte[] clientAddr = frontend.recv();
                    frontend.recv(); // Frame vacío
                    byte[] request = frontend.recv();

                    // Obtener trabajador disponible
                    byte[] workerAddr = workerQueue.poll();
                    System.out.println("[BROKER] Enviando solicitud a trabajador ");

                    // Reenviar al trabajador: [workerAddr][empty][clientAddr][empty][request]
                    backend.sendMore(workerAddr);
                    backend.sendMore("");
                    backend.sendMore(clientAddr);
                    backend.sendMore("");
                    backend.send(request);
                }
            }
        }
    }
}