
package sk.stuba.fiit.reputator.plugin.model.jsonpojo;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Match {

    @SerializedName("Line")
    @Expose
    private String Line;
    @SerializedName("LineNumber")
    @Expose
    private Integer LineNumber;
    @SerializedName("Before")
    @Expose
    private List<String> Before = new ArrayList<String>();
    @SerializedName("After")
    @Expose
    private List<String> After = new ArrayList<String>();

    public String getLine() {
        return Line;
    }

    public void setLine(String Line) {
        this.Line = Line;
    }

    public Integer getLineNumber() {
        return LineNumber;
    }

    public void setLineNumber(Integer LineNumber) {
        this.LineNumber = LineNumber;
    }

    public List<String> getBefore() {
        return Before;
    }

    public void setBefore(List<String> Before) {
        this.Before = Before;
    }

    public List<String> getAfter() {
        return After;
    }

    public void setAfter(List<String> After) {
        this.After = After;
    }

}
