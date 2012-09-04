package net.oschina.app.ui;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.AppManager;
import net.oschina.app.R;
import net.oschina.app.api.ApiClient;
import net.oschina.app.bean.FavoriteList;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.Software;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 软件详情
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class SoftwareDetail extends Activity {
	
	private FrameLayout mHeader;
	private ImageView mBack;
	private ImageView mFavorite;
	private ImageView mRefresh;
	private ProgressBar mProgressbar;
	private ScrollView mScrollView;
	
	private ImageView mLogo;
	private TextView mTitle;
	
	private TextView mLicense;
	private TextView mLanguage;
	private TextView mOS;
	private TextView mRecordtime;
	
	private LinearLayout ll_language;
	private LinearLayout ll_os;
	private ImageView iv_language;
	private ImageView iv_os;
	
	private Button mHomepage;
	private Button mDocment;
	private Button mDownload;
	
	private WebView mWebView;
    private Handler mHandler;
    private Software softwareDetail;
    private Bitmap logo;
    private String ident;
    
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	private final static int DATA_LOAD_FAIL = 0x003;
	
	private GestureDetector gd;
	private boolean isFullScreen;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.software_detail);
        
        AppManager.getAppManager().addActivity(this);
        
        this.initView();        
        this.initData();
        
        //注册双击全屏事件
    	this.regOnDoubleEvent();
    }
    
    //初始化视图控件
    private void initView()
    {
    	mHeader = (FrameLayout)findViewById(R.id.software_detail_header);
    	mBack = (ImageView)findViewById(R.id.software_detail_back);
    	mFavorite = (ImageView)findViewById(R.id.software_detail_favorite);
    	mRefresh = (ImageView)findViewById(R.id.software_detail_refresh);
    	mProgressbar = (ProgressBar)findViewById(R.id.software_detail_head_progress);
    	mScrollView = (ScrollView)findViewById(R.id.software_detail_scrollview);
    	
    	mLogo = (ImageView)findViewById(R.id.software_detail_logo);
    	mTitle = (TextView)findViewById(R.id.software_detail_title);
    	
    	mLicense = (TextView)findViewById(R.id.software_detail_license);
    	mLanguage = (TextView)findViewById(R.id.software_detail_language);
    	mOS = (TextView)findViewById(R.id.software_detail_os);
    	mRecordtime = (TextView)findViewById(R.id.software_detail_recordtime);
    	
    	mHomepage = (Button)findViewById(R.id.software_detail_homepage);
    	mDocment = (Button)findViewById(R.id.software_detail_document);
    	mDownload = (Button)findViewById(R.id.software_detail_download);
    	
    	ll_language = (LinearLayout)findViewById(R.id.software_detail_language_ll);
    	ll_os = (LinearLayout)findViewById(R.id.software_detail_os_ll);
    	iv_language = (ImageView)findViewById(R.id.software_detail_language_iv);
    	iv_os = (ImageView)findViewById(R.id.software_detail_os_iv);
    	
    	mWebView = (WebView)findViewById(R.id.software_detail_webview);
    	mWebView.getSettings().setJavaScriptEnabled(false);
    	mWebView.getSettings().setSupportZoom(true);
    	mWebView.getSettings().setBuiltInZoomControls(true);
    	mWebView.getSettings().setDefaultFontSize(15);
    	
    	mBack.setOnClickListener(UIHelper.finish(this));
    	mFavorite.setOnClickListener(favoriteClickListener);
    	mRefresh.setOnClickListener(refreshClickListener);
    }
    
    //初始化控件数据
	private void initData()
	{
		ident = getIntent().getStringExtra("ident");
		
		mHandler = new Handler()
		{
			public void handleMessage(Message msg) 
			{
				if(msg.what == 1)
				{	
					headButtonSwitch(DATA_LOAD_COMPLETE);
					
					//是否收藏
					if(softwareDetail.getFavorite() == 1)
						mFavorite.setImageResource(R.drawable.head_favorite_y);
					else
						mFavorite.setImageResource(R.drawable.head_favorite_n);
					
					mLogo.setImageBitmap(logo);
					
					String title = softwareDetail.getExtensionTitle()+" "+softwareDetail.getTitle();
					mTitle.setText(title);
					
					String body = UIHelper.WEB_STYLE + softwareDetail.getBody();
					//读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
					boolean isLoadImage;
					AppContext ac = (AppContext)getApplication();
					if(AppContext.NETTYPE_WIFI == ac.getNetworkType()){
						isLoadImage = true;
					}else{
						isLoadImage = ac.isLoadImage();
					}
					if(isLoadImage){
						body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+","$1");
						body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+","$1");
					}else{
						body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>","");
					}

					mWebView.loadDataWithBaseURL(null, body, "text/html", "utf-8",null);
					mWebView.setWebViewClient(UIHelper.getWebViewClient());
					
					mLicense.setText(softwareDetail.getLicense());
					mRecordtime.setText(softwareDetail.getRecordtime());
					String language = softwareDetail.getLanguage();
					String os = softwareDetail.getOs();
					if(StringUtils.isEmpty(language)){
						ll_language.setVisibility(View.GONE);
						iv_language.setVisibility(View.GONE);
					}else{
						mLanguage.setText(language);
					}
					if(StringUtils.isEmpty(os)){
						ll_os.setVisibility(View.GONE);
						iv_os.setVisibility(View.GONE);
					}else{
						mOS.setText(os);
					}

					if(StringUtils.isEmpty(softwareDetail.getHomepage())){
						mHomepage.setVisibility(View.GONE);
					}else{
						mHomepage.setOnClickListener(homepageClickListener);						
					}
					if(StringUtils.isEmpty(softwareDetail.getDocument())){
						mDocment.setVisibility(View.GONE);
					}else{
						mDocment.setOnClickListener(docmentClickListener);						
					}
					if(StringUtils.isEmpty(softwareDetail.getDownload())){
						mDownload.setVisibility(View.GONE);
					}else{
						mDownload.setOnClickListener(downloadClickListener);						
					}
					
					//发送通知广播
					if(msg.obj != null){
						UIHelper.sendBroadCast(SoftwareDetail.this, (Notice)msg.obj);
					}					
				}
				else if(msg.what == 0)
				{
					headButtonSwitch(DATA_LOAD_FAIL);
					
					UIHelper.ToastMessage(SoftwareDetail.this, R.string.msg_load_is_null);
				}
				else if(msg.what == -1 && msg.obj != null)
				{
					headButtonSwitch(DATA_LOAD_FAIL);
					
					((AppException)msg.obj).makeToast(SoftwareDetail.this);
				}
			}
		};
		
		initData(ident, false);
	}
	
    private void initData(final String ident, final boolean isRefresh)
    {
    	headButtonSwitch(DATA_LOAD_ING);
		
		new Thread(){
			public void run() {
                Message msg = new Message();
				try {
					softwareDetail = ((AppContext)getApplication()).getSoftware(ident, isRefresh);
					if(softwareDetail != null){
						logo = ApiClient.getNetBitmap(softwareDetail.getLogo());
					}
	                msg.what = (softwareDetail!=null && softwareDetail.getId()>0) ? 1 : 0;
	                msg.obj = (softwareDetail!=null) ? softwareDetail.getNotice() : null;
	            } catch (AppException e) {
	                e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
                mHandler.sendMessage(msg);
			}
		}.start();
    }
    
    /**
     * 头部按钮展示
     * @param type
     */
    private void headButtonSwitch(int type) {
    	switch (type) {
		case DATA_LOAD_ING:
			mScrollView.setVisibility(View.GONE);
			mProgressbar.setVisibility(View.VISIBLE);
			mFavorite.setVisibility(View.GONE);
			mRefresh.setVisibility(View.GONE);
			break;
		case DATA_LOAD_COMPLETE:
			mScrollView.setVisibility(View.VISIBLE);
			mProgressbar.setVisibility(View.GONE);
			mFavorite.setVisibility(View.VISIBLE);
			mRefresh.setVisibility(View.GONE);
			break;
		case DATA_LOAD_FAIL:
			mScrollView.setVisibility(View.GONE);
			mProgressbar.setVisibility(View.GONE);
			mFavorite.setVisibility(View.GONE);
			mRefresh.setVisibility(View.VISIBLE);
			break;
		}
    }
	
	private View.OnClickListener favoriteClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			if(ident == "" || softwareDetail == null){
				return;
			}
			
			final AppContext ac = (AppContext)getApplication();
			if(!ac.isLogin()){
				UIHelper.showLoginDialog(SoftwareDetail.this);
				return;
			}
			final int uid = ac.getLoginUid();
						
			final Handler handler = new Handler(){
				public void handleMessage(Message msg) {
					if(msg.what == 1){
						Result res = (Result)msg.obj;
						if(res.OK()){
							if(softwareDetail.getFavorite() == 1){
								softwareDetail.setFavorite(0);
								mFavorite.setImageResource(R.drawable.head_favorite_n);
							}else{
								softwareDetail.setFavorite(1);
								mFavorite.setImageResource(R.drawable.head_favorite_y);
							}	
						}
						UIHelper.ToastMessage(SoftwareDetail.this, res.getErrorMessage());
					}else{
						((AppException)msg.obj).makeToast(SoftwareDetail.this);
					}
				}        			
    		};
    		new Thread(){
				public void run() {
					Message msg = new Message();
					Result res = null;
					try {
						if(softwareDetail.getFavorite() == 1){
							res = ac.delFavorite(uid, softwareDetail.getId(), FavoriteList.TYPE_SOFTWARE);
						}else{
							res = ac.addFavorite(uid, softwareDetail.getId(), FavoriteList.TYPE_SOFTWARE);
						}
						msg.what = 1;
						msg.obj = res;
		            } catch (AppException e) {
		            	e.printStackTrace();
		            	msg.what = -1;
		            	msg.obj = e;
		            }
	                handler.sendMessage(msg);
				}        			
    		}.start();	
		}
	};
	
	private View.OnClickListener homepageClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			UIHelper.openBrowser(v.getContext(), softwareDetail.getHomepage());
		}
	};
	
	private View.OnClickListener refreshClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			initData(ident, true);
		}
	};
	
	private View.OnClickListener docmentClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			UIHelper.openBrowser(v.getContext(), softwareDetail.getDocument());
		}
	};
	
	private View.OnClickListener downloadClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			UIHelper.openBrowser(v.getContext(), softwareDetail.getDownload());
		}
	};
	
	/**
	 * 注册双击全屏事件
	 */
	private void regOnDoubleEvent(){
		gd = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				isFullScreen = !isFullScreen;
				if (!isFullScreen) {   
                    WindowManager.LayoutParams params = getWindow().getAttributes();   
                    params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);   
                    getWindow().setAttributes(params);   
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);  
                    mHeader.setVisibility(View.VISIBLE);
                } else {    
                    WindowManager.LayoutParams params = getWindow().getAttributes();   
                    params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;   
                    getWindow().setAttributes(params);   
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);   
                    mHeader.setVisibility(View.GONE);
                }
				return true;
			}
		});
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		gd.onTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}
}
