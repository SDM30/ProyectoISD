package org.grupo4proyecto;

import org.grupo4proyecto.entidades.Facultad;
import org.grupo4proyecto.entidades.Solicitud;
import org.grupo4proyecto.redes.ClienteFacultadAsincrona;
import org.grupo4proyecto.redes.ResultadoEnvio;
import org.grupo4proyecto.redes.ServidorFacultad;
import org.grupo4proyecto.repositorio.ContenedorDatos;
import org.grupo4proyecto.repositorio.RepositorioPrograma;
import org.zeromq.ZContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class MainFacultadAsincrona {

    static int semestrePorDefecto = 1;

    public static void main(String[] args) {
        ContenedorDatos datos = new ContenedorDatos();

        List<Long> tiemposRespuesta = new ArrayList<>();
        int solicitudesAtendidas = 0;
        int solicitudesNoAtendidas = 0;

        if (interpreteArgumentos(args, datos)) {

            // Comentado: Carga directa de archivo
            /*
            if (datos.solicitudes.isEmpty()) {
                cargarSolicitudesEmergencia(datos);
            }
            */

            Facultad facultad = datos.facultad;
            List<Solicitud> solicitudes = datos.solicitudes;
            ResultadoEnvio res = null;

            System.out.println(facultad.toString());
            System.out.println("Configuración del servidor interno:");
            System.out.println("IP Servidor Facultad: " + datos.ipServidorFacultad);
            System.out.println("Puerto Servidor Facultad: " + datos.puertoServidorFacultad);
            System.out.println(solicitudes.toString());

            Scanner scanner = new Scanner(System.in);
            ZContext context = new ZContext();

            facultad.iniciarSuscriptor(context, facultad);

            // Crear y configurar el servidor de facultad
            ServidorFacultad servidorFacultad = new ServidorFacultad(
                    datos.ipServidorFacultad,
                    datos.puertoServidorFacultad,
                    context
            );

            try (ClienteFacultadAsincrona clienteFacultad = new ClienteFacultadAsincrona(facultad, context)) {
                // Registrar el ClienteFacultad como listener
                facultad.getSuscriptor().setIpChangeListener(clienteFacultad);

                // Configurar el cliente en el servidor
                servidorFacultad.setClienteFacultad(clienteFacultad);

                // Iniciar el servidor en un hilo separado
                Thread hiloServidor = new Thread(() -> {
                    try {
                        servidorFacultad.iniciarServidor();
                    } catch (Exception e) {
                        System.err.println("[MAIN] Error en hilo del servidor: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                hiloServidor.setDaemon(true);
                hiloServidor.start();

                System.out.println("Press ENTER to terminate...");
                try {
                    System.in.read();
                    // Detener el suscriptor
                    facultad.getSuscriptor().detener();
                    System.out.println("IP DEL SERVIDOR: " + facultad.getDirServidorCentral().getHostAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                System.out.println("[MAIN] Servidor de facultad iniciado en hilo separado");
                System.out.println("[MAIN] Esperando solicitudes de programas...");

                // Procesar solicitudes cargadas desde archivo (si las hay)
                if (!solicitudes.isEmpty()) {
                    System.out.println("[MAIN] Procesando solicitudes cargadas desde archivo...");
                    for (int i = 0; i < solicitudes.size(); i++) {
                        long inicio = System.nanoTime();
                        res = clienteFacultad.enviarSolicitudServidor(solicitudes.get(i));
                        long fin = System.nanoTime();
                        long duracion = fin - inicio;
                        tiemposRespuesta.add(duracion);

                        if (res == null) {
                            System.out.println("[CLIENTE] No se recibió respuesta del servidor.");
                            solicitudesNoAtendidas++;
                            clienteFacultad.getSolicitudesPendientes().add(solicitudes.get(i));
                            continue;
                        }

                        if (res.getInfoGeneral().equals("[ALERTA] No hay suficientes aulas o laboratorios para responder a la demanda")) {
                            System.out.println(res.getInfoGeneral());
                            solicitudesNoAtendidas++;
                            return;
                        }

                        clienteFacultad.confirmarAsignacion(solicitudes.get(i), res, true);
                        if (i < facultad.getProgramas().size()) {  // Verificar bounds
                            facultad.getProgramas().get(i).setNumSalones(res.getSalonesAsignados());
                            facultad.getProgramas().get(i).setNumLabs(res.getLabsAsignados());
                        }
                        solicitudesAtendidas++;
                    }
                    // Opcional: Imprimir pendientes o realizar reintentos.
                    System.out.println("[CLIENTE] Solicitudes pendientes locales: " + clienteFacultad.getSolicitudesPendientes().size());
                }

                System.out.println("\n=== SERVIDOR DE FACULTAD ACTIVO ===");
                System.out.println("El servidor está escuchando solicitudes de programas en:");
                System.out.println("tcp://" + datos.ipServidorFacultad + ":" + datos.puertoServidorFacultad);
                System.out.println("Presiona ENTER para terminar...");

            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                // Detener el servidor
                servidorFacultad.close();
            }

            if (!tiemposRespuesta.isEmpty()) {
                long min = Collections.min(tiemposRespuesta);
                long max = Collections.max(tiemposRespuesta);
                double promedio = tiemposRespuesta.stream().mapToLong(Long::longValue).average().orElse(0.0);

                System.out.println("\n--- MÉTRICAS DE DESEMPEÑO ---");
                System.out.println("Solicitudes atendidas: " + solicitudesAtendidas);
                System.out.println("Solicitudes no atendidas: " + solicitudesNoAtendidas);
                System.out.printf("Tiempo mínimo de respuesta: %.2f ms%n", min / 1_000_000.0);
                System.out.printf("Tiempo máximo de respuesta: %.2f ms%n", max / 1_000_000.0);
                System.out.printf("Tiempo promedio de respuesta: %.2f ms%n", promedio / 1_000_000.0);
            } else {
                System.out.println("\nNo se registraron tiempos de respuesta.");
            }
        }
    }

    public static boolean interpreteArgumentos(String[] args, ContenedorDatos datos) {
        if (args.length >= 7) {  // Actualizado para incluir los nuevos parámetros
            try {
                datos.facultad.setNombre(args[0]);
                datos.facultad.setDirServidorCentral(InetAddress.getByName(args[1]));
                datos.facultad.setPuertoServidorCentral(Integer.parseInt(args[2]));
                datos.facultad.setIpHealthcheck(args[3]);
                datos.facultad.setPuertoHealthcheck(Integer.parseInt(args[4]));

                // Nuevos parámetros para el servidor de facultad
                datos.ipServidorFacultad = args[5];
                datos.puertoServidorFacultad = Integer.parseInt(args[6]);

                int semestre = args.length >= 8 ? Integer.parseInt(args[7]) : 1;

                if (args.length >= 9) {
                    RepositorioPrograma.inicializarCliente(datos, args[8], semestre);
                } else {
                    // Solo cargar programas por defecto si se especifica explícitamente
                    // cargarProgramasPorDefecto(datos, semestre);
                    System.out.println("[MAIN] No se especificó archivo de programas. Solo servidor activo.");
                }
                return true;

            } catch (Exception e) {
                System.err.println("Error en argumentos: " + e.getMessage());
                mostrarAyuda();
                return false;
            }
        } else if (args.length == 0) {
            datos.facultad.setNombre("Facultad de Ingeniería");
            cargarConfiguracionServidor(datos);
            // Comentado: cargarProgramasPorDefecto(datos, semestrePorDefecto);
            System.out.println("[MAIN] Modo por defecto: Solo servidor activo, sin cargar programas desde archivo.");
            return true;
        }
        mostrarAyuda();
        return false;
    }

    private static void mostrarAyuda() {
        String ayuda = """
            ==================================================================
            SISTEMA DE GESTIÓN DE RECURSOS PARA FACULTADES - USO DEL PROGRAMA
            ==================================================================
        
            Modo de uso:
            1. Sin parámetros (valores por defecto):
               java -jar Facultad.jar
               * Usará:
                 - Nombre facultad: 'Facultad de Ingeniería'
                 - IP servidor central: localhost
                 - Puerto servidor central: 5555
                 - IP healthcheck: 127.0.0.1
                 - Puerto healthcheck: 5553
                 - IP servidor facultad: 127.0.0.1 (desde config)
                 - Puerto servidor facultad: 5554 (desde config)
                 - Sin cargar programas desde archivo
        
            2. Con parámetros personalizados:
               java -jar Facultad.jar <nombre> <ip_servidor_central> <puerto_servidor_central> <ip_healthcheck> <puerto_healthcheck> <ip_servidor_facultad> <puerto_servidor_facultad> [semestre] [archivo_programas]
        
               Ejemplo completo:
               java -jar Facultad.jar "Facultad de Ciencias" 192.168.1.100 5555 192.168.1.101 5553 127.0.0.1 5554 2 misProgramas.txt
        
            3. Parámetros mínimos requeridos:
               java -jar Facultad.jar <nombre> <ip_servidor_central> <puerto_servidor_central> <ip_healthcheck> <puerto_healthcheck> <ip_servidor_facultad> <puerto_servidor_facultad>
        
               Ejemplo:
               java -jar Facultad.jar "Facultad de Medicina" 127.0.0.1 5556 127.0.0.1 5553 127.0.0.1 5557
        
            ==================================================================
            ARCHIVOS DE CONFIGURACIÓN:
            - configCliente.properties: Contiene configuración por defecto
              * server.ip=localhost (IP servidor central)
              * server.port=5555 (Puerto servidor central)
              * server.healthcheck=127.0.0.1 (IP healthcheck)
              * server.healthcheck.port=5553 (Puerto healthcheck)
              * facultad.server.ip=127.0.0.1 (IP servidor facultad)
              * facultad.server.port=5554 (Puerto servidor facultad)
            - programaDefecto.txt: Listado de programas con formato:
              Nombre Programa,salones,laboratorios
            ==================================================================
            
            NUEVA FUNCIONALIDAD:
            - El servidor de facultad escucha solicitudes de programas
            - Los programas pueden conectarse como clientes REQ al servidor facultad
            - El servidor facultad reenvía automáticamente al servidor central
        """;
        System.out.println(ayuda);
    }

    private static void cargarProgramasPorDefecto(ContenedorDatos datos, int semestre) {
        try (InputStream input = MainFacultadAsincrona.class.getResourceAsStream("/programasDefecto.txt")) {
            if (input == null) throw new IOException("Archivo no encontrado en recursos");
            RepositorioPrograma.inicializarCliente(datos, input, semestre);
        } catch (Exception e) {
            System.err.println("Error cargando programas: " + e.getMessage());
            cargarSolicitudesEmergencia(datos);
        }
    }

    private static void cargarSolicitudesEmergencia(ContenedorDatos datos) {
        datos.solicitudes.add(new Solicitud(
                datos.facultad.getNombre(),
                "Programa de Emergencia",
                semestrePorDefecto,
                5,
                5
        ));
    }

    public static void cargarConfiguracionServidor(ContenedorDatos datos) {
        try (InputStream input = new FileInputStream("src/main/resources/configCliente.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            String ip = prop.getProperty("server.ip", "localhost");
            try {
                InetAddress direccion = InetAddress.getByName(ip);
                datos.facultad.setDirServidorCentral(direccion);
            } catch (UnknownHostException e) {
                System.err.println("Dirección IP inválida en configuración, usando localhost");
                datos.facultad.setDirServidorCentral(InetAddress.getLoopbackAddress());
            }

            String puerto = prop.getProperty("server.port", "5555");
            try {
                datos.facultad.setPuertoServidorCentral(Integer.parseInt(puerto));
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido en configuración, usando 5555");
                datos.facultad.setPuertoServidorCentral(5555);
            }

            String ipHealth = prop.getProperty("server.healthcheck", "localhost");
            datos.facultad.setIpHealthcheck(ipHealth);

            String puertoHealth = prop.getProperty("server.healthcheck.port", "5553");
            try {
                datos.facultad.setPuertoHealthcheck(Integer.parseInt(puertoHealth));
            } catch (NumberFormatException e) {
                System.err.println("Puerto healthcheck inválido en configuración, usando 5553");
                datos.facultad.setPuertoHealthcheck(5553);
            }

            // Nuevos parámetros para el servidor de facultad
            String ipServidorFacultad = prop.getProperty("facultad.server.ip", "127.0.0.1");
            datos.ipServidorFacultad = ipServidorFacultad;

            String puertoServidorFacultad = prop.getProperty("facultad.server.port", "5554");
            try {
                datos.puertoServidorFacultad = Integer.parseInt(puertoServidorFacultad);
            } catch (NumberFormatException e) {
                System.err.println("Puerto servidor facultad inválido en configuración, usando 5554");
                datos.puertoServidorFacultad = 5554;
            }

        } catch (IOException e) {
            System.err.println("No se encontró configCliente.properties, usando valores por defecto");
            datos.facultad.setDirServidorCentral(InetAddress.getLoopbackAddress());
            datos.facultad.setPuertoServidorCentral(5555);
            datos.facultad.setIpHealthcheck("localhost");
            datos.facultad.setPuertoHealthcheck(5553);
            datos.ipServidorFacultad = "127.0.0.1";
            datos.puertoServidorFacultad = 5554;
        }
    }
}