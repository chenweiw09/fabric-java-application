package com.my.chen.fabric;

import com.my.chen.fabric.app.util.Util;

/**
 * @author chenwei
 * @version 1.0
 * @date 2018/9/28
 * @description
 */
public class TestMain {

    public static void main(String[] args) {

        try {
            Util.readUserContext("df","dfggh");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("hah");

    }
}
