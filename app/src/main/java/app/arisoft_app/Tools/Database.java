package app.arisoft_app.Tools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import app.arisoft_app.Tools.AuthResponse;

public class Database extends SQLiteOpenHelper {

    public Database(Context context, CursorFactory factory,int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "arisoft_app";
    public static final String TABLA_AUTH = "authresponse";
    public static final String COLUM_SUCC = "success";
    public static final String COLUM_TOKEN = "token";
    public static final String COLUM_MSG = "message";
    public static final String COLUM_EMPRESA = "empresa";
    public static final String COLUM_USERNAME = "username";
    public static final String COLUM_LOGT = "loggedTime";
    public static final String COLUM_EXP = "expiresIn";
    public static final String COLUM_DEVICE = "deviceId";
    public static final String COLUM_DOMAIN = "dominio";
    public static final String TABLA_EXI="existencia";
    public static final String TABLA_ART="articulos";
    public static final String TABLA_ALM="almacenes";
    public static final String TABLA_CTO="conteo";
    public static final String TABLA_EXI_ALM_ACT="exiAlmAct";
    public static final String TABLA_SERIES="series";
    public static final String TABLA_SINCTO="sinconteo";
    public static final String TABLA_RECIBIDO="recibido";
    public static final String TABLA_TIPO_INV="tipoInventario";
    public static final String TABLA_CONFIG="configuracion";

    private static final String SQL_CREAR = "CREATE TABLE "
            + TABLA_AUTH + "(" + COLUM_SUCC + "," + COLUM_TOKEN + ","
            + COLUM_MSG + "," + COLUM_EMPRESA + ","+ COLUM_USERNAME + ","
            + COLUM_LOGT + "," + COLUM_EXP + "," + COLUM_DEVICE + "," + COLUM_DOMAIN + ")";

    public static final String SQL_EXISTENCIA="CREATE TABLE "+TABLA_EXI+"(codProd text ," +
            "codProd2 text," +
            "codDesc text," +
            "clas text," +
            "idalmacen text," +
            "nomAlmacen text, " +
            "existenciaActual text)";
    private static final String SQL_ARTICULOS="CREATE TABLE "+TABLA_ART+"(CodigoArticulo text PRIMARY KEY," +
            "Descripcion text," +
            "Precio01 text," +
            "Precio02 text," +
            "Precio03 text," +
            "Clasificacion text," +
            "UnidadCompra text," +
            "UnidadVenta text," +
            "FechaAlta text," +
            "FechaMod text," +
            "Costo text," +
            "FechaActual text)";
    public static final String SQL_ALMACENES="CREATE TABLE "+TABLA_ALM+"(idalmacen text," +
            "almacen text)";
    public static final String SQL_SERIES="CREATE TABLE "+TABLA_SERIES+"(_id INTEGER PRIMARY KEY, serie text," +
            "codigo text," +
            "almacen text," +
            "estatus text)";
    public static final String SQL_EXIACTALM="CREATE TABLE "+TABLA_EXI_ALM_ACT+"(idalmacen text," +
            "descripcion text," +
            "cod01 text," +
            "cod02 int," +
            "exiActual text)";
    public static final String SQL_CONTEO="CREATE TABLE "+TABLA_CTO+" (_id INTEGER PRIMARY KEY," +
            "codigo text," +
            "descripcion text," +
            "conteo Float," +
            "existencia Float," +
            "idalmacen text," +
            "serie text," +
            "diferencia Float," +
            "estatus text," +
            "comentarios text"+
            ")";
    public static final String SQL_SINCONTEO="CREATE TABLE "+TABLA_SINCTO+" (_id INTEGER PRIMARY KEY," +
            "codigo text," +
            "descripcion text," +
            "conteo float," +
            "existencia float," +
            "idalmacen text," +
            "serie text," +
            "diferencia float," +
            "estatus text," +
            "comentarios text"+
            ")";

    public static final String SQL_RECIBIDO="CREATE TABLE "+TABLA_RECIBIDO+"(_id INTEGER PRIMARY KEY," +
            "folio text," +
            "codigo text," +
            "descripcion text," +
            "cantidad float," +
            "recibido float," +
            "diferencia float," +
            "codigo2 text,"+
            "posicion text," +
            "fechafactura text," +
            "cliente text," +
            "estatus text," +
            "cantidadtmp float," +
            "surt int)";
    public static final String SQL_TIPO_INV="CREATE TABLE "+TABLA_TIPO_INV+"(idalmacen text," +
            "almacen text," +
            "tipo text," +
            "id text)";

    public static final String SQL_CONFIG="CREATE TABLE "+TABLA_CONFIG+"(id int," +
            "bus_alm_inv_fis text," +
            "dialog_tipo_inv text)";

    private static final String SQL_INICIO = "DROP TABLE IF EXISTS authresponse";
    private static final String SQL_INICIOEXI = "DROP TABLE IF EXISTS "+TABLA_EXI;
    private static final String SQL_INICIOART = "DROP TABLE IF EXISTS "+TABLA_ART;
    private static final String SQL_INICIOALM = "DROP TABLE IF EXISTS "+TABLA_ALM;
    private static final String SQL_INICIOCTO = "DROP TABLE IF EXISTS "+TABLA_CTO;
    private static final String SQL_INICIOSINCTO = "DROP TABLE IF EXISTS "+TABLA_SINCTO;
    private static final String SQL_INICIOSERIES = "DROP TABLE IF EXISTS "+TABLA_SERIES;
    private static final String SQL_INICIOEXIACTALM = "DROP TABLE IF EXISTS "+TABLA_EXI_ALM_ACT;
    private static final String SQL_INICIORECIBIDO= "DROP TABLE IF EXISTS "+TABLA_RECIBIDO;
    private static final String SQL_INICIOTIPOINV = "DROP TABLE IF EXISTS "+TABLA_TIPO_INV;
    private static final String SQL_INICIOCONFIG = "DROP TABLE IF EXISTS "+TABLA_CONFIG;

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_INICIO);
        db.execSQL(SQL_INICIOEXI);
        db.execSQL(SQL_INICIOART);
        db.execSQL(SQL_INICIOALM);
        db.execSQL(SQL_INICIOCTO);
        db.execSQL(SQL_INICIOSINCTO);
        db.execSQL(SQL_INICIOSERIES);
        db.execSQL(SQL_INICIOEXIACTALM);
        db.execSQL(SQL_INICIORECIBIDO);
        db.execSQL(SQL_INICIOTIPOINV);
        db.execSQL(SQL_INICIOCONFIG);


        db.execSQL(SQL_CREAR);
        db.execSQL(SQL_ARTICULOS);
        db.execSQL(SQL_EXISTENCIA);
        db.execSQL(SQL_ALMACENES);
        db.execSQL(SQL_CONTEO);
        db.execSQL(SQL_SINCONTEO);
        db.execSQL(SQL_SERIES);
        db.execSQL(SQL_EXIACTALM);
        db.execSQL(SQL_RECIBIDO);
        db.execSQL(SQL_TIPO_INV);
        db.execSQL(SQL_CONFIG);
    }

    public void articuloDetalle(String codigo){
        SQLiteDatabase db = this.getWritableDatabase();
        db.rawQuery("SELECT * FROM existencia WHERE CodigoArticulo = ?", new String[]{codigo});
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //    METODO PARA INSERTAR INFORMACIÓN A LA BASE DE DATOS
    //    TOMA LOS VALORES ALMACENADOS DE LA CLASE AUTHRESPONSE;

    AuthResponse auth = new AuthResponse();

    public void createAuth(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_CREAR);
    }

    public void agregar(AuthResponse response){
        auth = response;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUM_SUCC, auth.getSuccess());//0
        values.put(COLUM_TOKEN, auth.getToken());//1
        values.put(COLUM_MSG, auth.getMessages());//2
        values.put(COLUM_EMPRESA, auth.getEmpresa());//3
        values.put(COLUM_USERNAME, auth.getUsername());//4
        values.put(COLUM_LOGT, auth.getLoggedTime());//5
        values.put(COLUM_EXP, auth.getExpiresIn());//6
        values.put(COLUM_DEVICE, auth.getDeviceId());//7
        values.put(COLUM_DOMAIN, auth.getDominio());//8
        db.insert(TABLA_AUTH, null,values);
        db.close();
    }

    public String getDomain(){
        String dominio = "N";
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor fila = db.rawQuery("SELECT dominio FROM authresponse",null);
            if(fila.moveToFirst())
            {
                dominio = "http://wsar.homelinux.com:" + (fila.getString(0) + "/");
            }
        }catch (SQLiteException sql){
        }
        db.close();
        return dominio;
    }

    public String getToken(){
        String token = "N";
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor fila = db.rawQuery("SELECT token FROM authresponse",null);
            if(fila.moveToFirst())
            {
                token = fila.getString(0);
            }
        }catch (SQLiteException sql){
        }
        db.close();
        return token;
    }

}

