package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.R;
import net.oschina.app.adapter.ListViewFavoriteAdapter;
import net.oschina.app.bean.FavoriteList;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.FavoriteList.Favorite;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 用户收藏
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UserFavorite extends BaseActivity {

	private ImageView mBack;
	private ProgressBar mProgressbar;
	private Button favorite_catalog_software;
	private Button favorite_catalog_post;
	private Button favorite_catalog_code;
	private Button favorite_catalog_blog;
	private Button favorite_catalog_news;
	
	private PullToRefreshListView mlvFavorite;
	private ListViewFavoriteAdapter lvFavoriteAdapter;
	private List<Favorite> lvFavoriteData = new ArrayList<Favorite>();
	private View lvFavorite_footer;
	private TextView lvFavorite_foot_more;
	private ProgressBar lvFavorite_foot_progress;
    private Handler mFavoriteHandler;
    private int lvSumData;
	
	private int curFavoriteCatalog;
	private int curLvDataState;
    
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_favorite);
        
        this.initView();
        
        this.initData();
	}
	
    /**
     * 头部按钮展示
     * @param type
     */
    private void headButtonSwitch(int type) {
    	switch (type) {
    	case DATA_LOAD_ING:
			mProgressbar.setVisibility(View.VISIBLE);
			break;
		case DATA_LOAD_COMPLETE:
			mProgressbar.setVisibility(View.GONE);
			break;
		}
    }
	
	//初始化视图控件
    private void initView()
    {
    	mBack = (ImageView)findViewById(R.id.favorite_head_back);
    	mBack.setOnClickListener(UIHelper.finish(this));
    	mProgressbar = (ProgressBar)findViewById(R.id.favorite_head_progress);
    	
    	favorite_catalog_software = (Button)findViewById(R.id.favorite_catalog_software);
    	favorite_catalog_post = (Button)findViewById(R.id.favorite_catalog_post);
    	favorite_catalog_code = (Button)findViewById(R.id.favorite_catalog_code);
    	favorite_catalog_blog = (Button)findViewById(R.id.favorite_catalog_blog);
    	favorite_catalog_news = (Button)findViewById(R.id.favorite_catalog_news);
    	
    	favorite_catalog_software.setOnClickListener(this.favoriteBtnClick(favorite_catalog_software,FavoriteList.TYPE_SOFTWARE));
    	favorite_catalog_post.setOnClickListener(this.favoriteBtnClick(favorite_catalog_post,FavoriteList.TYPE_POST));
    	favorite_catalog_code.setOnClickListener(this.favoriteBtnClick(favorite_catalog_code,FavoriteList.TYPE_CODE));
    	favorite_catalog_blog.setOnClickListener(this.favoriteBtnClick(favorite_catalog_blog,FavoriteList.TYPE_BLOG));
    	favorite_catalog_news.setOnClickListener(this.favoriteBtnClick(favorite_catalog_news,FavoriteList.TYPE_NEWS));
    	
    	favorite_catalog_software.setEnabled(false);
    	curFavoriteCatalog = FavoriteList.TYPE_SOFTWARE;
    	
    	lvFavorite_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
    	lvFavorite_foot_more = (TextView)lvFavorite_footer.findViewById(R.id.listview_foot_more);
    	lvFavorite_foot_progress = (ProgressBar)lvFavorite_footer.findViewById(R.id.listview_foot_progress);

    	lvFavoriteAdapter = new ListViewFavoriteAdapter(this, lvFavoriteData, R.layout.favorite_listitem); 
    	mlvFavorite = (PullToRefreshListView)findViewById(R.id.favorite_listview);
    	
    	mlvFavorite.addFooterView(lvFavorite_footer);//添加底部视图  必须在setAdapter前
    	mlvFavorite.setAdapter(lvFavoriteAdapter); 
    	mlvFavorite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == lvFavorite_footer) return;
        		
        		Favorite fav = null;
        		//判断是否是TextView
        		if(view instanceof TextView){
        			fav = (Favorite)view.getTag();
        		}else{
        			TextView title = (TextView)view.findViewById(R.id.favorite_listitem_title);
        			fav = (Favorite)title.getTag();
        		} 
        		if(fav == null) return;
        		
        		//跳转
        		UIHelper.showUrlRedirect(view.getContext(), fav.url);
        	}
		});
    	mlvFavorite.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mlvFavorite.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(lvFavoriteData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvFavorite_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				if(scrollEnd && curLvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					mlvFavorite.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvFavorite_foot_more.setText(R.string.load_ing);
					lvFavorite_foot_progress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = lvSumData/20;
					loadLvFavoriteData(curFavoriteCatalog, pageIndex, mFavoriteHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				mlvFavorite.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
    	mlvFavorite.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				//点击头部、底部栏无效
        		if(position == 0 || view == lvFavorite_footer) return false;				
				
        		Favorite _fav = null;
        		//判断是否是TextView
        		if(view instanceof TextView){
        			_fav = (Favorite)view.getTag();
        		}else{
        			TextView title = (TextView)view.findViewById(R.id.favorite_listitem_title);
            		_fav = (Favorite)title.getTag();
        		} 
        		if(_fav == null) return false;
        		
        		final Favorite fav = _fav;
        		
        		final AppContext ac = (AppContext)getApplication();
				//操作--删除
        		final int uid = ac.getLoginUid();

        		final Handler handler = new Handler(){
					public void handleMessage(Message msg) {
						if(msg.what == 1){
							Result res = (Result)msg.obj;
							if(res.OK()){
								lvFavoriteData.remove(fav);
								lvFavoriteAdapter.notifyDataSetChanged();
							}
							UIHelper.ToastMessage(UserFavorite.this, res.getErrorMessage());
						}else{
							((AppException)msg.obj).makeToast(UserFavorite.this);
						}
					}        			
        		};
        		final Thread thread = new Thread(){
					public void run() {
						Message msg = new Message();
						try {
							Result res = ac.delFavorite(uid, fav.objid, fav.type);
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
        		UIHelper.showFavoriteOptionDialog(UserFavorite.this, thread);
			
				return true;
			}        	
		});
    	mlvFavorite.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadLvFavoriteData(curFavoriteCatalog, 0, mFavoriteHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }
    
    //初始化控件数据
  	private void initData()
  	{			
		mFavoriteHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE);

				if(msg.what >= 0){						
					FavoriteList list = (FavoriteList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
					case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
						lvSumData = msg.what;
						lvFavoriteData.clear();//先清除原有数据
						lvFavoriteData.addAll(list.getFavoritelist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvSumData += msg.what;
						if(lvFavoriteData.size() > 0){
							for(Favorite fav1 : list.getFavoritelist()){
								boolean b = false;
								for(Favorite fav2 : lvFavoriteData){
									if(fav1.objid == fav2.objid){
										b = true;
										break;
									}
								}
								if(!b) lvFavoriteData.add(fav1);
							}
						}else{
							lvFavoriteData.addAll(list.getFavoritelist());
						}
						break;
					}	
					
					if(msg.what < 20){
						curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
						lvFavoriteAdapter.notifyDataSetChanged();
						lvFavorite_foot_more.setText(R.string.load_full);
					}else if(msg.what == 20){					
						curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
						lvFavoriteAdapter.notifyDataSetChanged();
						lvFavorite_foot_more.setText(R.string.load_more);
					}
					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(UserFavorite.this, notice);
					}
				}
				else if(msg.what == -1){
					//有异常--显示加载出错 & 弹出错误消息
					curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
					lvFavorite_foot_more.setText(R.string.load_error);
					((AppException)msg.obj).makeToast(UserFavorite.this);
				}
				if(lvFavoriteData.size()==0){
					curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
					lvFavorite_foot_more.setText(R.string.load_empty);
				}
				lvFavorite_foot_progress.setVisibility(View.GONE);
				if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH){
					mlvFavorite.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
					mlvFavorite.setSelection(0);
				}else if(msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG){
					mlvFavorite.onRefreshComplete();
					mlvFavorite.setSelection(0);
				}
			}
		};
		this.loadLvFavoriteData(curFavoriteCatalog,0,mFavoriteHandler,UIHelper.LISTVIEW_ACTION_INIT);
  	}
  	
    /**
     * 线程加载收藏数据
     * @param type 0:全部收藏 1:软件 2:话题 3:博客 4:新闻 5:代码
     * @param pageIndex 当前页数
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvFavoriteData(final int type,final int pageIndex,final Handler handler,final int action){  
		headButtonSwitch(DATA_LOAD_ING);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					FavoriteList favoriteList = ((AppContext)getApplication()).getFavoriteList(type, pageIndex, isRefresh);
					msg.what = favoriteList.getPageSize();
					msg.obj = favoriteList;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
				if(curFavoriteCatalog == type)
					handler.sendMessage(msg);
			}
		}.start();
	} 
	
	private View.OnClickListener favoriteBtnClick(final Button btn,final int catalog){
    	return new View.OnClickListener() {
			public void onClick(View v) {
		    	if(btn == favorite_catalog_blog)
		    		favorite_catalog_blog.setEnabled(false);
		    	else
		    		favorite_catalog_blog.setEnabled(true);
		    	if(btn == favorite_catalog_code)
		    		favorite_catalog_code.setEnabled(false);
		    	else
		    		favorite_catalog_code.setEnabled(true);	
		    	if(btn == favorite_catalog_news)
		    		favorite_catalog_news.setEnabled(false);
		    	else
		    		favorite_catalog_news.setEnabled(true);
		    	if(btn == favorite_catalog_post)
		    		favorite_catalog_post.setEnabled(false);
		    	else
		    		favorite_catalog_post.setEnabled(true);
		    	if(btn == favorite_catalog_software)
		    		favorite_catalog_software.setEnabled(false);
		    	else
		    		favorite_catalog_software.setEnabled(true);		    	
		    	
				lvFavorite_foot_more.setText(R.string.load_more);
				lvFavorite_foot_progress.setVisibility(View.GONE);
				
				curFavoriteCatalog = catalog;
				loadLvFavoriteData(curFavoriteCatalog, 0, mFavoriteHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);		    	
			}
		};
    }
}
