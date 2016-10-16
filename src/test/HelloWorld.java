package test;

//// This sample uses the Apache HTTP client from HTTP Components (http://hc.apache.org/httpcomponents-client-ga/)
import java.net.URI;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.imageio.*;

import org.apache.http.entity.StringEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost; 
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.*;

public class HelloWorld
{
	private static class rect{
		int top, left, width, height;
		double smile;
		boolean init;
		int count;
		rect(){top = 0; left = 0; width = 0; height = 0; smile = 0; init = false; count = 5;}
		
	}
 
 public	static rect[] people = new rect[5];
 public static String key[] = {"b46d5eb882b34ef48f98c501e4c39bfe", "a5ed05a763ef44fbadf5f577897f3ac9", "59ea58bc5c784098a3f5bcd862132d08", "7794f8768ae743e88fc8b26ded51187f"};
 
 public static void main(String[] argu)
 {
	 String filename, savepath;
	 for(int i = 0; i<5; i++)
		 people[i] = new rect();
	 for(int i = 200; i < 1263; i++){
		 filename = String.format("C:/Users/ZRY/Desktop/pic2/%d.jpg", i);
		 savepath = String.format("C:/Users/ZRY/Desktop/results/image-%d.jpg", i);
		 detect(filename, savepath, i%4);
	 }
 }
 public static int detect (String filename, String savepath, int turns) 
 {
     HttpClient httpclient = HttpClients.createDefault();
     System.out.println(String.format("%s, %d", filename, turns));
     //System.out.println(people[1].height);
     try
     {
         URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/face/v1.0/detect");

         builder.setParameter("returnFaceId", "false");
         builder.setParameter("returnFaceLandmarks", "false");
         builder.setParameter("returnFaceAttributes", "smile");

         URI uri = builder.build();
         HttpPost request = new HttpPost(uri);
         //request.setHeader("Content-Type", "application/json");
         request.setHeader("Content-Type", "application/octet-stream");
         request.setHeader("Ocp-Apim-Subscription-Key", key[turns]);


         // Request body
         //StringEntity reqEntity = new StringEntity( "{'url':'http://images.ifanr.cn/wp-content/uploads/2015/12/xiaoyuankeji.jpg'}");
         //request.setEntity(reqEntity);
         File f = new File(filename);
         BufferedImage image = ImageIO.read(f);
         FileEntity reqEntity = new FileEntity(f);
         request.setEntity(reqEntity);

         HttpResponse response = httpclient.execute(request);
         HttpEntity entity = response.getEntity();
         
         String result = EntityUtils.toString(entity);
         System.out.println(result);
         
         JSONArray Objects;
         JSONObject faceRectangle;
         Graphics2D g;
         g = (Graphics2D)image.getGraphics();
      	 g.setStroke(new BasicStroke(5));
      	 g.setFont(new Font("Serif",Font.BOLD|Font.ITALIC,20));
         try {  
             Objects = new JSONArray(result); 
             for(int i = 0; i < Objects.length();i++){
            	//faceid = Objects.getJSONObject(i).getString("faceId");
                rect tmp = new rect();
             	faceRectangle = Objects.getJSONObject(i).getJSONObject("faceRectangle");
             	tmp.top = faceRectangle.getInt("top");
             	tmp.left = faceRectangle.getInt("left");
             	tmp.width = faceRectangle.getInt("width");
             	tmp.height = faceRectangle.getInt("height");
             	tmp.smile = Objects.getJSONObject(i).getJSONObject("faceAttributes").getDouble("smile");
             	boolean found = false;
             	for(int j=0; j<4; j++){
             		if(Math.abs(people[j].top - tmp.top) < 12 && Math.abs(people[j].left - tmp.left) < 12){
             			people[j] = tmp;
             			people[j].init = true;
             			people[j].count = people[j].count + 1;
             			found = true;
             			break;
             		}
             	}
             	if(!found){
             		for(int j=0; j<4; j++){
             			if(!people[j].init){
             				System.out.println("here");
             				people[j] = tmp;
             				people[j].init = true;
             				people[j].count = people[j].count + 1;
             				break;
             			}
             		}
             	}
             }
         } 
         catch (JSONException e) {  
             e.printStackTrace();  
         }
         int empty = 0;
         int m_height = 0, m_width = 0;
         for(int i=0; i<4; i++){
        	 rect tmp = people[i];
        	 if(tmp.init){
        		people[i].count = people[i].count - 1;
        		if(people[i].count <= 0){
        			people[i].init = false;
        			people[i].count = 5;
        			empty = empty + 1;
        			continue;
        		}
        		m_height += tmp.top + tmp.height / 2;
        		m_width += tmp.left + tmp.width / 2;
              	int color = (int)(255 * tmp.smile);
              	g.setColor(new Color(255 - color,0,color));

              	g.drawRect(tmp.left, tmp.top, tmp.width, tmp.height);
              	g.drawString(String.format("%f", tmp.smile), tmp.left, tmp.top - 5);
              	System.out.printf("%d, %d, %d, %d, %f\n", tmp.top, tmp.left, tmp.width, tmp.height, tmp.smile);
        	 }
        	 else
        		 empty = empty + 1;
         }
         m_height /= (4 - empty);
         m_width /= (4 - empty);
         System.out.printf("%d, %d\n", m_height, m_width);
         if(m_height > 320){
        	 g.drawLine(150, 100, 150, 200);
        	 g.drawLine(150, 200, 120, 150);
        	 g.drawLine(150, 200, 180, 150);
             ImageIO.write(image,  "JPG", new File(savepath));
        	 return 1;
         }
         if(m_height < 180){
        	 g.drawLine(150, 100, 150, 200);
        	 g.drawLine(150, 100, 120, 150);
        	 g.drawLine(150, 100, 180, 150);
             ImageIO.write(image,  "JPG", new File(savepath));
        	 return 2;
         }
         if(m_width > 700){
        	 g.drawLine(200, 150, 100, 150);
        	 g.drawLine(200, 150, 150, 120);
        	 g.drawLine(200, 150, 150, 180);
             ImageIO.write(image,  "JPG", new File(savepath));
        	 return 3;
         }
         if(m_width < 580){
        	 g.drawLine(100, 150, 200, 150);
        	 g.drawLine(100, 150, 150, 120);
        	 g.drawLine(100, 150, 150, 180);
             ImageIO.write(image,  "JPG", new File(savepath));
        	 return 4;
         }
         ImageIO.write(image,  "JPG", new File(savepath));
     }
     catch (Exception e)
     {
         System.out.println(e.getMessage());
     }
     return 0;
 }
}
 
