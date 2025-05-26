package org.grupo4proyecto.redes;

import org.zeromq.ZContext;

public interface IPChangeListener {
    void onIPChanged(String newIP, String newPort, ZContext context);
}
