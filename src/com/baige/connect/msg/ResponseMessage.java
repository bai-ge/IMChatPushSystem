package com.baige.connect.msg;

import com.baige.common.Parm;
import org.json.JSONException;
import org.json.JSONObject;

import com.baige.util.Tools;

/**
 * Created by baige on 2018/3/26.
 */

public class ResponseMessage {
    private String to;
    private String from;
    private Object response; //一般是Json, 包含数据，callback, code, 以及 data_type
    
    

    public String toJson(){
        JSONObject jsonObject = new JSONObject();
        try {
        	if(!Tools.isEmpty(from)){
        		jsonObject.put(Parm.FROM, from);
        	}
        	if(!Tools.isEmpty(to)){
        		jsonObject.put(Parm.TO, to);
        	}
            jsonObject.put(Parm.RESPONSE, response);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
