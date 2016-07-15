
package sk.stuba.fiit.reputator.plugin.model.jsonpojo;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Hit {

    @SerializedName("Filename")
    @Expose
    private String Filename;
    @SerializedName("Matches")
    @Expose
    private List<Match> Matches = new ArrayList<Match>();

    public String getFilename() {
        return Filename;
    }

    public void setFilename(String Filename) {
        this.Filename = Filename;
    }

    public List<Match> getMatches() {
        return Matches;
    }

    public void setMatches(List<Match> Matches) {
        this.Matches = Matches;
    }

}
