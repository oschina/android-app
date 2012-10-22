package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.R;
import net.oschina.app.adapter.ListViewSearchAdapter;
import net.oschina.app.bean.SearchList;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.SearchList.Result;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 搜索
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Search extends BaseActivity{
	private Button mSearchBtn;
	private EditText mSearchEditer;
	private ProgressBar mProgressbar;
	
	private Button search_catalog_software;
	private Button search_catalog_post;
	private Button search_catalog_code;
	private Button search_catalog_blog;
	private Button search_catalog_news;
	
	private ListView mlvSearch;
	private ListViewSearchAdapter lvSearchAdapter;
	private List<Result> lvSearchData = new ArrayList<Result>();
	private View lvSearch_footer;
	private TextView lvSearch_foot_more;
	private ProgressBar lvSearch_foot_progress;
    private Handler mSearchHandler;
    private int lvSumData;
	
	private String curSearchCatalog = SearchList.CATALOG_SOFTWARE;
	private int curLvDataState;
	private String curSearchContent = "";
    
	private InputMethodManager imm;
	
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
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
    		mSearchBtn.setClickable(false);
			mProgressbar.setVisibility(View.VISIBLE);
			break;
		case DATA_LOAD_COMPLETE:
			mSearchBtn.setClickable(true);
			mProgressbar.setVisibility(View.GONE);
			break;
		}
    }
	
	//初始化视图控件
    private void initView()
    {
    	imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	
    	mSearchBtn = (Button)findViewById(R.id.search_btn);
    	mSearchEditer = (EditText)findViewById(R.id.search_editer);
    	mProgressbar = (ProgressBar)findViewById(R.id.search_progress);
    	
    	mSearchBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mSearchEditer.clearFocus();
				curSearchContent = mSearchEditer.getText().toString();
				loadLvSearchData(curSearchCatalog, 0, mSearchHandler, UIHelper.LISTVIEW_ACTION_INIT);
			}
		});
    	mSearchEditer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){  
					imm.showSoftInput(v, 0);  
		        }  
		        else{  
		            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
		        }  
			}
		}); 
    	mSearchEditer.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH) {
					if(v.getTag() == null) {
						v.setTag(1);
						mSearchEditer.clearFocus();
						curSearchContent = mSearchEditer.getText().toString();
						loadLvSearchData(curSearchCatalog, 0, mSearchHandler, UIHelper.LISTVIEW_ACTION_INIT);						
					}else{
						v.setTag(null);
					}
					return true;
				}
				return false;
			}
		});
    	
    	search_catalog_software = (Button)findViewById(R.id.search_catalog_software);
    	search_catalog_post = (Button)findViewById(R.id.search_catalog_post);
    	search_catalog_code = (Button)findViewById(R.id.search_catalog_code);
    	search_catalog_blog = (Button)findViewById(R.id.search_catalog_blog);
    	search_catalog_news = (Button)findViewById(R.id.search_catalog_news);
    	
    	search_catalog_software.setOnClickListener(this.searchBtnClick(search_catalog_software,SearchList.CATALOG_SOFTWARE));
    	search_catalog_post.setOnClickListener(this.searchBtnClick(search_catalog_post,SearchList.CATALOG_POST));
    	search_catalog_code.setOnClickListener(this.searchBtnClick(search_catalog_code,SearchList.CATALOG_CODE));
    	search_catalog_blog.setOnClickListener(this.searchBtnClick(search_catalog_blog,SearchList.CATALOG_BLOG));
    	search_catalog_news.setOnClickListener(this.searchBtnClick(search_catalog_news,SearchList.CATALOG_NEWS));
    	
    	search_catalog_software.setEnabled(false);
    	
    	lvSearch_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
    	lvSearch_foot_more = (TextView)lvSearch_footer.findViewById(R.id.listview_foot_more);
    	lvSearch_foot_progress = (ProgressBar)lvSearch_footer.findViewById(R.id.listview_foot_progress);

    	lvSearchAdapter = new ListViewSearchAdapter(this, lvSearchData, R.layout.search_listitem); 
    	mlvSearch = (ListView)findViewById(R.id.search_listview);
    	mlvSearch.setVisibility(ListView.GONE);
    	mlvSearch.addFooterView(lvSearch_footer);//添加底部视图  必须在setAdapter前
    	mlvSearch.setAdapter(lvSearchAdapter); 
    	mlvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击底部栏无效
        		if(view == lvSearch_footer) return;
        		
        		Result res = null;
        		//判断是否是TextView
        		if(view instanceof TextView){
        			res = (Result)view.getTag();
        		}else{
        			TextView title = (TextView)view.findViewById(R.id.search_listitem_title);
        			res = (Result)title.getTag();
        		} 
        		if(res == null) return;
        		
        		//跳转
        		UIHelper.showUrlRedirect(view.getContext(), res.getUrl());
        	}
		});
    	mlvSearch.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {				
				//数据为空--不用继续下面代码了
				if(lvSearchData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvSearch_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				if(scrollEnd && curLvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					mlvSearch.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvSearch_foot_more.setText(R.string.load_ing);
					lvSearch_foot_progress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = lvSumData/20;
					loadLvSearchData(curSearchCatalog, pageIndex, mSearchHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
			}
		});
    }
    
    //初始化控件数据
  	private void initData()
  	{			
		mSearchHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE);

				if(msg.what >= 0){						
					SearchList list = (SearchList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
					case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
						lvSumData = msg.what;
						lvSearchData.clear();//先清除原有数据
						lvSearchData.addAll(list.getResultlist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvSumData += msg.what;
						if(lvSearchData.size() > 0){
							for(Result res1 : list.getResultlist()){
								boolean b = false;
								for(Result res2 : lvSearchData){
									if(res1.getObjid() == res2.getObjid()){
										b = true;
										break;
									}
								}
								if(!b) lvSearchData.add(res1);
							}
						}else{
							lvSearchData.addAll(list.getResultlist());
						}
						break;
					}	
					
					if(msg.what < 20){
						curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
						lvSearchAdapter.notifyDataSetChanged();
						lvSearch_foot_more.setText(R.string.load_full);
					}else if(msg.what == 20){					
						curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
						lvSearchAdapter.notifyDataSetChanged();
						lvSearch_foot_more.setText(R.string.load_more);
					}
					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(Search.this, notice);
					}
				}
				else if(msg.what == -1){
					//有异常--显示加载出错 & 弹出错误消息
					curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
					lvSearch_foot_more.setText(R.string.load_error);
					((AppException)msg.obj).makeToast(Search.this);
				}
				if(lvSearchData.size()==0){
					curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
					lvSearch_foot_more.setText(R.string.load_empty);
				}
				lvSearch_foot_progress.setVisibility(View.GONE);
				if(msg.arg1 != UIHelper.LISTVIEW_ACTION_SCROLL){
					mlvSearch.setSelection(0);//返回头部
				}
			}
		};
  	}
  	
    /**
     * 线程加载收藏数据
     * @param type 0:全部收藏 1:软件 2:话题 3:博客 4:新闻 5:代码
     * @param pageIndex 当前页数
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvSearchData(final String catalog,final int pageIndex,final Handler handler,final int action){  
		if(StringUtils.isEmpty(curSearchContent)){
			UIHelper.ToastMessage(Search.this, "请输入搜索内容");
			return;
		}
		
		headButtonSwitch(DATA_LOAD_ING);
		mlvSearch.setVisibility(ListView.VISIBLE);
		
		new Thread(){
			public void run() {
				Message msg = new Message();
				try {
					SearchList searchList = ((AppContext)getApplication()).getSearchList(catalog, curSearchContent, pageIndex, 20);
					msg.what = searchList.getPageSize();
					msg.obj = searchList;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
				if(curSearchCatalog.equals(catalog))
					handler.sendMessage(msg);
			}
		}.start();
	} 
	
	private View.OnClickListener searchBtnClick(final Button btn,final String catalog){
    	return new View.OnClickListener() {
			public void onClick(View v) {
		    	if(btn == search_catalog_blog)
		    		search_catalog_blog.setEnabled(false);
		    	else
		    		search_catalog_blog.setEnabled(true);
		    	if(btn == search_catalog_code)
		    		search_catalog_code.setEnabled(false);
		    	else
		    		search_catalog_code.setEnabled(true);	
		    	if(btn == search_catalog_news)
		    		search_catalog_news.setEnabled(false);
		    	else
		    		search_catalog_news.setEnabled(true);
		    	if(btn == search_catalog_post)
		    		search_catalog_post.setEnabled(false);
		    	else
		    		search_catalog_post.setEnabled(true);
		    	if(btn == search_catalog_software)
		    		search_catalog_software.setEnabled(false);
		    	else
		    		search_catalog_software.setEnabled(true);
				
				//开始搜索
				mSearchEditer.clearFocus();
				curSearchContent = mSearchEditer.getText().toString();
				curSearchCatalog = catalog;
				loadLvSearchData(catalog, 0, mSearchHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);		    	
			}
		};
    }
}
