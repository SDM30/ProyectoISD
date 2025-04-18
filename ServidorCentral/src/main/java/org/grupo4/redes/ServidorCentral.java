package org.grupo4.redes;

import org.grupo4.repositorio.Configuracion;

import java.util.List;

public class ServidorCentral {


    public ServidorCentral (String rutaConfig) {
        List<String> configuraciones = Configuracion.cargarConfiguracionServidor(rutaConfig);
    }
}
