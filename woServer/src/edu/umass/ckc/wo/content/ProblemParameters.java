package edu.umass.ckc.wo.content;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import edu.umass.ckc.wo.db.DbStudentProblemHistory;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.response.ProblemResponse;
import net.sf.json.*;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Jess
 * Date: 10/9/14
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */



public class ProblemParameters {
    List<Binding> bindings;

    public ProblemParameters() {
        this.bindings = null;
    }

    public ProblemParameters(HashMap<String, ArrayList<String>> params) {
        bindings = new ArrayList<Binding>();
        for (int i = 0; i < params.entrySet().iterator().next().getValue().size(); ++i) {
            Binding b =  new Binding();
            for (String key : params.keySet()) {
                b.addKVPair(key, params.get(key).get(i));
            }
            bindings.add(b);
        }
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public Map<String, String> getRandomAssignment() {
        if (bindings == null || bindings.size() == 0) {
            return null;
        }
        Random randomGenerator = new Random();
        int randomIndex = randomGenerator.nextInt(bindings.size());
        return bindings.get(randomIndex).getMap();
    }

    private List<Binding> generateBindings(List<String> jsonSeenBindings) {
        List<Binding> usedBindings = new ArrayList<Binding>();
        for (String bind : jsonSeenBindings) {
            Binding b = new Binding();
            JSONObject jParams = (JSONObject) JSONSerializer.toJSON(bind);
            Iterator<String> keys = jParams.keys();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                String param = jParams.get(key).toString();
                b.addKVPair(key, param);
            }
            usedBindings.add(b);
        }
        return usedBindings;
    }

    private List<Binding> getUnusedBindings(List<Binding> seenBindings) {
        List<Binding> unusedBindings = new ArrayList<Binding>();
        for (Binding binding : bindings) {
            int i = seenBindings.indexOf(binding);
            if (i == -1) {
                unusedBindings.add(binding);
            }
        }
        return unusedBindings;
    }


    public Binding getUnusedAssignment(int probId, int studId, Connection conn) throws SQLException {
        if (bindings == null || bindings.size() == 0 || conn == null) {
            return null;
        }
        Random randomGenerator = new Random();
        int randomIndex = -1;

        List<Binding> seenBindings = generateBindings(DbStudentProblemHistory.getSeenBindings(probId, studId, conn));
        List<Binding> unusedBindings = getUnusedBindings(seenBindings);
        if (unusedBindings.size() == 0) {
            randomIndex = randomGenerator.nextInt(bindings.size());
            return bindings.get(randomIndex);
        }
        else {
            randomIndex = randomGenerator.nextInt(unusedBindings.size());
            Binding chosenBinding = unusedBindings.get(randomIndex);
            return chosenBinding;
        }
    }

    public void addBindings(ProblemResponse r, int studId, Connection conn, StudentState state) throws SQLException {
        JSONObject rJson = r.getJSON();
        Binding unusedBinding = getUnusedAssignment(r.getProblem().getId(), studId, conn);
        saveAssignment(unusedBinding, state);
        JSONObject pJson = unusedBinding.getJSON(new JSONObject());
        r.setParams(pJson.toString());
        rJson.element("parameters", pJson);

    }
    private void saveAssignment(Binding b, StudentState state) throws SQLException {
        state.setProblemBinding(b.toString());
    }

    public boolean hasUnusedParametrization(int timesEncountered) {
        if (bindings != null && bindings.size() > timesEncountered) {
            return true;
        }
        return false;
    }


    public JSONObject getJSON(JSONObject jo, Map<String, String> bindings) {
        for(Map.Entry<String, String> entry : bindings.entrySet()){
            jo.element(entry.getKey(), entry.getValue());
        }
        return jo;
    }

    public static void main(String[] args) {
        String jsonString = "{\n" +
                "  \"$a\": [\"40\", \"40\"],\n" +
                "  \"$b\": [\"30\", \"30\"],\n" +
                "  \"$c\": [\"x\", \"45\"],\n" +
                "  \"$d\": [\"25\", \"x\"],\n" +
                "  \"$ans_A\": [\"65\", \"65\"],\n" +
                "  \"$ans_B\": [\"45\", \"45\"],\n" +
                "  \"$ans_C\": [\"50\", \"50\"],\n" +
                "  \"$ans_D\": [\"35\", \"35\"],\n" +
                "  \"$ans_E\": [\"45\", \"25\"]\n" +
                "}";

        String usedBinding = "{\n" +
                "\"$a\": \"40\",\n" +
                "\"$b\":\"30\",\n" +
                "\"$c\": \"x\",\n" +
                "\"$d\": \"25\",\n" +
                "\"$ans_A\": \"65\",\n" +
                "\"$ans_B\": \"45\",\n" +
                "\"$ans_C\": \"50\",\n" +
                "\"$ans_D\": \"35\",\n" +
                "\"$ans_E\": \"45\"\n" +
                "}";
//        List<String> bindingStrings = new ArrayList<String>();
//        bindingStrings.add(usedBinding);
//        ProblemParameters parameters = new ProblemParameters(jsonString);
//        List<Binding> b = parameters.generateBindings(bindingStrings);
//        System.out.println(parameters.getUnusedBindings(b));
    }
}
