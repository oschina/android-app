package net.oschina.app.bean;

import java.io.IOException;
import java.io.InputStream;

import net.oschina.app.AppException;
import net.oschina.app.common.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 软件实体类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Software extends Entity {

	private String title;
	private String extensionTitle;
	private String license;
	private String body;	
	private String homepage;
	private String document;
	private String download;
	private String logo;	
	private String language;
	private String os;
	private String recordtime;
	private int favorite;
	
	public int getFavorite() {
		return favorite;
	}
	public void setFavorite(int favorite) {
		this.favorite = favorite;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getExtensionTitle() {
		return extensionTitle;
	}
	public void setExtensionTitle(String extensionTitle) {
		this.extensionTitle = extensionTitle;
	}
	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	public String getDocument() {
		return document;
	}
	public void setDocument(String document) {
		this.document = document;
	}
	public String getDownload() {
		return download;
	}
	public void setDownload(String download) {
		this.download = download;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public String getRecordtime() {
		return recordtime;
	}
	public void setRecordtime(String recordtime) {
		this.recordtime = recordtime;
	}
	
	public static Software parse(InputStream inputStream) throws IOException, AppException {
		Software sw = null;
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
			    		if(tag.equalsIgnoreCase("software"))
			    		{
			    			sw = new Software();
			    		}  
			    		else if(sw != null)
			    		{	
				            if(tag.equalsIgnoreCase("id"))
				            {			      
				            	sw.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase("title"))
				            {			            	
				            	sw.setTitle(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("extensionTitle"))
				            {			            	
				            	sw.setExtensionTitle(xmlParser.nextText());
				            }				            
				            else if(tag.equalsIgnoreCase("license"))
				            {			            	
				            	sw.setLicense(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("body"))
				            {			            	
				            	sw.setBody(xmlParser.nextText());
				            }				            
				            else if(tag.equalsIgnoreCase("homepage"))
				            {			            	
				            	sw.setHomepage(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("document"))
				            {			            	
				            	sw.setDocument(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("download"))
				            {			            	
				            	sw.setDownload(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("logo"))
				            {			            	
				            	sw.setLogo(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("language"))
				            {			            	
				            	sw.setLanguage(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("os"))
				            {			            	
				            	sw.setOs(xmlParser.nextText());
				            }				            
				            else if(tag.equalsIgnoreCase("recordtime"))
				            {			            	
				            	sw.setRecordtime(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("favorite"))
				            {			            	
				            	sw.setFavorite(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            //通知信息
				            else if(tag.equalsIgnoreCase("notice"))
				    		{
				            	sw.setNotice(new Notice());
				    		}
				            else if(sw.getNotice() != null)
				    		{
				    			if(tag.equalsIgnoreCase("atmeCount"))
					            {			      
				    				sw.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("msgCount"))
					            {			            	
					            	sw.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("reviewCount"))
					            {			            	
					            	sw.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("newFansCount"))
					            {			            	
					            	sw.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
				    		}
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:		    		
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
        return sw;       
	}
}
