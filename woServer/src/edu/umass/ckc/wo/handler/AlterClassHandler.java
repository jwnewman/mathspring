package edu.umass.ckc.wo.handler;

import edu.umass.ckc.wo.beans.*;
import edu.umass.ckc.wo.db.DbClassPedagogies;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbPrePost;
import edu.umass.ckc.wo.event.admin.*;
import ckc.servlet.servbase.UserException;


import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.exc.DeveloperException;
import edu.umass.ckc.wo.admin.ClassCloner;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.event.admin.AdminAlterClassCreateStudentsEvent;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.io.IOException;

/**
 * Copyright (c) University of Massachusetts
 * Written by: David Marshall
 * Date: Jan 23, 2008
 * Time: 2:18:57 PM
 */
public class AlterClassHandler {
    private int teacherId;
    private HttpSession sess;

    public final static String ALTER_CLASSES_JSP = "/teacherTools/alterClasses.jsp";
    public final static String EDIT_CLASS_JSP = "/teacherTools/editClass.jsp";
    public final static String CLONE_CLASS_JSP = "/teacherTools/cloneClass.jsp";
    public final static String OTHER_CONFIG_JSP = "/teacherTools/otherConfig.jsp";
    public final static String ROSTER_JSP = "/teacherTools/roster.jsp";
    public final static String MAIN_WAYANG_JSP = "/teacherTools/wayangMain.jsp";
    public final static String MAIN_NOCLASS_JSP = "/teacherTools/mainNoClasses.jsp";



    public AlterClassHandler() {
    }

    public void handleEvent (ServletContext sc, Connection conn, AdminClassEvent e, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        teacherId = e.getTeacherId();
        // a general request to alter classes produces a page listing all the classes with
        // some buttons next to them allowing delete and edit
        if (e instanceof AdminAlterClass1Event) { // START state
            ClassInfo[] classes = DbClass.getClasses(conn,teacherId);
            Classes bean = new Classes(classes);
            String action = "ALTER_CLASS_JSP";
            req.setAttribute("action", action);
            req.setAttribute("bean",bean);
            req.setAttribute("teacherId",teacherId);
            req.getRequestDispatcher(ALTER_CLASSES_JSP).forward(req,resp);
        }
        // When a request is to delete a class, delete it and its pedagogies
        // then just show the list of classes remaining
         else if (e instanceof AdminDeleteClassEvent) {
            // first get rid of any classpedagogies rows in the db
            DbClassPedagogies.removeClassPedagogies(conn, ((AdminAlterClassEditEvent) e).getClassId());
            // now get rid of the class row in the db
            DbClass.deleteClass(conn,((AdminAlterClassEditEvent) e).getClassId());
            ClassInfo[] classes = DbClass.getClasses(conn,teacherId);
            Classes bean = new Classes(classes);
            req.setAttribute("bean",bean);
            req.setAttribute("teacherId",teacherId);
            req.getRequestDispatcher(ALTER_CLASSES_JSP).forward(req,resp);
        }
         // when a request is to clone a class, then goto a JSP that requests info on the name and section.
        else if (e instanceof AdminAlterClassCloneClassEvent) {
            int classId =  ((AdminAlterClassCloneClassEvent) e).getClassId();
            ClassInfo[] classes = DbClass.getClasses(conn,teacherId);
            Classes bean = new Classes(classes);
            req.setAttribute("bean",bean);
            ClassInfo classInfo = DbClass.getClass(conn,classId);
            List<Pedagogy> pedsInUse = DbClassPedagogies.getClassPedagogies(conn,classId);
            PretestPool pool = DbPrePost.getPretestPool(conn,classId);
            req.setAttribute("action","AdminAlterClassCloneClass");
            req.setAttribute("pedagogies",pedsInUse);
            req.setAttribute("classInfo",classInfo);
            req.setAttribute("pool",pool);
            req.setAttribute("classId", classId);
            req.setAttribute("teacherId", teacherId);
            req.getRequestDispatcher(CLONE_CLASS_JSP).forward(req,resp);
        }
        // when a request is to edit a class, then goto a JSP that allows fields of the class
        // to be edited.
        else if (e instanceof AdminAlterClassEditEvent) {
            ClassInfo classInfo = DbClass.getClass(conn,((AdminAlterClassEditEvent) e).getClassId());
            List<Pedagogy> pedsInUse = DbClassPedagogies.getClassPedagogies(conn, ((AdminAlterClassEditEvent) e).getClassId());
            PretestPool pool = DbPrePost.getPretestPool(conn,((AdminAlterClassEditEvent) e).getClassId());
            ClassInfo[] classes = DbClass.getClasses(conn,teacherId);
            Classes bean = new Classes(classes);
            req.setAttribute("teacherId", teacherId);
            req.setAttribute("classId",((AdminAlterClassEditEvent) e).getClassId());
            req.setAttribute("bean",bean);
            req.setAttribute("action", "AdminAlterClassEdit");
            req.setAttribute("pedagogies",pedsInUse);
            req.setAttribute("classInfo",classInfo);
            req.setAttribute("pool",pool);
            req.getRequestDispatcher(EDIT_CLASS_JSP).forward(req,resp);
        }

        // creating a clone of an existing class
        else if (e instanceof AdminAlterClassCloneSubmitInfoEvent) {
            AdminAlterClassCloneSubmitInfoEvent e2 = (AdminAlterClassCloneSubmitInfoEvent) e;
            int classId = ClassCloner.cloneClass(conn,e2.getClassId(),e2.getClassName(),e2.getSection());
            if (classId < 0) {
                classId = e2.getClassId();
                ClassInfo classInfo = DbClass.getClass(conn,classId);
                List<Pedagogy> pedsInUse = DbClassPedagogies.getClassPedagogies(conn,classId);
                PretestPool pool = DbPrePost.getPretestPool(conn,classId);
                req.setAttribute("action", "AdminAlterClassCloneSubmitInfo");
                req.setAttribute("pedagogies",pedsInUse);
                req.setAttribute("classInfo",classInfo);
                req.setAttribute("pool",pool);
                req.setAttribute("message","Cloning the class failed.  For identification purposes you MUST give a new name and section to this class.");
                req.getRequestDispatcher(CLONE_CLASS_JSP).forward(req,resp);
            }
            else {
                ClassInfo classInfo = DbClass.getClass(conn,classId);
                List<Pedagogy> pedsInUse = DbClassPedagogies.getClassPedagogies(conn, classId);
                PretestPool pool = DbPrePost.getPretestPool(conn,classId);
                req.setAttribute("action", "AdminAlterClassCloneSubmitInfo" );
                req.setAttribute("pedagogies",pedsInUse);
                req.setAttribute("pool",pool);
                req.setAttribute("classInfo",classInfo);
                req.getRequestDispatcher(CreateClassHandler.CLASS_INFO_JSP).forward(req,resp);
            }

        }

        // update in db and then forward to classInfo.jsp.
        // see editClass.jsp for note about invalid XML (in trying to get buttons to be on a single line)
        // that may cause problems
        else if (e instanceof AdminAlterClassSubmitInfoEvent) {
            AdminAlterClassSubmitInfoEvent e2= (AdminAlterClassSubmitInfoEvent) e;
            DbClass.updateClass(conn,e2.getClassId(),e2.getClassName(),e2.getSchool(),e2.getSchoolYear(),
                    e2.getTown(),e2.getSection(),e2.getPropGroupId());
            ClassInfo classInfo = DbClass.getClass(conn,((AdminAlterClassSubmitInfoEvent) e).getClassId());
            List<Pedagogy> pedsInUse = DbClassPedagogies.getClassPedagogies(conn, ((AdminAlterClassSubmitInfoEvent) e).getClassId());
            PretestPool pool = DbPrePost.getPretestPool(conn,e2.getClassId());
            req.setAttribute("action","AdminAlterClassSubmitInfoEvent");
            req.setAttribute("pedagogies",pedsInUse);
            req.setAttribute("pool",pool);
            req.setAttribute("classInfo",classInfo);
            ClassInfo[] classes = DbClass.getClasses(conn,teacherId);
            Classes bean = new Classes(classes);
            req.setAttribute("teacherId", teacherId);
            req.setAttribute("classId",((AdminAlterClassSubmitInfoEvent) e).getClassId());
            req.setAttribute("bean",bean);
            req.setAttribute("action", "AdminAlterClassEdit");
            req.getRequestDispatcher(EDIT_CLASS_JSP).forward(req,resp);
        }

        // Events below have to do with altering an existing class

        // Change Class's Pedagogies Event coming in from either classInfo.jsp or editClass.jsp
        // Produce a new list of pedagogies to choose from (when the pedagogy selections on this
        // form are submitted, the old pedagogies for the class are cleared out)
        else if (e instanceof AdminAlterClassPedagogiesEvent) {
            // This JSP is used by both the creation of class pages and the alteration of class
            // pages.   This first parameter is the type of event that will be created when the
            // form is submitted.   For Class alteration, it should generate AdminAlterClassSubmitSelectedPedagogiesEvent
            // which is processed above.
            req.setAttribute("formSubmissionEvent","AdminAlterClassSubmitSelectedPedagogies");
            int classId = ((AdminAlterClassPedagogiesEvent) e).getClassId();
            ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
            Classes bean1 = new Classes(classes1);
            ClassInfo classInfo = DbClass.getClass(conn,classId);
            PedagogyBean[] pedagogies = DbClassPedagogies.getClassPedagogyBeans(conn,classId);
            req.setAttribute("action", "AdminAlterClassPedagogies");
            req.setAttribute("classId", classId);
             req.setAttribute("pedagogies", pedagogies);
            req.setAttribute("teacherId",((AdminAlterClassPedagogiesEvent) e).getTeacherId());
            req.setAttribute("classInfo", classInfo);
            req.setAttribute("bean", bean1);
            req.getRequestDispatcher(CreateClassHandler.SELECT_PEDAGOGIES_JSP).forward(req,resp);
        }
        // Alter Class's pretest pool Event coming in from either classInfo.jsp or editClass.jsp
        // Produce a new list of pretest pools to choose from
        else if (e instanceof AdminAlterClassPretestEvent) {
            // This JSP is used by both the creation of class pages and the alteration of class
            // pages.   This first parameter is the type of event that will be created when the
            // form is submitted.   For Class alteration, it should generate AdminAlterClassPretestSubmissionEvent
            // which is processed above.
            ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
            Classes bean1 = new Classes(classes1);
            ClassInfo classInfo = DbClass.getClass(conn,((AdminAlterClassPretestEvent) e).getClassId());
            int pretestPool = DbClass.getPretestPool(conn,((AdminAlterClassPretestEvent) e).getClassId());
            req.setAttribute("action","AdminAlterClassPretest");
            req.setAttribute("formSubmissionEvent","AdminAlterClassSubmitSelectedPretest");
            req.setAttribute("selectedPool",pretestPool);
            req.setAttribute("pools", DbPrePost.getAllPretestPools(conn));
            req.setAttribute("classId",((AdminAlterClassPretestEvent) e).getClassId());
            req.setAttribute("teacherId",((AdminAlterClassPretestEvent) e).getTeacherId());
            req.setAttribute("bean", bean1); //contains list of classes for dropdown menu
            req.setAttribute("classInfo", classInfo);    // info for teachers name header

            req.getRequestDispatcher(CreateClassHandler.SELECT_PRETEST_POOL_JSP).forward(req,resp);
        }

        // Pretest pool submitted.   Overwrite the class's pretest pool with the one submitted.
        // Then generate the editClass.jsp
        else if (e instanceof AdminAlterClassSubmitSelectedPretestEvent) {
            AdminAlterClassSubmitSelectedPretestEvent e2 = (AdminAlterClassSubmitSelectedPretestEvent) e;
            ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
            Classes bean1 = new Classes(classes1);
            ClassInfo classInfo = DbClass.getClass(conn,e2.getClassId());
            req.setAttribute("action", "AdminAlterClassEdit");
            req.setAttribute("classId",e2.getClassId());
            req.setAttribute("teacherId",e2.getTeacherId());
            req.setAttribute("bean", bean1); //contains list of classes for dropdown menu
            req.setAttribute("classInfo", classInfo);    // info for teachers name header

            ClassAdminHelper.processSelectedPretestSubmission(conn, e2.getClassId(), e2.getPoolId(),
                    req, resp, EDIT_CLASS_JSP, e2.isGivePretest());
        }
        // Pedagogies submitted.  Error check the submission and regenerate page if errors.  Otherwise
        // overwrite the class's pedagogies with those submitted and then generate the next page
        // as editClass.jsp (which does not need its form submission event set - hence the null)
        else if (e instanceof AdminAlterClassSubmitSelectedPedagogiesEvent) {
            AdminAlterClassSubmitSelectedPedagogiesEvent e2 = (AdminAlterClassSubmitSelectedPedagogiesEvent) e;
            if (!ClassAdminHelper.errorCheckSelectedPedagogySubmission(e2.getClassId(),e2.getPedagogyIds(),req,resp,
                    "AdminAlterClassSubmitSelectedPedagogies", e2.getTeacherId(), conn)) {
                ClassAdminHelper.saveSelectedPedagogies(conn,e2.getClassId(),e2.getPedagogyIds());
                generateValidPedagogySubmissionNextPage(conn,e2.getClassId(),req,resp);
            }
        }
        else if (e instanceof AdminAlterClassActivateHutsEvent) {
            ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
            Classes bean1 = new Classes(classes1);
            ClassInfo classInfo = DbClass.getClass(conn,((AdminAlterClassActivateHutsEvent) e).getClassId());

            req.setAttribute("action","AdminAlterClassActivateHuts");
            req.setAttribute("formSubmissionEvent","AdminAlterClassSubmitActivatedHuts");
            req.setAttribute("teacherId",((AdminAlterClassActivateHutsEvent) e).getTeacherId());
            ClassConfig cc = DbClass.getClassConfig(conn,((AdminAlterClassActivateHutsEvent) e).getClassId());
            req.setAttribute("activeHuts",cc);
            req.setAttribute("classId",((AdminAlterClassActivateHutsEvent) e).getClassId()); 
            req.setAttribute("classInfo", classInfo);
            req.setAttribute("bean", bean1);
            
            req.getRequestDispatcher(CreateClassHandler.ACTIVATE_HUTS_JSP).forward(req,resp);
        }
        else if (e instanceof AdminAlterClassSubmitActivatedHutsEvent) {
            AdminAlterClassSubmitActivatedHutsEvent ee = (AdminAlterClassSubmitActivatedHutsEvent) e;
            req.setAttribute("formSubmissionEvent","AdminAlterClassSubmitActivatedHuts");
            ClassConfig cc;
            if (ee.isRestoreDefaults()) {
                cc=ClassConfig.getDefaultConfig();
                DbClass.setClassConfig(conn,ee.getClassId(),cc);   
            }
            else {
                boolean pre = ee.isPretest();
                boolean tutoring = ee.isTutor();
                boolean post = ee.isPosttest();
                boolean adv = ee.isAdv();
                boolean mfr = ee.isMfr();
                boolean mr = ee.isMr();
                boolean defRules = ee.isRestoreDefaults();
                // the final 0 is to mark the class config as not following default hut activation rules
                cc = new ClassConfig(pre?1:0,post?1:0,adv?1:0,mfr?1:0,mr?1:0,tutoring?1:0, defRules);
                DbClass.setClassConfig(conn,ee.getClassId(),cc);
            }
            ClassInfo classInfo = DbClass.getClass(conn,ee.getClassId());
            ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
            Classes bean1 = new Classes(classes1);
            req.setAttribute("action","AdminAlterClassSubmitActivatedHuts");
            req.setAttribute("classId",ee.getClassId());
            req.setAttribute("teacherId",ee.getTeacherId());
            req.setAttribute("classInfo", classInfo);
            req.setAttribute("bean", bean1);

            req.setAttribute("activeHuts",cc);
            req.getRequestDispatcher(CreateClassHandler.ACTIVATE_HUTS_JSP).forward(req,resp);
        }
        else if (e instanceof AdminOtherClassConfigEvent) {
            AdminOtherClassConfigEvent e2 = (AdminOtherClassConfigEvent) e;
            ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
            Classes bean1 = new Classes(classes1);
            int classId = e2.getClassId();
            ClassInfo classInfo = DbClass.getClass(conn,classId);
            req.setAttribute("action","AdminOtherClassConfig");
            req.setAttribute("classInfo",classInfo);
            req.setAttribute("bean", bean1);
            req.setAttribute("classId", classId);
            req.setAttribute("teacherId",teacherId);
            req.getRequestDispatcher(OTHER_CONFIG_JSP).forward(req,resp);
        }
        else if (e instanceof AdminUpdateClassIdEvent){
            //Opens ups new page with updated classID
            ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
            Classes bean1 = new Classes(classes1);
            AdminUpdateClassIdEvent e2 = (AdminUpdateClassIdEvent) e;
            int newteachId = e2.getTeacherId();
            int classId1 = e2.getClassId();
            System.out.println("***********  new classId: "+classId1);
            System.out.println("***********  teacherId "+ newteachId);
            ClassInfo classInfo = DbClass.getClass(conn, classId1);
            req.setAttribute("action","AdminUpdateClassId");
            req.setAttribute("bean", bean1);
            req.setAttribute("classInfo", classInfo);
            req.setAttribute("classId", classId1);
            req.setAttribute("teacherId",teacherId);
            //req.setAttribute("teacherName", teacherName);
            req.getRequestDispatcher(MAIN_WAYANG_JSP).forward(req,resp);
        }
        else if (e instanceof AdminAlterClassOtherConfigSubmitInfoEvent) {
            AdminAlterClassOtherConfigSubmitInfoEvent e2 = (AdminAlterClassOtherConfigSubmitInfoEvent) e;
            int classId = e2.getClassId();

            DbClass.updateClassEmailSettings(conn,classId,e2.studentEmailInterval,e2.studentReportPeriod,e2.teacherEmailInterval,e2.teacherReportPeriod);
            ClassInfo classInfo = DbClass.getClass(conn,classId);
            List<Pedagogy> pedsInUse = DbClassPedagogies.getClassPedagogies(conn, classId);
            PretestPool pool = DbPrePost.getPretestPool(conn,classId);
            req.setAttribute("action","AdminAlterClassOtherConfigSubmitInfo");
            req.setAttribute("pedagogies",pedsInUse);
            req.setAttribute("pool",pool);
            req.setAttribute("classInfo",classInfo);
            ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
            Classes bean1 = new Classes(classes1);

            req.setAttribute("action","AdminOtherClassConfig");
            req.setAttribute("classInfo",classInfo);
            req.setAttribute("bean", bean1);
            req.setAttribute("classId", classId);
            req.setAttribute("teacherId",teacherId);

            req.getRequestDispatcher(OTHER_CONFIG_JSP).forward(req,resp);
        }
        else if (e instanceof AdminEditClassListEvent) {
            AdminEditClassListEvent e2 = (AdminEditClassListEvent) e;
            int classId = e2.getClassId();
            ClassInfo classInfo = DbClass.getClass(conn,classId);
            ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
            Classes bean1 = new Classes(classes1);
            req.setAttribute("action","AdminEditClassList");
            req.setAttribute("classInfo",classInfo);
            req.setAttribute("students",DbClass.getClassStudents(conn,classId));
            req.setAttribute("bean", bean1);
            req.setAttribute("classId", classId);
            req.setAttribute("teacherId",teacherId);

            req.getRequestDispatcher(ROSTER_JSP).forward(req,resp);
        }
        else if (e instanceof AdminAlterClassCreateStudentsEvent) {
                    AdminAlterClassCreateStudentsEvent e2 = (AdminAlterClassCreateStudentsEvent) e;
                    int classId = e2.getClassId();
                    ClassInfo classInfo = DbClass.getClass(conn,classId);
                    List<User> students = DbClass.getClassStudents(conn, classId);
                    List<String> peds = DbClassPedagogies.getClassPedagogyIds(conn, classId);
                    ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
                    Classes bean1 = new Classes(classes1);
                    String errMessage="";
                    if (peds.size() == 0)
                        errMessage = "Class does not have pedagogies selected.   Please select some before generating students.";
                    else if (e2.getEndNum() < e2.getBeginNum())
                        errMessage = "Begin Number must be <= End Number";
                    else if (students.size() == 0) {
                        try {
                            DbClass.createClassStudents(conn,classInfo,e2.getPrefix(),e2.getPassword(),e2.getBeginNum(),e2.getEndNum(),e2.getTestUserPrefix(), e2.getPassword());
                        } catch (UserException ue) {
                            errMessage = "Failure while creating class. " + ue.getMessage();
                        }
                    }
                    else errMessage =  "Class already has students.   Cannot generate new ones.";
                    req.setAttribute("action","AdminAlterClassCreateStudents");
                    req.setAttribute("message",errMessage);
                    req.setAttribute("classInfo",classInfo);
                    req.setAttribute("classId", classId);
                    req.setAttribute("teacherId",teacherId);
                    req.setAttribute("bean", bean1);
                    req.setAttribute("students", DbClass.getClassStudents(conn, classId));
                    req.getRequestDispatcher(ROSTER_JSP).forward(req,resp);
                }


       
    }



    /**
     * This is called after the pedagogy selections have been validated and the selected pedagogies have been
     * saved as part of this class.   This just generates the next JSP page which is the editClass.jsp
     */
 public void generateValidPedagogySubmissionNextPage (Connection conn, int classId,
                                                           HttpServletRequest req,
                                                           HttpServletResponse resp
                                                           ) throws SQLException, IOException, ServletException, DeveloperException {
     ClassInfo info = DbClass.getClass(conn,classId);
     ClassInfo[] classes1 = DbClass.getClasses(conn, teacherId);
     Classes bean1 = new Classes(classes1);
     req.setAttribute("action", "AdminUpdateClassId");
     req.setAttribute("classInfo",info);
     req.setAttribute("pedagogies", DbClassPedagogies.getClassPedagogies(conn, classId));
     req.setAttribute("classId",classId);
     PretestPool pool = DbPrePost.getPretestPool(conn,classId);
     req.setAttribute("pool",pool);
     req.setAttribute("bean", bean1);
     req.setAttribute("teacherId", teacherId);
     req.getRequestDispatcher(MAIN_WAYANG_JSP).forward(req,resp);
    }


}