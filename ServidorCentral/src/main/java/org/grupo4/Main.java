package org.grupo4;

import org.grupo4.redes.TrabajadorPeticion;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import java.util.LinkedList;
import java.util.Queue;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        final int NBR_WORKERS = 3;
        final String FRONTEND_ADDR = "tcp://*:5555";
        final String BACKEND_ADDR  = "inproc://backend";

        try (ZContext context = new ZContext()) {
            Socket frontend = context.createSocket(SocketType.ROUTER);
            frontend.bind(FRONTEND_ADDR);

            Socket backend  = context.createSocket(SocketType.ROUTER);
            backend.bind(BACKEND_ADDR);

            // Arrancamos nuestros TrabajadorPeticion con el contexto compartido
            for (int i = 0; i < NBR_WORKERS; i++) {
                new TrabajadorPeticion(context).start();                      // usa run() y enviarResultado internamente
            }

            Queue<String> workerQueue = new LinkedList<>();
            Poller poller = context.createPoller(2);

            while (!Thread.currentThread().isInterrupted()) {
                poller.register(backend,  Poller.POLLIN);
                if (!workerQueue.isEmpty()) {
                    poller.register(frontend, Poller.POLLIN);
                }

                if (poller.poll() < 0) break;

                // Actividad de workers en backend
                if (poller.pollin(0)) {
                    String workerAddr = backend.recvStr();
                    backend.recvStr();                      // empty
                    String clientAddr = backend.recvStr();

                    if ("LISTO".equals(clientAddr)) {
                        workerQueue.add(workerAddr);        // reenfila worker disponible
                    } else {
                        backend.recvStr();                  // empty
                        String reply = backend.recvStr();
                        frontend.sendMore(clientAddr);
                        frontend.sendMore("");
                        frontend.send(reply);
                        workerQueue.add(workerAddr);
                    }
                }

                // Peticiones de clientes en frontend
                if (!workerQueue.isEmpty() && poller.pollin(1)) {
                    String clientAddr = frontend.recvStr();
                    frontend.recvStr();                    // empty
                    String request = frontend.recvStr();

                    String workerAddr = workerQueue.poll();
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