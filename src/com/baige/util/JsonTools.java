package com.baige.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.baige.data.entity.Candidate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by baige on 2018/4/12.
 */

/**
 * @author baige
 *
 */
public class JsonTools {
    /**
     * 获取属性名数组
     */
    private static String[] getFieldName(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
//            System.out.println(fields[i].getType());
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    public static JSONObject getJSON(Object obj) {
        JSONObject jsonObject = new JSONObject();
        Map<String, Object> map = getFieldValueByName(obj);
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entity = iterator.next();
            try {
                jsonObject.put(entity.getKey(), entity.getValue());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public static JSONObject getJSON(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entity = iterator.next();
            try {
                jsonObject.put(entity.getKey(), entity.getValue());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    /**
     * @param obj
     * @return 返回对象的属性名和属性值的哈希表
     */
    public static Map<String, Object> getFieldValueByName(Object obj) {
        String[] fieldNames = getFieldName(obj);
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < fieldNames.length; i++) {
            try {
                String firstLetter = fieldNames[i].substring(0, 1).toUpperCase();
                String getter = "get" + firstLetter + fieldNames[i].substring(1);
                Method method = obj.getClass().getMethod(getter, new Class[]{});
                Object value = method.invoke(obj, new Object[]{});
                map.put(fieldNames[i], value);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return map;
    }

    /**
     *
     * @param klass 待转换Java对象的class
     * @return Java对象
     */
    public static Object toJavaBean(Class<?> klass, JSONObject jsonObject){
        Object object = null;
        if(jsonObject == null || jsonObject.length() == 0){
            return null;
        }
        try {
            object = klass.newInstance();
            for(Method method : klass.getMethods()){
                if (Modifier.isPublic(method.getModifiers())) {

                    String name = method.getName();
                    String key = "";
                    if (name.startsWith("set")) {
                        key = name.substring(3);
                    }
                    //格式化Key字符串
                    if (key.length() > 0
                            && Character.isUpperCase(key.charAt(0))
                            && method.getParameterTypes().length == 1) {//getParameterTypes() 返回方法形参类型数组
                        if (key.length() == 1) {
                            key = key.toLowerCase();
                        } else if (!Character.isUpperCase(key.charAt(1))) {
                            key = key.substring(0, 1).toLowerCase()
                                    + key.substring(1);
                        }
                        if(jsonObject.has(key)){
                            try {
                                Object value = jsonObject.get(key);
                                /*value 可能是org.json.JSONObject.Null对象*/
                                if(value != null && !value.equals(null)){
                                	//System.out.println("value :"+value);
                                	//System.out.println("class :"+value.getClass().getCanonicalName());
                                	setValue(object, value, method);
                                }
                                
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * 通过set方法，将value值赋值给对象target的对象属性
     * @param target Java对象
     * @param value 属性值
     * @param method set方法
     */
    protected static void setValue(Object target, Object value, Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException{

        Class<?> klass = method.getParameterTypes()[0]; //参数类型

        boolean isJDKLibClass = (klass.getClassLoader() == null) ? true : false;
        String jsonObjectName = JSONObject.class.getName();
        String jsonArrayName = JSONArray.class.getName();
        String jsonStringName = JSONStringer.class.getName();
        String className = klass.getName();
        String classSimpleName = klass.getSimpleName();

        if(isJDKLibClass || className.equals(jsonObjectName) || className.equals(jsonArrayName) || className.equals(jsonStringName)){

            //处理映射基本类型对象
            if (classSimpleName.equalsIgnoreCase("Byte") || classSimpleName.equalsIgnoreCase("Character")
                    || classSimpleName.equalsIgnoreCase("Short") || classSimpleName.equalsIgnoreCase("Integer")
                    || classSimpleName.equalsIgnoreCase("int") || classSimpleName.equalsIgnoreCase("char")
                    || classSimpleName.equalsIgnoreCase("Long") || classSimpleName.equalsIgnoreCase("Boolean")
                    || classSimpleName.equalsIgnoreCase("Float") || classSimpleName.equalsIgnoreCase("Double")
                    || classSimpleName.equalsIgnoreCase("String") || classSimpleName.equalsIgnoreCase("BigInteger")
                    || classSimpleName.equalsIgnoreCase("BigDecimal") || classSimpleName.equalsIgnoreCase("Enum")
                    || className.equals(jsonObjectName) || className.equals(jsonArrayName) || className.equals(jsonStringName)) {
            	method.invoke(target, value);
                return;
            }

            //TODO 处理映射List和Map类型的对象
            if(klass.isInterface()){

                if(klass.getSimpleName().equals("List")){
//                    List<?> coll = ((JSONArray)value).toList();
//                    method.invoke(target, coll);
//                    return;
                    throw new IllegalAccessException("未处理类型 List， Map");

                }

                if(klass.getSimpleName().equals("Map")){
//                    Map<?, ?> map = ((JSONObject)value).toMap();
//                    method.invoke(target, map);
//                    return;
                    throw new IllegalAccessException("未处理类型 List， Map");
                }


            } else {
                for(Class<?> interF : klass.getInterfaces()){
                    if(interF.getSimpleName().equals("List")){
//                        List<?> coll = ((JSONArray)value).toList();
//                        method.invoke(target, coll);
//                        return;
                        throw new IllegalAccessException("未处理类型 List， Map");
                    }
                    if(interF.getSimpleName().equals("Map")){
//                        Map<?, ?> map = ((JSONObject)value).toMap();
//                        method.invoke(target, map);
//                        return;
                        throw new IllegalAccessException("未处理类型 List， Map");
                    }
                }

            }

            //TODO 处理映射数组对象
            if(klass.isArray()){
//                Object array = null;
//                switch(classSimpleName){
//                    case "Byte[]" :{
//                        array = ((JSONArray)value).toList().toArray(new Byte[0]);
//                        break;
//                    }
//                    case "Character[]" :{
//                        array = ((JSONArray)value).toList().toArray(new Character[0]);
//                        break;
//                    }
//                    case "Short[]" :{
//                        array = ((JSONArray)value).toList().toArray(new Short[0]);
//                        break;
//                    }
//                    case "Integer[]" :{
//                        array = ((JSONArray)value).toList().toArray(new Integer[0]);
//                        break;
//                    }
//                    case "Long[]" :{
//                        array = ((JSONArray)value).toList().toArray(new Long[0]);
//                        break;
//                    }
//                    case "Boolean[]" :{
//                        array = ((JSONArray)value).toList().toArray(new Boolean[0]);
//                        break;
//                    }
//                    case "Float[]" :{
//                        array = ((JSONArray)value).toList().toArray(new Float[0]);
//                        break;
//                    }
//                    case "Double[]" :{
//                        array = ((JSONArray)value).toList().toArray(new Double[0]);
//                        break;
//                    }
//                    case "String[]" :{
//                        array = ((JSONArray)value).toList().toArray(new String[0]);
//                        break;
//                    }
//                    case "BigInteger[]" :{
//                        array = ((JSONArray)value).toList().toArray(new BigInteger[0]);
//                        break;
//                    }
//                    case "BigDecimal[]" :{
//                        array = ((JSONArray)value).toList().toArray(new BigDecimal[0]);
//                        break;
//                    }
//                    default : {
//                        array = toBaseTypeArrayObject(((JSONArray)value).toList().toArray(), classSimpleName);
//                        break;
//                    }
//                }
//                method.invoke(target, array);
//                return;
                throw new IllegalAccessException("未处理类型 Array");
            }

        } else {//嵌套处理一般Java对象
            Object object = toJavaBean(klass, (JSONObject)value);
            method.invoke(target, object);
        }
    }
    /*
     *   "user": {
        "alias": null,
        "deviceId": "fefegesafe",
        "id": 1,
        "imgName": null,
        "loginIp": null,
        "loginTime": 1525892656361,
        "name": "13077790403@163.com",
        "password": "E10ADC3949BA59ABBE56E057F20F883E",
        "registerTime": 1525890523782,
        "remark": null,
        "verification": "1525892656368_4597"
    }
     * */
    public static void main(String[] args) {
    	JSONObject jsonObject;
		try {
			jsonObject = new JSONObject("{\"alias\":null,\"deviceId\":\"1525893226593_430\",\"id\":1,\"imgName\":null,\"loginIp\":null,\"loginTime\":1525893225691,\"name\":\"13077790403@163.com\",\"password\":\"E10ADC3949BA59ABBE56E057F20F883E\",\"registerTime\":1525890523782,\"remark\":null,\"verification\":\"1525893225695_7557\"}");
			User user = User.createByJson(jsonObject);
//			user.setName("13077790403@163.com");
//			user.setPassword("E10ADC3949BA59ABBE56E057F20F883E");
//			user.setLoginTime(System.currentTimeMillis());
			System.out.println("源数据"+jsonObject.toString());
			System.out.println("用户"+user.toString());
			jsonObject = getJSON(user);
			System.out.println("对象转json="+jsonObject.toString());
			
			User user2= (User) toJavaBean(User.class, jsonObject);
			System.out.println("json转对象="+user2.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
}
