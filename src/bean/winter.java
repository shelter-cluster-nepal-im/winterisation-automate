package bean;
//This bean stores data from 
//select dist_code,district,hlcit_code,vdc,num_hh_damage
//from winterisation where map_cd='-1'
//order by 2;
/**
 *
 * @author Gaurab Pradhan
 */
public class winter {
    String dist_code;
    String district;
    String hlcit_code;
    String vdc;
    String num_hh_damage;

    public String getDist_code() {
        return dist_code;
    }

    public void setDist_code(String dist_code) {
        this.dist_code = dist_code;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getHlcit_code() {
        return hlcit_code;
    }

    public void setHlcit_code(String hlcit_code) {
        this.hlcit_code = hlcit_code;
    }

    public String getVdc() {
        return vdc;
    }

    public void setVdc(String vdc) {
        this.vdc = vdc;
    }

    public String getNum_hh_damage() {
        return num_hh_damage;
    }

    public void setNum_hh_damage(String num_hh_damage) {
        this.num_hh_damage = num_hh_damage;
    }
    
    
}
