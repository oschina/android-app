package net.oschina.app.adapter;

import java.util.List;

import net.oschina.app.R;
import net.oschina.app.bean.Post;
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
 * 问答Adapter类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewQuestionAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<Post> 					listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源 
	private BitmapManager 				bmpManager;
	static class ListItemView{				//自定义控件集合  
			public ImageView face;
	        public TextView title;  
		    public TextView author;
		    public TextView date;  
		    public TextView count;
	 }  

	/**
	 * 实例化Adapter
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewQuestionAdapter(Context context, List<Post> data,int resource) {
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
			listItemView.face = (ImageView)convertView.findViewById(R.id.question_listitem_userface);
			listItemView.title = (TextView)convertView.findViewById(R.id.question_listitem_title);
			listItemView.author = (TextView)convertView.findViewById(R.id.question_listitem_author);
			listItemView.count= (TextView)convertView.findViewById(R.id.question_listitem_count);
			listItemView.date= (TextView)convertView.findViewById(R.id.question_listitem_date);
			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}	
      
		//设置文字和图片
		Post post = listItems.get(position);
		String faceURL = post.getFace();
		if(faceURL.endsWith("portrait.gif") || StringUtils.isEmpty(faceURL)){
			listItemView.face.setImageResource(R.drawable.widget_dface);
		}else{
			bmpManager.loadBitmap(faceURL, listItemView.face);
		}
		listItemView.face.setOnClickListener(faceClickListener);
		listItemView.face.setTag(post);
		
		listItemView.title.setText(post.getTitle());
		listItemView.title.setTag(post);//设置隐藏参数(实体类)
		listItemView.author.setText(post.getAuthor());
		listItemView.date.setText(StringUtils.friendly_time(post.getPubDate()));
		listItemView.count.setText(post.getAnswerCount()+"回|"+post.getViewCount()+"阅");
		
		return convertView;
	}
	
	private View.OnClickListener faceClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			Post post = (Post)v.getTag();
			UIHelper.showUserCenter(v.getContext(), post.getAuthorId(), post.getAuthor());
		}
	};
}