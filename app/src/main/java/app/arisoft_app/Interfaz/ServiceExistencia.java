package app.arisoft_app.Interfaz;

import app.arisoft_app.Modelo.RespuestaExistencia;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ServiceExistencia {
    @GET("existencia")
    Call<RespuestaExistencia>respuesta();
}
