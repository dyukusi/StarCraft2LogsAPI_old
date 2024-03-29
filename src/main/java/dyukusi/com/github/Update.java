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

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String searchByDisplayName(
            @QueryParam("regionId")  int regionId,
            @QueryParam("profileId") int profileId,
            @QueryParam("raceId")    int raceId
    ) {

        try {
            System.out.println(String.format(
                    "Update request received. regionId: %d, profileId: %d, raceId %d",
                    regionId, profileId, raceId
            ));

            Connection con = this.db.connect();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM profile_log WHERE region_id = ? AND id = ? AND race_id = ? ORDER BY created_at DESC");
            ps.setInt(1, regionId);
            ps.setInt(2, profileId);
            ps.setInt(3, raceId);

            ResultSet result = ps.executeQuery();
            if (!result.next()) {
                return "profile not found";
            }

            int ladderId = result.getInt("ladder_id");
            ps.close();
            con.close();

            // update
            API.updateProfile(regionId, ladderId, profileId);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return String.format("update process finished. regionId: %d, profileId: %d, raceId %d",
                regionId,
                profileId,
                raceId
        );
    }
}
