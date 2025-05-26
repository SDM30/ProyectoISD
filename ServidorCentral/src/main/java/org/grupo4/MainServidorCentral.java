package org.grupo4;

import org.grupo4.redes.ManejadorHealthCheck;
import org.grupo4.redes.ServidorCentral;
import org.grupo4.repositorio.ConectorCassandra;
import org.grupo4.repositorio.Configuracion;
import org.zeromq.ZContext;

import java.util.List;

public class MainServidorCentral {
    public static void main(String[] args) {
        ServidorCentral servidor;
        String ip;
        String port;
        String inproc;
        String healthcheckIp;
        String healthcheckPort;
        String cassandraIp;
        int cassandraPort;
        int maxSalones;
        int maxLabs;
        int aulasMoviles;

        if (args.length == 0) {
            // Usar configuración por defecto desde archivo
            List<String> config = Configuracion.cargarConfiguracionServidor(null);
            maxSalones = Integer.parseInt(config.get(0));
            maxLabs = Integer.parseInt(config.get(1));
            aulasMoviles = Integer.parseInt(config.get(2));
            ip = config.get(3);
            port = config.get(4);
            inproc = config.get(5);
            healthcheckIp = config.get(6);
            healthcheckPort = config.get(7);
            cassandraIp = config.get(8);
            cassandraPort = Integer.parseInt(config.get(9));
            servidor = new ServidorCentral(ip, port, inproc, maxSalones, maxLabs, aulasMoviles);
            System.out.println("Usando archivo de configuración por defecto");
        } else if (args.length == 1) {
            if (args[0].equals("-h") || args[0].equals("--help")) {
                imprimirAyuda();
                return;
            }
            // Usar archivo de propiedades proporcionado
            List<String> config = Configuracion.cargarConfiguracionServidor(args[0]);
            maxSalones = Integer.parseInt(config.get(0));
            maxLabs = Integer.parseInt(config.get(1));
            aulasMoviles = Integer.parseInt(config.get(2));
            ip = config.get(3);
            port = config.get(4);
            inproc = config.get(5);
            healthcheckIp = config.get(6);
            healthcheckPort = config.get(7);
            cassandraIp = config.get(8);
            cassandraPort = Integer.parseInt(config.get(9));
            servidor = new ServidorCentral(ip, port, inproc, maxSalones, maxLabs, aulasMoviles);
        } else if (args.length == 10) {
            try {
                maxSalones = Integer.parseInt(args[0]);
                maxLabs = Integer.parseInt(args[1]);
                aulasMoviles = Integer.parseInt(args[2]);
                ip = args[3];
                port = args[4];
                inproc = args[5];
                healthcheckIp = args[6];
                healthcheckPort = args[7];
                cassandraIp = args[8];
                cassandraPort = Integer.parseInt(args[9]);

                servidor = new ServidorCentral(ip, port, inproc, maxSalones, maxLabs, aulasMoviles);
                System.out.println("Configuración cargada desde argumentos:");
                System.out.println("Max salones: " + maxSalones);
                System.out.println("Max laboratorios: " + maxLabs);
                System.out.println("Aulas móviles: " + aulasMoviles);
                System.out.println("IP: " + ip);
                System.out.println("Puerto: " + port);
                System.out.println("Inproc: " + inproc);
                System.out.println("Healthcheck IP: " + healthcheckIp);
                System.out.println("Healthcheck Puerto: " + healthcheckPort);
                System.out.println("Cassandra IP: " + cassandraIp);
                System.out.println("Cassandra Puerto: " + cassandraPort);
            } catch (NumberFormatException e) {
                System.err.println("Error: Los argumentos numéricos no son válidos");
                imprimirAyuda();
                return;
            }
        } else {
            System.err.println("Error: Número de argumentos incorrecto");
            imprimirAyuda();
            return;
        }

        // Inicializar contexto compartido
        ZContext context = new ZContext();

        // Responder a healthcheck
        new Thread(new ManejadorHealthCheck(context, healthcheckIp, healthcheckPort)).start();

        // Atender peticiones
        try {
            if (ConectorCassandra.conectar(cassandraIp, cassandraPort)) {
                System.out.println("[CASSANDRA] Conexión establecida exitosamente");
                servidor.loadBalancingBroker(context);
            } else {
                System.err.println("[CASSANDRA] No se pudo establecer la conexión. El servidor no iniciará.");
                context.close();
                return;
            }
        } catch (Exception e) {
            System.err.println("[CASSANDRA] Error al conectar: " + e.getMessage());
            System.err.println("[SERVIDOR] No se iniciará el servidor debido al error de conexión");
            context.close();
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(servidor::imprimirMetricas));
    }

    private static void imprimirAyuda() {
        System.out.println("Uso del servidor:");
        System.out.println("1. Con archivo de configuración:");
        System.out.println("   java -jar servidor.jar <ruta-archivo.properties>");
        System.out.println("\n2. Con argumentos por línea de comandos:");
        System.out.println("   java -jar servidor.jar <max_salones> <max_labs> <aulas_moviles> <ip_servidor> <puerto> <inproc> <healthcheck_ip> <healthcheck_puerto> <cassandra_ip> <cassandra_puerto>");
        System.out.println("\nEjemplos:");
        System.out.println("   java -jar servidor.jar config.properties");
        System.out.println("   java -jar servidor.jar 380 60 10 0.0.0.0 5555 backend 0.0.0.0 5554 localhost 9042");
    }
}
