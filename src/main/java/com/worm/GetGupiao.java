package com.worm;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * @Project: internetworm
 * @Package: com.worm
 * @Author: 冯前进
 * @Date: 2020-07-09 14:59
 * @Description: TODO
 **/
public class GetGupiao {
    public static void main(String[] args) throws IOException {
        String url="http://data.eastmoney.com/DataCenter_V3/yjfp/getlist.ashx?js=var%20gyeyFTLe&pagesize=548&page=1" +
                "&sr=-1&sortType=YAGGR&mtk=" + URLEncoder.encode("全部股票", "GBK")+
//                "&filter=(YCQTS%3C0)(ReportingPeriod=^2019-12-31^)" +
                "&filter=" +URLEncoder.encode("(YCQTS<0)(ReportingPeriod=^2019-12-31^)","GBK")+
                "&rt=53142565";
        String html=HttpClientUtils.doGet(url);
        String Json =html.substring(13);
        JSONObject jsonObject=JSONObject.parseObject(Json);
        String data =jsonObject.get("data").toString();
        List<Gu> guList=JSONObject.parseArray(data,Gu.class);
        guList.forEach(gu->{
            if(gu.getGqdjr().length()>9){
                gu.setGqdjr(gu.getGqdjr().substring(0,10));
            }else {
                gu.setGqdjr("");
            }
        });
        System.out.println(guList);
    }
}
