package com.franklinwireless.android.jexkids.locker.base;


public interface ApiInterface {

    //String URL_BASE = "https://gw6c9gvrhk.execute-api.us-east-2.amazonaws.com/dev/";
    //String URL_BASE="https://4t9pmvjrq6.execute-api.us-east-2.amazonaws.com/prod/";
    String URL_BASE="https://fa78rlupuc.execute-api.us-east-2.amazonaws.com/prod/";
    String APP_CONFIGURATION = "kids_app_configuration_control";
    String CHILD_APP_LIST="child_app_list";
    String UPLOAD_IMAGE="https://w4lh04c5ok.execute-api.us-east-2.amazonaws.com/dev/multer";
    //String UPLOAD_IMAGE="https://4t9pmvjrq6.execute-api.us-east-2.amazonaws.com/prod/multer";
    String LOGIN="jex_app_child_access";
    String LOGIN_URL="https://gw6c9gvrhk.execute-api.us-east-2.amazonaws.com/dev/sign_up_code_validation";
    String GET_CHILD_DETAILS="get_child_details";
    //String UPLOAD_IMAGE="https://gw6c9gvrhk.execute-api.us-east-2.amazonaws.com/dev/s3_upload_icon_details";
}