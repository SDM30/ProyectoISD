package org.grupo4;

import org.grupo4.redes.ManejadorHealthCheck;
import org.grupo4.redes.ServidorCentral;
import org.grupo4.repositorio.Configuracion;
import org.zeromq.ZContext;

import java.util.List;

public class MainServidorRespaldo {
    // Valores por defecto
    private static final int DEFAULT_MAX_SALONES = 380;
    private static final int DEFAULT_MAX_LABS = 60;
    private static final String DEFAULT_IP = "0.0.0.0";
    private static final String DEFAULT_PORT = "5555";
    private static final String DEFAULT_INPROC = "backend";

    // Valores por defecto healthcheck
    private static final String DEFAULT_HEALTHCHECK_IP = "0.0.0.0";
    private static final String DEFAULT_HEALTHCHECK_PORT = "5554";

    public static void main(String[] args) {
        ServidorCentral servidor;
        String healthcheckIp;
        String healthcheckPort;

        if (args.length == 0) {
            // Usar configuración por defecto desde archivo
            List<String> config = Configuracion.cargarConfiguracionServidor(null);
            healthcheckIp = config.get(5);
            healthcheckPort = config.get(6);
            servidor = new ServidorCentral(config.get(2), config.get(3), config.get(4),
                    Integer.parseInt(config.get(0)), Integer.parseInt(config.get(1)));
            System.out.println("Usando archivo de configuración por defecto");
        } else if (args.length == 1) {
            if (args[0].equals("-h") || args[0].equals("--help")) {
                imprimirAyuda();
                return;
            }
            // Usar archivo de propiedades proporcionado
            List<String> config = Configuracion.cargarConfiguracionServidor(args[0]);
            healthcheckIp = config.get(5);
            healthcheckPort = config.get(6);
            servidor = new ServidorCentral(args[0]);
        } else if (args.length == 7) {
            try {
                int maxSalones = Integer.parseInt(args[0]);
                int maxLabs = Integer.parseInt(args[1]);
                String ip = args[2];
                String port = args[3];
                String inproc = args[4];
                healthcheckIp = args[5];
                healthcheckPort = args[6];

                servidor = new ServidorCentral(ip, port, inproc, maxSalones, maxLabs);
                System.out.println("Configuración cargada desde argumentos:");
                System.out.println("Max salones: " + maxSalones);
                System.out.println("Max laboratorios: " + maxLabs);
                System.out.println("IP: " + ip);
                System.out.println("Puerto: " + port);
                System.out.println("Inproc: " + inproc);
                System.out.println("Healthcheck IP: " + healthcheckIp);
                System.out.println("Healthcheck Puerto: " + healthcheckPort);
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

        // Esperar activación antes de iniciar el broker
        ManejadorHealthCheck healthCheck = new ManejadorHealthCheck(context, healthcheckIp, healthcheckPort);
        if (healthCheck.esperarActivacion()) {
            System.out.println("Activación recibida. Iniciando broker...");
            servidor.loadBalancingBroker(context);
        } else {
            System.err.println("Error en la activación del servidor");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(servidor::imprimirMetricas));
    }

    private static void imprimirAyuda() {
        System.out.println("Uso del servidor:");
        System.out.println("1. Con archivo de configuración:");
        System.out.println("   java -jar servidor.jar <ruta-archivo.properties>");
        System.out.println("\n2. Con argumentos por línea de comandos:");
        System.out.println("   java -jar servidor.jar <max_salones> <max_labs> <ip_servidor> <puerto> <inproc> <healthcheck_ip> <healthcheck_puerto>");
        System.out.println("\nEjemplos:");
        System.out.println("   java -jar servidor.jar config.properties");
        System.out.println("   java -jar servidor.jar 380 60 0.0.0.0 5555 backend 0.0.0.0 5554");
    }
}