package net.oschina.app.widget;

import net.oschina.app.bean.URLs;
import net.oschina.app.common.UIHelper;
import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * 超链接文本控件
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class LinkView extends TextView {

	public LinkView(Context context) {
		super(context);
	}
	
	public LinkView(Context context, AttributeSet attrs) {  
        super(context, attrs);
    }  
	
	public LinkView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);
    }  
	
	public void setLinkText(String linktxt){
		Spanned span = Html.fromHtml(linktxt);
		setText(span);
		setMovementMethod(LinkMovementMethod.getInstance());
		
		parseLinkText(span);
	}
	
	public void parseLinkText(Spanned spanhtml){
		CharSequence text = getText();
		if(text instanceof Spannable){
			int end = text.length();
			Spannable sp = (Spannable)getText();
			URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);	
			
			URLSpan[] htmlurls = spanhtml != null ? spanhtml.getSpans(0, end, URLSpan.class) : new URLSpan[]{};
			
			if(urls.length == 0 && htmlurls.length == 0) return;
			
			SpannableStringBuilder style = new SpannableStringBuilder(text);
			//style.clearSpans();// 这里会清除之前所有的样式
			for(URLSpan url : urls){
				style.removeSpan(url);//只需要移除之前的URL样式，再重新设置
				MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
				style.setSpan(myURLSpan,sp.getSpanStart(url),sp.getSpanEnd(url),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			for(URLSpan url : htmlurls){
				style.removeSpan(url);//只需要移除之前的URL样式，再重新设置
				MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
				style.setSpan(myURLSpan,spanhtml.getSpanStart(url),spanhtml.getSpanEnd(url),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			setText(style);
		}
	}
	
	public void parseLinkText(){
		parseLinkText(null);
	}
	
	private static class MyURLSpan extends ClickableSpan{     
		private String mUrl;     
		MyURLSpan(String url) {     
			mUrl =url;     
		}     
		@Override  
		public void onClick(View widget) {  
			URLs urls = URLs.parseURL(mUrl);
			if(urls != null){
				UIHelper.showLinkRedirect(widget.getContext(), urls.getObjType(), urls.getObjId(), urls.getObjKey());
			}else{
				UIHelper.openBrowser(widget.getContext(), mUrl);
			}
		}     
	} 

}
