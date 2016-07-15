package sk.stuba.fiit.reputator.plugin.core;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class JsonConverter {

    private final Gson gson;

    public JsonConverter() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public <T> List<T> convertJsonStringToPojo(Class<T> type, String json) {
        List<T> result = new ArrayList<>();
        Set<String> individualJsons = getReposJsons(json);
        individualJsons.stream().forEach(i ->
                                            {
                                                T pojo = gson.fromJson(i, type);
                                                result.add(pojo);
                                            });
        return result;
    }

    private Set<String> getReposJsons(String completeJson) {
        JsonObject wrapper = gson.fromJson(completeJson, JsonObject.class);
        JsonObject allResults = gson.fromJson(wrapper.get("Results").toString(), JsonObject.class);
        Set<String> result = new HashSet<>();

        if(allResults.has("org.eclipse.mylyn.all")) {
            result.add(allResults.get("org.eclipse.mylyn.all").toString());
        }

        if (allResults.has("org.eclipse.mylyn.builds")) {
            result.add(allResults.get("org.eclipse.mylyn.builds").toString());
        }

        if (allResults.has("org.eclipse.mylyn.commons")) {
            result.add(allResults.get("org.eclipse.mylyn.commons").toString());
        }

        if (allResults.has("org.eclipse.mylyn.context")) {
            result.add(allResults.get("org.eclipse.mylyn.context").toString());
        }

        if (allResults.has("org.eclipse.mylyn.context.mft")) {
            result.add(allResults.get("org.eclipse.mylyn.context.mft").toString());
        }

        if (allResults.has("org.eclipse.mylyn.docs")) {
            result.add(allResults.get("org.eclipse.mylyn.docs").toString());
        }

        if (allResults.has("org.eclipse.mylyn.docs.vex")) {
            result.add(allResults.get("org.eclipse.mylyn.docs.vex").toString());
        }

        if (allResults.has("org.eclipse.mylyn.incubator")) {
            result.add(allResults.get("org.eclipse.mylyn.incubator").toString());
        }

        if (allResults.has("org.eclipse.mylyn.reviews")) {
            result.add(allResults.get("org.eclipse.mylyn.reviews").toString());
        }

        if (allResults.has("org.eclipse.mylyn.reviews.ui")) {
            result.add(allResults.get("org.eclipse.mylyn.reviews.ui").toString());
        }

        if (allResults.has("org.eclipse.mylyn.tasks")) {
            result.add(allResults.get("org.eclipse.mylyn.tasks").toString());
        }

        if (allResults.has("org.eclipse.mylyn.versions")) {
            result.add(allResults.get("org.eclipse.mylyn.versions").toString());
        }

        return result;
    }
}
