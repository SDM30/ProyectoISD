package org.grupo4.asincrono.facultad;

public class MainFacultadAsincrona {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Uso: java MainFacultadAsincrona <nombre_facultad> <salones> <laboratorios>");
            return;
        }

        String nombre = args[0];
        int salones = Integer.parseInt(args[1]);
        int labs = Integer.parseInt(args[2]);

        FacultadAsincrona facultad = new FacultadAsincrona();
        facultad.ejecutar(nombre, salones, labs);
    }
}