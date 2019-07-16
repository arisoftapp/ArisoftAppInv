package app.arisoft_app.Helpers;

import android.util.Log;

import app.arisoft_app.Tools.User;
import app.arisoft_app.Tools.AuthResponse;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTime {

    public String setDate(){
        String Date = "";
        Date date = Calendar.getInstance().getTime();
        Date = (date.toString());
        Log.i("Date: ", "" + Date);
        return Date;
        }
}
