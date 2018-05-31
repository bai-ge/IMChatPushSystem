package com.baige.callback;



import java.util.List;
import java.util.TimerTask;

/**
 * Created by baige on 2018/2/12.
 *
 * 所有回调函数的基类
 */

public abstract class BaseCallback {
    private long timeout;

    private String id;

    protected int taskcount; //对于多任务事件，可以设置任务完成数

    protected Object taskLock = new Object();

    public void setTaskcount(int taskcount){
        this.taskcount = taskcount;
    }

    private TimerTask mTimerTask;

    public final void destroy(){
        if(mTimerTask != null){
            mTimerTask.cancel();
        }
        CallbackManager.getInstance().remote(id);
    }

    public final TimerTask getTimerTask(){
        if(mTimerTask == null){
            synchronized (taskLock){
                if(mTimerTask == null){
                    mTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            timeout();

                            synchronized (taskLock){
                                if(taskcount != 0){
                                    onFinish();
                                }
                            }
                            CallbackManager.getInstance().remote(id);
                        }
                    };
                }
            }
        }
        return mTimerTask;
    }

    public final String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final long getTimeout() {
        return timeout;
    }

    public final void setTimeout(long timeout) {
        if(timeout > 0){
            this.timeout = timeout;
        }
    }
    public final int finishOneTask(){
        synchronized (taskLock){
            taskcount --;
            if(taskcount == 0){
                onFinish();
            }
        }
        return taskcount;
    }

    public final int getTaskcount(){
        return taskcount;
    }

    /**
     * 只有主动去调用CallbackManager.put(baseCallBack)
     * 计时器才会起作用，根据设置的时间自动调用timeout()函数
     * 要想取消调用，必须主动destroy()
     */
    public abstract void timeout();

    public void onFinish() {

    }

    public void loadObject(Object obj){

    }

    public void loadList(List<Object> list){

    }

    public void loadFail(){

    }


}
