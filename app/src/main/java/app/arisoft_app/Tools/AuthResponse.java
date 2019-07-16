package app.arisoft_app.Tools;

public class AuthResponse {
    private boolean success;
    private String token;
    private String message;
    private String empresa;
    private String username;
    private String loggedTime;
    private String expiresIn;
    private int deviceId;
    private String dominio;

    //      GETTERS
    public boolean getSuccess(){ return success;}
    public String getToken () { return token; }
    public String getMessages (){ return message; }
    public String getExpiresIn (){ return expiresIn; }
    public String getLoggedTime (){ return loggedTime; }
    public int getDeviceId (){ return deviceId; }

    //      SETTERS
    public void setSuccess(boolean value){ this.success = value; }
    public void setToken (String value) { this.token = value;; }
    public void setMessages (String value){ this.message = value; }
    public void setExpiresIn (String value){ this.expiresIn = value; }
    public void setLoggedTime (String value){ this.loggedTime = value; }
    public void setDeviceId (int value){ this.deviceId = value; }

    public String getEmpresa() { return empresa; }

    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }
}
