package com.hou.cartweb.controller;

import java.math.BigDecimal;

public class TestBigDecimal {
    public static void main(String[] args) {
        //初始化    建议用string，否则造成精度损失。
        BigDecimal b1 = new BigDecimal(0.01F);
        BigDecimal b2 = new BigDecimal(0.01D);
        BigDecimal b3 = new BigDecimal("0.01");
        System.out.println(b1); //0.00999999977648258209228515625
        System.out.println(b2); //0.01000000000000000020816681711721685132943093776702880859375
        System.out.println(b3); //0.01


        //比较
        int i = b1.compareTo(b2);
        System.out.println("比较的结果"+i); // -1  b1 < b2
        //运算
        BigDecimal add = b1.add(b2);
        System.out.println("加法"+add);  //0.01999999977648258230045197336721685132943093776702880859375
        BigDecimal subtract = b1.subtract(b2);
        System.out.println("减法" + subtract); //减法-2.2351741811588166086721685132943093776702880859375E-10

        BigDecimal b4 = new BigDecimal("6");
        BigDecimal b5 = new BigDecimal("7");
        System.out.println("乘法"+b4.multiply(b5));//42

//        System.out.println( b4.divide(b5)); //java.lang.ArithmeticException  除不尽  不会约等于 所以报错了
        BigDecimal divide = b4.divide(b5, 3, BigDecimal.ROUND_HALF_DOWN); //保留3位 四舍五入
        System.out.println("除法"+divide);  //0.857

        //约等于
        BigDecimal subtract1 = b2.add(b1);
        System.out.println("减法"+subtract1); //约数2.2351741811588166086721685132943093776702880859375E-10
        BigDecimal bigDecimal = subtract1.setScale(3, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(bigDecimal);  //0.020

        BigDecimal b7 = new BigDecimal(7.00);
        BigDecimal b8 = new BigDecimal("0");
       System.out.println( b8.add(b7));
       System.out.println(0.01);

    }
}
