package org.grupo4.repositorio;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.grupo4.entidades.Solicitud;

import java.net.InetSocketAddress;

public class ConectorCassandra {
    private static CqlSession session;

    public static boolean conectar(String ip, int port) {
        try {
            session = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress(ip, port))
                    .withLocalDatacenter("datacenter1")
                    .build();

            crearEsquema();

            String clusterName = session.getMetadata()
                    .getClusterName()
                    .orElse("Desconocido");
            System.out.println("[CASSANDRA] Conectado al cluster: " + clusterName);
            return session != null && !session.isClosed();

        } catch (Exception e) {
            System.err.println("[CASSANDRA] Error de conexión: " + e.getMessage());
        }
        return false;
    }

    private static void crearEsquema() {
        session.execute("CREATE KEYSPACE IF NOT EXISTS servidor_central " +
                "WITH replication = {'class':'SimpleStrategy', 'replication_factor':1}");

        session.execute("CREATE TABLE IF NOT EXISTS servidor_central.solicitudes_pendientes (" +
                "uuid text PRIMARY KEY, " +
                "facultad text, " +
                "programa text, " +
                "semestre int, " +
                "num_salones int, " +
                "num_laboratorios int)");

        session.execute("CREATE TABLE IF NOT EXISTS servidor_central.solicitudes_atendidas (" +
                "uuid text PRIMARY KEY, " +
                "facultad text, " +
                "programa text, " +
                "semestre int, " +
                "num_salones int, " +
                "num_laboratorios int)");
    }

    public static void insertarSolicitudPendiente(Solicitud solicitud) {
        try {
            PreparedStatement pstmt = session.prepare(
                "INSERT INTO servidor_central.solicitudes_pendientes " +
                "(uuid, facultad, programa, semestre, num_salones, num_laboratorios) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            );

            BoundStatement bound = pstmt.bind(
                solicitud.getUuid(),
                solicitud.getFacultad(),
                solicitud.getPrograma(),
                solicitud.getSemestre(),
                solicitud.getNumSalones(),
                solicitud.getNumLaboratorios()
            ).setConsistencyLevel(ConsistencyLevel.ONE);

            session.execute(bound);
            System.out.println("[CASSANDRA] Solicitud pendiente insertada: " + solicitud.getUuid());
        } catch (Exception e) {
            System.err.println("[CASSANDRA] Error al insertar solicitud pendiente: " + e.getMessage());
        }
    }

    public static void moverSolicitudAAtendidas(Solicitud solicitud) {
        try {
            PreparedStatement insertPstmt = session.prepare(
                "INSERT INTO servidor_central.solicitudes_atendidas " +
                "(uuid, facultad, programa, semestre, num_salones, num_laboratorios) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            );

            BoundStatement boundInsert = insertPstmt.bind(
                solicitud.getUuid(),
                solicitud.getFacultad(),
                solicitud.getPrograma(),
                solicitud.getSemestre(),
                solicitud.getNumSalones(),
                solicitud.getNumLaboratorios()
            ).setConsistencyLevel(ConsistencyLevel.ONE);

            PreparedStatement deletePstmt = session.prepare(
                "DELETE FROM servidor_central.solicitudes_pendientes WHERE uuid = ?"
            );

            BoundStatement boundDelete = deletePstmt.bind(solicitud.getUuid())
                .setConsistencyLevel(ConsistencyLevel.ONE);

            session.execute(boundInsert);
            session.execute(boundDelete);

            System.out.println("[CASSANDRA] Solicitud movida a atendidas: " + solicitud.getUuid());
        } catch (Exception e) {
            System.err.println("[CASSANDRA] Error al mover solicitud a atendidas: " + e.getMessage());
        }
    }

    public static void cerrar() {
        if (session != null && !session.isClosed()) {
            session.close();
            System.out.println("[CASSANDRA] Conexión cerrada");
        }
    }
}
