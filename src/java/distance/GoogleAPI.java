package distance;

import java.sql.Connection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class GoogleAPI {

//    String source; 
//    String dest; 
//    Connection con;
//
//    public GoogleAPI(String source, String dest, Connection con) {
//        this.source = source;
//        this.dest = dest;
//        this.con = con;
//    }
//       
//    public GoogleAPI(){}
    
    PreparedStatement ps;
    ResultSet res = null;
    String sql = "";
    int idSource = -1;
    int idDest = -1;

    public String manageGetKm(String source, String dest, Connection con) throws SQLException, UnsupportedEncodingException {
        double km = checkInDB(source, dest, con);
        if (km != -1) {
            return String.valueOf(km);
        }
        String kmString = callGoogleAPI(source, dest, con);
        if (kmString.matches("[0-9][0-9]*\\.?[0-9]")) { //add option of , for big distance
            saveInDB(source, dest, Double.parseDouble(kmString), con);
        }
        return kmString;
    }

    public double checkInDB(String source, String dest, Connection con) throws SQLException {
        // if this search already in db returns distance in km, else -1

        int idSearch;
        double km = -1;
        
        // or do the next in 1 query with union - then first line is source and second is dest                
        sql = "select id_place from places where city='" + source+"'";
        ps = con.prepareStatement(sql);
        res = ps.executeQuery(sql); 
        if (res.next()) {
            idSource = res.getInt(1);
        }
        sql = "select id_place from places where city='" + dest+"'";
        ps = con.prepareStatement(sql);
        res = ps.executeQuery(sql); 
        if (res.next()) {
            idDest = res.getInt(1);
        }
        if (idSource != -1 && idDest != -1) {
            sql = "select id_search, km from searches where "
                    + "(id_source='" + idSource + "' and id_dest='" + idDest + "') or (id_source='" + idDest + "' and id_dest='" + idSource + "')";
            ps = con.prepareStatement(sql);
            res = ps.executeQuery(sql); 
            if (res.next()) {
                km = res.getDouble(2);
                increaseHits(res.getInt(1), con);
            }
        }
        return km;
    }

    public void increaseHits(int id, Connection con) throws SQLException {
        sql = "update searches set hits=hits+1 where id_search=" + id; 
        ps = con.prepareStatement(sql);
        ps.executeUpdate();
    }

    public void saveInDB(String source, String dest, double km, Connection con) throws SQLException {
        // save in places table only if not exsits, and in searches table anyway
        if (idSource == -1) {
            sql = "insert into places (country, city, street)"
                    + " values('','" + source + "','')";
            ps = con.prepareStatement(sql);
            idSource = ps.executeUpdate(sql, ps.RETURN_GENERATED_KEYS);
        }
        if (idDest == -1) {
            sql = "insert into places (country, city, street)"
                    + " values('','" + dest + "','')";
            ps = con.prepareStatement(sql);
            idDest = ps.executeUpdate(sql, ps.RETURN_GENERATED_KEYS);
        }
        sql = "insert into searches (id_source, id_dest, km)"
                + " values(" + idSource + "," + idDest + "," + km + ")";
        ps = con.prepareStatement(sql);
        ps.executeUpdate();
    }

    public String callGoogleAPI(String source, String dest, Connection con) throws UnsupportedEncodingException {

        // only if their aren't utf8
        String source2 = URLEncoder.encode(source, "UTF-8");
        String dest2 = URLEncoder.encode(dest, "UTF-8");
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + source2 + "&destination=" + dest2 + "&key=%20AIzaSyCeBdq7rr-R7w7vZCXscLWgEDb3oO9CUhw";

        JSONObject json = null;
        try {
            json = readJsonFromUrl(url);
        } catch (IOException ex) {
            Logger.getLogger(GoogleAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GoogleAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            JSONArray array = (JSONArray) json.get("routes");
            JSONObject obj = (JSONObject) array.get(0);
            JSONArray array1 = (JSONArray) obj.get("legs");
            JSONObject obj2 = (JSONObject) array1.get(0);
            JSONObject obj3 = (JSONObject) obj2.get("distance");
            String kmText = (String) obj3.get("text");
            Pattern pattern = Pattern.compile("[0-9][0-9]*\\.?[0-9]");
            Matcher matcher = pattern.matcher(kmText);
            if (matcher.find()) {
                return matcher.group();
            } else {
                return "המקומות אותרו אך התרחשה שגיאה";
            }
        } catch (Exception e) {
            throw new webservice.exceptions.MyExceptions("המקומות שהזנת לא אותרו. בדוק שנית את תקינותם");
        }
        ///אם לא תקין יזרוק שגיאה באמצע, לתפוס אותה
    }

    public JSONObject readJsonFromUrl(String url) throws IOException, ParseException {
        createTrustManager();
        try {
            int read;
            char[] chars = new char[1024];
            URL urlPath = new URL(url);
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlPath.openStream()));
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(buffer.toString());
            return json;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    public void createTrustManager() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }};
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {;
        }
    }
}
