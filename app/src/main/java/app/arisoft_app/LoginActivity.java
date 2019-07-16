package app.arisoft_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.TextInputEditText;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.*;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import app.arisoft_app.Helpers.LocalDataAccess;
import app.arisoft_app.Modelo.Usuario;
import app.arisoft_app.Tools.Database;
import app.arisoft_app.Tools.User;
import app.arisoft_app.Tools.AuthResponse;
import app.arisoft_app.Helpers.DateTime;

public class LoginActivity extends AppCompatActivity{
    private Button acceder;
    private TextInputEditText email;
    private TextInputEditText password;
    private ProgressDialog progreso;
    private RequestQueue requestQueue;
    String id = android.os.Build.ID;
    boolean log;
    AuthResponse auth = new AuthResponse();
    DateTime Date = new DateTime();
    Database database = new Database(this, null, 1);
    boolean can = false;
    boolean save = false;
    //private static final String URL = "http://192.168.1.65:3000/logM";
    private static final String URL = "http://wsar.homelinux.com:3000/logM";
    //private static String url_device = "http://192.168.1.65:3000/user/dispositivos/";
    boolean canLogin;
    String pass, user_email;
    final User user_class = new User();
    Integer cont = 0;
    String mensaje;
    //prueba
    //x2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ValidateLogin()){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.putExtra("DATA_USER", user_class);
            intent.putExtra("RELOAD", false);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);
        email = findViewById(R.id.etusuario);
        password = findViewById(R.id.etpass);
        acceder = (Button)findViewById(R.id.btn_acceder);
        requestQueue = Volley.newRequestQueue(this);
        final String android_id = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        id = android_id;
        Usuario.getInstance(this);
        acceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_email = email.getText().toString();
                pass =  password.getText().toString();
                Login();
            }
        });
    }

    public void Login(){
        progreso = new ProgressDialog(this);
        progreso.setMessage("Iniciando...");
        progreso.show();
        final AuthResponse jsonObject = new AuthResponse();
        final JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("Username", user_email);
            jsonParams.put("Password", pass);
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

                        jsonObject.setSuccess(json.getBoolean("success"));
                        jsonObject.setMessages(json.getString("message"));
                        if (jsonObject.getSuccess()) {
                            String date = Date.setDate();
                            jsonObject.setLoggedTime(date);
                            jsonObject.setEmpresa(json.getString("empresa"));
                            jsonObject.setUsername(json.getString("username"));
                            jsonObject.setToken(json.getString("token"));
                            jsonObject.setDominio(json.getString("Dominio"));
                            SaveCredentials(jsonObject);
                            Log.i("LOGIN_SUCCESS","CAN GET SUCCESS");
                            config();
                            Toast.makeText(getApplicationContext(),"" + jsonObject.getMessages() ,Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("user", jsonObject.getUsername());
                            intent.putExtra("enter", jsonObject.getEmpresa());
                            intent.putExtra("RELOAD", true);
                            startActivity(intent);
                            progreso.dismiss();
                            finish();
                        } else{
                            Log.i("LOGIN_ERROR","CAN NOT GET SUCCESS");
                            Toast.makeText(getApplicationContext(),"" + jsonObject.getMessages() ,Toast.LENGTH_LONG).show();
                            progreso.dismiss();
                        }
                    }
                } catch (JSONException e) {
                    canLogin = false;
                    Log.i("LOGIN_ERROR","JSON EXCEPTION " + e);
                    progreso.dismiss();
                    Toast.makeText(getApplicationContext(),"" + e ,Toast.LENGTH_LONG).show();
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
        requestQueue.start();
        mensaje = jsonObject.getMessages();
        //Toast.makeText(getApplicationContext(),"" + jsonObject.getMessages() ,Toast.LENGTH_LONG).show();
    }
    public void config()
    {
        try{
            Database admin=new Database(this,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues r = new ContentValues();
            r.put("id",1);
            r.put("bus_alm_inv_fis","false");
            r.put("dialog_tipo_inv","false");
            db.insert("configuracion",null,r);
            db.close();
        }catch (Exception e)
        {
            Toast.makeText(this,"error al insertar configuraciones:"+e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    //ESTE METODO SE USABA CUANDO SE VERIFICABA EL DISPOSITIVO
    /*private void Login() {
        progreso = new ProgressDialog(getApplicationContext());
        progreso.setMessage("Iniciando...");
        progreso.show();
        String sEmail = email.getText().toString();
        String sPassword =  password.getText().toString();
        new Login().execute(sEmail,sPassword);
        boolean can = Usuario.getInstance().Login(this, sEmail, sPassword );
        boolean save = Usuario.getInstance().CheckDeviceID(this, sEmail, id);

        if (can & save) {
            Log.i("LOGIN_STATE","SUCCESS");
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
            progreso.dismiss();
        }else{
            if (!can) Toast.makeText(getApplicationContext(),"ERROR: No se ha podido autenticar el usuario",Toast.LENGTH_LONG).show();
            if (!save) Toast.makeText(getApplicationContext(),"ERROR: No se ha podido autenticar el dispositivo",Toast.LENGTH_LONG).show();
            progreso.dismiss();
        }
    }*/

    public void SaveCredentials(AuthResponse response) {
        Database admin = new Database( getApplicationContext(),null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS authresponse");
        admin.createAuth();
        JWT jwt = new JWT(response.getToken());

        Claim exp = jwt.getClaim("expiresIn");
        String expires = exp.asString();
        auth.setExpiresIn(expires);

        /*Claim idDev = jwt.getClaim("deviceId");
        String idDevice = idDev.asString();
        int intDev = Integer.parseInt(idDevice);
        response.setDeviceId(intDev);*/
        admin.agregar(response);
        LocalDataAccess.setLastChecked(response.getLoggedTime());
    }

    private boolean validar() {
        boolean valid = true;

        String sEmail = email.getText().toString();
        String sPassword = password.getText().toString();

        if (sEmail.isEmpty()){
            email.setError("Ingrese un nombre de usuario válido");
            valid = false;
        } else {
            email.setError(null);
        }

        if (sPassword.isEmpty() || password.length() < 3 || password.length() > 10) {
            password.setError("Ingrese una contraseña correcta");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

    public boolean ValidateLogin () {
        boolean validate = false;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM authresponse",null);
        if(fila.moveToFirst())
        {
            validate = true;
            GetUser();
        }
        else
        {
            //Toast.makeText(this, "Usuario no ha iniciado sesión", Toast.LENGTH_SHORT).show();
            validate = false;
        }
        db.close();
        return validate;
    }

    public boolean GetUser () {
        boolean validate = false;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM authresponse",null);
        if(fila.moveToFirst())
        {
            JWT jwt = new JWT(fila.getString(1));
            Claim email = jwt.getClaim("email");
            user_class.setEmail(email.asString());
            Claim user = jwt.getClaim("name");
            user_class.setUsername(user.asString());
            String id = fila.getString(5);
            user_class.setId(id);
        }
        else
        {
            validate = false;
        }
        db.close();
        return validate;
    }

}
