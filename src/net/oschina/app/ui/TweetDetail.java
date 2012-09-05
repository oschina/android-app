package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.oschina.app.AppConfig;
import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.R;
import net.oschina.app.adapter.GridViewFaceAdapter;
import net.oschina.app.adapter.ListViewCommentAdapter;
import net.oschina.app.bean.Comment;
import net.oschina.app.bean.CommentList;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.Tweet;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * 动弹详情
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class TweetDetail extends Activity {
	
	private ImageView mBack;
	private ImageView mRefresh;
	private LinearLayout mLinearlayout;
	private ProgressBar mProgressbar;
	
	private PullToRefreshListView mLvComment;
	private ListViewCommentAdapter lvCommentAdapter;
	private List<Comment> lvCommentData = new ArrayList<Comment>();
	private View lvComment_footer;
	private TextView lvComment_foot_more;
	private ProgressBar lvComment_foot_progress;
    private Handler mCommentHandler;
    private int lvSumData;
    
    private View lvHeader;
    private ImageView userface;
    private TextView username;
    private TextView date;
    private TextView commentCount;
    private WebView content;
    private ImageView image;
    private Handler mHandler;
    private Tweet tweetDetail;
    
    private int curId;
	private int curCatalog;	
	private int curLvDataState;

	private ViewSwitcher mFootViewSwitcher;
	private ImageView mFootEditebox;
	private EditText mFootEditer;
	private Button mFootPubcomment;
	private ProgressDialog mProgress;
	private InputMethodManager imm;
	private String tempCommentKey = AppConfig.TEMP_COMMENT;
	
	private ImageView mFace;
	private GridView mGridView;
	private GridViewFaceAdapter mGVFaceAdapter;
	
	private int _catalog;
	private int _id;
	private int _uid;
	private String _content;
	private int _isPostToMyZone; 
	
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tweet_detail);
        
        //初始化视图控件
        this.initView();
        //初始化控件数据
        this.initData();   
        //初始化表情视图
      	this.initGridView();
    }
    
    /**
     * 头部加载展示
     * @param type
     * @param action 1-init 2-refresh
     */
    private void headButtonSwitch(int type,int action) {
    	switch (type) {
		case DATA_LOAD_ING:
			if(action==1)mLinearlayout.setVisibility(View.GONE);
			mProgressbar.setVisibility(View.VISIBLE);
			mRefresh.setVisibility(View.GONE);
			break;
		case DATA_LOAD_COMPLETE:
			mLinearlayout.setVisibility(View.VISIBLE);
			mProgressbar.setVisibility(View.GONE);
			mRefresh.setVisibility(View.VISIBLE);
			break;
		}
    }
    
    //初始化视图控件
    private void initView()
    {    	
		curId = getIntent().getIntExtra("tweet_id", 0);
		curCatalog = CommentList.CATALOG_TWEET;
    	
    	if(curId > 0) tempCommentKey = AppConfig.TEMP_COMMENT + "_" + curCatalog + "_" + curId;
    	
    	mBack = (ImageView)findViewById(R.id.tweet_detail_back);
    	mRefresh = (ImageView)findViewById(R.id.tweet_detail_refresh);
    	mLinearlayout = (LinearLayout)findViewById(R.id.tweet_detail_linearlayout);
    	mProgressbar = (ProgressBar)findViewById(R.id.tweet_detail_head_progress);
    	mFace = (ImageView)findViewById(R.id.tweet_detail_foot_face);
    	
    	mBack.setOnClickListener(UIHelper.finish(this));
    	mRefresh.setOnClickListener(refreshClickListener);
    	mFace.setOnClickListener(facesClickListener);
    	
    	imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); 
    	
    	mFootViewSwitcher = (ViewSwitcher)findViewById(R.id.tweet_detail_foot_viewswitcher);
    	mFootPubcomment = (Button)findViewById(R.id.tweet_detail_foot_pubcomment);
    	mFootPubcomment.setOnClickListener(commentpubClickListener);
    	mFootEditebox = (ImageView)findViewById(R.id.tweet_detail_footbar_editebox);
    	mFootEditebox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mFootViewSwitcher.showNext();
				mFootEditer.setVisibility(View.VISIBLE);
				mFootEditer.requestFocus();
				mFootEditer.requestFocusFromTouch();
				imm.showSoftInput(mFootEditer, 0);//显示软键盘
			}
		});
    	mFootEditer = (EditText)findViewById(R.id.tweet_detail_foot_editer);
    	mFootEditer.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//显示软键盘&隐藏表情
				showIMM();
			}
		});
    	mFootEditer.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if(mFootViewSwitcher.getDisplayedChild()==1){
						mFootViewSwitcher.setDisplayedChild(0);
						mFootEditer.clearFocus();//隐藏软键盘
						mFootEditer.setVisibility(View.GONE);//隐藏编辑框
						hideFace();//隐藏表情
					}
					return true;
				}
				return false;
			}
		});
    	//编辑器添加文本监听
    	mFootEditer.addTextChangedListener(UIHelper.getTextWatcher(this, tempCommentKey));
    	
    	//显示临时编辑内容
    	UIHelper.showTempEditContent(this, mFootEditer, tempCommentKey);
    	
    	lvHeader = View.inflate(this, R.layout.tweet_detail_content, null);
    	userface = (ImageView)lvHeader.findViewById(R.id.tweet_listitem_userface);
    	username = (TextView)lvHeader.findViewById(R.id.tweet_listitem_username);
    	date = (TextView)lvHeader.findViewById(R.id.tweet_listitem_date);
    	commentCount = (TextView)lvHeader.findViewById(R.id.tweet_listitem_commentCount);
    	image = (ImageView)lvHeader.findViewById(R.id.tweet_listitem_image);
    	
    	content = (WebView)lvHeader.findViewById(R.id.tweet_listitem_content);
    	content.getSettings().setJavaScriptEnabled(false);
    	content.getSettings().setSupportZoom(true);
    	content.getSettings().setBuiltInZoomControls(true);
    	content.getSettings().setDefaultFontSize(12);
    	
    	lvComment_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
    	lvComment_foot_more = (TextView)lvComment_footer.findViewById(R.id.listview_foot_more);
        lvComment_foot_progress = (ProgressBar)lvComment_footer.findViewById(R.id.listview_foot_progress);

    	lvCommentAdapter = new ListViewCommentAdapter(this, lvCommentData, R.layout.comment_listitem); 
    	mLvComment = (PullToRefreshListView)findViewById(R.id.tweet_detail_commentlist);
    	
    	mLvComment.addHeaderView(lvHeader);//把动弹详情放进listview头部
        mLvComment.addFooterView(lvComment_footer);//添加底部视图  必须在setAdapter前
        mLvComment.setAdapter(lvCommentAdapter); 
        mLvComment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == lvComment_footer || position == 1 || view == lvHeader) return;
        		
        		Comment com = null;
        		//判断是否是TextView
        		if(view instanceof TextView){
        			com = (Comment)view.getTag();
        		}else{
            		ImageView img = (ImageView)view.findViewById(R.id.comment_listitem_userface);
            		com = (Comment)img.getTag();
        		} 
        		if(com == null) return;        		
        		
        		//跳转--回复评论界面
        		UIHelper.showCommentReply(TweetDetail.this,curId, curCatalog, com.getId(), com.getAuthorId(), com.getAuthor(), com.getContent());
        	}
		});
        mLvComment.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mLvComment.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(lvCommentData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvComment_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				if(scrollEnd && curLvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					lvComment_foot_more.setText(R.string.load_ing);
					lvComment_foot_progress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = lvSumData/20;
					loadLvCommentData(curId, curCatalog, pageIndex, mCommentHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				mLvComment.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
        mLvComment.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				//点击头部、底部栏无效
        		if(position == 0 || view == lvComment_footer || position == 1 || view == lvHeader) return false;
				
        		Comment _com = null;
        		//判断是否是TextView
        		if(view instanceof TextView){
        			_com = (Comment)view.getTag();
        		}else{
            		ImageView img = (ImageView)view.findViewById(R.id.comment_listitem_userface);
            		_com = (Comment)img.getTag();
        		} 
        		if(_com == null) return false;
        		
        		final Comment com = _com;
        		
        		final AppContext ac = (AppContext)getApplication();
				//操作--回复 & 删除
        		int uid = ac.getLoginUid();
        		//判断该评论是否是当前登录用户发表的：true--有删除操作  false--没有删除操作
        		if(uid == com.getAuthorId())
        		{
	        		final Handler handler = new Handler(){
						public void handleMessage(Message msg) {
							if(msg.what == 1){
								Result res = (Result)msg.obj;
								if(res.OK()){
									lvSumData--;
									lvCommentData.remove(com);
									lvCommentAdapter.notifyDataSetChanged();
								}
								UIHelper.ToastMessage(TweetDetail.this, res.getErrorMessage());
							}else{
								((AppException)msg.obj).makeToast(TweetDetail.this);
							}
						}        			
	        		};
	        		final Thread thread = new Thread(){
						public void run() {
							Message msg = new Message();
							try {
								Result res = ac.delComment(curId, curCatalog, com.getId(), com.getAuthorId());
								msg.what = 1;
								msg.obj = res;
				            } catch (AppException e) {
				            	e.printStackTrace();
				            	msg.what = -1;
				            	msg.obj = e;
				            }
			                handler.sendMessage(msg);
						}        			
	        		};
	        		UIHelper.showCommentOptionDialog(TweetDetail.this, curId, curCatalog, com, thread);
        		}
        		else
        		{
        			UIHelper.showCommentOptionDialog(TweetDetail.this, curId, curCatalog, com, null);
        		}
				return true;
			}        	
		});
        mLvComment.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadLvCommentData(curId, curCatalog, 0, mCommentHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }
    
    //初始化控件数据
	private void initData()
	{		
    	//加载动弹
    	mHandler = new Handler(){
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE,1);
				
				if(msg.what == 1){
					username.setText(tweetDetail.getAuthor());
			    	username.setOnClickListener(faceClickListener);
					date.setText(StringUtils.friendly_time(tweetDetail.getPubDate()));
					commentCount.setText(tweetDetail.getCommentCount()+"");
					
					String body = UIHelper.WEB_STYLE + tweetDetail.getBody();
					body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+","$1");
					body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+","$1");

					content.loadDataWithBaseURL(null, body, "text/html", "utf-8",null);
					content.setWebViewClient(UIHelper.getWebViewClient());
					
					//加载用户头像
					String faceURL = tweetDetail.getFace();
					if(faceURL.endsWith("portrait.gif") || StringUtils.isEmpty(faceURL)) {
						userface.setImageResource(R.drawable.widget_dface);
					}else{
						UIHelper.showUserFace(userface, faceURL);
					}
					userface.setOnClickListener(faceClickListener);
					
					//加载图片
					String imgSmall = tweetDetail.getImgSmall();
					if(!StringUtils.isEmpty(imgSmall)) {
						UIHelper.showLoadImage(image, imgSmall, null);
						image.setVisibility(View.VISIBLE);
						image.setOnClickListener(imageClickListener);
					}
					
					//发送通知广播
					if(msg.obj != null){
						UIHelper.sendBroadCast(TweetDetail.this, (Notice)msg.obj);
					}
				}else if(msg.what == 0){
						UIHelper.ToastMessage(TweetDetail.this, R.string.msg_load_is_null);	
				}else{
					((AppException)msg.obj).makeToast(TweetDetail.this);
				}
			}
		};
    	this.loadTweetDetail(curId, mHandler, false);
    	
    	//加载评论
    	mCommentHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE,2);
				
				if(msg.what >= 0){						
					CommentList list = (CommentList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
						lvSumData = msg.what;
						lvCommentData.clear();//先清除原有数据
						lvCommentData.addAll(list.getCommentlist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvSumData += msg.what;
						if(lvCommentData.size() > 0){
							for(Comment com1 : list.getCommentlist()){
								boolean b = false;
								for(Comment com2 : lvCommentData){
									if(com1.getId() == com2.getId() && com1.getAuthorId() == com2.getAuthorId()){
										b = true;
										break;
									}
								}
								if(!b) lvCommentData.add(com1);
							}
						}else{
							lvCommentData.addAll(list.getCommentlist());
						}
						break;
					}	
					
					if(msg.what < 20){
						curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
						lvCommentAdapter.notifyDataSetChanged();
						lvComment_foot_more.setText(R.string.load_full);
					}else if(msg.what == 20){					
						curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
						lvCommentAdapter.notifyDataSetChanged();
						lvComment_foot_more.setText(R.string.load_more);
					}
					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(TweetDetail.this, notice);
					}
				}				
				else if(msg.what == -1){
					//有异常--也显示更多 & 弹出错误消息
					curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
					lvComment_foot_more.setText(R.string.load_more);
					((AppException)msg.obj).makeToast(TweetDetail.this);
				}
				if(lvCommentData.size()==0){
					curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
					lvComment_foot_more.setText(R.string.load_empty);
				}
				lvComment_foot_progress.setVisibility(View.GONE);
				if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH)
					mLvComment.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
			}
		};
		this.loadLvCommentData(curId,curCatalog,0,mCommentHandler,UIHelper.LISTVIEW_ACTION_INIT);
    }
    /**
     * 线程加载评论数据
     * @param id 当前文章id
     * @param catalog 分类
     * @param pageIndex 当前页数
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvCommentData(final int id,final int catalog,final int pageIndex,final Handler handler,final int action){  
		
		this.headButtonSwitch(DATA_LOAD_ING,2);
		
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					CommentList commentlist = ((AppContext)getApplication()).getCommentList(catalog, id, pageIndex, isRefresh);				
					msg.what = commentlist.getPageSize();
					msg.obj = commentlist;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
                handler.sendMessage(msg);
			}
		}.start();
	} 
	
	/**
	 * 线程加载动弹详情
	 * @param tweetId
	 * @param handler
	 */
	private void loadTweetDetail(final int tweetId, final Handler handler, final boolean isRefresh){
		
		this.headButtonSwitch(DATA_LOAD_ING,1);
		
		new Thread(){
			public void run() {
				Message msg = new Message();
				try {
					tweetDetail = ((AppContext)getApplication()).getTweet(tweetId, isRefresh);
					msg.what = (tweetDetail!=null && tweetDetail.getId()>0) ? 1 : 0;
					msg.obj = (tweetDetail!=null) ? tweetDetail.getNotice() : null;
				} catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{ 	
		if (resultCode != RESULT_OK) return;   
    	if (data == null) return;
        if (requestCode == UIHelper.REQUEST_CODE_FOR_RESULT) 
        { 
        	Comment comm = (Comment)data.getSerializableExtra("COMMENT_SERIALIZABLE");
        	lvCommentData.add(0, comm);
        	lvCommentAdapter.notifyDataSetChanged();
        	mLvComment.setSelection(0);
        }
	}
	
	//初始化表情控件
    private void initGridView() {
    	mGVFaceAdapter = new GridViewFaceAdapter(this);
    	mGridView = (GridView)findViewById(R.id.tweet_detail_foot_faces);
    	mGridView.setAdapter(mGVFaceAdapter);
    	mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//插入的表情
				SpannableString ss = new SpannableString(view.getTag().toString());
				Drawable d = getResources().getDrawable((int)mGVFaceAdapter.getItemId(position));
				d.setBounds(0, 0, 35, 35);//设置表情图片的显示大小
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
				ss.setSpan(span, 0, view.getTag().toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);				 
				//在光标所在处插入表情
				mFootEditer.getText().insert(mFootEditer.getSelectionStart(), ss);				
			}    		
    	});
    }
    
    private void showIMM() {
    	mFace.setTag(1);
    	showOrHideIMM();
    }
    private void showFace() {
		mFace.setImageResource(R.drawable.widget_bar_keyboard);
		mFace.setTag(1);
		mGridView.setVisibility(View.VISIBLE);
    }
    private void hideFace() {
    	mFace.setImageResource(R.drawable.widget_bar_face);
		mFace.setTag(null);
		mGridView.setVisibility(View.GONE);
    }
    private void showOrHideIMM() {
    	if(mFace.getTag() == null){
			//隐藏软键盘
			imm.hideSoftInputFromWindow(mFootEditer.getWindowToken(), 0);
			//显示表情
			showFace();
		}else{
			//显示软键盘
			imm.showSoftInput(mFootEditer, 0);
			//隐藏表情
			hideFace();
		}
    }
    
    //表情控件点击事件
    private View.OnClickListener facesClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			showOrHideIMM();
		}
	};
	
    private View.OnClickListener refreshClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			loadTweetDetail(curId, mHandler, true);
			loadLvCommentData(curId,curCatalog,0,mCommentHandler,UIHelper.LISTVIEW_ACTION_REFRESH);
		}
	};
	
	private View.OnClickListener faceClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			if(tweetDetail != null)
				UIHelper.showUserCenter(v.getContext(), tweetDetail.getAuthorId(), tweetDetail.getAuthor());
		}
	};
	
	private View.OnClickListener imageClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			if(tweetDetail!=null)
				UIHelper.showImageZoomDialog(v.getContext(), tweetDetail.getImgBig());
		}
	};
	
	private View.OnClickListener commentpubClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			_id = curId;
			
			if(curId == 0){
				return;
			}
			
			_catalog = curCatalog;
			
			_content = mFootEditer.getText().toString();
			if(StringUtils.isEmpty(_content)){
				UIHelper.ToastMessage(v.getContext(), "请输入评论内容");
				return;
			}
			
			final AppContext ac = (AppContext)getApplication();
			if(!ac.isLogin()){
				UIHelper.showLoginDialog(TweetDetail.this);
				return;
			}
			
//			if(mZone.isChecked())
//				_isPostToMyZone = 1;
				
			_uid = ac.getLoginUid();
			
			mProgress = ProgressDialog.show(v.getContext(), null, "发布中···",true,true); 
			
			final Handler handler = new Handler(){
				public void handleMessage(Message msg) {
					
					if(mProgress!=null)mProgress.dismiss();
					
					if(msg.what == 1 && msg.obj != null){
						Result res = (Result)msg.obj;
						UIHelper.ToastMessage(TweetDetail.this, res.getErrorMessage());
						if(res.OK()){
							//发送通知广播
							if(res.getNotice() != null){
								UIHelper.sendBroadCast(TweetDetail.this, res.getNotice());
							}
							//恢复初始底部栏
							mFootViewSwitcher.setDisplayedChild(0);
							mFootEditer.clearFocus();
							mFootEditer.setText("");
							mFootEditer.setVisibility(View.GONE);
							//隐藏软键盘
							imm.hideSoftInputFromWindow(mFootEditer.getWindowToken(), 0);
							//隐藏表情
							hideFace();
							//更新评论列表
							lvCommentData.add(0, res.getComment());
							lvCommentAdapter.notifyDataSetChanged();
							mLvComment.setSelection(0);
							//清除之前保存的编辑内容
							ac.removeProperty(tempCommentKey);
						}
					}
					else {
						((AppException)msg.obj).makeToast(TweetDetail.this);
					}
				}
			};
			new Thread(){
				public void run() {
					Message msg = new Message();
					Result res = new Result();
					try {
						//发表评论
						res = ac.pubComment(_catalog, _id, _uid, _content, _isPostToMyZone);
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
}
