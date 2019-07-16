package app.arisoft_app;

public class ExiAlmancen {
    private String almacen,existencia,idalmacen;

    public String getIdalmacen() {
        return idalmacen;
    }

    public void setIdalmacen(String idalmacen) {
        this.idalmacen = idalmacen;
    }


    public ExiAlmancen(String almacen, String existencia,String idalmacen) {
        this.almacen = almacen;
        this.existencia = existencia;
        this.idalmacen = idalmacen;
    }

    public String getAlmacen() {
        return almacen;
    }

    public void setAlmacen(String almacen) {
        this.almacen = almacen;
    }

    public String getExistencia() {
        return existencia;
    }

    public void setExistencia(String existencia) {
        this.existencia = existencia;
    }
}
