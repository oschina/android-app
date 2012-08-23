package net.oschina.app.adapter;

import java.util.List;

import net.oschina.app.R;
import net.oschina.app.bean.Tweet;
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
 * 动弹Adapter类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewTweetAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<Tweet> 				listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源
	private BitmapManager 				bmpManager;
	static class ListItemView{				//自定义控件集合  
			public ImageView userface;  
	        public TextView username;  
		    public TextView date;  
		    public TextView content;
		    public TextView commentCount;
		    public TextView client;
		    public ImageView image;
	 }  

	/**
	 * 实例化Adapter
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewTweetAdapter(Context context, List<Tweet> data,int resource) {
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
			listItemView.userface = (ImageView)convertView.findViewById(R.id.tweet_listitem_userface);
			listItemView.username = (TextView)convertView.findViewById(R.id.tweet_listitem_username);
			listItemView.content = (TextView)convertView.findViewById(R.id.tweet_listitem_content);
			listItemView.image= (ImageView)convertView.findViewById(R.id.tweet_listitem_image);
			listItemView.date= (TextView)convertView.findViewById(R.id.tweet_listitem_date);
			listItemView.commentCount= (TextView)convertView.findViewById(R.id.tweet_listitem_commentCount);
			listItemView.client= (TextView)convertView.findViewById(R.id.tweet_listitem_client);
			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}
				
		//设置文字和图片
		Tweet tweet = listItems.get(position);
		listItemView.username.setText(tweet.getAuthor());
		listItemView.username.setTag(tweet);//设置隐藏参数(实体类)
		listItemView.content.setText(tweet.getBody());
		listItemView.date.setText(StringUtils.friendly_time(tweet.getPubDate()));
		listItemView.commentCount.setText(tweet.getCommentCount()+"");

		switch(tweet.getAppClient())
		{	
			case 0:
			case 1:
				listItemView.client.setText("");
				break;
			case 2:
				listItemView.client.setText("来自:手机");
				break;
			case 3:
				listItemView.client.setText("来自:Android");
				break;
			case 4:
				listItemView.client.setText("来自:iPhone");
				break;
			case 5:
				listItemView.client.setText("来自:Windows Phone");
				break;
		}
		if(StringUtils.isEmpty(listItemView.client.getText().toString()))
			listItemView.client.setVisibility(View.GONE);
		else
			listItemView.client.setVisibility(View.VISIBLE);
		
		String faceURL = tweet.getFace();
		if(faceURL.endsWith("portrait.gif") || StringUtils.isEmpty(faceURL)){
			listItemView.userface.setImageResource(R.drawable.widget_dface);
		}else{
			bmpManager.loadBitmap(faceURL, listItemView.userface);
		}
		listItemView.userface.setOnClickListener(faceClickListener);
		listItemView.userface.setTag(tweet);
		
		String imgSmall = tweet.getImgSmall();
		if(!StringUtils.isEmpty(imgSmall)) {
			bmpManager.loadBitmap(imgSmall, listItemView.image, BitmapFactory.decodeResource(context.getResources(), R.drawable.image_loading));
			listItemView.image.setOnClickListener(imageClickListener);
			listItemView.image.setTag(tweet.getImgBig());
			listItemView.image.setVisibility(ImageView.VISIBLE);
		}else{
			listItemView.image.setVisibility(ImageView.GONE);
		}
		
		return convertView;
	}
	
	private View.OnClickListener faceClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			Tweet tweet = (Tweet)v.getTag();
			UIHelper.showUserCenter(v.getContext(), tweet.getAuthorId(), tweet.getAuthor());
		}
	};
	
	private View.OnClickListener imageClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			UIHelper.showImageDialog(v.getContext(), (String)v.getTag());
		}
	};
}