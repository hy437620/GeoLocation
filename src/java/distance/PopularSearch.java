package distance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PopularSearch {

    private String source;
    private String dest;
    private int hits;

    public void fillFromDB(Connection con, PopularSearch p) throws SQLException {
        String sql = "select s.hits, p1.city source, p2.city dest\n" +
                    " from searches s inner join places p1 on s.id_source=p1.id_place \n" +
                    " inner join places p2 on s.id_dest=p2.id_place\n" +
                    " order by s.hits desc limit 1"; // for bonus- limit 10
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet res = ps.executeQuery(sql); 
        if (res.next()) {
            p.setHits(res.getInt("hits"));
            p.setSource(res.getString("source")); 
            p.setDest(res.getString("dest"));
        }
//        int idSource, idDest;
//        String sql = "select * from searches order by hits desc limit 1";
//        PreparedStatement ps = con.prepareStatement(sql);
//        ResultSet res = ps.executeQuery(sql); 
//        if (res.next()) {
//            idSource= res.getInt("id_source"); 
//            idDest= res.getInt("id_dest");
//            p.setHits(res.getInt("hits"));
//        }
//        else {
//            throw new webservice.exceptions.MyExceptions("שגיאה");
//        }
//        sql = "select * from places where id_place in ("+idSource+","+idDest+")";
//        ps = con.prepareStatement(sql);
//        res = ps.executeQuery(sql); 
//        for (int i=1; res.next(); i++) {
//            if(i==1)
//                p.setSource(res.getString("city")); 
//            else
//                p.setDest(res.getString("city")); 
//        }
    }

    public PopularSearch(String source, String dest, int hits) {
        this.source = source;
        this.dest = dest;
        this.hits = hits;
    }

    public PopularSearch() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

}
