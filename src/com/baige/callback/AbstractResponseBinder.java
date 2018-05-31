package com.baige.callback;


import com.baige.common.Parm;

/**
 * Created by baige on 2018/2/13.
 */

public abstract class AbstractResponseBinder {

    public void callbackCode(PushCallback callBack, int code) {
        switch (code) {
            case Parm.CODE_SUCCESS:
                callBack.success();
                break;
            case Parm.CODE_FAIL:
                callBack.fail();
                break;
            case Parm.CODE_UNKNOWN:
                callBack.unknown();
                break;
            case Parm.CODE_NOTFIND:
                callBack.notFind();
                break;
            case Parm.CODE_TYPE_CONVERT:
                callBack.typeConvert();
                break;
            case Parm.CODE_EXIST:
                callBack.exist();
                break;
            case Parm.CODE_BLANK:
                callBack.isBlank();
                break;
            case Parm.CODE_TIMEOUT:
                callBack.timeout();
                break;
            case Parm.CODE_INVALID:
                callBack.invalid();
                break;
            default:
                callBack.unknown();
        }
    }


    public abstract void parse(String json, PushCallback callback);
}
