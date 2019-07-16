package app.arisoft_app.Tools;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import app.arisoft_app.Tools.AuthResponse;

public class Token {
    AuthResponse auth = new AuthResponse();
    String key = "97cB11ADd";
    String token = auth.getToken();
    String retorno = mess();
    public String getRetorno(){ return retorno; }

    public String mess() {
        Jws<Claims> parser;
        /*try {
            jws = json.getJSONArray("usuario").getJSONObject(0).getString("success");
        }catch (JSONException jsonEx){
            jsonEx.printStackTrace();
        }*/

        //assert Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject().equals("token");
        try {

            parser = Jwts.parser().setSigningKey(key).parseClaimsJws(token);

            //OK, we can trust this JWT

        } catch (JwtException e) {

            //don't trust the JWT!
        }
        return retorno;

    }
}




