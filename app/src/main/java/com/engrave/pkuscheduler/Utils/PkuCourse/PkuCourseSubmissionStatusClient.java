package com.engrave.pkuscheduler.Utils.PkuCourse;

import com.engrave.pkuscheduler.Models.CourseLoginInfoModel;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.engrave.pkuscheduler.Utils.StringUtils.convertStreamToString;

//输入ToDoItem（来自教学网）检查作业有没有交
public final class PkuCourseSubmissionStatusClient {
    public final static boolean fetchSubmissionStatus(String ObjectIdentifier, CourseLoginInfoModel courseLoginInfoModel){
        HttpURLConnection conn;
        String request = ApiRepository.getSubmisstionStatusUrl(ObjectIdentifier);
        try{
        URL url = new URL(request);
        conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("Cookie", "JSESSIONID=" + courseLoginInfoModel.jSessionId_Frameset
                +"; session_id=" + courseLoginInfoModel.sessionId
                +"; s_session_id=" + courseLoginInfoModel.sSessionId
                +"; web_client_cache_guid=" + courseLoginInfoModel.guid);
        conn.setRequestMethod("GET");
        InputStream in = conn.getInputStream();
        String str = convertStreamToString(in);
        return str.contains("复查提交历史记录");
        } catch (Exception e) {
            return true;
        }
    }
}
