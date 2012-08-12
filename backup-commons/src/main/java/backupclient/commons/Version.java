package backupclient.commons;

import org.json.simple.JSONObject;

public class Version {

    public final String name, updater_link, agent_link, id;
    public Version(String id, String name, String update_link, String agent_link) {
        this.id = id;
        this.name = name;
        this.updater_link = update_link;
        this.agent_link = agent_link;
    }
    
    static Version buildFromJson(JSONObject json, Machine machine) {
        try {
            String id =  ((Long) json.get("id")).toString();
            String name = (String) json.get("name");
            String updater_link = (String) json.get("updater_link");
            String agent_link = (String) json.get("agent_link");
            return new Version(id, name, updater_link, agent_link);

        } catch (NullPointerException e) {
            machine.log_error("Error getting info about the Client version");
        }
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  Version)) return false;
        Version v = (Version) o;
        return name.equals(v.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return "Focus24 Version" + name;
    }
}
