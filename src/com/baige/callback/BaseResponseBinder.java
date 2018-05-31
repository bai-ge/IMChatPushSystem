package com.baige.callback;

import com.baige.common.Parm;
import com.baige.util.JsonTools;
import com.baige.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by baige on 2018/5/27.
 */

public class BaseResponseBinder<T> extends AbstractResponseBinder{

    public final static String DEFAULT_KEY = "data";
    private Class<T> EntityClass; // 获取实体类
    private String key; // 复数形式默认加s或key_list;

    @SuppressWarnings("unchecked")
    public BaseResponseBinder() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        EntityClass = (Class<T>) type.getActualTypeArguments()[0]; // 获取实体类
        key = DEFAULT_KEY;
    }

    @SuppressWarnings("unchecked")
    public BaseResponseBinder(String key) {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        EntityClass = (Class<T>) type.getActualTypeArguments()[0]; // 获取实体类
        this.key = key;
    }


    @Override
    public void parse(String json, PushCallback callback) {
        if(!Tools.isEmpty(json)){
            try {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has(Parm.CODE)){
                    int codeNum = jsonObject.getInt(Parm.CODE);
                    JSONObject objJson;

                    if(codeNum == Parm.CODE_SUCCESS && jsonObject.has(key)){
                        objJson = jsonObject.getJSONObject(key);
                        Object o = JsonTools.toJavaBean(EntityClass, objJson);
                        if(o != null){
                            callback.loadObject(o);
                        }else{
                            callback.loadFail();
                        }
                    }else if(codeNum == Parm.CODE_SUCCESS && jsonObject.has(key+"s")){
                        JSONArray jsonArray = jsonObject.getJSONArray(key+"s");
                        List<Object> list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);
                            Object o = JsonTools.toJavaBean(EntityClass, objJson);
                            if(o != null){
                                list.add(o);
                            }
                        }
                        if(list.size() > 0){
                            callback.loadList(list);
                        }else{
                            callback.loadFail();
                        }
                    }else if(codeNum == Parm.CODE_SUCCESS && jsonObject.has(key+"_list")){
                        JSONArray jsonArray = jsonObject.getJSONArray(key+"_list");
                        List<Object> list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);
                            Object o = JsonTools.toJavaBean(EntityClass, objJson);
                            if(o != null){
                                list.add(o);
                            }
                        }
                        if(list.size() > 0){
                            callback.loadList(list);
                        }else{
                            callback.loadFail();
                        }
                    }
                    callbackCode(callback, codeNum);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callback.fail();
            }
        }
    }
}
