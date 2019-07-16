package app.arisoft_app.Modelo;

import android.util.Log;

import java.text.DecimalFormat;

public class Inventario {
    private boolean isSelected;
    private String CodigoArticulo;
    private String Descripcion;
    private Float Existencia;
    private Float Conteo;
    private Float Diferencia;
    private String Serie;
    private String Comentario;
    private Float DiferenciaSerie;

    public Inventario (boolean isSelected, String codigo, String des, String conteo, String exis, String serie, String comentario){
        this.isSelected = isSelected;
        this.CodigoArticulo = codigo;
        this.Descripcion = des;
        this.Existencia = Float.parseFloat(exis);
        this.Conteo = Float.parseFloat(conteo);
        this.Serie = serie;
        this.Comentario = comentario;
    }

    public Inventario (boolean isSelected, String codigo, String des, String conteo, String exis, String serie, Float dife){
        this.isSelected = isSelected;
        this.CodigoArticulo = codigo;
        this.Descripcion = des;
        this.Existencia = Float.parseFloat(exis);
        this.Conteo = Float.parseFloat(conteo);
        this.Diferencia = dife;
        this.Serie = serie;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Float getDiferencia() {
        setDiferencia();
        return Diferencia;
    }

    public void setDiferencia() {
        String aux;
        DecimalFormat df = new DecimalFormat("#.######");
        aux=df.format(Conteo-Existencia);
        Diferencia =Float.parseFloat(aux);
        Log.i("obtenerdif",""+Conteo+" "+Existencia+" "+Diferencia);

    }

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

    public Float getExistencia() {
        return Existencia;
    }
    public void setExistencia(Float existencia) {
        Existencia = existencia;
    }

    public Float getConteo() {
        return Conteo;
    }
    public void setConteo(Float conteo) {
        Conteo = conteo;
    }

    public String getSerie() { return Serie; }
    public void setSerie(String serie) { Serie = serie; }

    public String getComentario() {
        return Comentario;
    }

    public void setComentario(String comentario) {
        Comentario = comentario;
    }
}
