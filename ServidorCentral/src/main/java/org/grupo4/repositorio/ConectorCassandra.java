package org.grupo4.repositorio;

import com.datastax.oss.driver.api.core.CqlSession;

import java.net.InetSocketAddress;

public class ConectorCassandra {
    public static void conectar() {
        // Construye el CqlSession apuntando a localhost:9042 (nodo1)
        try (CqlSession session = CqlSession.builder()
                // Contact point: IP y puerto del nodo1 expuesto en el host
                .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                // DataCenter que definiste en la configuración (CASSANDRA_DC)
                .withLocalDatacenter("datacenter1")
                .build()) {

            // Si la conexión fue exitosa, imprime el nombre del cluster
            String clusterName = session.getMetadata()
                    .getClusterName()
                    .orElse("Desconocido");
            System.out.println("Conectado al cluster: " + clusterName);

            // Ejemplo de consulta simple
            session.execute("SELECT release_version FROM system.local")
                    .forEach(row -> System.out.println("Versión Cassandra: "
                            + row.getString("release_version")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
