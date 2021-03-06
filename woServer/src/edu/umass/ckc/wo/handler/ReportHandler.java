package edu.umass.ckc.wo.handler;


import edu.umass.ckc.wo.event.admin.AdminViewReportEvent;
import edu.umass.ckc.wo.html.admin.SelectClassPage;
import edu.umass.ckc.wo.woreports.*;
import ckc.servlet.servbase.View;
import ckc.servlet.servbase.ServletEvent;
import ckc.servlet.servbase.UserException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;

/**
 * User: marshall
 * Date: Mar 2, 2004
 * Time: 10:08:01 AM
 *
 */
public class ReportHandler {
    private int teacherId;

    public static final int PER_SKILL_HTML = 2 ;
    public static final int OVERALL_HTML = 3;
    public static final int PER_PROB_HTML = 4;
    public static final int PER_STUD_HTML= 5;
    public static final int PER_STUD_PER_SKILL_HTML = 7;
    public static final int PER_STUD_PREPOST_HTML = 10;
    public static final int PASSWORDS_HTML = 12;
    public static final int EPISODICDATA_HTML = 13 ;
    public static final int PER_CLIENT_TIME_CSV = 14;
    public static final int PER_PRETEST_SUMMARY_HTML = 15;
    public static final int PER_EMOTION_HTML = 16 ;
    public static final int PER_INTERVENTION_HTML = 17 ;
    public static final int PER_PRETEST_PROB_HTML = 18;
    public static final int PER_STUDENT_LEARNING_HUT_ACTIVITY_HTML = 19 ;
    public static final int PER_PROBLEM_DIFFICULTY_ATTEMPTS_HTML = 20 ;
    public static final int PER_PROBLEM_DIFFICULTY_HINTS_HTML = 21 ;
    public static final int PER_PROBLEM_DIFFICULTY_TIME_HTML = 22 ;
    public static final int STUDENT_TOPIC_LEVELS_HTML = 23 ;
    public static final int STUDENT_TOPIC_MASTERY_TRAJECTORY_HTML = 24 ;
    public static final int STUDENT_ALL_TOPICS_MASTERY_TRAJECTORY_HTML = 25 ;
    public static final int CLASS_TOPIC_MASTERY_TRAJECTORY_HTML = 26 ;
    public static final int COLLABORATIVE_HTML = 27 ;
    public static final int TEST_G_CHARTS = 28;
    public static final int OVERALL_CSV = 30;
    public static final int PER_PROB_CSV = 40;
    public static final int PER_STUD_CSV = 50;
    public static final int PER_STUD_PER_SKILL_CSV = 70;
    public static final int PER_STUD_PREPOST_CSV = 100;
    public static final int PER_EMOTION_CSV = 160 ;

    public ReportHandler () {

    }

    public ReportHandler (int teacherId) {
        this.teacherId = teacherId;
    }

    public View handleEvent(ServletContext sc, ServletEvent se, Connection conn, HttpServletRequest req) throws Exception {
        AdminViewReportEvent e = (AdminViewReportEvent) se;

        if (e.getState().equals(AdminViewReportEvent.CHOOSE_REPORT))
            return new ChooseReportPage();

        else if (e.getState().equals(AdminViewReportEvent.CHOOSE_CLASS))
            return buildChooseClassPage(e, conn);

        else if (e.getState().equals(AdminViewReportEvent.CHOOSE_STUDENT)) {
            String nextState = AdminViewReportEvent.SHOW_REPORT;

            return new ChooseStudentPage(conn, nextState, e.getReportId(), e.getClassId());

        }
        else if (e.getState().equals(AdminViewReportEvent.SHOW_REPORT))
            return buildReport(e, conn, req);

       throw new UserException("Event state unknown: " + e.getState());

    }

 public List<TeachersClass> getClasses (Connection conn, int teacherId)  throws Exception {

    String SQL = "SELECT id,school,town,section,name,schoolYear "+
                 "FROM class where teacherId=? order by schoolYear DESC, name ASC";

    PreparedStatement ps = conn.prepareStatement(SQL);
    ps.setInt(1,teacherId);
    ResultSet rs = ps.executeQuery();
    List<TeachersClass> results = new ArrayList<TeachersClass>();
    while (rs.next()) {
      int    id      = rs.getInt("id");
      String school  = rs.getString("school");
      String town  = rs.getString("town");
      String section  = rs.getString("section");
      String name    = rs.getString("name");
      String year    = rs.getString("schoolYear");
      results.add(new TeachersClass(id,name,year,section,school,town));
    }
     return results;
 }

    private View  buildChooseClassPage(AdminViewReportEvent e, Connection conn) throws Exception {
        List<TeachersClass> classes = getClasses(conn,this.teacherId);
        return new SelectClassPage(classes, teacherId);
//        String nextState = "";
//        int repId = e.getReportId();
//        switch (repId) {
//            case 1:
//            case 2:
//            case 8:
//                nextState = AdminViewReportEvent.CHOOSE_STUDENT;
//                break;
//            case 3:
//            case 4:
//            case 5:
//            case 6:
//            case 7:
//            case 9:
//                nextState = AdminViewReportEvent.SHOW_REPORT;
//                break;
//
//        }
//
//        ChooseClassPage repPage = new ChooseClassPage(conn, nextState, e.getReportId());
//        return repPage;
    }

    private View buildReport(AdminViewReportEvent e, Connection conn, HttpServletRequest req) throws Exception {
        Report r=null;
        int id=-1;
        switch (e.getReportId()) {
//            case 1:   // Not in use
//                r=new Report1();
//                id=e.getStudId();
//                break;
            case PER_SKILL_HTML: // 3/16/10 dm checked v1/v2 working.  
                r=new PerSkillClassSummaryReport();
                id=e.getClassId();
                break;
            case OVERALL_HTML:  // dm checked v1/v2 working
                r=new ClassSummaryReport();
                id=e.getClassId();
                break;
            case PER_PROB_HTML: // dm checked v1/v2 working
//                r=new PerProbClassSummaryReport2();
                r=new ProblemDifficultyReport();
                int classid=e.getClassId();
                String skillid = e.getExtraParam() ;

                if ( skillid != null ) {
//                    PerProbClassSummaryReport2 rep = new PerProbClassSummaryReport2();
                    ProblemDifficultyReport rep = new ProblemDifficultyReport();
                    rep.createReport(conn, classid, (new Integer(skillid)).intValue(), e,req);
                    return rep ;
                }
                id = classid ;
                break;
            case STUDENT_TOPIC_LEVELS_HTML: // report 23
                id=e.getClassId();
                r = new ClassTopicLevelsReport();
                break;
            case PER_STUD_HTML: // dm checked v1/v2 working
                r=new PerStudClassSummaryReport();
                classid=e.getClassId();
                int studid=e.getStudId() ;
                String gain=e.getExtraParam() ;

                if ( classid > 0 && studid > 0 ) {
                    PerStudClassSummaryReport rep = new PerStudClassSummaryReport() ;
                    rep.createReport(conn, classid, studid, gain, e,req);
                    return rep ;
                }
                id = classid ;
                break;
            case PER_PROBLEM_DIFFICULTY_ATTEMPTS_HTML: //Ivon for problem difficulty estimates
                r=new PerProblemDifficultyAttemptsReport() ;
                break ;
            case PER_PROBLEM_DIFFICULTY_HINTS_HTML: //Ivon for problem difficulty estimates
                r=new PerProblemDifficultyHintsReport() ;
                break ;
            case PER_PROBLEM_DIFFICULTY_TIME_HTML: //Ivon for problem difficulty estimates
                r=new PerProblemDifficultyTimeReport() ;
                break ;

//            case 6:   // Not in use
//                r=new Report6();
//                id=e.getClassId();
//                break;
//            case PER_STUD_PER_SKILL_HTML:  // Not in use
//                r=new Report7();
//                id=e.getClassId();
//                break;
//           case 8: // Not in use
//                r=new Report8();
//                id=e.getStudId();
//                break;
//           case 9:    // Not in use
//                r=new Report9();
//                id=e.getClassId();
//                break;
           case PER_STUD_PREPOST_HTML: // 3/16/10 dm checked for v1 Blinder,v2 compat on Henderson
                r=new PerStudPrePostReport();
                id=e.getClassId();
                break ;
            case PASSWORDS_HTML:   // dm checked for v2 compat
                 r=new ReportUserNamesAndPasswords();
                 id=e.getClassId();
                 break ;
            case EPISODICDATA_HTML:  // 3/17/10 dm 
                 r=new ReportProblemAndEmotions(e.getExtraParam());
                 id=e.getClassId();
                 break ;
            case COLLABORATIVE_HTML:
                 r = new ReportCollaborativeLearning();
                 id = e.getClassId() ;
                 break ;
            case PER_CLIENT_TIME_CSV:
                 r=new StartStopTimeSensorReport();
                 id=e.getClassId();
                 break ;
            case PER_PRETEST_SUMMARY_HTML:  // dm 3/16/10 verified v1 Blinder,v2 Henderson
                 r=new PrePostProblemSummaryReport();
                 id=e.getClassId();
                 break ;
            case PER_EMOTION_HTML:    // dm 3/17/10 converted to v1/v2 compatibility
                r = new PerStudPerEmotionClassSummaryReport();
                id=e.getClassId();
                break;
            case PER_INTERVENTION_HTML:
                r = new ReportInterventionImpact();
                id=e.getClassId();
                break;
            case PER_PRETEST_PROB_HTML:  // dm 3/16/10 verified v1 Blinder, v2 Henderson
                r = new PerStudPrePostDetailReport();
                id=e.getClassId();
                break;
            case PER_STUDENT_LEARNING_HUT_ACTIVITY_HTML:    // dm 3/17/10 converted to v1/v2
                r = new PerStudLearningHutActivityReport();
                id=e.getClassId();
                break;
            case STUDENT_TOPIC_MASTERY_TRAJECTORY_HTML:
                r = new StudentTopicMasteryTrajectoryReport();
                id =e.getClassId();
                break;
            case STUDENT_ALL_TOPICS_MASTERY_TRAJECTORY_HTML:
                r = new StudentAllTopicsMasteryTrajectoryReport();
                id =e.getClassId();
                break;
            case CLASS_TOPIC_MASTERY_TRAJECTORY_HTML:
                r = new ClassTopicMasteryTrajectoryReport();
                id =e.getClassId();
                break;

//            case 19:    // Not used
//                 r=new Report19();
//                 id=e.getClassId();
//                 break ;


        }
        r.createReport(conn, id, e, req);
        return r;
    }

}
