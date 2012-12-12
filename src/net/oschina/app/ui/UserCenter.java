package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.R;
import net.oschina.app.adapter.ListViewActiveAdapter;
import net.oschina.app.adapter.ListViewBlogAdapter;
import net.oschina.app.bean.Active;
import net.oschina.app.bean.Blog;
import net.oschina.app.bean.BlogList;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.User;
import net.oschina.app.bean.UserInformation;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.app.widget.UserInfoDialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * 用户专页
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UserCenter extends BaseActivity{
	
	private ImageView mBack;
	private ImageView mRefresh;
	private TextView mHeadTitle;
	private ProgressBar mProgressbar;
	private RadioButton mRelation;
	private RadioButton mMessage;
	private RadioButton mAtme;
	private UserInfoDialog mUserinfoDialog;
	private Button mTabActive;
	private Button mTabBlog;
	
	private ImageView mUserface;
	private TextView mUsername;
	private TextView mFrom;
	private TextView mGender;
	private TextView mJointime;
	private TextView mDevplatform;
	private TextView mExpertise;
	private TextView mLatestonline;
	
	private PullToRefreshListView mLvActive;
	private ListViewActiveAdapter lvActiveAdapter;
	private List<Active> lvActiveData = new ArrayList<Active>();
	private View lvActive_footer;
	private TextView lvActive_foot_more;
	private ProgressBar lvActive_foot_progress;
    private Handler mActiveHandler;
	private int lvActiveSumData;
	
	private PullToRefreshListView mLvBlog;
	private ListViewBlogAdapter lvBlogAdapter;
	private List<Blog> lvBlogData = new ArrayList<Blog>();
	private View lvBlog_footer;
	private TextView lvBlog_foot_more;
	private ProgressBar lvBlog_foot_progress;
    private Handler mBlogHandler;
	private int lvBlogSumData;
    
    private User mUser;
    private Handler mUserHandler;
	private int relationAction;	
	private int curLvActiveDataState;
	private int curLvBlogDataState;	
	
	private int _uid;
	private int _hisuid;
	private String _hisname;
	private String _username;
	private int _pageSize = 20;
	
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center);
		
		this.initView();
		
		this.initData();
	}
	
    //初始化视图控件
    private void initView()
    {
    	_hisuid = getIntent().getIntExtra("his_id", 0);
    	_hisname = getIntent().getStringExtra("his_name");
    	_username = getIntent().getStringExtra("his_name");
    	_uid = ((AppContext)getApplication()).getLoginUid();
    	
    	mBack = (ImageView)findViewById(R.id.user_center_back);
    	mRefresh = (ImageView)findViewById(R.id.user_center_refresh);
    	mHeadTitle = (TextView)findViewById(R.id.user_center_head_title);
    	mProgressbar = (ProgressBar)findViewById(R.id.user_center_head_progress);
    	mRelation = (RadioButton)findViewById(R.id.user_center_footbar_relation);
    	mMessage = (RadioButton)findViewById(R.id.user_center_footbar_message);
    	mAtme = (RadioButton)findViewById(R.id.user_center_footbar_atme);
    	
    	mTabActive = (Button)findViewById(R.id.user_center_btn_active);
    	mTabBlog = (Button)findViewById(R.id.user_center_btn_blog);
    	
    	mUserinfoDialog = new UserInfoDialog(UserCenter.this);
    	mUserface = (ImageView)mUserinfoDialog.findViewById(R.id.user_center_userface);
    	mUsername = (TextView)mUserinfoDialog.findViewById(R.id.user_center_username);
    	mFrom = (TextView)mUserinfoDialog.findViewById(R.id.user_center_from);
    	mGender = (TextView)mUserinfoDialog.findViewById(R.id.user_center_gender);
    	mJointime = (TextView)mUserinfoDialog.findViewById(R.id.user_center_jointime);
    	mDevplatform = (TextView)mUserinfoDialog.findViewById(R.id.user_center_devplatform);
    	mExpertise = (TextView)mUserinfoDialog.findViewById(R.id.user_center_expertise);
    	mLatestonline = (TextView)mUserinfoDialog.findViewById(R.id.user_center_latestonline);
    	
    	mHeadTitle.setText(_username + " ▼");
    	//设置第一选中项
    	mTabActive.setEnabled(false);
    	mTabActive.setOnClickListener(tabBtnClick(mTabActive));
    	mTabBlog.setOnClickListener(tabBtnClick(mTabBlog));
    	
    	mBack.setOnClickListener(UIHelper.finish(this));
    	mRefresh.setOnClickListener(refreshClickListener);
    	mHeadTitle.setOnClickListener(headTitleClickListener);
    	mMessage.setOnClickListener(messageClickListener);
    	mAtme.setOnClickListener(atmeClickListener);
    	mUserinfoDialog.setOnCancelListener(dialogCancelListener);
    	
    	this.initLvActive();
    	this.initLvBlog();
    }    
    
    //初始化动态列表控件
    private void initLvActive() {
    	lvActive_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
    	lvActive_foot_more = (TextView)lvActive_footer.findViewById(R.id.listview_foot_more);
        lvActive_foot_progress = (ProgressBar)lvActive_footer.findViewById(R.id.listview_foot_progress);

    	lvActiveAdapter = new ListViewActiveAdapter(this, lvActiveData, R.layout.active_listitem, false); 
    	mLvActive = (PullToRefreshListView)findViewById(R.id.user_center_activelist);
    	
        mLvActive.addFooterView(lvActive_footer);//添加底部视图  必须在setAdapter前
        mLvActive.setAdapter(lvActiveAdapter); 
        mLvActive.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == lvActive_footer) return;
        		
        		ImageView img = (ImageView)view.findViewById(R.id.active_listitem_userface);
        		Active active = (Active)img.getTag();
        		//跳转
        		UIHelper.showActiveRedirect(view.getContext(), active);
        	}
		});
        mLvActive.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mLvActive.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(lvActiveData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvActive_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				if(scrollEnd && curLvActiveDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					mLvActive.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvActive_foot_more.setText(R.string.load_ing);
					lvActive_foot_progress.setVisibility(View.VISIBLE);
					//当前页数
					int pageIndex = lvActiveSumData/_pageSize;
					loadLvActiveData(mActiveHandler, pageIndex, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				mLvActive.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
        mLvActive.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
				loadLvActiveData(mActiveHandler, 0, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }
    
    //初始化博客列表控件
    private void initLvBlog() {
    	lvBlog_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
    	lvBlog_foot_more = (TextView)lvBlog_footer.findViewById(R.id.listview_foot_more);
        lvBlog_foot_progress = (ProgressBar)lvBlog_footer.findViewById(R.id.listview_foot_progress);

    	lvBlogAdapter = new ListViewBlogAdapter(this, BlogList.CATALOG_USER, lvBlogData, R.layout.blog_listitem); 
    	mLvBlog = (PullToRefreshListView)findViewById(R.id.user_center_bloglist);
    	
        mLvBlog.addFooterView(lvBlog_footer);//添加底部视图  必须在setAdapter前
        mLvBlog.setAdapter(lvBlogAdapter); 
        mLvBlog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == lvBlog_footer) return;
        		
        		TextView txt = (TextView)view.findViewById(R.id.blog_listitem_title);
        		Blog blog = (Blog)txt.getTag();
        		//跳转
        		UIHelper.showUrlRedirect(view.getContext(), blog.getUrl());
        	}
		});
        mLvBlog.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mLvBlog.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(lvBlogData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvBlog_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				if(scrollEnd && curLvBlogDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					mLvBlog.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvBlog_foot_more.setText(R.string.load_ing);
					lvBlog_foot_progress.setVisibility(View.VISIBLE);
					//当前页数
					int pageIndex = lvBlogSumData/_pageSize;
					loadLvBlogData(mBlogHandler, pageIndex, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				mLvBlog.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
        mLvBlog.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				//点击头部、底部栏无效
        		if(position == 0 || view == lvBlog_footer) return false;				
				
        		Blog _blog = null;
        		//判断是否是TextView
        		if(view instanceof TextView){
        			_blog = (Blog)view.getTag();
        		}else{
        			TextView txt = (TextView)view.findViewById(R.id.blog_listitem_title);
            		_blog = (Blog)txt.getTag();
        		} 
        		if(_blog == null) return false;
        		
        		final Blog blog = _blog;
        		
        		final AppContext ac = (AppContext)getApplication();
				//操作--删除
        		final int uid = ac.getLoginUid();
        		//判断该博客是否是当前登录用户发表的
        		if(uid == blog.getAuthorId())
        		{
	        		final Handler handler = new Handler(){
						public void handleMessage(Message msg) {
							if(msg.what == 1){
								Result res = (Result)msg.obj;
								if(res.OK()){
									lvBlogData.remove(blog);
									lvBlogAdapter.notifyDataSetChanged();
								}
								UIHelper.ToastMessage(UserCenter.this, res.getErrorMessage());
							}else{
								((AppException)msg.obj).makeToast(UserCenter.this);
							}
						}        			
	        		};
	        		final Thread thread = new Thread(){
						public void run() {
							Message msg = new Message();
							try {
								Result res = ac.delBlog(uid, blog.getAuthorId(), blog.getId());
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
	        		UIHelper.showBlogOptionDialog(UserCenter.this, thread);
        		}
        		else
        		{
        			UIHelper.showBlogOptionDialog(UserCenter.this, null);
        		}
				return true;
			}        	
		});
        mLvBlog.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
				loadLvBlogData(mBlogHandler, 0, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }
    
    //初始化控件数据
	private void initData()
	{    	
    	mActiveHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				headButtonSwitch(DATA_LOAD_COMPLETE);
				lvActiveHandleMessage(msg);
			}
		};
		
    	mBlogHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				headButtonSwitch(DATA_LOAD_COMPLETE);
				lvBlogHandleMessage(msg);
			}
		};
		
		mUserHandler = new Handler(){
			public void handleMessage(Message msg) {
				headButtonSwitch(DATA_LOAD_COMPLETE);
				if(mUser != null){
					_username = mUser.getName();
					mHeadTitle.setText(_username + " ▼");
					mUsername.setText(mUser.getName());
					mFrom.setText(mUser.getLocation());
					mGender.setText(mUser.getGender());
					mJointime.setText(StringUtils.friendly_time(mUser.getJointime()));
					mDevplatform.setText(mUser.getDevplatform());
					mExpertise.setText(mUser.getExpertise());
					mLatestonline.setText(StringUtils.friendly_time(mUser.getLatestonline()));
					
					//初始化用户关系 & 点击事件
					loadUserRelation(mUser.getRelation());
					
					//加载用户头像
					UIHelper.showUserFace(mUserface, mUser.getFace());
				}
				lvActiveHandleMessage(msg);
			}
		};
		
		this.loadLvActiveData(mUserHandler, 0 ,UIHelper.LISTVIEW_ACTION_INIT);
		this.loadLvBlogData(mBlogHandler, 0, UIHelper.LISTVIEW_ACTION_INIT);
	}

	//加载动态列表
	private void loadLvActiveData(final Handler handler, final int pageIndex, final int action){  
		headButtonSwitch(DATA_LOAD_ING);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					UserInformation uinfo = ((AppContext)getApplication()).getInformation(_uid, _hisuid, _hisname, pageIndex, isRefresh);					
					mUser = uinfo.getUser();
					msg.what = uinfo.getPageSize();
					msg.obj = uinfo;
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
	
	//加载博客列表
	private void loadLvBlogData(final Handler handler, final int pageIndex, final int action){  
		headButtonSwitch(DATA_LOAD_ING);
		new Thread(){
				public void run() {
					Message msg = new Message();
					boolean isRefresh = false;
					if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
						isRefresh = true;
					try {
						BlogList bloglist= ((AppContext)getApplication()).getUserBlogList(_hisuid, _hisname, pageIndex, isRefresh);
						msg.what = bloglist.getPageSize();
						msg.obj = bloglist;
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
	
	private void loadUserRelation(int relation){
		switch (relation) {
			case User.RELATION_TYPE_BOTH:
				mRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.widget_bar_relation_del, 0, 0, 0);
				mRelation.setText("取消互粉");
				break;
			case User.RELATION_TYPE_FANS_HIM:
				mRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.widget_bar_relation_del, 0, 0, 0);
				mRelation.setText("取消关注");
				break;
			case User.RELATION_TYPE_FANS_ME:
				mRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.widget_bar_relation_add, 0, 0, 0);
				mRelation.setText("加关注");
				break;
			case User.RELATION_TYPE_NULL:
				mRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.widget_bar_relation_add, 0, 0, 0);
				mRelation.setText("加关注");
				break;
		}
		if(relation > 0)
			mRelation.setOnClickListener(relationClickListener);
	}
	
	private void lvActiveHandleMessage(Message msg){
		if(msg.what >= 0){
			UserInformation uinfo = (UserInformation)msg.obj;
			Notice notice = uinfo.getNotice();
			//处理listview数据			
			switch (msg.arg1) {
			case UIHelper.LISTVIEW_ACTION_INIT:
			case UIHelper.LISTVIEW_ACTION_REFRESH:
			case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
				lvActiveSumData = msg.what;
				lvActiveData.clear();//先清除原有数据
				lvActiveData.addAll(uinfo.getActivelist());
				break;
			case UIHelper.LISTVIEW_ACTION_SCROLL:
				lvActiveSumData += msg.what;
				if(lvActiveData.size() > 0){
					for(Active active1 : uinfo.getActivelist()){
						boolean b = false;
						for(Active active2 : lvActiveData){
							if(active1.getId() == active2.getId()){
								b = true;
								break;
							}
						}
						if(!b) lvActiveData.add(active1);
					}
				}else{
					lvActiveData.addAll(uinfo.getActivelist());
				}
				break;
			}
			if(msg.what<_pageSize){
				curLvActiveDataState = UIHelper.LISTVIEW_DATA_FULL;
				lvActiveAdapter.notifyDataSetChanged();
				lvActive_foot_more.setText(R.string.load_full);
			}
			else if(msg.what == _pageSize){	
				curLvActiveDataState = UIHelper.LISTVIEW_DATA_MORE;
				lvActiveAdapter.notifyDataSetChanged();
				lvActive_foot_more.setText(R.string.load_more);
			}
			//发送通知广播
			if(msg.obj != null){
				UIHelper.sendBroadCast(UserCenter.this, notice);
			}
		}
		else if(msg.what == -1){
			//有异常--显示加载出错 & 弹出错误消息
			curLvActiveDataState = UIHelper.LISTVIEW_DATA_MORE;
			lvActive_foot_more.setText(R.string.load_error);
			((AppException)msg.obj).makeToast(UserCenter.this);
		}
		if(lvActiveData.size()==0){
			curLvActiveDataState = UIHelper.LISTVIEW_DATA_EMPTY;
			lvActive_foot_more.setText(R.string.load_empty);
		}
		lvActive_foot_progress.setVisibility(View.GONE);
		if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH){
			mLvActive.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
			mLvActive.setSelection(0);
		}else if(msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG){
			mLvActive.setSelection(0);
		}
	}
	
	private void lvBlogHandleMessage(Message msg){
		if(msg.what >= 0){
			BlogList bloglist = (BlogList)msg.obj;
			Notice notice = bloglist.getNotice();
			//显示用户博客数量
			String tabBlogText = String.format("博客(%d)", bloglist.getBlogsCount());
			mTabBlog.setText(tabBlogText);
			//处理listview数据			
			switch (msg.arg1) {
			case UIHelper.LISTVIEW_ACTION_INIT:
			case UIHelper.LISTVIEW_ACTION_REFRESH:
			case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
				lvBlogSumData = msg.what;
				lvBlogData.clear();//先清除原有数据
				lvBlogData.addAll(bloglist.getBloglist());
				break;
			case UIHelper.LISTVIEW_ACTION_SCROLL:
				lvBlogSumData += msg.what;
				if(lvBlogData.size() > 0){
					for(Blog blog1 : bloglist.getBloglist()){
						boolean b = false;
						for(Blog blog2 : lvBlogData){
							if(blog1.getId() == blog2.getId()){
								b = true;
								break;
							}
						}
						if(!b) lvBlogData.add(blog1);
					}
				}else{
					lvBlogData.addAll(bloglist.getBloglist());
				}
				break;
			}
			if(msg.what<_pageSize){
				curLvBlogDataState = UIHelper.LISTVIEW_DATA_FULL;
				lvBlogAdapter.notifyDataSetChanged();
				lvBlog_foot_more.setText(R.string.load_full);
			}
			else if(msg.what == _pageSize){	
				curLvBlogDataState = UIHelper.LISTVIEW_DATA_MORE;
				lvBlogAdapter.notifyDataSetChanged();
				lvBlog_foot_more.setText(R.string.load_more);
			}
			//发送通知广播
			if(msg.obj != null){
				UIHelper.sendBroadCast(UserCenter.this, notice);
			}
		}
		else if(msg.what == -1){
			//有异常--显示加载出错 & 弹出错误消息
			curLvBlogDataState = UIHelper.LISTVIEW_DATA_MORE;
			lvBlog_foot_more.setText(R.string.load_error);
			((AppException)msg.obj).makeToast(UserCenter.this);
		}
		if(lvBlogData.size()==0){
			curLvBlogDataState = UIHelper.LISTVIEW_DATA_EMPTY;
			lvBlog_foot_more.setText(R.string.load_empty);
		}
		lvBlog_foot_progress.setVisibility(View.GONE);
		if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH){
			mLvBlog.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
			mLvBlog.setSelection(0);
		}else if(msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG){
			mLvBlog.setSelection(0);
		}
	}
	
    /**
     * 头部按钮展示
     * @param type
     */
    private void headButtonSwitch(int type) {
    	switch (type) {
    	case DATA_LOAD_ING:
			mProgressbar.setVisibility(View.VISIBLE);
			mRefresh.setVisibility(View.GONE);
			break;
		case DATA_LOAD_COMPLETE:
			mProgressbar.setVisibility(View.GONE);
			mRefresh.setVisibility(View.VISIBLE);
			break;
		}
    }
	
	private View.OnClickListener tabBtnClick(final Button btn){
    	return new View.OnClickListener() {
			public void onClick(View v) {
		    	if(btn == mTabActive){
		    		mTabActive.setEnabled(false);
		    	}else{
		    		mTabActive.setEnabled(true);
		    	}
		    	if(btn == mTabBlog){
		    		mTabBlog.setEnabled(false);
		    	}else{
		    		mTabBlog.setEnabled(true);
		    	}	    	
				
				if(btn == mTabActive){
					mLvActive.setVisibility(View.VISIBLE);
					mLvBlog.setVisibility(View.GONE);
					
					if(lvActiveData.size() == 0)
						loadLvActiveData(mActiveHandler, 0, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
				}else{
					mLvActive.setVisibility(View.GONE);
					mLvBlog.setVisibility(View.VISIBLE);
					
					if(lvBlogData.size() == 0)
						loadLvBlogData(mBlogHandler, 0, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
				}
			}
		};
    }
	
	private View.OnClickListener refreshClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			loadLvActiveData(mUserHandler, 0 ,UIHelper.LISTVIEW_ACTION_REFRESH);
			loadLvBlogData(mBlogHandler, 0, UIHelper.LISTVIEW_ACTION_REFRESH);
		}
	};
	private View.OnClickListener headTitleClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if(mUserinfoDialog != null && mUserinfoDialog.isShowing()){
				mHeadTitle.setText(_username + " ▼");
				mUserinfoDialog.hide();
			}else{
				mHeadTitle.setText(_username + " ▲");				
				mUserinfoDialog.show();
			}
		}
	};
	private DialogInterface.OnCancelListener dialogCancelListener = new DialogInterface.OnCancelListener(){
		public void onCancel(DialogInterface dialog) {
			mHeadTitle.setText(_username + " ▼");
		}		
	};	
	private View.OnClickListener messageClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			if(mUser == null)	return;
			UIHelper.showMessagePub(UserCenter.this, mUser.getUid(), mUser.getName());
		}
	};
	private View.OnClickListener atmeClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			if(mUser == null)	return;
			UIHelper.showTweetPub(UserCenter.this, "@"+mUser.getName()+" ", mUser.getUid());
		}
	};
	private View.OnClickListener relationClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			if(mUser == null)	return;
			//判断登录
			final AppContext ac = (AppContext)getApplication();
			if(!ac.isLogin()){
				UIHelper.showLoginDialog(UserCenter.this);
				return;
			}
			
			final Handler handler = new Handler(){
				public void handleMessage(Message msg) {
					if(msg.what == 1){
						Result res = (Result)msg.obj;
						if(res.OK()){
							switch (mUser.getRelation()) {
								case User.RELATION_TYPE_BOTH:
									mRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.widget_bar_relation_add, 0, 0, 0);
									mRelation.setText("加关注");
									mUser.setRelation(User.RELATION_TYPE_FANS_ME);
									break;
								case User.RELATION_TYPE_FANS_HIM:
									mRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.widget_bar_relation_add, 0, 0, 0);
									mRelation.setText("加关注");
									mUser.setRelation(User.RELATION_TYPE_NULL);
									break;
								case User.RELATION_TYPE_FANS_ME:
									mRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.widget_bar_relation_del, 0, 0, 0);
									mRelation.setText("取消互粉");
									mUser.setRelation(User.RELATION_TYPE_BOTH);
									break;
								case User.RELATION_TYPE_NULL:
									mRelation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.widget_bar_relation_del, 0, 0, 0);
									mRelation.setText("取消关注");
									mUser.setRelation(User.RELATION_TYPE_FANS_HIM);
									break;
							}
						}
						UIHelper.ToastMessage(UserCenter.this, res.getErrorMessage());
					}else{
						((AppException)msg.obj).makeToast(UserCenter.this);
					}
				}
			};
			final Thread thread = new Thread(){
				public void run() {
					Message msg = new Message();
					try {
						Result res = ac.updateRelation(_uid, _hisuid, relationAction);
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
			String dialogTitle = "";
			switch (mUser.getRelation()) {
				case User.RELATION_TYPE_BOTH:
					dialogTitle = "确定取消互粉吗？";
					relationAction = User.RELATION_ACTION_DELETE;
					break;
				case User.RELATION_TYPE_FANS_HIM:
					dialogTitle = "确定取消关注吗？";
					relationAction = User.RELATION_ACTION_DELETE;
					break;
				case User.RELATION_TYPE_FANS_ME:
					dialogTitle = "确定关注他吗？";
					relationAction = User.RELATION_ACTION_ADD;
					break;
				case User.RELATION_TYPE_NULL:
					dialogTitle = "确定关注他吗？";
					relationAction = User.RELATION_ACTION_ADD;
					break;
			}
			new AlertDialog.Builder(v.getContext())
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(dialogTitle)
			.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					thread.start();
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create().show();
		}
	};
}
