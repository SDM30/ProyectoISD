package org.grupo4.redes;

import org.grupo4.entidades.AdministradorInstalaciones;
import org.grupo4.entidades.ResultadoAsignacion;
import org.grupo4.entidades.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class TrabajadorPeticion extends Thread{
    private final ZContext contexto;
    private final ZMQ.Socket trabajador;

    public TrabajadorPeticion(ZContext contexto) {
        this.contexto = contexto;
        this.trabajador = contexto.createSocket(SocketType.REQ);
        trabajador.connect("inproc://backend");
    }


    @Override
    public void run() {
        //Notificar al backend que esta preparado
        trabajador.send("LISTO");

        while(!Thread.currentThread().isInterrupted()) {
            // Recibir todas las partes del mensaje
            String semestre = trabajador.recvStr();
            String nombreFacultad = trabajador.recvStr();
            String programa = trabajador.recvStr();
            String numSalonesStr = trabajador.recvStr();
            String numLaboratoriosStr = trabajador.recvStr();

            // Convertir los valores numéricos
            int numSalones = Integer.parseInt(numSalonesStr);
            int numLaboratorios = Integer.parseInt(numLaboratoriosStr);
            int numSem = Integer.parseInt(semestre);

            Solicitud solicitud = new Solicitud(nombreFacultad, programa, numSem, numSalones, numLaboratorios);

            ResultadoAsignacion resultado = AdministradorInstalaciones.getInstance().asignar(numSalones, numLaboratorios);
            enviarResultado(resultado, solicitud);

        }
    }

    public void enviarResultado (ResultadoAsignacion resultado, Solicitud solicitud) {
        /*
         * Estructura Trama
         * Trama 1: Informacion general
         * Trama 2: Laboratorios asignados
         * Trama 3: Aulas moviles
         * Trama 4: Salones asignados
         */

        String infoGeneral = "[ALERTA] No hay suficientes aulas o laboratorios para responder a la demanda";
        //Caso 1
        if (resultado.esExitoso()) {
            if(resultado.aulaMovilAsignadas() == 0) {
                infoGeneral = String.format("Asignacion exitosa de laboratorios y salones para %s", solicitud.getPrograma());
            } else {
                infoGeneral = String.format("Asignacion exitosa para %s, algunos laboratorios se asignaron como aulas moviles", solicitud.getPrograma());
            }
        }

        trabajador.sendMore(infoGeneral);
        trabajador.sendMore(String.valueOf(resultado.labsAsignados()));
        trabajador.sendMore(String.valueOf(resultado.aulaMovilAsignadas()));
        trabajador.send(String.valueOf(resultado.salonesAsignados()));
    }
}
