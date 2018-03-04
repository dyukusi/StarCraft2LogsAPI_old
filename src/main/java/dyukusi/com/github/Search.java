package dyukusi.com.github;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.yaml.snakeyaml.Yaml;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
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
        @QueryParam("race")   String race
    ) throws SQLException, IOException {
        if (region == null || name == null || race == null) return "region, name and race parameter required.";
        if (Region.valueOf(region) == null) return "invalid region";

        Connection con = this.db.connect();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * FROM profile_log WHERE region_id = ? AND name = ? AND race_id IN (?, ?) ORDER BY last_played_at DESC, created_at DESC LIMIT 10;");

        PreparedStatement ps = con.prepareStatement(sql.toString());
        ps.setInt(1, Region.valueOf(region).getId());
        ps.setString(2, name);
        ps.setInt(3, Race.valueOf(race).getId());
        ps.setInt(4, Race.Null.getId());

        ResultSet result = ps.executeQuery();
        return createResponseJSONString(result);
    }

    private String createResponseJSONString(ResultSet result) throws SQLException, IOException {
        ResultSetMetaData meta = result.getMetaData();
        ArrayList<Column> columns = new ArrayList<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            columns.add(new Column(meta.getColumnName(i), meta.getColumnTypeName(i)));
        }

        JsonFactory f = new JsonFactory();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        JsonGenerator g = f.createGenerator(b, JsonEncoding.UTF8);

        g.writeStartArray();

        while (result.next()) {
            g.writeStartObject();
            for (Column column : columns) {
                g.writeFieldName(column.getColumnName());

                switch (column.getTypeName()) {
                    case "INT":
                        g.writeNumber(result.getInt(column.getColumnName()));
                        break;
                    case "VARCHAR":
                    case "TEXT":
                        String str = result.getString(column.getColumnName());

                        if (str == null) {
                            g.writeNull();
                        } else {
                            g.writeString(result.getString(column.getColumnName()));
                        }
                        break;
                    case "DATETIME":
                        g.writeNumber(result.getTimestamp(column.getColumnName()).getTime() / 1000);
                        break;
                    default:
                        break;
                }
            }
            g.writeEndObject();
        }

        g.writeEndArray();
        g.flush();

        return b.toString();
    }
}
