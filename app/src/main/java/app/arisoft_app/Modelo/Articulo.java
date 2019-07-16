package app.arisoft_app.Modelo;

public class Articulo {
    private String CodigoArticulo;
    private String Descripcion;
    private String Precio01;
    private String Precio02;
    private String Precio03;
    private String Clasificacion;
    private String UnidadCompra;
    private String UnidadVenta;
    private String fechaAlta;

    public Articulo(String codigo, String desc, String p1, String p2, String p3) {
        this.CodigoArticulo = codigo;
        this.Descripcion = desc;
        this.Precio01 = p1;
        this.Precio02 = p2;
        this.Precio03 = p3;
    }

    public String getCosto() {
        return Costo;
    }

    public void setCosto(String costo) {
        Costo = costo;
    }

    private String Costo;

    public String getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(String fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(String fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    private String fechaModificacion;

    public String getCodigoArticulo() {
        return CodigoArticulo;
    }

    public void setCodigoArticulo(String codigoArticulo) {
        CodigoArticulo = codigoArticulo;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getPrecio01() {
        return Precio01;
    }

    public void setPrecio01(String precio01) {
        Precio01 = precio01;
    }

    public String getPrecio02() {
        return Precio02;
    }

    public void setPrecio02(String precio02) {
        Precio02 = precio02;
    }

    public String getPrecio03() {
        return Precio03;
    }

    public void setPrecio03(String precio03) {
        Precio03 = precio03;
    }

    public String getClasificacion() {
        return Clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        Clasificacion = clasificacion;
    }

    public String getUnidadCompra() {
        return UnidadCompra;
    }

    public void setUnidadCompra(String unidadCompra) {
        UnidadCompra = unidadCompra;
    }

    public String getUnidadVenta() {
        return UnidadVenta;
    }

    public void setUnidadVenta(String unidadVenta) {
        UnidadVenta = unidadVenta;
    }
}
