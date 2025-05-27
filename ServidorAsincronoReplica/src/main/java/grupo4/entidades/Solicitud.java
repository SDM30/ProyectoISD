package grupo4.entidades;

public class Solicitud {
    private String uuid;
    private String facultad;
    private String programa;
    private int semestre;
    private int numSalones;
    private int numLaboratorios;

    public Solicitud(String facultad, String programa, int semestre, int numSalones, int numLaboratorios) {
        this.facultad = facultad;
        this.programa = programa;
        this.semestre = semestre;
        this.numSalones = numSalones;
        this.numLaboratorios = numLaboratorios;
    }

    public Solicitud() {}

    public String getFacultad () {
        return facultad;
    }

    public void setFacultad (String facultad) {
        this.facultad = facultad;
    }

    public String getPrograma () {
        return programa;
    }

    public void setPrograma (String programa) {
        this.programa = programa;
    }

    public int getSemestre () {
        return semestre;
    }

    public void setSemestre (int semestre) {
        this.semestre = semestre;
    }

    public int getNumSalones () {
        return numSalones;
    }

    public void setNumSalones (int numSalones) {
        this.numSalones = numSalones;
    }

    public int getNumLaboratorios () {
        return numLaboratorios;
    }

    public void setNumLaboratorios (int numLaboratorios) {
        this.numLaboratorios = numLaboratorios;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "Solicitud{" +
                "uuid='" + uuid + '\'' +
                ", facultad='" + facultad + '\'' +
                ", programa='" + programa + '\'' +
                ", semestre=" + semestre +
                ", numSalones=" + numSalones +
                ", numLaboratorios=" + numLaboratorios +
                '}';
    }
}
