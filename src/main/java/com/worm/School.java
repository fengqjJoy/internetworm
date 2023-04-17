package com.worm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Project: internetworm
 * @Package: com.worm
 * @Author: 冯前进
 * @Date: 2020-05-16 9:51
 * @Description: TODO
 **/
public class School {
    //创建线程池
    static ExecutorService threadPool = Executors.newFixedThreadPool(20);
    //创建原生阻塞队列  队列最大容量为1000
    static BlockingQueue<String> queue = new ArrayBlockingQueue<String>(1000);

    public static void main(String[] args) throws InterruptedException, IOException {
        //监视队列大小的线程
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //获得队列当前的大小
                    int size = queue.size();
                    System.out.println("当前队列中有" + size + "个pid");
                }
            }
        });

        //开启10个线程去解析手机列表页获得的pids
        for (int i = 1; i <= 10; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String pid = null;
                        try {
                            //从队列中取出pid
                            pid = queue.take();
                            Product product = parsePid(pid);
                            //存入数据库
//                            productDao.addProduct(product);
                            System.out.println(product.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                //出现异常则放回队列
                                queue.put(pid);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            });
        }

        //分页查找手机数据 共100页
        for (int i = 1; i <= 145; i++) {
            String url = "https://api.eol.cn/gkcx/api/?access_token=&admissions=&central=&department=&dual_class=" +
                    "&f211=&f985=&is_dual_class=&keyword=&province_id=&request_type=1&school_type=&signsafe=&size=20" +
                    "&sort=view_total&type=&uri=apigkcx/api/school/hotlists&page="+ i;
            String html = HttpClientUtils.doGet(url);
            writeTxt("F:\\school.txt",html);
            parseIndex(html);
        }
    }
    public static void writeTxt(String txtPath,String content){
        FileOutputStream fileOutputStream = null;
        File file = new File(txtPath);
        try {
            if(file.exists()){
                //判断文件是否存在，如果不存在就新建一个txt
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //解析手机列表页
    private static void parseIndex(String html) throws IOException, InterruptedException {
        Document document = Jsoup.parse(html);
        //手机列表
        Elements elements = document.select("#J_goodsList>ul>li");
        if (elements != null || elements.size() != 0) {
            for (Element element : elements) {
                //获得每个li的pid
                String pid = element.attr("data-pid");
                //将pid放入队列中
                queue.put(pid);
            }
        }
    }

    //解析每个手机的页面 获得某个手机的详细数据
    private static Product parsePid(String pid) throws IOException {
        //拼接url 进入手机详情页
        String productUrl = "https://item.jd.com/" + pid + ".html";
        String productHtml = HttpClientUtils.doGet(productUrl);
        Document document = Jsoup.parse(productHtml);
        Product product = new Product();
        //获得手机标题
        if (document.select("div.sku-name").size() > 0) {
            String title = document.select("div.sku-name").get(0).text();
            product.setTitle(title);
        }
        //获得手机品牌
        String brand = document.select("#parameter-brand li").attr("title");
        product.setBrand(brand);
        //获得手机名称
        String pname = document.select("[class=parameter2 p-parameter-list] li:first-child").attr("title");
        product.setPname(pname);
        /*  此方案无法获取到价格
            jd的价格采用异步刷新,price不在返回的html文档中,需要我们去请求价格页面
            Elements select = document.select("span[class=price J-p-" + pid + "]");
            System.out.println(select);
        */
        //拼接价格页面url 经过测试 返回Json数据  jd对IP进行了限制,加入pduid为随机数,是为了可以获取更多数据,但是依然只能爬取部分
        String priceUrl = "https://p.3.cn/prices/mgets?pduid=" + Math.random() + "&skuIds=J_" + pid;
        String priceJson = HttpClientUtils.doGet(priceUrl);
        System.out.println(priceJson);
        Gson gson = new GsonBuilder().create();
        List<Map<String, String>> list = gson.fromJson(priceJson, List.class);
        String price = list.get(0).get("p");
        product.setPrice(price);
        product.setPid(pid);
        return product;
    }
}
