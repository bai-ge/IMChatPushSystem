package com.baige.linux;

import java.io.PipedOutputStream;

public abstract class AjaxPrecessAbstract extends Thread{
    protected String[] args;
    protected PipedOutputStream out;

    public AjaxPrecessAbstract(PipedOutputStream out, String[] args) {
        this.args = args;
        this.out = out;
    }
    protected abstract boolean canExecute();

    public abstract void run();
}
