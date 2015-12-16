package main;

import bean.*;
import java.io.*;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.*;

/**
 *
 * @author Gaurab Pradhan
 */
public class WinterUncover {

    static String path_Ward = PropertiesUtil.getPath_Ward();
    static String path_reach = PropertiesUtil.getPath_reach();
    static String path_pop = PropertiesUtil.getPath_pop();

    public static void main(String[] args) {
        File file = new File(path_pop);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
        try {
            PropertiesUtil.loadPropertiesFile();
            Connection con = DBConnection.getConnection();
            List<winter> winterList = new ArrayList<winter>();
            List<pop_concern> popList = new ArrayList<pop_concern>();
            winterList = getDatawinterisation(con);
            con.close();
            popList = filter_hh_1500(winterList);
            write(popList); // Generates pop concern file
        } catch (SQLException ex) {
            Logger.getLogger(WinterUncover.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static List<winter> getDatawinterisation(Connection con) {
        Statement stmt = null;
        List<winter> winterList = new ArrayList<winter>();
        try {
            if (con != null) {
                stmt = con.createStatement();
                String sql = "select dist_code,district,hlcit_code,vdc,num_hh_damage from winterisation where map_cd='-1' order by 2";
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    winter w = new winter();
                    w.setDist_code(rs.getString(1));
                    w.setDistrict(rs.getString(2));
                    w.setHlcit_code(rs.getString(3));
                    w.setVdc(rs.getString(4));
                    w.setNum_hh_damage(rs.getString(5));
                    winterList.add(w);
                }
                rs.close();
                stmt.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(WinterUncover.class.getName()).log(Level.SEVERE, null, ex);
        }
        return winterList;
    }

    private static List<pop_concern> filter_hh_1500(List<winter> winterList) {
        List<pop_concern> popList = new ArrayList<pop_concern>();
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(path_Ward));
            int i = 0;
            while ((sCurrentLine = br.readLine()) != null) {
                pop_concern pop = new pop_concern();
//                System.out.println(sCurrentLine);
                if (i == 0) {
                    pop.setDist_code("dist_code");
                    pop.setDistrict("district");
                    pop.setHlcit_code("hlcit_code");
                    pop.setVdc("vdc");
                    pop.setNum_hh_damage("num_hh_damage");

                    pop.setNum_hh_above1500("num_hh_above1500");
                    pop.setNum_hh_ts("pct_hh_ts");// reach %
                    pop.setNum_hh_ts_above1500("num_hh_ts_above1500");
                    popList.add(pop);

                } else {
                    BufferedReader br_reach = null;
                    String line;
                    String[] temp = sCurrentLine.split(",");
                    if (temp.length > 4) {
                        for (int x = 0; x < winterList.size(); x++) {
                            String vdc_code = winterList.get(x).getHlcit_code();
                            if (vdc_code.trim().equals(temp[2].trim())) {
                                pop.setDist_code(winterList.get(x).getDist_code());
                                pop.setDistrict(winterList.get(x).getDistrict());
                                pop.setHlcit_code(winterList.get(x).getHlcit_code());
                                pop.setVdc(winterList.get(x).getVdc());
                                pop.setNum_hh_damage(winterList.get(x).getNum_hh_damage());

                                pop.setNum_hh_above1500(temp[4]);
                                br_reach = new BufferedReader(new FileReader(path_reach));
                                int k = 0;
                                while ((line = br_reach.readLine()) != null) {
                                    if (k > 0) {
                                        String[] div = line.split(",");
                                        String dis_code = winterList.get(x).getDist_code();
                                        if (div.length > 2) {
                                            if (dis_code.trim().equals(div[0].trim())) {
                                                float pct_ts = Float.parseFloat(div[2]);
                                                float num_hh_damage = Float.parseFloat(temp[4]);
                                                pop.setNum_hh_ts(div[2]);
                                                pop.setNum_hh_ts_above1500(String.valueOf(pct_ts * num_hh_damage));
                                                popList.add(pop);
                                                br_reach.close();
                                                break;
                                            }
                                        }
                                    }
                                    k++;
                                }
                                break;
                            }
                        }
                    }
                }
                i++;
            }
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
        return popList;
    }

    private static void write(List<pop_concern> popList) {
        BufferedWriter bw = null;
        try {
            File file = new File(path_pop + "/pop_concern.csv");
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            for (int i = 0; i < popList.size(); i++) {
                String content = popList.get(i).getDist_code() + ","
                        + popList.get(i).getDistrict() + ","
                        + popList.get(i).getHlcit_code() + ","
                        + popList.get(i).getVdc() + ","
                        + popList.get(i).getNum_hh_damage() + ","
                        + popList.get(i).getNum_hh_above1500() + ","
                        + popList.get(i).getNum_hh_ts() + ","
                        + popList.get(i).getNum_hh_ts_above1500();
                bw.write(content);
                bw.write("\n");
            }
            bw.close();
            System.out.println("Population of Concern file created path : " + path_pop + "/pop_concern.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
