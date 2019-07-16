package app.arisoft_app.Modelo;

public class Serie {

    private String Id;
    private String Serie;
    private String Estado;
    private String IdAlmacen;
    private String Codigo;



    public Serie(String id, String serie, String codigo, String idAlmacen, String estado ) {
        Id = id;
        Serie = serie;
        Codigo = codigo;
        IdAlmacen = idAlmacen;
        Estado = estado;
    }

    public String getSerie() {
        return Serie;
    }

    public void setSerie(String serie) {
        Serie = serie;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getEstado() {
        return Estado;
    }

    public void setEstado(String estado) {
        Estado = estado;
    }

    public String getIdAlmacen() {
        return IdAlmacen;
    }

    public void setIdAlmacen(String idAlmacen) {
        IdAlmacen = idAlmacen;
    }

    public String getIdArticulo() {
        return Codigo;
    }

    public void setIdArticulo(String idArticulo) {
        Codigo = idArticulo;
    }
}
