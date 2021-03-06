package edu.umass.ckc.wo.beans;

import edu.umass.ckc.wo.woreports.Report;

public class ClassInfo {
    private String school;
    private int schoolYear;
    private String name;
    private String town;
    private String section;
    private int classid;
    private int teachid;
    private String teacherName;
    private int propGroupId;
    private int pretestPoolId;
    private String pretestPoolDescr;
    private int logType; // 1 means version 1 events in EpisodicData2, 2 means version 2 events in EventLog
    private int emailInterval;
    private int statusReportPeriodDays;
    private int studentEmailPeriodDays;
    private int studentEmailIntervalDays;
    private String flashClient;

    public ClassInfo(String school, int schoolYear, String name, String town, String section,
                     int classid, int teachid, String teacherName, int propGroupId, int pretestPoolId, String pretestPoolDescr,
                     int logType, int emailStatusInterval, int statusReportPeriodDays, int studentEmailIntervalDays,
                     int studentReportPeriodDays) {
        this.school = school;
        this.schoolYear = schoolYear;
        this.name = name;
        this.town = town;
        this.section = section;
        this.classid = classid;
        this.teachid = teachid;
        this.teacherName = teacherName;
        this.propGroupId = propGroupId;
        this.pretestPoolId = pretestPoolId;
        this.pretestPoolDescr =pretestPoolDescr;
        this.logType = logType;
        this.emailInterval=emailStatusInterval;
        this.statusReportPeriodDays=statusReportPeriodDays;
        this.studentEmailIntervalDays=studentEmailIntervalDays;
        this.studentEmailPeriodDays =studentReportPeriodDays;

    }
    
    public ClassInfo(String school, int schoolYear, String name, String town, String section,
                     int classid, int teachid, String teacherName, int propGroupId, int logType, int pretestPoolId,
                     int emailStatusReportIntervalDays, int statusReportPeriodDays, int studentReportIntervalDays, int studentReportPeriodDays) {
        this(school,schoolYear,name,town,section,classid,teachid,teacherName,propGroupId, pretestPoolId, null,logType,
                emailStatusReportIntervalDays, statusReportPeriodDays, studentReportIntervalDays, studentReportPeriodDays);
    }

    public ClassInfo(String school, int schoolYear, String name, String town, String section,
                     int classid, int teachid, String teacherName, int propGroupId, int logType, int pretestPoolId,
                     int emailStatusReportIntervalDays, int statusReportPeriodDays, int studentReportIntervalDays,
                     int studentReportPeriodDays, String flashClient) {
        this(school,schoolYear,name,town,section,classid,teachid,teacherName,propGroupId, pretestPoolId, null,logType,
                emailStatusReportIntervalDays, statusReportPeriodDays, studentReportIntervalDays, studentReportPeriodDays);
        this.flashClient = flashClient;
    }

    public String getSchool() {
        return school;
    }

    public int getSchoolYear() {
        return schoolYear;
    }

    public String getName() {
        return name;
    }

    public String getTown() {
        return town;
    }

    public String getSection() {
        return section;
    }

    public int getClassid() {
        return classid;
    }

    public int getTeachid() {
        return teachid;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public int getPropGroupId() {
        return propGroupId;
    }

    public void setPropGroupId(int propGroupId) {
        this.propGroupId = propGroupId;
    }

    public String getPretestPoolDescr() {
        return pretestPoolDescr;
    }

    public int getPretestPoolId() {
        return pretestPoolId;
    }

    public int getLogType() {
        return logType;
    }

    public boolean isNewLog () {
        return logType == Report.EVENT_LOG;
    }

    public int getEmailInterval() {
        return emailInterval;
    }

    public void setEmailInterval(int emailInterval) {
        this.emailInterval = emailInterval;
    }

    public int getStatusReportPeriodDays() {
        return statusReportPeriodDays;
    }

    public void setStatusReportPeriodDays(int statusReportPeriodDays) {
        this.statusReportPeriodDays = statusReportPeriodDays;
    }

    public int getStudentEmailPeriodDays() {
        return studentEmailPeriodDays;
    }

    public void setStudentEmailPeriodDays(int studentEmailPeriodDays) {
        this.studentEmailPeriodDays = studentEmailPeriodDays;
    }

    public int getStudentEmailIntervalDays() {
        return studentEmailIntervalDays;
    }

    public void setStudentEmailIntervalDays(int studentEmailIntervalDays) {
        this.studentEmailIntervalDays = studentEmailIntervalDays;
    }

    public String getFlashClient() {
        return flashClient;
    }

    public void setFlashClient(String flashClient) {
        this.flashClient = flashClient;
    }
}