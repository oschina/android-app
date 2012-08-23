package net.oschina.app.adapter;

import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.bean.Messages;
import net.oschina.app.common.BitmapManager;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 用户留言Adapter类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewMessageAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<Messages> 				listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源
	private BitmapManager 				bmpManager;
	static class ListItemView{				//自定义控件集合  
			public ImageView userface;
			public TextView username;
		    public TextView date;  
		    public TextView messageCount;
	 }  

	/**
	 * 实例化Adapter
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewMessageAdapter(Context context, List<Messages> data,int resource) {
		this.context = context;			
		this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;
		this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(), R.drawable.widget_dface_loading));
	}
	
	public int getCount() {
		return listItems.size();
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}
	   
	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.d("method", "getView");
		
		//自定义视图
		ListItemView  listItemView = null;
		
		if (convertView == null) {
			//获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);
			
			listItemView = new ListItemView();
			//获取控件对象
			listItemView.userface = (ImageView)convertView.findViewById(R.id.message_listitem_userface);
			listItemView.username = (TextView)convertView.findViewById(R.id.message_listitem_username);
			listItemView.date = (TextView)convertView.findViewById(R.id.message_listitem_date);
			listItemView.messageCount = (TextView)convertView.findViewById(R.id.message_listitem_messageCount);
			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}
		
		//设置文字和图片
		Messages msg = listItems.get(position);
		AppContext ac = (AppContext)context.getApplicationContext();
		if(msg.getSenderId() == ac.getLoginUid()){
			listItemView.username.setText(UIHelper.parseMessageSpan(msg.getFriendName(), msg.getContent(), "发给 "));
		}else{
			listItemView.username.setText(UIHelper.parseMessageSpan(msg.getSender(), msg.getContent(), ""));
		}
		listItemView.username.setTag(msg);//设置隐藏参数(实体类)
		listItemView.date.setText(StringUtils.friendly_time(msg.getPubDate()));
		listItemView.messageCount.setText("共有 "+msg.getMessageCount()+" 条留言");
		
		String faceURL = msg.getFace();
		if(faceURL.endsWith("portrait.gif") || StringUtils.isEmpty(faceURL)){
			listItemView.userface.setImageResource(R.drawable.widget_dface);
		}else{
			bmpManager.loadBitmap(faceURL, listItemView.userface);
		}
		listItemView.userface.setOnClickListener(faceClickListener);
		listItemView.userface.setTag(msg);
		
		return convertView;
	}
	
	private View.OnClickListener faceClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			Messages msg = (Messages)v.getTag();
			UIHelper.showUserCenter(v.getContext(), msg.getFriendId(), msg.getFriendName());
		}
	};
}