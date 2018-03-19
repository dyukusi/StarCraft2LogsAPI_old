package dyukusi.com.github;

import org.yaml.snakeyaml.Yaml;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Path("league")
public class League {
    private DB db;

    public League() throws FileNotFoundException {
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
    public String getLeagueInfo(
        @QueryParam("regionId") int regionId
    ) throws SQLException, IOException {
        Connection con = this.db.connect();
        PreparedStatement selectCurrentSeasonPS = con.prepareStatement(
                "SELECT * FROM season WHERE region_id = ? ORDER BY id DESC, number DESC"
        );
        selectCurrentSeasonPS.setInt(1, regionId);

        ResultSet seasonResult = selectCurrentSeasonPS.executeQuery();
        if (!seasonResult.next()) return "season not found";
        int currentSeasonId = seasonResult.getInt("id");

        PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM league WHERE region_id = ? AND season_id = ?;"
        );
        ps.setInt(1, regionId);
        ps.setInt(2, currentSeasonId);

        ResultSet result = ps.executeQuery();

        ps.close();
        con.close();

        return Util.createResponseJSONString(result);
    }
}
