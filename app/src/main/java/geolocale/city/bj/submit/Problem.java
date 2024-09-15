package geolocale.city.bj.submit;

import org.json.JSONObject;

public class Problem {

    JSONObject problemData;

    public Problem() {
    }

    public JSONObject getProblemData() {
        return problemData;
    }

    public void setProblemData(JSONObject problemData) {
        this.problemData = problemData;
    }
}
