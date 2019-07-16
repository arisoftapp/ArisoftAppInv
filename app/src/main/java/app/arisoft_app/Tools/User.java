package app.arisoft_app.Tools;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {

    private String username;
    private String email;
    private String password;
    private String id;
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public User() {
    }

    public User(String email, String username, String ID) {
        this.email = email;
        this.username = username;
        this.id = ID;
    }

    public User(String correo, String username, String pass, String user) {
        this.email = correo;
        this.password = pass;
        this.username = user;

    }

    class Wrap {
        private ArrayList<User> user;
    }

    private ArrayList<AuthResponse> authResponse;


    public void setEmail(String email) {
        this.email = email;
    }

    public void setPass(String pass) {
        this.password = pass;
    }


    public String getEmail() {
        return email;
    }

    public String getId() { return id;}
    public void setId (String ID) { this.id = ID;}

    public String getPass() {
        return password;
    }

    public String getNombre() {
        return username;
    }

    public void setUsername(String user) {
        this.username = user;
    }

    protected User(Parcel in) {
        email = in.readString();
        password = in.readString();
        username = in.readString();
        id = in.readString();
        image = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(username);
        dest.writeString(id);
        dest.writeString(image);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
