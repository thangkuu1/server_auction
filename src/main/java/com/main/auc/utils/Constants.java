package com.main.auc.utils;

public class Constants {
    public static final String CACHE_PREFIX = "test";
    public static class Redis{
        public static final String AUC_LOGIN = "auc_login_session_";
        public static final String AUC_LOGIN_KICKOUT = "auc_login_kickout_session_";
    }
    public static class Login{
        public static final String SIGNUP_INIT = "0"; // user chua confirm
        public static final String SIGNUP_CONFIRM = "1"; // user da confirm
        public static final String LOGIN_TYPE_SYS = "SYSTEM";
        public static final String LOGIN_TYPE_GOOGLE = "GOOGLE";
        public static final String LOGIN_TYPE_FB = "FACEBOOK";
        public static final String GG_CLIENT_ID = "8598854436-j2ma3vahpd2vgvcieocid7dnbcblgm8r.apps.googleusercontent.com";
        public static final String GG_CLIENT_SECRET  = "Qd-yf43-cRCere6wFVZorl6L";
//        public static final String FB_CLIENT_ID = "2624942597802137";
        public static final String FB_CLIENT_ID = "540092220598011";
        public static final String FB_CLIENT_SECRET = "161f062c7e71096cd291be07199236dd";
//        public static final String FB_CLIENT_SECRET = "a8eeb0da56c3947d499e80b42b7673d0";

        public static final String FB_DIRECT_SIT = "https://localhost:3005/callback/facebook";
//        public static final String FB_DIRECT_SIT = "https://vvi-fe-uat.herokuapp.com/callback/facebook";
//        public static final String FB_DIRECT_SIT = "https://vvi-fe-qa.herokuapp.com/callback/facebook";

        public static final String GG_DIRECT_SIT = "http://localhost:3005/callback/google";
//        public static final String GG_DIRECT_SIT = "https://vvi-fe-uat.herokuapp.com/callback/google";
//        public static final String GG_DIRECT_SIT = "https://vvi-fe-qa.herokuapp.com/callback/google";

        public static final String URI = "http://localhost:3005";
//        public static final String URI = "https://vvi-fe-uat.herokuapp.com";
//        public static final String URI = "https://vvi-fe-qa.herokuapp.com";
//        public static final String GG_DIRECT_SIT = "http://localhost:8089/callback";
    }
    public static class Base{
        public static final String SUCCESS = "00";
        public static final String EXCEPTION = "96";
    }

    public static class Contact{
        public static final String STATUS_INIT = "0";
    }
}
