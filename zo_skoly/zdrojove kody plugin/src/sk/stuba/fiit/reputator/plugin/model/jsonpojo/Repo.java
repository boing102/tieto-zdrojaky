
package sk.stuba.fiit.reputator.plugin.model.jsonpojo;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Repo {

    @SerializedName("Matches")
    @Expose
    private List<Hit> Hits = new ArrayList<Hit>();
    @SerializedName("FilesWithMatch")
    @Expose
    private Integer FilesWithMatch;
    @SerializedName("Revision")
    @Expose
    private String Revision;

    public List<Hit> getHits() {
        return Hits;
    }

    public void setHits(List<Hit> Hits) {
        this.Hits = Hits;
    }

    public Integer getFilesWithMatch() {
        return FilesWithMatch;
    }

    public void setFilesWithMatch(Integer FilesWithMatch) {
        this.FilesWithMatch = FilesWithMatch;
    }

    public String getRevision() {
        return Revision;
    }

    public void setRevision(String Revision) {
        this.Revision = Revision;
    }

}
