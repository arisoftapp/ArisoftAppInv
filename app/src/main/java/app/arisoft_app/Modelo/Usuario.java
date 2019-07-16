package app.arisoft_app.Modelo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.arisoft_app.Helpers.Constants;
import app.arisoft_app.Helpers.DateTime;
import app.arisoft_app.Helpers.LocalDataAccess;
import app.arisoft_app.LoginActivity;
import app.arisoft_app.Tools.Database;

public class Usuario {

    private static DateTime Date = new DateTime();
    public RequestQueue requestQueue;
    static boolean canLogin;
    static boolean device;
    private static final String TAG = "Usuario";
    private static Usuario instance = null;
    private static final String URL = "http://192.168.1.67:3001/log";
    private static String url_device = "http://192.168.1.67:3001/user/dispositivos/";
    private Context c;

    public Usuario (Context context){
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        c = context;
    }

    public static synchronized Usuario getInstance(Context context)
    {
        if (null == instance)
            instance = new Usuario(context);
        return instance;
    }

    //this is so you don't need to pass context each time
    public static synchronized Usuario getInstance()
    {
        if (null == instance)
        {
            throw new IllegalStateException(Usuario.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }

    private String username;
    private String email;
    private String password;
    //      GETTERS
    public String getUsername(){ return username;}
    public String getEmail () { return email; }
    public String getPassword (){ return password; }
    //      SETTERS
    public void setUsername (String value){ this.username = value; }
    public void setEmail (String value) { this.email = value; }
    public void setPassword (String value){ this.password = value; }

    public class UsuarioRoot {
        private ArrayList<Usuario> Usuario = new ArrayList<Usuario>();
        private ArrayList<Dispositivo> Dispositivo = new ArrayList<>();
        //      GETTERS
        public ArrayList getArrayUsuario(){ return Usuario;}
        public ArrayList getArrayDispositivos(){ return Dispositivo;}
        //      SETTERS
        public void setArrayUsuario(ArrayList value){ this.Usuario = value; }
        public void setArrayDispositivos(ArrayList value){ this.Dispositivo = value; }

    }

    public static class AuthResponse {
        //PRIVATE MEMBERS
        private boolean success;
        private String token;
        private String message;
        private String loggedTime;
        private String expiresIn;
        private String idUsuario;
        private int deviceId;

        //      GETTERS
        public boolean getSuccess(){ return success;}
        public String getToken () { return token; }
        public String getMessages (){ return message; }
        public String getExpiresIn (){ return expiresIn; }
        public String getLoggedTime (){ return loggedTime; }
        public String getIdUsuario (){ return idUsuario; }
        public int getDeviceId (){ return deviceId; }

        //      SETTERS
        public void setSuccess(boolean value){ this.success = value; }
        public void setToken (String value) { this.token = value;; }
        public void setMessages (String value){ this.message = value; }
        public void setExpiresIn (String value){ this.expiresIn = value; }
        public void setLoggedTime (String value){ this.loggedTime = value; }
        public void setIdUsuario (String value){ this.idUsuario = value; }
        public void setDeviceId (int value){ this.deviceId = value; }
    }

    public class Dispositivo {
        private Integer id;
        private String deviceid;
        //      GETTERS
        public Integer getId () { return id; }
        public String getDeviceid (){ return deviceid; }
        //      SETTERS
        public void setId(Integer value){ this.id = value; }
        public void setDeviceid (String value) { this.deviceid = value; }
    }



    public boolean Login (Context c, String User, String Password) {
        final Context context = c;
        final AuthResponse jsonObject = new AuthResponse();
        final JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("Username", User);
            jsonParams.put("Password", Password);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RESPUESTA JSON: ",""+ response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.names().get(0).equals("success")) {
                        jsonObject.setSuccess(json.getJSONArray("hola").getJSONObject(0).getBoolean("success"));
                        jsonObject.setMessages(json.getJSONArray("hola").getJSONObject(0).getString("message"));
                        if (jsonObject.getSuccess()) {
                            String date = Date.setDate();
                            jsonObject.setLoggedTime(date);
                            jsonObject.setToken(json.getJSONArray("hola").getJSONObject(0).getString("token"));
                            //SaveCredentials(context, jsonObject);
                            canLogin = true;
                        } else{
                            canLogin = false;
                            Log.i("LOGIN_ERROR","CAN NOT GET SUCCESS");
                        }
                    }
                } catch (JSONException e) {
                    canLogin = false;
                    Log.i("LOGIN_ERROR","JSON EXCEPTION " + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { canLogin = false; }
        })  {
            @Override
            public byte[] getBody() throws AuthFailureError { return jsonParams.toString().getBytes(); }
            @Override
            public String getBodyContentType() { return "application/json"; }
        };
        requestQueue.add(stringRequest);
        return canLogin;
    }

    /*public static void SaveCredentials(Context c, AuthResponse response) {

        Database admin = new Database(c,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS authresponse");
        admin.createAuth();
        JWT jwt = new JWT(response.getToken());

        Claim email = jwt.getClaim("idUsuario");
        String iduser = email.asString();
        response.setIdUsuario(iduser);
        String obtenido = response.getIdUsuario();
        Log.i("Hola","Lo hice saveCredentials USUARIO:)" + iduser + ", " + iduser + ", Obtenido: " + obtenido);

        Claim idDev = jwt.getClaim("deviceId");
        String idDevice = idDev.asString();
        int intDev = Integer.parseInt(idDevice);
        response.setDeviceId(intDev);
        admin.agregar(response);
        LocalDataAccess.setLastChecked(response.getLoggedTime());
    }*/

    public boolean CheckDeviceID(Context c, String username, String deviceID){
        boolean devices;
        Log.i("DEVICE_STATUS","ENTRADA CHECKDEVICE " + deviceID);
        final String token = Constants.GetToken(c);
        final UsuarioRoot root = new UsuarioRoot();
        final String deviceId = deviceID;
        //requestQueue = Volley.newRequestQueue(c);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_device + username, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RESPUESTA_DISP: ",""+ response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.names().get(0).equals("dispositivo")) {
                        JSONArray dispositivos = json.getJSONArray("dispositivo");
                        if (dispositivos != null) {
                            for (int i=0;i < dispositivos.length();i++){
                                JSONObject jsonobject = dispositivos.getJSONObject(i);
                                String disp = jsonobject.getString("deviceid");
                                Log.i("DEVICE_ID", ""+ disp);
                                if (disp == deviceId)
                                    device = true;
                                //list.add(dispositivos.get(i).toString());
                            }
                            Log.i("DEVICE_STATUS","DISPOSITIVOS NOT NULL");
                        }

                    } else{
                        device = false;
                        Log.i("DEVICE_STATUS","DISPOSITIVOS NULL");
                    }
                } catch (JSONException e) {
                    Log.i("JSON_ERROR : ", ""+ e);
                    device = false; }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { device = false; }
        });
        devices = device;
        requestQueue.add(stringRequest);
        Log.i("RESPUESTA_CHECK", "" + device);
        return devices;
    }

}
