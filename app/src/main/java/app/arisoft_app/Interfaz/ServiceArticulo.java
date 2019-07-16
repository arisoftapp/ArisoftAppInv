package app.arisoft_app.Interfaz;

import app.arisoft_app.Modelo.RespuestaArticulo;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ServiceArticulo {

    @GET("CatalogoArticulo")
    Call<RespuestaArticulo> respuestaLista();
}
