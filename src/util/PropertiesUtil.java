package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Gaurab Pradhan
 */
public class PropertiesUtil {

    private static final String APPLICATION_PROPERTY_FILE = "conf/application.properties";
    static String username;
    static String password;
    static String dbName;
    static String dbUrl;
    static String psql;
    static String command;
    static String path_winter_db;
    static String winter_map_data_filename;
    static String path_Ward;
    static String path_reach;
    static String path_pop;

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getDbName() {
        return dbName;
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getPsql() {
        return psql;
    }

    public static String getCommand() {
        return command;
    }

    public static String getPath_winter_db() {
        return path_winter_db;
    }

    public static String getWinter_map_data_filename() {
        return winter_map_data_filename;
    }

    public static String getPath_Ward() {
        return path_Ward;
    }

    public static String getPath_reach() {
        return path_reach;
    }

    public static String getPath_pop() {
        return path_pop;
    }

    public static void loadPropertiesFile() {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(APPLICATION_PROPERTY_FILE));
            username = prop.getProperty("username");
            password = prop.getProperty("password");
            dbName = prop.getProperty("dbName");
            dbUrl = prop.getProperty("dbUrl");

            psql = prop.getProperty("psql");
            command = prop.getProperty("command");
            path_winter_db = prop.getProperty("path_winter_db");
            winter_map_data_filename = prop.getProperty("winter_map_data_filename");
            path_Ward = prop.getProperty("path_Ward");
            path_reach = prop.getProperty("path_reach");
            path_pop = prop.getProperty("path_pop");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
