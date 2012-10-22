package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.R;
import net.oschina.app.adapter.ListViewFriendAdapter;
import net.oschina.app.bean.FriendList;
import net.oschina.app.bean.FriendList.Friend;
import net.oschina.app.bean.Notice;
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
 * 用户关注、粉丝
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UserFriend extends BaseActivity {

	private ImageView mBack;
	private ProgressBar mProgressbar;
	
	private Button friend_type_fans;
	private Button friend_type_follower;
	
	private PullToRefreshListView mlvFriend;
	private ListViewFriendAdapter lvFriendAdapter;
	private List<Friend> lvFriendData = new ArrayList<Friend>();
	private View lvFriend_footer;
	private TextView lvFriend_foot_more;
	private ProgressBar lvFriend_foot_progress;
    private Handler mFriendHandler;
    private int lvSumData;
	
	private int curLvCatalog;
	private int curLvDataState;
    
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_friend);
        
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
    	mBack = (ImageView)findViewById(R.id.friend_head_back);
    	mBack.setOnClickListener(UIHelper.finish(this));
    	mProgressbar = (ProgressBar)findViewById(R.id.friend_head_progress);
    	
    	friend_type_fans = (Button)findViewById(R.id.friend_type_fans);
    	friend_type_follower = (Button)findViewById(R.id.friend_type_follower);
    	
    	friend_type_fans.setOnClickListener(this.friendBtnClick(friend_type_fans,FriendList.TYPE_FANS));
    	friend_type_follower.setOnClickListener(this.friendBtnClick(friend_type_follower,FriendList.TYPE_FOLLOWER));
    	
    	//设置当前分类
    	curLvCatalog = getIntent().getIntExtra("friend_type", FriendList.TYPE_FOLLOWER);
    	if(curLvCatalog == FriendList.TYPE_FANS) {
        	friend_type_fans.setEnabled(false);
    	} else {
    		friend_type_follower.setEnabled(false);
    	}
    	
    	//设置粉丝与关注的数量
    	int followers = getIntent().getIntExtra("friend_followers", 0);
    	int fans = getIntent().getIntExtra("friend_fans", 0);
    	friend_type_follower.setText(getString(R.string.user_friend_follower, followers));
    	friend_type_fans.setText(getString(R.string.user_friend_fans, fans));
    	
    	lvFriend_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
    	lvFriend_foot_more = (TextView)lvFriend_footer.findViewById(R.id.listview_foot_more);
    	lvFriend_foot_progress = (ProgressBar)lvFriend_footer.findViewById(R.id.listview_foot_progress);

    	lvFriendAdapter = new ListViewFriendAdapter(this, lvFriendData, R.layout.friend_listitem); 
    	mlvFriend = (PullToRefreshListView)findViewById(R.id.friend_listview);
    	
    	mlvFriend.addFooterView(lvFriend_footer);//添加底部视图  必须在setAdapter前
    	mlvFriend.setAdapter(lvFriendAdapter); 
    	mlvFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == lvFriend_footer) return;
        		
    			TextView name = (TextView)view.findViewById(R.id.friend_listitem_name);
    			Friend friend = (Friend)name.getTag();

        		if(friend == null) return;
        		
        		//跳转
        		UIHelper.showUserCenter(view.getContext(), friend.getUserid(), friend.getName());
        	}
		});
    	mlvFriend.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mlvFriend.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(lvFriendData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvFriend_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				if(scrollEnd && curLvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					mlvFriend.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvFriend_foot_more.setText(R.string.load_ing);
					lvFriend_foot_progress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = lvSumData/20;
					loadLvFriendData(curLvCatalog, pageIndex, mFriendHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				mlvFriend.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
    	mlvFriend.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadLvFriendData(curLvCatalog, 0, mFriendHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }
    
    //初始化控件数据
  	private void initData()
  	{	
		mFriendHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE);

				if(msg.what >= 0){						
					FriendList list = (FriendList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
					case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
						lvSumData = msg.what;
						lvFriendData.clear();//先清除原有数据
						lvFriendData.addAll(list.getFriendlist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvSumData += msg.what;
						if(lvFriendData.size() > 0){
							for(Friend friend1 : list.getFriendlist()){
								boolean b = false;
								for(Friend friend2 : lvFriendData){
									if(friend1.getUserid() == friend2.getUserid()){
										b = true;
										break;
									}
								}
								if(!b) lvFriendData.add(friend1);
							}
						}else{
							lvFriendData.addAll(list.getFriendlist());
						}
						break;
					}	
					
					if(msg.what < 20){
						curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
						lvFriendAdapter.notifyDataSetChanged();
						lvFriend_foot_more.setText(R.string.load_full);
					}else if(msg.what == 20){					
						curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
						lvFriendAdapter.notifyDataSetChanged();
						lvFriend_foot_more.setText(R.string.load_more);
					}
					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(UserFriend.this, notice);
					}
				}
				else if(msg.what == -1){
					//有异常--显示加载出错 & 弹出错误消息
					curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
					lvFriend_foot_more.setText(R.string.load_error);
					((AppException)msg.obj).makeToast(UserFriend.this);
				}
				if(lvFriendData.size()==0){
					curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
					lvFriend_foot_more.setText(R.string.load_empty);
				}
				lvFriend_foot_progress.setVisibility(View.GONE);
				if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH){
					mlvFriend.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
					mlvFriend.setSelection(0);
				}else if(msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG){
					mlvFriend.onRefreshComplete();
					mlvFriend.setSelection(0);
				}
			}
		};
		this.loadLvFriendData(curLvCatalog,0,mFriendHandler,UIHelper.LISTVIEW_ACTION_INIT);
  	}
  	
    /**
     * 线程加载好友列表数据
     * @param type 0:显示自己的粉丝 1:显示自己的关注者
     * @param pageIndex 当前页数
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvFriendData(final int type,final int pageIndex,final Handler handler,final int action){  
		headButtonSwitch(DATA_LOAD_ING);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					FriendList FriendList = ((AppContext)getApplication()).getFriendList(type, pageIndex, isRefresh);
					msg.what = FriendList.getFriendlist().size();
					msg.obj = FriendList;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
				if(curLvCatalog == type)
					handler.sendMessage(msg);
			}
		}.start();
	} 
	
	private View.OnClickListener friendBtnClick(final Button btn,final int catalog){
    	return new View.OnClickListener() {
			public void onClick(View v) {
		    	if(btn == friend_type_fans)
		    		friend_type_fans.setEnabled(false);
		    	else
		    		friend_type_fans.setEnabled(true);
		    	if(btn == friend_type_follower)
		    		friend_type_follower.setEnabled(false);
		    	else
		    		friend_type_follower.setEnabled(true);		    	
		    	
				lvFriend_foot_more.setText(R.string.load_more);
				lvFriend_foot_progress.setVisibility(View.GONE);
				
				curLvCatalog = catalog;
				loadLvFriendData(curLvCatalog, 0, mFriendHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);		    	
			}
		};
    }
}
