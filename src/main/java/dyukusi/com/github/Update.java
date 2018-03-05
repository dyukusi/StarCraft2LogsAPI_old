package dyukusi.com.github;

import github.dyukusi.API;
import org.yaml.snakeyaml.Yaml;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.sql.*;
import java.util.Map;

@Path("update")
public class Update {
    private DB db;

    public Update() throws FileNotFoundException {
        Map y = new Yaml().load(new FileInputStream(new File("/etc/sc2logs.yaml")));
        Map dbSetting = (Map) y.get("database");
        this.db = new DB(
                (String) dbSetting.get("hostname"),
                (String) dbSetting.get("database"),
                (String) dbSetting.get("user"),
                (String) dbSetting.get("password")
        );
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String searchByDisplayName(
            @FormParam("regionId")  int regionId,
            @FormParam("profileId") int profileId,
            @FormParam("raceId")    int raceId
    ) throws SQLException, IOException {
        Connection con = this.db.connect();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM profile_log WHERE region_id = ? AND id = ? AND race_id = ? ORDER BY created_at DESC;");
        PreparedStatement ps = con.prepareStatement(sql.toString());

        ps.setInt(1, regionId);
        ps.setInt(2, profileId);
        ps.setInt(3, raceId);

        System.out.println(ps.toString());

        ResultSet result = ps.executeQuery();
        con.close();

        if (!result.next()) {
            return "profile not found";
        }

        int ladderId = result.getInt("ladder_id");

        // update
        API.updateProfile(regionId, ladderId, profileId);

        return String.format("update process finished. regionId: %d, profileId: %d, raceId %d",
                regionId,
                profileId,
                raceId
        );
    }
}
