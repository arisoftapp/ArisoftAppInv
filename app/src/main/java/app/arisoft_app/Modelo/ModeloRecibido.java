package app.arisoft_app.Modelo;

public class ModeloRecibido {
    String Codigo;
    float Cantidad;
    float Recibido;
    float Diferencias;


    public ModeloRecibido(String codigo,float cantidad,float recibido,float diferencias)
    {
        this.Codigo = codigo;
        this.Cantidad = cantidad;
        this.Recibido = recibido;
        this.Diferencias = diferencias;
    }


    public String getCodigo() {
        return Codigo;
    }

    public void setCodigo(String codigo) {
        Codigo = codigo;
    }

    public float getCantidad() {
        return Cantidad;
    }

    public void setCantidad(int cantidad) {
        Cantidad = cantidad;
    }

    public float getRecibido() {
        return Recibido;
    }

    public void setRecibido(int recibido) {
        Recibido = recibido;
    }

    public float getDiferencias() {
        return Diferencias;
    }

    public void setDiferencias(int diferencias) {
        Diferencias = diferencias;
    }





}
