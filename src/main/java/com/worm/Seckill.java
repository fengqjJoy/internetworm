package com.worm;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Project: internetworm 淘宝秒杀
 * @Package: com.worm
 * @Author: 冯前进
 * @Date: 2023-04-17 14:21
 * @Description: TODO
 **/
public class Seckill {
    public static void main(String[] args) throws Exception {
        taoBao();
    }

    public static void taoBao() throws Exception {

        //浏览器驱动路径
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver.exe");

        //设置秒杀时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSSSSSSS");
        Date date = sdf.parse("2022-04-17 14:51:00 000000000");

        //1、打开浏览器
        ChromeDriver browser = new ChromeDriver();
        Actions actions = new Actions(browser);
        //2、输入网址
        browser.get("https://www.taobao.com");
        Thread.sleep(3000);

        //3、点击登录
        browser.findElement(By.linkText("亲，请登录")).click();

        Thread.sleep(2000);

        //4、扫码登录
        browser.findElement(By.className("icon-qrcode")).click();
        Thread.sleep(4000);

        //5、进入购物车页面
        browser.get("https://cart.taobao.com/cart.htm");
        Thread.sleep(3000);

        //6、点击选择第一个按钮
        browser.findElement(By.xpath("//*[@id=\"J_Order_s_2214159300635_1\"]/div[1]/div/div/label")).click();

        Thread.sleep(2000);
        while (true) {
            //当前时间
            Date now = new Date();
            if (now.after(date)) {
                if (browser.findElement(By.linkText("结 算")).isEnabled()) {
                    browser.findElement(By.linkText("结 算")).click();
                    Thread.sleep(3000L);
                    while (true) {
                        System.out.println(By.linkText("提交订单"));
                        if (browser.findElement(By.linkText("提交订单")).isEnabled()) {
                            browser.findElement(By.linkText("提交订单")).click();
                            System.out.println("提交订单成功");
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }
}
