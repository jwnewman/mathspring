package edu.umass.ckc.wo.woserver;


import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.beans.Classes;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.event.admin.*;
import ckc.servlet.servbase.UserException;
import edu.umass.ckc.wo.handler.*;
import edu.umass.ckc.wo.html.admin.Variables;
import ckc.servlet.servbase.ServletEvent;
import ckc.servlet.servbase.View;
import edu.umass.ckc.wo.content.PrePostProblemDefn;
import edu.umass.ckc.wo.db.DbPrePost;
import edu.umass.ckc.wo.tutor.Settings;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Connection;


public class AdminHandler {
    public static final Logger logger = Logger.getLogger(AdminHandler.class);
    public static final String USERID = "userId";
    public static final String CLASSID = "classId";

    public AdminHandler() {
    }

    private HttpSession getSession (HttpServletRequest r) {
        HttpSession s = r.getSession();
        if (s.isNew())
            s.setMaxInactiveInterval(-1);  // make it persist indefinitely
        return s;
    }

    // todo Class creation needs to create a user/login for the class so that report access requires
    // the user/password
    /**
     *
     * @param servletRequest
     * @param servletResponse
     * @param sc
     * @param conn
     * @param e                                                      H
     * @param servletOutput
     * @return  true if the calling servlet is going to get some stuff to write into its PrintStream. false if
     * this forwards the request on to JSP
     * @throws Exception
     */
    public boolean handleEvent(HttpServletRequest servletRequest, HttpServletResponse servletResponse, ServletContext sc,
                               Connection conn, ServletEvent e, StringBuffer servletOutput) throws Exception {
        logger.info("AdminHandler.handleEvent " + e.getClass().getName());
        View v = null;
        Variables vv = new Variables(servletRequest.getServerName(),
                servletRequest.getServletPath(),
                servletRequest.getServerPort());
        if (e instanceof AdminTeacherLoginEvent) {
            if (Settings.useAdminServletSession) {
                HttpSession sess = servletRequest.getSession();
                sess.setMaxInactiveInterval(Settings.adminServletSessionTimeoutSeconds); // allow 30 minutes of inactive time before session expires
            }
            new AdminToolLoginHandler().handleEvent(conn,vv,(AdminTeacherLoginEvent) e, servletRequest, servletResponse);
            return false;  // request forwarded to JSP, so tell caller not to write servlet output
        }
        if (Settings.useAdminServletSession && !(e instanceof UserRegistrationEvent)) {
            HttpSession sess = servletRequest.getSession(false);
            if (sess == null) {
                servletRequest.setAttribute("message","Your session has expired.   You must relogin");
                servletRequest.getRequestDispatcher("/teacherTools/teacherLogin.jsp").forward(servletRequest,servletResponse);
                return false;
            }
        }
        if (e instanceof AdminTeacherRegistrationEvent) {
            new TeacherRegistrationHandler().handleEvent(conn, (AdminTeacherRegistrationEvent) e, servletRequest, servletResponse);
            return false; // forward to JSP , tell caller not to generate output
        }
        else if (e instanceof AdminChooseActivityEvent) {
            AdminChooseActivityEvent ee = (AdminChooseActivityEvent) e;
            int teacherId = ee.getTeacherId();
            servletRequest.setAttribute("teacherId",teacherId);
            servletRequest.getRequestDispatcher("/teacherTools/teacherActivities.jsp").forward(servletRequest,servletResponse);
            return false; // tell caller not to generate output
        }
        else if (e instanceof AdminCreateClassEvent) {
            v = new CreateClassHandler().handleEvent(sc,conn,e, servletRequest, servletResponse);
            if (v == null)    // v is null when JSP is generated
                return false; // tell caller not to generate output
        }
        else if (e instanceof AdminAlterStudentInClassEvent) {
            new AlterStudentInClassHandler().handleEvent(sc,conn, (AdminAlterStudentInClassEvent) e, servletRequest,servletResponse);
            return false; // Tells caller not to write servletOutput into servlet output stream
        }
        else if (e instanceof AdminEditTopicsEvent) {
            v = new TopicEditorHandler().handleEvent(sc,conn,(AdminEditTopicsEvent) e,servletRequest,servletResponse);
            if (v == null)
                return false;
        }
        else if (e instanceof AdminProblemSelectionEvent) {
            v = new ProblemSelectionHandler().handleEvent(sc,conn,(AdminProblemSelectionEvent) e,servletRequest,servletResponse);
            if (v == null)
                return false;
        }
        else if (e instanceof AdminSelectClassEvent) {
            AdminSelectClassEvent ee = (AdminSelectClassEvent) e;
//            servletRequest.getSession().setAttribute(AdminHandler.CLASSID,ee.getClassId());
            servletRequest.setAttribute("classId",ee.getClassId());
            servletRequest.setAttribute("teacherId",ee.getTeacherId());
            servletRequest.getRequestDispatcher("/teacherTools/selectReport.jsp").forward(servletRequest,servletResponse);
            return false;
        }

        else if (e instanceof AdminViewReportEvent) {
            int teacherId = ((AdminViewReportEvent) e).getTeacherId();
            v =  new ReportHandler(teacherId).handleEvent(sc, e, conn, servletRequest);
        }
        else if (e instanceof AdminEditSurveysEvent && !((AdminEditSurveysEvent) e).isSaveMode()) {
            servletRequest.setAttribute("teacherId",((AdminEditSurveysEvent) e).getTeacherId());
            servletRequest.setAttribute("preSurvey",Settings.preSurvey);
            servletRequest.setAttribute("postSurvey",Settings.preSurvey);
            servletRequest.getRequestDispatcher(CreateClassHandler.EDIT_SURVEYS_JSP).forward(servletRequest,servletResponse);
            return false;
        }
        else if (e instanceof AdminEditSurveysEvent && ((AdminEditSurveysEvent) e).isSaveMode()) {
            servletRequest.setAttribute("teacherId",((AdminEditSurveysEvent) e).getTeacherId());
            Settings.setSurveys(conn, ((AdminEditSurveysEvent) e).getPreSurvey(),((AdminEditSurveysEvent) e).getPostSurvey());
            Settings.preSurvey= ((AdminEditSurveysEvent) e).getPreSurvey();
            Settings.postSurvey= ((AdminEditSurveysEvent) e).getPostSurvey();
            servletRequest.setAttribute("preSurvey",Settings.preSurvey);
            servletRequest.setAttribute("postSurvey",Settings.postSurvey);
            servletRequest.setAttribute("message","Survey settings saved.");
            servletRequest.getRequestDispatcher(CreateClassHandler.EDIT_SURVEYS_JSP).forward(servletRequest,servletResponse);
            return false;
        }
        // this else class must be after the previous ones because their events are subclasses of AdminClassEvent
        else if (e instanceof AdminClassEvent)  {
            new AlterClassHandler().handleEvent(sc,conn,(AdminClassEvent) e, servletRequest,servletResponse);
            return false; // Tells caller not to write servletOutput into servlet output stream
        }
//        else if (e instanceof AdminTeacherRegistrationEvent)  {
//            new TeacherRegistrationHandler().handleEvent(conn,(AdminTeacherRegistrationEvent) e, servletRequest, servletResponse);
//            return false; // forward to JSP , tell caller not to generate servlet output
//        }
        else if (e instanceof UserRegistrationEvent)
            v = new UserRegistrationHandler().handleEvent(sc, servletRequest, conn, e);


        else if (e instanceof AdminGetPrePostProblemPreviewEvent) {
            int probId =  ((AdminGetPrePostProblemPreviewEvent) e).getProbId();
            PrePostProblemDefn p = DbPrePost.getPrePostProblem(conn,probId);
            int ansType = showProblem(p,servletOutput);
            return true;
        }
        else if (e instanceof AdminMainPageEvent){
            AdminMainPageEvent e1 = (AdminMainPageEvent)((AdminEvent) e);
            ClassInfo[] classes1 = DbClass.getClasses(conn, e1.getTeacherId());
            Classes bean1 = new Classes(classes1);

            int hasClasses =-1;
            for (ClassInfo cl: classes1){
                hasClasses = cl.getClassid();
            }

            if(hasClasses<0){
                servletRequest.getRequestDispatcher("/teacherTools/mainNoClasses.jsp").forward(servletRequest,servletResponse);
            }
            ClassInfo classInfo = DbClass.getClass(conn,hasClasses);
            servletRequest.setAttribute("classInfo",classInfo);
            servletRequest.setAttribute("action","AdminUpdateClassId");
            servletRequest.setAttribute("bean", bean1);

            servletRequest.setAttribute("classId", Integer.toString(hasClasses));
            servletRequest.setAttribute("teacherId",e1.getTeacherId());

            servletRequest.getRequestDispatcher("/teacherTools/wayangMain.jsp").forward(servletRequest,servletResponse);
            return false;
        }

        else if (e instanceof AdminTestDbEvent) {
            v = new TestDbHandler().handleEvent(sc,conn,servletRequest,servletResponse,servletOutput);
        }
        else if (e instanceof AdminDeactivateLiveProblemEvent) {
            new TutorAdminHandler().processEvent(servletRequest, servletResponse, e, conn);
        }

        else if (e instanceof AdminReloadProblemsEvent || e instanceof AdminTutorEvent) {
            new TutorAdminHandler().processEvent(servletRequest, servletResponse, e, conn);
            return false;
        }
        else
            throw new UserException("Unknown event " + e);
        servletOutput.append(v.getView());
        return true; // Tells caller to write servletOutput into servlet output stream
    }

    private int showProblem(PrePostProblemDefn p, StringBuffer out) {
        // I think we can assume that there is a web server on the same machine as the web app?
        String host= Settings.prePostProblemURI;
        String url = host + p.getUrl();
        if (url != null)
            out.append("<img src=\"" + url +"\" /><p/>\n");
        String descr = p.getDescr();
        out.append(descr + "<p/>\n");
        int ansType = p.getAnsType();
        if (ansType == PrePostProblemDefn.MULTIPLE_CHOICE) {
            String ansA,ansB,ansC,ansD,ansE;
            String ansAURL,ansBURL,ansCURL,ansDURL,ansEURL;
            ansA= p.getaAns();
            if (ansA == null) {
                ansAURL =  p.getaURL();
                if (ansAURL != null)
                    out.append("a: <img src=\"" + host +ansAURL + "\"><br/>\n");
            }
            else out.append("a: " + ansA + "<br/>\n");
            ansB= p.getbAns();
            if (ansB == null) {
                ansBURL =  p.getbURL();
                if (ansBURL != null)
                    out.append("b: <img src=\"" + host +ansBURL + "\"><br/>\n");
            }
            else out.append("b: " + ansB + "<br/>\n");
            ansC= p.getcAns();
            if (ansC == null) {
                ansCURL =  p.getcURL();
                if (ansCURL != null)
                    out.append("c: <img src=\"" + host +ansCURL + "\"><br/>\n");
            }
            else out.append("c: " + ansC + "<br/>\n");
            ansD= p.getdAns();
            if (ansD == null) {
                ansDURL =  p.getdURL();
                if (ansDURL != null)
                    out.append("d: <img src=\"" + host + ansDURL + "\"><br/>\n");
            }
            else if (ansD != null) out.append("d: " + ansD + "<br/>\n");
            ansE= p.geteAns();
            if (ansE == null) {
                ansEURL =  p.geteURL();
                if (ansEURL != null)
                    out.append("e: <img src=\"" + host +ansEURL + "\"><br/>\n");
            }
            else if (ansE != null) out.append("e: " + ansE + "<br/>\n");

        }
        return ansType;
    }

}