package com.baige.common;

public class Parm {

    public final static String USER = "user";
    public final static String USERS = "users";
    public final static String NAME = "name";
    public final static String PASSWORD = "password";
    public final static String CODE = "code";
    public final static String MEAN = "mean";
    public final static String FILE = "file";
    public final static String FILE_NAME = "file_name";
    public final static String CONTENT_TYPE = "content_type";
    public final static String KEYWORD = "keyword";
    public final static String RESPONSE = "response";


    public final static String UID = "uid";

    public final static String FRIENDS = "friends";
    public final static String FRIEND = "friend";
    public final static String OPERATION = "operation";
    public final static String DATA = "data";
    public final static String CHAT = "chat";
    public final static String CHAT_LIST = "chat_list";

    public final static String FILES = "files";

    public final static String CMD = "cmd";

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_FAIL = 500;
    public static final int CODE_UNKNOWN = 999;
    public static final int CODE_NOTFIND = 404;
    public static final int CODE_TYPE_CONVERT = 1001;
    public static final int CODE_EXIST = 1002;
    public static final int CODE_BLANK = 1003;
    public static final int CODE_TIMEOUT = 1004;
    public static final int CODE_INVALID = 1005;

    public static final int MSG_TYPE_TEXT = 1;
    public static final int MSG_TYPE_IMG = 2;
    public static final int MSG_TYPE_FILE = 4;


    public static final int MSG_IS_RECEIVE = 1;
    public static final int MSG_IS_SEND = 2;
    public static final int MSG_IS_INFORM = 3;

    public static final int MSG_IS_SENDING = 10;
    public static final int MSG_IS_SEND_SUCESS = 11;
    public static final int MSG_IS_SEND_FAIL = 12;

    public final static int CONNECTING = 1;
    public final static int CONNECTED = 2;
    public final static int LOGIN = 3;
    public final static int DISCONNECTED = 4;

    public final static String FROM = "from";
    public final static String TO = "to";
    public final static String DATA_TYPE = "data_type";
    public final static String CONTENT_DATA_TYPE = "content_data_type";

    public final static String DEVICE_ID = "device_id";
    public final static String CALLBACK = "callback";

    public final static String LOCAL_IP = "local_ip";
    public final static String REMOTE_IP = "remote_ip";
    public final static String LOCAL_PORT = "local_port";
    public final static String ACCEPT_PORT = "accept_port";
    public final static String REMOTE_PORT = "remote_port";
    public final static String LOCAL_UDP_PORT = "local_udp_port";
    public final static String REMOTE_UDP_PORT = "remote_udp_port";
    public final static String CANDIDATES = "candidates";
    public final static String SEND_TIME = "send_time";
    public final static String DELAY_TIME = "delay_time";
    public final static String USERNAME = "username";
    public final static String SPEAKER_PHONE = "speaker_phone";

    private final static int MSG_TYPE = 0x0010;
    public final static int TYPE_LOGIN = MSG_TYPE + 1;
    public final static int TYPE_LOGOUT = MSG_TYPE + 2;
    public final static int TYPE_UDP_TEST = MSG_TYPE + 3;
    public final static int TYPE_TRANSPOND = MSG_TYPE + 4;
    public final static int TYPE_VOICE = MSG_TYPE + 5;
    public final static int TYPE_FILE = MSG_TYPE + 6;

    public final static int TYPE_CALL_TO = MSG_TYPE + 7;
    public final static int TYPE_REPLY_CALL_TO = MSG_TYPE + 8;
    public final static int TYPE_PICK_UP = MSG_TYPE + 9;
    public final static int TYPE_HANG_UP = MSG_TYPE + 10;
    public final static int TYPE_TRY_PTP = MSG_TYPE + 11;
    public final static int TYPE_TRY_PTP_CONNECT = MSG_TYPE + 12;




}
