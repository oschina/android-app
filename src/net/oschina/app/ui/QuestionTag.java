package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.R;
import net.oschina.app.adapter.ListViewQuestionAdapter;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Post;
import net.oschina.app.bean.PostList;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Tag相关帖子列表
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-8-27
 */
public class QuestionTag extends BaseActivity{
	
	private ImageView mHome;
	private TextView mHeadTitle;
	private ProgressBar mProgressbar;
	
	private PullToRefreshListView lvQuestion;
	private ListViewQuestionAdapter lvQuestionAdapter;
	private List<Post> lvQuestionData = new ArrayList<Post>();
	private View lvQuestion_footer;
	private TextView lvQuestion_foot_more;
	private ProgressBar lvQuestion_foot_progress;
    private Handler lvQuestionHandler;
    private int lvQuestionSumData;
    
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	private final static int DATA_LOAD_FAIL = 0x003;
    
    private String curTag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_tag);
		
		//初始化视图控件
		initView();
		
		//初始化控件数据
		initData();
	}
	
	//初始化视图控件
	private void initView(){
		curTag = getIntent().getStringExtra("post_tag");
		
		mHome = (ImageView)findViewById(R.id.question_tag_home);
		mHeadTitle = (TextView)findViewById(R.id.question_tag_head_title);
		mProgressbar = (ProgressBar)findViewById(R.id.question_tag_head_progress);
		
		mHome.setOnClickListener(homeClickListener);
		mHeadTitle.setText(curTag);
		
		lvQuestionAdapter = new ListViewQuestionAdapter(this, lvQuestionData, R.layout.question_listitem);        
        lvQuestion_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
        lvQuestion_foot_more = (TextView)lvQuestion_footer.findViewById(R.id.listview_foot_more);
        lvQuestion_foot_progress = (ProgressBar)lvQuestion_footer.findViewById(R.id.listview_foot_progress);
        lvQuestion = (PullToRefreshListView)findViewById(R.id.question_tag_listview);
        lvQuestion.addFooterView(lvQuestion_footer);//添加底部视图  必须在setAdapter前
        lvQuestion.setAdapter(lvQuestionAdapter); 
        lvQuestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == lvQuestion_footer) return;
        		
        		Post post = null;		
        		//判断是否是TextView
        		if(view instanceof TextView){
        			post = (Post)view.getTag();
        		}else{
        			TextView tv = (TextView)view.findViewById(R.id.question_listitem_title);
        			post = (Post)tv.getTag();
        		}
        		if(post == null) return;
        		
        		//跳转到问答详情
        		UIHelper.showQuestionDetail(view.getContext(), post.getId());
        	}        	
		});
        lvQuestion.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				lvQuestion.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(lvQuestionData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvQuestion_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				int lvDataState = StringUtils.toInt(lvQuestion.getTag());
				if(scrollEnd && lvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					lvQuestion.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvQuestion_foot_more.setText(R.string.load_ing);
					lvQuestion_foot_progress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = lvQuestionSumData/AppContext.PAGE_SIZE;
					loadLvQuestionData(curTag, pageIndex, lvQuestionHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				lvQuestion.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
        lvQuestion.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
            	loadLvQuestionData(curTag, 0, lvQuestionHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });			
	}
	
	//初始化控件数据
	private void initData(){
		lvQuestionHandler = new Handler()
		{
			public void handleMessage(Message msg) 
			{				
				if(msg.what >= 0){	
					
					headButtonSwitch(DATA_LOAD_COMPLETE);
					
					PostList list = (PostList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
						lvQuestionSumData = msg.what;
						lvQuestionData.clear();//先清除原有数据
						lvQuestionData.addAll(list.getPostlist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvQuestionSumData += msg.what;
						if(lvQuestionData.size() > 0){
							for(Post p1 : list.getPostlist()){
								boolean b = false;
								for(Post p2 : lvQuestionData){
									if(p1.getId() == p2.getId() && p1.getAuthorId() == p2.getAuthorId()){
										b = true;
										break;
									}
								}
								if(!b) lvQuestionData.add(p1);
							}
						}else{
							lvQuestionData.addAll(list.getPostlist());
						}
						break;
					}						
					
					if(msg.what < 20){
						lvQuestion.setTag(UIHelper.LISTVIEW_DATA_FULL);
						lvQuestionAdapter.notifyDataSetChanged();
						lvQuestion_foot_more.setText(R.string.load_full);
					}else if(msg.what == 20){					
						lvQuestion.setTag(UIHelper.LISTVIEW_DATA_MORE);
						lvQuestionAdapter.notifyDataSetChanged();
						lvQuestion_foot_more.setText(R.string.load_more);
					}
					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(QuestionTag.this, notice);
					}
				}
				else if(msg.what == -1){
					
					headButtonSwitch(DATA_LOAD_FAIL);
					
					//有异常--显示加载出错 & 弹出错误消息
					lvQuestion.setTag(UIHelper.LISTVIEW_DATA_MORE);
					lvQuestion_foot_more.setText(R.string.load_error);
					((AppException)msg.obj).makeToast(QuestionTag.this);
				}
				if(lvQuestionData.size()==0){
					lvQuestion.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
					lvQuestion_foot_more.setText(R.string.load_empty);
				}
				lvQuestion_foot_progress.setVisibility(View.GONE);
				if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH){
					lvQuestion.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
					lvQuestion.setSelection(0);
				}
			}
		};
		this.loadLvQuestionData(curTag, 0, lvQuestionHandler, UIHelper.LISTVIEW_ACTION_INIT);
    }
	
    /**
     * 线程加载问答列表数据
     * @param tag 当前Tag
     * @param pageIndex 当前页数
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvQuestionData(final String tag, final int pageIndex, final Handler handler, final int action){
		
		headButtonSwitch(DATA_LOAD_ING);
		
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					PostList list = ((AppContext)getApplication()).getPostListByTag(tag, pageIndex, isRefresh);				
					msg.what = list.getPageSize();
					msg.obj = list;
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
     * 头部加载动画展示
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
		case DATA_LOAD_FAIL:
			mProgressbar.setVisibility(View.GONE);
			break;
		}
    }
    
	private View.OnClickListener homeClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			UIHelper.showHome(QuestionTag.this);
		}
	};
}
