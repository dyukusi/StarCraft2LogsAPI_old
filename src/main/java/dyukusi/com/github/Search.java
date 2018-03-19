package dyukusi.com.github;

import org.yaml.snakeyaml.Yaml;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.sql.*;
import java.util.Map;

@Path("search")
public class Search {
    private DB db;

    public Search() throws FileNotFoundException {
        Map y = new Yaml().load(new FileInputStream(new File("/etc/sc2logs.yaml")));
        Map dbSetting = (Map) y.get("database");
        this.db = new DB(
                (String) dbSetting.get("hostname"),
                (String) dbSetting.get("database"),
                (String) dbSetting.get("user"),
                (String) dbSetting.get("password")
        );
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String searchByDisplayName(
        @QueryParam("region") String region,
        @QueryParam("name")   String name,
        @QueryParam("race")   String race,
        @QueryParam("rating") int rating
    ) throws SQLException, IOException {
        if (region == null || name == null || race == null || rating == 0) return "region, name, race and rating parameter required.";
        if (Region.valueOf(region) == null) return "invalid region";

        Connection con = this.db.connect();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT DISTINCT * FROM profile_log WHERE region_id = ? AND name = ? AND race_id IN (?, ?) ORDER BY ABS(rating - ?) ASC, last_played_at DESC, created_at DESC LIMIT 10;");

        PreparedStatement ps = con.prepareStatement(sql.toString());
        ps.setInt(1, Region.valueOf(region).getId());
        ps.setString(2, name);
        ps.setInt(3, Race.valueOf(race).getId());
        ps.setInt(4, Race.Null.getId());
        ps.setInt(5, rating);

        System.out.println(ps.toString());

        ResultSet result = ps.executeQuery();

        ps.close();
        con.close();

        return Util.createResponseJSONString(result);
    }
}
