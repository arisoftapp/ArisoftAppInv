package app.arisoft_app.Interfaz;

import app.arisoft_app.Modelo.RespuestaRecibido;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ServiceRecibido {
    @GET("RecibidoFolioFac/{codigo}/{almacen}")
    Call<RespuestaRecibido> recibido(@Path("codigo") String codigo,
                                       @Path("almacen") String almacen);
}
