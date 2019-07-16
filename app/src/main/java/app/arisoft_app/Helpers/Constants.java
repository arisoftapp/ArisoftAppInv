package app.arisoft_app.Helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import app.arisoft_app.Tools.Database;

public class Constants {
    //Dirección del webservice
    public final String API_URL = "http://192.168.1.67:3001/%s";

    public static final int _DeviceID = 0;

    public static String GetToken(Context c){
        Database admin = new Database(c,null,1);
        String token="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * from authresponse" ,null);
        if(fila.moveToFirst()) {
            token = fila.getString(1);
        }
        return token;
    }

    public static String GetIdUsuario(Context c){
        Database admin = new Database(c,null,1);
        String Id="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * from authresponse" ,null);
        if(fila.moveToFirst()) {
            Id = fila.getString(5);
        }
        return Id;

    }

    public static String GetDispositivoId(Context c){
        Database admin = new Database(c,null,1);
        String Id="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * from authresponse" ,null);
        if(fila.moveToFirst()) {
            Id = fila.getString(6);
        }
        return Id;

    }
}
