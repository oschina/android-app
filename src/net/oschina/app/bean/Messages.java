package net.oschina.app.bean;

import java.io.IOException;
import java.io.InputStream;

import net.oschina.app.AppException;
import net.oschina.app.common.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 消息实体类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Messages extends Entity {

	public final static int CLIENT_MOBILE = 2;
	public final static int CLIENT_ANDROID = 3;
	public final static int CLIENT_IPHONE = 4;
	public final static int CLIENT_WINDOWS_PHONE = 5;
	
	private String face;
	private int friendId;
	private String friendName;
	private String sender;
	private int senderId;
	private String content;
	private int messageCount;
	private String pubDate;	
	private int appClient; 
	
	public int getAppClient() {
		return appClient;
	}
	public void setAppClient(int appClient) {
		this.appClient = appClient;
	}
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}	
	public int getFriendId() {
		return friendId;
	}
	public void setFriendId(int friendId) {
		this.friendId = friendId;
	}
	public String getFriendName() {
		return friendName;
	}
	public void setFriendName(String friendName) {
		this.friendName = friendName;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public int getSenderId() {
		return senderId;
	}
	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}
	public int getMessageCount() {
		return messageCount;
	}
	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}
	public String getPubDate() {
		return pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public static Messages parse(InputStream inputStream) throws IOException, AppException {
		Messages msg = null;
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
			    		if(tag.equalsIgnoreCase("message"))
			    		{
			    			msg = new Messages();
			    		}
			    		else if(msg != null)
			    		{	
				            if(tag.equalsIgnoreCase("id"))
				            {			      
				            	msg.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase("portrait"))
				            {			            	
				            	msg.setFace(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("friendid"))
				            {
				            	msg.setFriendId(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("friendname"))
				            {
				            	msg.setFriendName(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("content"))
				            {			            	
				            	msg.setContent(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("sender"))
				            {			            	
				            	msg.setSender(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("senderid"))
				            {			            	
				            	msg.setSenderId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase("messageCount"))
				            {			            	
				            	msg.setMessageCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("pubDate"))
				            {			            	
				            	msg.setPubDate(xmlParser.nextText());	            	
				            }
				            else if(tag.equalsIgnoreCase("appclient"))
				            {			            	
				            	msg.setAppClient(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            //通知信息
				            else if(tag.equalsIgnoreCase("notice"))
				    		{
				            	msg.setNotice(new Notice());
				    		}
				            else if(msg.getNotice() != null)
				    		{
				    			if(tag.equalsIgnoreCase("atmeCount"))
					            {			      
				    				msg.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("msgCount"))
					            {			            	
					            	msg.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("reviewCount"))
					            {			            	
					            	msg.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("newFansCount"))
					            {			            	
					            	msg.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
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
        return msg;       
	}
}
