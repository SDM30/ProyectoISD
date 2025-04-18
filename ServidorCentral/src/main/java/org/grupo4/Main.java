package org.grupo4;

import org.grupo4.redes.ServidorCentral;


public class Main {
    public static void main(String[] args) {
        // Iniciar el servidor central con la configuración por defecto
        ServidorCentral servidor = new ServidorCentral("configServidor.properties");
        servidor.loadBalancingBroker();
    }
}