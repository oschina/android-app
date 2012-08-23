package net.oschina.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.oschina.app.AppException;
import net.oschina.app.bean.Active.ObjectReply;
import net.oschina.app.common.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 用户专页信息实体类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UserInformation extends Entity{
	
	private int pageSize;
	private User user = new User();
	private List<Active> activelist = new ArrayList<Active>();	

	public int getPageSize() {
		return pageSize;
	}
	public User getUser() {
		return user;
	}
	public List<Active> getActivelist() {
		return activelist;
	}

	public static UserInformation parse(InputStream inputStream) throws IOException, AppException {
		UserInformation uinfo = new UserInformation();
		User user = null;
		Active active = null;
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {        	
            xmlParser.setInput(inputStream, UTF8);
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType=xmlParser.getEventType();
			//一直循环，直到文档结束    
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
				String tag = xmlParser.getName(); 
	    		int depth = xmlParser.getDepth();
			    switch(evtType){ 
			    	case XmlPullParser.START_TAG:
			    		if(tag.equalsIgnoreCase("user")) 
			    		{
			    			user = new User();
			    		}
			    		else if(tag.equalsIgnoreCase("pageSize")) 
			    		{
			    			uinfo.pageSize = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if (tag.equalsIgnoreCase("active")) 
			    		{
			    			active = new Active();
			    		}
			    		else if (user != null)
			    		{
			    			if(tag.equalsIgnoreCase("uid")){
								user.setUid(StringUtils.toInt(xmlParser.nextText(), 0));
							}else if(tag.equalsIgnoreCase("from")){
								user.setLocation(xmlParser.nextText());
							}else if(tag.equalsIgnoreCase("name")){
								user.setName(xmlParser.nextText());
							}else if(depth==3 && tag.equalsIgnoreCase("portrait")){
								user.setFace(xmlParser.nextText());
							}else if(tag.equalsIgnoreCase("jointime")){
								user.setJointime(xmlParser.nextText());
							}else if(tag.equalsIgnoreCase("gender")){
								user.setGender(xmlParser.nextText());
							}else if(tag.equalsIgnoreCase("devplatform")){
								user.setDevplatform(xmlParser.nextText());
							}else if(tag.equalsIgnoreCase("expertise")){
								user.setExpertise(xmlParser.nextText());
							}else if(tag.equalsIgnoreCase("relation")){
								user.setRelation(StringUtils.toInt(xmlParser.nextText(), 0));
							}else if(tag.equalsIgnoreCase("latestonline")){
								user.setLatestonline(xmlParser.nextText());
							}
			    		}
			    		else if (active != null)
			    		{	
				            if(tag.equalsIgnoreCase("id"))
				            {			      
				            	active.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(depth==4 && tag.equalsIgnoreCase("portrait"))
				            {			            	
				            	active.setFace(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("message"))
				            {	
				            	active.setMessage(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("author"))
				            {
				            	active.setAuthor(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("authorid"))
				            {			            	
				            	active.setAuthorId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase("catalog"))
				            {			            	
				            	active.setActiveType(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("objectID"))
				            {			            	
				            	active.setObjectId(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("objecttype"))
				            {			            	
				            	active.setObjectType(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("objectcatalog"))
				            {			            	
				            	active.setObjectCatalog(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("objecttitle"))
				            {			            	
				            	active.setObjectTitle(xmlParser.nextText());			            	
				            }
				            else if(tag.equalsIgnoreCase("objectreply"))
				            {			            	
				            	active.setObjectReply(new ObjectReply());	            	
				            }
				            else if(active.getObjectReply()!=null && tag.equalsIgnoreCase("objectname"))
				            {			            	
				            	active.getObjectReply().objectName = xmlParser.nextText();		            	
				            }
				            else if(active.getObjectReply()!=null && tag.equalsIgnoreCase("objectbody"))
				            {			            	
				            	active.getObjectReply().objectBody = xmlParser.nextText();		            	
				            }
				            else if(tag.equalsIgnoreCase("commentCount"))
				            {			            	
				            	active.setCommentCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("pubDate"))
				            {			            	
				            	active.setPubDate(xmlParser.nextText());	            	
				            }
				            else if(tag.equalsIgnoreCase("tweetimage"))
				            {			            	
				            	active.setTweetimage(xmlParser.nextText());			            	
				            }
				            else if(tag.equalsIgnoreCase("appclient"))
				            {			            	
				            	active.setAppClient(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("url"))
				            {			            	
				            	active.setUrl(xmlParser.nextText());			            	
				            }
			    		}  
			            //通知信息
			            else if(tag.equalsIgnoreCase("notice"))
			    		{
			            	uinfo.setNotice(new Notice());
			    		}
			            else if(uinfo.getNotice() != null)
			    		{
			    			if(tag.equalsIgnoreCase("atmeCount"))
				            {			      
			    				uinfo.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("msgCount"))
				            {			            	
				            	uinfo.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("reviewCount"))
				            {			            	
				            	uinfo.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("newFansCount"))
				            {			            	
				            	uinfo.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//如果遇到标签结束，则把对象添加进集合中
			    		if (tag.equalsIgnoreCase("user") && user != null) {
			    			uinfo.user = user;
			    			user = null;
			    		}
			    		else if (tag.equalsIgnoreCase("active") && active != null) { 
				       		uinfo.getActivelist().add(active); 
				       		active = null; 
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
        return uinfo;       
	}
}
