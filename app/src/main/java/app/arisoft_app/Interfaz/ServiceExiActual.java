package app.arisoft_app.Interfaz;

import app.arisoft_app.Modelo.RespExiActual;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ServiceExiActual {
    @GET("existenciaALM/{almacen}")
    Call<RespExiActual> respuesta(@Path("almacen") String almacen);

    @GET("existencia/{almacen}/{codigo}/{busqueda}")
    Call<RespExiActual>r1(@Path("almacen") String almacen,
                          @Path("codigo") String codigo,
                            @Path("busqueda") String busqueda);
    @GET("series/{serie}/{codigo}")
    Call<RespExiActual>compSerie(@Path("serie")String serie,
                                 @Path("codigo")String codigo);
    @GET("SoloExistencia/{almacen}")
    Call<RespExiActual>soloExi(@Path("almacen")String almacen);
    @GET("SoloExi/{almacen}")
    Call<RespExiActual>soloEx(@Path("almacen")String almacen);
    @GET("sinconteo/{almacen}/{codigos}")
    Call<RespExiActual>rdata(@Path("almacen")String almacen,
                             @Path("codigos")String codigos);
}
