package grupo4.redes;

public class ResultadoEnvio {
    private String uuid;
    private String infoGeneral;
    private int labsAsignados;
    private int aulaMovilAsignadas;
    private int salonesAsignados;

    public ResultadoEnvio(String uuid,String infoGeneral, int labsAsignados,
                          int aulaMovilAsignadas, int salonesAsignados) {
        this.uuid = uuid;
        this.infoGeneral = infoGeneral;
        this.labsAsignados = labsAsignados;
        this.aulaMovilAsignadas = aulaMovilAsignadas;
        this.salonesAsignados = salonesAsignados;
    }

    public ResultadoEnvio() {}

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getInfoGeneral() {
        return infoGeneral;
    }

    public void setInfoGeneral(String infoGeneral) {
        this.infoGeneral = infoGeneral;
    }

    public int getLabsAsignados() {
        return labsAsignados;
    }

    public void setLabsAsignados(int labsAsignados) {
        this.labsAsignados = labsAsignados;
    }

    public int getAulaMovilAsignadas() {
        return aulaMovilAsignadas;
    }

    public void setAulaMovilAsignadas(int aulaMovilAsignadas) {
        this.aulaMovilAsignadas = aulaMovilAsignadas;
    }

    public int getSalonesAsignados() {
        return salonesAsignados;
    }

    public void setSalonesAsignados(int salonesAsignados) {
        this.salonesAsignados = salonesAsignados;
    }

    @Override
    public String toString() {
        return "ResultadoEnvio{" +
                "uuid='" + uuid + '\'' +
                ", infoGeneral='" + infoGeneral + '\'' +
                ", labsAsignados=" + labsAsignados +
                ", aulaMovilAsignadas=" + aulaMovilAsignadas +
                ", salonesAsignados=" + salonesAsignados +
                '}';
    }
}