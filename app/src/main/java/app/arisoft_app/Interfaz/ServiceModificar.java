package app.arisoft_app.Interfaz;

import com.android.volley.toolbox.StringRequest;

import java.util.List;

import app.arisoft_app.Modelo.RespuestaExistencia;
import app.arisoft_app.Modelo.respuestaCosto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ServiceModificar {
        @PUT("modificarExistencia/{almacen}/{codigo}/{existencia}/{valor}")
        Call<RespuestaExistencia> respuesta(@Path("almacen") String almacen,
                                            @Path("codigo") String codigo,
                                            @Path("existencia") Float existencia,
                                            @Path("valor")float valor);
        @PUT("modificarAdicionales/{almacen}/{entrada}/{salida}")
        Call<RespuestaExistencia> folios(@Path("almacen") String almacen,
                                            @Path("entrada") String entrada,
                                            @Path("salida") String salida);
        @GET("costoUnitario/{almacen}/{codigo}")
        Call<respuestaCosto>costoValor(@Path("almacen") String almacen,
                                       @Path("codigo") String codigo);
        @GET("adicionales/{almacen}")
        Call<RespuestaExistencia>adicional(@Path("almacen")String almacen);

        @POST("agregarMovimiento/{movimiento}/{folio}/{posicion}/{fecha}/{almacen}/{cantidad}/{articulo}/{costoUni}/{costeo}/{fechaSys}/{hora}/{fechaMod}")
        Call<RespuestaExistencia>movimiento(@Path("movimiento") int movimiento,
                                            @Path("folio") String folio,
                                            @Path("posicion") int posicion,
                                            @Path("fecha") String fecha,
                                            @Path("almacen") String almacen,
                                            @Path("cantidad") float cantidad,
                                            @Path("articulo") String articulo,
                                            @Path("costoUni") float costoUni,
                                            @Path("costeo") float costeo,
                                            @Path("fechaSys") int fechaSys,
                                            @Path("hora") int hora,
                                            @Path("fechaMod") int fechaMod
                                            );
}
