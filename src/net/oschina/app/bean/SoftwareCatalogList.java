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
 * 软件分类列表实体类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class SoftwareCatalogList extends Entity{
	
	private int softwarecount;
	private List<SoftwareType> softwaretypelist = new ArrayList<SoftwareType>();
	
	public static class SoftwareType implements Serializable {
		public String name;
		public int tag;
	}

	public int getSoftwarecount() {
		return softwarecount;
	}
	public void setSoftwarecount(int softwarecount) {
		this.softwarecount = softwarecount;
	}
	public List<SoftwareType> getSoftwareTypelist() {
		return softwaretypelist;
	}
	public void setSoftwareTypelist(List<SoftwareType> softwaretypelist) {
		this.softwaretypelist = softwaretypelist;
	}
	
	public static SoftwareCatalogList parse(InputStream inputStream) throws IOException, AppException {
		SoftwareCatalogList softwarecatalogList = new SoftwareCatalogList();
		SoftwareType softwaretype = null;
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
			    		if(tag.equalsIgnoreCase("softwarecount")) 
			    		{
			    			softwarecatalogList.setSoftwarecount(StringUtils.toInt(xmlParser.nextText(),0));
			    		}
			    		else if (tag.equalsIgnoreCase("softwareType")) 
			    		{ 
			    			softwaretype = new SoftwareType();
			    		}
			    		else if(softwaretype != null)
			    		{	
				            if(tag.equalsIgnoreCase("name"))
				            {			      
				            	softwaretype.name = xmlParser.nextText();
				            }
				            else if(tag.equalsIgnoreCase("tag"))
				            {			            	
				            	softwaretype.tag = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            
			    		}
			            //通知信息
			            else if(tag.equalsIgnoreCase("notice"))
			    		{
			            	softwarecatalogList.setNotice(new Notice());
			    		}
			            else if(softwarecatalogList.getNotice() != null)
			    		{
			    			if(tag.equalsIgnoreCase("atmeCount"))
				            {			      
			    				softwarecatalogList.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("msgCount"))
				            {			            	
				            	softwarecatalogList.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("reviewCount"))
				            {			            	
				            	softwarecatalogList.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("newFansCount"))
				            {			            	
				            	softwarecatalogList.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//如果遇到标签结束，则把对象添加进集合中
				       	if (tag.equalsIgnoreCase("softwareType") && softwaretype != null) { 
				       		softwarecatalogList.getSoftwareTypelist().add(softwaretype); 
				       		softwaretype = null; 
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
        return softwarecatalogList;       
	}
}
