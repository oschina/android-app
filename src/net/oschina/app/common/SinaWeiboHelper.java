package net.oschina.app.common;

import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

import net.oschina.app.AppConfig;
import net.oschina.app.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
/**
 * 新浪微博帮助类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-22
 */
public class SinaWeiboHelper {

	private static final String CONSUMER_KEY = "3616966952";
	private static final String CONSUMER_SECRET = "fd81f6d31427b467f49226e48a741e28";
	private static final String REDIRECT_URL = "http://www.oschina.net";
	
	public static final int OAUTH_ERROR = 0;
	public static final int OAUTH_RequestToken_ACCESS = 1;
	public static final int OAUTH_RequestToken_ERROR = 2;
	public static final int OAUTH_AccessToken_ACCESS = 3;
	public static final int OAUTH_AccessToken_ERROR = 4;
	public static final int OAUTH_AccessToken_SXPIRED = 5;
	public static final int Weibo_Message_CHECKED = 6;
	public static final int Weibo_Message_NULL = 7;
	public static final int Weibo_Message_LONG = 8;
	public static final int Weibo_Share_Success = 9;
	public static final int Weibo_Share_Error = 10;
	public static final int Weibo_Share_Repeat = 11;
	
	private static Weibo 			weibo = null;
	private static AccessToken		accessToken = null;
	private static String 			shareImage = null;
	private static String			shareMessage = null;
	private static Activity			context = null;
	public static ProgressDialog	progressDialog = null;
	
	
	public static void setAccessToken(String accessKey,String accessSecret,long expiresIn){
		accessToken = new AccessToken(accessKey, accessSecret);
		accessToken.setExpiresIn(expiresIn);
	}
	
	public static Handler  handler	= new Handler(){ 
		public void handleMessage(Message msg) { 
			if(progressDialog != null)
				progressDialog.dismiss();
		    switch (msg.what) 
		    { 
		    	case OAUTH_ERROR: 
		    		Toast.makeText(context, context.getString(R.string.OAUTH_ERROR), Toast.LENGTH_SHORT).show();
		    		break; 
		    	case OAUTH_RequestToken_ACCESS:
		    		Toast.makeText(context, context.getString(R.string.OAUTH_RequestToken_ACCESS), Toast.LENGTH_SHORT).show();
		    		break;
		    	case OAUTH_RequestToken_ERROR: 
		    		Toast.makeText(context, context.getString(R.string.OAUTH_RequestToken_ERROR), Toast.LENGTH_SHORT).show();
		    		break; 
		    	case OAUTH_AccessToken_ACCESS:
		    		Toast.makeText(context, context.getString(R.string.OAUTH_AccessToken_ACCESS), Toast.LENGTH_SHORT).show();
		    		break;
		    	case OAUTH_AccessToken_ERROR: 
		    		Toast.makeText(context, context.getString(R.string.OAUTH_AccessToken_ERROR), Toast.LENGTH_SHORT).show();
		    		authorize(context, shareMessage, shareImage);//跳转到授权页面
		    		break;
		    	case OAUTH_AccessToken_SXPIRED: 
		    		Toast.makeText(context, context.getString(R.string.OAUTH_AccessToken_SXPIRED), Toast.LENGTH_SHORT).show();
		    		authorize(context, shareMessage, shareImage);//跳转到授权页面
		    		break;
		    	case Weibo_Message_NULL:
		    		Toast.makeText(context, context.getString(R.string.Weibo_Message_NULL), Toast.LENGTH_SHORT).show();
		    		break;
		    	case Weibo_Message_LONG: 
					Toast.makeText(context, context.getString(R.string.Weibo_Message_LONG), Toast.LENGTH_SHORT ).show();
		    		break;
		    	case Weibo_Share_Success:
		    		Toast.makeText(context, context.getString(R.string.Weibo_Share_Success), Toast.LENGTH_SHORT).show();
		    		break;
		    	case Weibo_Share_Error:
					Toast.makeText(context, context.getString(R.string.Weibo_Share_Error), Toast.LENGTH_SHORT).show();
					break;
		    	case Weibo_Share_Repeat:
		    		Toast.makeText(context, context.getString(R.string.Weibo_Share_Repeat), Toast.LENGTH_SHORT).show();
		    		break;
		    }
		};
	};
	/**
	 * 判断weibo是否为null
	 * @return
	 */
	public static boolean isWeiboNull()
	{
		if(weibo == null)
			return true;
		else 
			return false;
	}
	/**
	 * 初始化weibo
	 */
	public static void initWeibo()
	{
    	weibo = Weibo.getInstance();
    	weibo.setupConsumerConfig(CONSUMER_KEY, CONSUMER_SECRET);
		weibo.setRedirectUrl(REDIRECT_URL);
	}
	/**
	 * 微博授权 并 分享(文本)
	 */
	public static void authorize(Activity cont, String shareMsg)
	{
		authorize(cont, shareMsg, null);
	}
	/**
	 * 微博授权 并 分享(文本、图片)
	 */
	public static void authorize(final Activity cont,final String shareMsg,final String shareImg)
	{		
		context = cont;
		
		if(isWeiboNull())
		{
			initWeibo();
		}
		weibo.authorize(cont, new WeiboDialogListener() {
	    	@Override
			public void onComplete(Bundle values) {
	    		try 
	    		{
					String token = values.getString(Weibo.TOKEN);
					String expires_in = values.getString(Weibo.EXPIRES);
					accessToken = new AccessToken(token, CONSUMER_SECRET);
					accessToken.setExpiresIn(expires_in);	
					//保存AccessToken
					AppConfig.getAppConfig(cont).setAccessInfo(accessToken.getToken(), accessToken.getSecret(), accessToken.getExpiresIn());
					//微博分享
					shareMessage(cont, shareMsg, null);
	    		} 
	    		catch (Exception e) 
	    		{
	    			e.printStackTrace();
	    		}
			}
			@Override
			public void onError(DialogError e) {
				Toast.makeText(context,"授权失败 : " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
			@Override
			public void onCancel() {
				//Toast.makeText(context, "取消授权", Toast.LENGTH_LONG).show();
			}
			@Override
			public void onWeiboException(WeiboException e) {
				Toast.makeText(context,"授权异常 : " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
		});		
	}
	/**
	 * 数据合法性判断
	 * @return
	 */
	public static int messageChecked(String shareMsg)
	{
		int ret = Weibo_Message_CHECKED;
		if( StringUtils.isEmpty(shareMsg) )
		{
			ret = Weibo_Message_NULL;
		}
		else if( shareMsg.length() > 140 )
		{
			ret = Weibo_Message_LONG;
		}
		return ret;
	}
	/**
	 * 微博分享
	 * 分享内容shareMessage & 分享图片shareImage & 当前Activity
     * Toast会提示分享成功或失败
	 */
    public static void shareMessage(Activity cont, String shareMsg, String shareImg)
    {	
    	context = cont;    
    	shareMessage = shareMsg;
    	shareImage = shareImg;
    	
    	if(isWeiboNull())
		{
			initWeibo();
		}
		
    	Message msg = new Message();
    	msg.what = Weibo_Share_Error;	
    	
    	//判断是否授权
    	if(accessToken == null)
    	{
    		msg.what = OAUTH_AccessToken_ERROR;
    		handler.sendMessage(msg);
    		return;
    	}
    	
    	//判断token是否过期
    	if(accessToken.getExpiresIn() < System.currentTimeMillis())
    	{
    		msg.what = OAUTH_AccessToken_SXPIRED;
    		handler.sendMessage(msg);
    		return;
    	}
    	
    	//检查文本是否超出限制
    	int checkCode = messageChecked(shareMsg);
    	if(checkCode != Weibo_Message_CHECKED)
    	{
    		msg.what = checkCode;
    		handler.sendMessage(msg);
    		return;
    	}    	
    	
        try 
        {   
        	if( StringUtils.isEmpty(shareImage) )
        	{
        		weibo.shareToweibo(cont, accessToken.getToken(), accessToken.getSecret(), shareMessage);
        	}
        	else
        	{
        		//分享带图片的微博
        	}
			
        	msg.what = Weibo_Share_Success;

		} 
        catch(WeiboException e)
        {
        	int statusCode = e.getStatusCode();
        	if(statusCode == 20019 || e.getMessage().contains("repeat"))
        		msg.what = Weibo_Share_Repeat;
        	e.printStackTrace();
        }
        catch (Exception e) 
        {
        	e.printStackTrace();
		}
        
        handler.sendMessage(msg);
	}
    /**
     * 微博分享   
     * 分享内容shareMessage & 当前Activity
     * Toast会提示分享成功或失败
     */
    public static void shareMessage(Activity cont, String shareMsg)
    {
    	shareMessage(cont, shareMsg, null);
    }
}
