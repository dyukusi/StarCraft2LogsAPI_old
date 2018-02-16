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
        @QueryParam("name") String name,
        @QueryParam("race") String race
    ) throws SQLException {
        if (name == null || race == null) return "name and race parameter required.";

        Connection con = this.db.connect();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * FROM profile_log WHERE name = ? AND race = ? ORDER BY last_played_at DESC, created_at DESC LIMIT 10;");

        PreparedStatement ps = con.prepareStatement(sql.toString());
        ps.setString(1, name);
        ps.setString(2, race);

        try {
            ResultSet result = ps.executeQuery();
            return createResponseJSONString(result);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            con.close();
            ps.close();
        }

        return "internal server error";
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
                        if (str.equals("null")) {
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
