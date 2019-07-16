package app.arisoft_app.Modelo;

import java.util.ArrayList;

public class RespuestaArticulo {
    private ArrayList<Articulo> CatalogoArticulo;

    public ArrayList<Articulo> getCatalogoArticulo() {
        return CatalogoArticulo;
    }

    public void setCatalogoArticulo(ArrayList<Articulo> catalogoArticulo) {
        CatalogoArticulo = catalogoArticulo;
    }
}
