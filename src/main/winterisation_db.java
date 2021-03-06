package main;

import java.io.*;
import java.sql.*;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import util.*;

/**
 *
 * @author Gaurab Pradhan
 */
public class winterisation_db {

    static Connection con = null;
    static String path_winter_db = PropertiesUtil.getPath_winter_db();
    static String psql = PropertiesUtil.getPsql();
    static String command = PropertiesUtil.getCommand();
    static String winter_map_data_filename = PropertiesUtil.getWinter_map_data_filename();
    static String path_sql = PropertiesUtil.getPath_sql();

    public static void main(String[] args) {
        String hostname = PropertiesUtil.getDbUrl();
        hostname = hostname.substring(0, hostname.lastIndexOf(":"));
        command = command.replaceAll("HOSTNAME", hostname);
        String path = winter_map_data_filename.substring(0, winter_map_data_filename.lastIndexOf("/") + 1);
        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
        con = DBConnection.getConnection();
        if (con != null) {
            try {
                delete_winterisation_db(con);
                insert_winterisation_db(con);
                update_winterisation();
                export_winterisation(con); // exports to csv
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void delete_winterisation_db(Connection con) {
        Statement stmt = null;
        if (con != null) {
            try {
                // delete content of winterisation_db
                stmt = con.createStatement();
                String drop = "DROP TABLE IF EXISTS winterisation_db";
                stmt.executeUpdate(drop);
                stmt.close();
                stmt = con.createStatement();
                String sql = "CREATE TABLE winterisation_db"
                        + "(dist_code varchar(11), "
                        + "vdc_code varchar(17), "
                        + "district varchar(15), "
                        + "vdc varchar(28), "
                        + "rep_ag varchar(26), "
                        + "imp_ag varchar(21), "
                        + "target_hh int, "
                        + "type varchar(11), "
                        + "\"full\" varchar(3), "
                        + "details varchar(120), "
                        + "act_state varchar(9), "
                        + "edit_dt varchar, "
                        + "comments varchar(362))";
                stmt.executeUpdate(sql);
                System.out.println("Created winterisation_db table in given shelter database...");
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void insert_winterisation_db(Connection con) throws SQLException {
        PreparedStatement pstmt = null;
        String dist_code = "", vdc_code = "", district = "", vdc = "", rep_ag = "", imp_ag = "", target_hh = "", type = "", full = "", details = "", act_state = "", edit_dt = "", comments = "";
        if (con != null) {
//            con.setAutoCommit(false);
            BufferedReader br = null;
            try {
                String sCurrentLine;
                System.out.println("Inserting Winterisation into the winterisation_db table...");
                br = new BufferedReader(new FileReader(path_winter_db));
                int i = 0;
                String insert = "INSERT INTO winterisation_db VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
                pstmt = con.prepareStatement(insert);
                while ((sCurrentLine = br.readLine()) != null) {
                    if (i != 0) {
//                        System.out.println(sCurrentLine);
                        String[] temp = sCurrentLine.split(",");
                        if (temp.length > 11) {
                            dist_code = temp[0];
                            vdc_code = temp[1];
                            district = temp[2];
                            vdc = temp[3];
                            rep_ag = temp[4];
                            imp_ag = temp[5];
                            target_hh = temp[6];
                            type = temp[7];
                            full = temp[8];
                            details = temp[9];
                            act_state = temp[10];
                            edit_dt = temp[11];
                            if (temp.length == 13) {
                                comments = temp[12];
                            } else {
                                comments = "";
                            }
                            pstmt.setString(1, dist_code);
                            pstmt.setString(2, vdc_code);
                            pstmt.setString(3, district);
                            pstmt.setString(4, vdc);
                            pstmt.setString(5, rep_ag);
                            pstmt.setString(6, imp_ag);
                            if (!target_hh.isEmpty()) {
                                pstmt.setInt(7, Integer.parseInt(target_hh));
                            } else {
                                pstmt.setNull(7, Types.INTEGER);
                            }
                            pstmt.setString(8, type);
                            pstmt.setString(9, full);
                            pstmt.setString(10, details);
                            pstmt.setString(11, act_state);
                            pstmt.setString(12, edit_dt);
                            pstmt.setString(13, comments);
                            pstmt.addBatch();
//                            int result = pstmt.executeUpdate();
                            System.out.println("Row" + i);
                        }
                    } else {
                        System.out.println(sCurrentLine);
                    }
                    i++;
                }
                int[] result = pstmt.executeBatch();
                pstmt.close();
//                int[] updateCounts = pstmt.executeBatch();
                System.out.println(result.length + " Rows inserted into winterisation_db");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void update_winterisation() {
        System.out.println("Updating winterisation table");
        File f = new File(path_sql);
        String abs_path = f.getAbsolutePath();
        String sql_command = psql + command + " " + abs_path;
        try {
            String line;
//            Process p = Runtime.getRuntime().exec("psql -U postgres -d shelter -h localhost -f d://winterisation.sql");
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec(sql_command);
            System.out.println(sql_command);
//            Process p = rt.exec(command);
            BufferedReader input
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private static void export_winterisation(Connection con) {
        Statement stmt;
        String query;
        System.out.println("Preparing to export winterisation dataset.");
        try {
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            //For comma separated file
//            query = "Copy (Select * From winterisation) To '" + winter_map_data_filename + "' With CSV HEADER";
            query = "Select * From winterisation";
            CopyManager cm = new CopyManager((BaseConnection) con);
            File file = new File(winter_map_data_filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

//and finally execute the COPY command to the file with this method:
            cm.copyOut("COPY (" + query + ") TO STDOUT WITH CSV HEADER", fileOutputStream);
            stmt.execute(query);
            System.out.println("Dataset For winterisation map created Path : " + winter_map_data_filename);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            stmt = null;
        }
    }
}
