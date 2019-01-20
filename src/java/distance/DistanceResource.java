package distance;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("distance")
public class DistanceResource {

    public DistanceResource() { }

    @GET
    @Path("/{THE_SOURCE}/{THE_DESTINATION}")
    @Produces("text/html")
    public Response getKM(@PathParam("THE_SOURCE") String source, @PathParam("THE_DESTINATION") String dest) 
            throws UnsupportedEncodingException, ClassNotFoundException, SQLException  {
        Connection con = createConnection(); 
        GoogleAPI g = new GoogleAPI();
        String km = g.manageGetKm(source, dest, con);
        return Response.ok(km).header("Access-Control-Allow-Origin", "*").build();
    }
    
    @GET
    @Path("/popularSearch")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPopularSearch() throws ClassNotFoundException, SQLException  { 
        Connection con =createConnection();        
        PopularSearch p = new PopularSearch();    
        p.fillFromDB(con, p);       
       return Response.ok(p).header("Access-Control-Allow-Origin", "*").build();
    }

    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        String encode="?useUnicode=true&characterEncoding=utf8";
        Connection con = DriverManager.getConnection("jdbc:mysql://sql10.freemysqlhosting.net/sql10274573"+encode, "sql10274573", "gstbcRAecB");
        return con;
    }
}
