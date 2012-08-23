package net.oschina.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.oschina.app.AppException;
import net.oschina.app.common.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 收藏列表实体类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class FavoriteList extends Entity{
	
	public final static int TYPE_ALL = 0x00;
	public final static int TYPE_SOFTWARE = 0x01;
	public final static int TYPE_POST = 0x02;
	public final static int TYPE_BLOG = 0x03;
	public final static int TYPE_NEWS = 0x04;
	public final static int TYPE_CODE = 0x05;	
	
	private int pageSize;
	private List<Favorite> favoritelist = new ArrayList<Favorite>();
	
	/**
	 * 收藏实体类
	 */
	public static class Favorite implements Serializable {
		public int objid;
		public int type;
		public String title;
		public String url;
	}

	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pagesize) {
		this.pageSize = pagesize;
	}
	public List<Favorite> getFavoritelist() {
		return favoritelist;
	}
	public void setFavoritelist(List<Favorite> favoritelist) {
		this.favoritelist = favoritelist;
	}
	
	public static FavoriteList parse(InputStream inputStream) throws IOException, AppException {
		FavoriteList favoritelist = new FavoriteList();
		Favorite favorite = null;
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {        	
            xmlParser.setInput(inputStream, UTF8);
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType=xmlParser.getEventType();
			//一直循环，直到文档结束    
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
			    switch(evtType){ 
			    	case XmlPullParser.START_TAG:
			    		if(tag.equalsIgnoreCase("pagesize")) 
			    		{
			    			favoritelist.setPageSize(StringUtils.toInt(xmlParser.nextText(),0));
			    		}
			    		else if (tag.equalsIgnoreCase("favorite")) 
			    		{ 
			    			favorite = new Favorite();
			    		}
			    		else if(favorite != null)
			    		{	
				            if(tag.equalsIgnoreCase("objid"))
				            {			      
				            	favorite.objid = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase("type"))
				            {			            	
				            	favorite.type = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase("title"))
				            {			            	
				            	favorite.title = xmlParser.nextText();		            	
				            }
				            else if(tag.equalsIgnoreCase("url"))
				            {			            	
				            	favorite.url = xmlParser.nextText();		            	
				            }
				            
			    		}
			            //通知信息
			            else if(tag.equalsIgnoreCase("notice"))
			    		{
			            	favoritelist.setNotice(new Notice());
			    		}
			            else if(favoritelist.getNotice() != null)
			    		{
			    			if(tag.equalsIgnoreCase("atmeCount"))
				            {			      
			    				favoritelist.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("msgCount"))
				            {			            	
				            	favoritelist.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("reviewCount"))
				            {			            	
				            	favoritelist.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("newFansCount"))
				            {			            	
				            	favoritelist.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//如果遇到标签结束，则把对象添加进集合中
				       	if (tag.equalsIgnoreCase("favorite") && favorite != null) { 
				       		favoritelist.getFavoritelist().add(favorite); 
				       		favorite = null; 
				       	}
				       	break; 
			    }
			    //如果xml没有结束，则导航到下一个节点
			    evtType=xmlParser.next();
			}		
        } catch (XmlPullParserException e) {
			throw AppException.xml(e);
        } finally {
        	inputStream.close();	
        }      
        return favoritelist;       
	}
}
