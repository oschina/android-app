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
 * 新闻实体类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class News extends Entity{
	
	public final static String NODE_ID = "id";
	public final static String NODE_TITLE = "title";
	public final static String NODE_URL = "url";
	public final static String NODE_BODY = "body";
	public final static String NODE_AUTHORID = "authorid";
	public final static String NODE_AUTHOR = "author";
	public final static String NODE_PUBDATE = "pubDate";
	public final static String NODE_COMMENTCOUNT = "commentCount";
	public final static String NODE_FAVORITE = "favorite";
	public final static String NODE_START = "news";
	
	public final static String NODE_SOFTWARELINK = "softwarelink";
	public final static String NODE_SOFTWARENAME = "softwarename";
	
	public final static String NODE_NEWSTYPE = "newstype";
	public final static String NODE_TYPE = "type";
	public final static String NODE_ATTACHMENT = "attachment";
	public final static String NODE_AUTHORUID2 = "authoruid2";
	
	public final static int NEWSTYPE_NEWS = 0x00;//0 新闻
	public final static int NEWSTYPE_SOFTWARE = 0x01;//1 软件
	public final static int NEWSTYPE_POST = 0x02;//2 帖子
	public final static int NEWSTYPE_BLOG = 0x03;//3 博客

	private String title;
	private String url;
	private String body;
	private String author;
	private int authorId;
	private int commentCount;
	private String pubDate;
	private String softwareLink;
	private String softwareName;
	private int favorite;
	private NewsType newType;
	private List<Relative> relatives;

	public News(){
		this.newType = new NewsType();
		this.relatives = new ArrayList<Relative>();
	}	
	
	public class NewsType implements Serializable{
		public int type;
		public String attachment;
		public int authoruid2;
	} 
	
	public static class Relative implements Serializable{
		public String title;
		public String url;
	} 
	
	public List<Relative> getRelatives() {
		return relatives;
	}
	public void setRelatives(List<Relative> relatives) {
		this.relatives = relatives;
	}
	public NewsType getNewType() {
		return newType;
	}
	public void setNewType(NewsType newType) {
		this.newType = newType;
	}	
	public int getFavorite() {
		return favorite;
	}
	public void setFavorite(int favorite) {
		this.favorite = favorite;
	}
	public String getSoftwareLink() {
		return softwareLink;
	}
	public void setSoftwareLink(String softwareLink) {
		this.softwareLink = softwareLink;
	}
	public String getSoftwareName() {
		return softwareName;
	}
	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}
	public String getPubDate() {
		return this.pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public int getAuthorId() {
		return authorId;
	}
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	
	public static News parse(InputStream inputStream) throws IOException, AppException {
		News news = null;
		Relative relative = null;
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
			    		if(tag.equalsIgnoreCase(NODE_START))
			    		{
			    			news = new News();
			    		}
			    		else if(news != null)
			    		{	
				            if(tag.equalsIgnoreCase(NODE_ID))
				            {			      
				            	news.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase(NODE_TITLE))
				            {			            	
				            	news.setTitle(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(NODE_URL))
				            {			            	
				            	news.setUrl(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(NODE_BODY))
				            {			            	
				            	news.setBody(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(NODE_AUTHOR))
				            {			            	
				            	news.setAuthor(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_AUTHORID))
				            {			            	
				            	news.setAuthorId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_COMMENTCOUNT))
				            {			            	
				            	news.setCommentCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_PUBDATE))
				            {			            	
				            	news.setPubDate(xmlParser.nextText());      	
				            }	
				            else if(tag.equalsIgnoreCase(NODE_SOFTWARELINK))
				            {			            	
				            	news.setSoftwareLink(xmlParser.nextText());			            	
				            }	
				            else if(tag.equalsIgnoreCase(NODE_SOFTWARENAME))
				            {			            	
				            	news.setSoftwareName(xmlParser.nextText());			            	
				            }	
				            else if(tag.equalsIgnoreCase(NODE_FAVORITE))
				            {			            	
				            	news.setFavorite(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_TYPE))
				            {	
				            	news.getNewType().type = StringUtils.toInt(xmlParser.nextText(),0); 
				            }
				            else if(tag.equalsIgnoreCase(NODE_ATTACHMENT))
				            {			            	
				            	news.getNewType().attachment = xmlParser.nextText(); 	
				            }
				            else if(tag.equalsIgnoreCase(NODE_AUTHORUID2))
				            {			            	
				            	news.getNewType().authoruid2 = StringUtils.toInt(xmlParser.nextText(),0); 
				            }
				            else if(tag.equalsIgnoreCase("relative"))
				            {			            	
				            	relative = new Relative();
				            }
				            else if(relative != null)
				            {			            	
				            	if(tag.equalsIgnoreCase("rtitle"))
				            	{
				            		relative.title = xmlParser.nextText(); 	
				            	}
				            	else if(tag.equalsIgnoreCase("rurl"))
				            	{
				            		relative.url = xmlParser.nextText(); 	
				            	}
				            }
				            //通知信息
				            else if(tag.equalsIgnoreCase("notice"))
				    		{
				            	news.setNotice(new Notice());
				    		}
				            else if(news.getNotice() != null)
				    		{
				    			if(tag.equalsIgnoreCase("atmeCount"))
					            {			      
				    				news.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("msgCount"))
					            {			            	
					            	news.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("reviewCount"))
					            {			            	
					            	news.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("newFansCount"))
					            {			            	
					            	news.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
				    		}
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:		
			    		//如果遇到标签结束，则把对象添加进集合中
				       	if (tag.equalsIgnoreCase("relative") && news!=null && relative!=null) { 
				       		news.getRelatives().add(relative);
				       		relative = null; 
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
        return news;       
	}
}
