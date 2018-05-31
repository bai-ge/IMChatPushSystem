package com.baige.callback;



/**
 * Created by baige on 2018/5/18.
 */

public class PushCallback extends BaseCallback{

    private final static String TAG = PushCallback.class.getCanonicalName();

    private AbstractResponseBinder mResponseBinder;


    public void setResponseBinder(AbstractResponseBinder responseBinder) {
        this.mResponseBinder = responseBinder;
    }

    public void response(String json) { // 可重写
        if(mResponseBinder != null){
            mResponseBinder.parse(json, this);
        }
    }

    @Override
    public void timeout() {

    }

    public void error(Exception e) {

    }






    public void success() {

    }


    public void fail() {

    }


    public void unknown() {

    }


    public void notFind() {

    }


    public void typeConvert() {

    }


    public void exist() {

    }


    public void isBlank() {

    }


    public void invalid() {

    }



}
