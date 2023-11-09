package com.example.demo.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class WaterMarkUtil {

    static SimpleDateFormat defaultDateFormat= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    //给byte制成带有时间水印的图片
    public static void SetWaterMark(String url, byte[] decodedBytes, Date nowTime) throws Exception{
        //文件导入byte
        File file = File.createTempFile(UUID.randomUUID().toString(), null);
        FileOutputStream fos=  new FileOutputStream(file);
        fos.write(decodedBytes);
        //加水印
        BufferedImage bi = ImageIO.read(file);
        Graphics2D g2d = (Graphics2D) bi.getGraphics();
        Font font = new Font("Arial", Font.BOLD, 20);
        g2d.setFont(font);
        g2d.setColor(Color.RED);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        //绘制水印
        String watermark = defaultDateFormat.format(nowTime);;
        g2d.drawString(watermark, 20, 20);
        //存入服务器的存储文件夹
        File watermarkedImage = new File(url);
        ImageIO.write(bi, "png", watermarkedImage);
    }
}
