package app.arisoft_app.Helpers;

public class LocalDataAccess {
    private static String lastChecked = "";

    public static void setLastChecked(String value){ lastChecked = value; }
    public static String getLastChecked () { return lastChecked; }
}
