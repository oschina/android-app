package net.oschina.app.adapter;

import java.util.List;

import net.oschina.app.R;
import net.oschina.app.bean.Blog;
import net.oschina.app.bean.BlogList;
import net.oschina.app.common.StringUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 用户博客Adapter类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewBlogAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<Blog> 					listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源 
	private int							blogtype;
	static class ListItemView{				//自定义控件集合  
	        public TextView title;
	        public TextView author;
		    public TextView date;  
		    public TextView count;
		    public ImageView type;
	 }  

	/**
	 * 实例化Adapter
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewBlogAdapter(Context context, int blogtype, List<Blog> data, int resource) {
		this.context = context;			
		this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;
		this.blogtype = blogtype;
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
			listItemView.title = (TextView)convertView.findViewById(R.id.blog_listitem_title);
			listItemView.author = (TextView)convertView.findViewById(R.id.blog_listitem_author);
			listItemView.count = (TextView)convertView.findViewById(R.id.blog_listitem_commentCount);
			listItemView.date = (TextView)convertView.findViewById(R.id.blog_listitem_date);
			listItemView.type = (ImageView)convertView.findViewById(R.id.blog_listitem_documentType);
			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}	
		
		//设置文字和图片
		Blog blog = listItems.get(position);
		
		listItemView.title.setText(blog.getTitle());
		listItemView.title.setTag(blog);//设置隐藏参数(实体类)
		listItemView.date.setText(StringUtils.friendly_time(blog.getPubDate()));
		listItemView.count.setText(blog.getCommentCount()+"");
		if(blog.getDocumentType() == Blog.DOC_TYPE_ORIGINAL)
			listItemView.type.setImageResource(R.drawable.widget_original_icon);
		else
			listItemView.type.setImageResource(R.drawable.widget_repaste_icon);
		
		if(blogtype == BlogList.CATALOG_USER){
			listItemView.author.setVisibility(View.GONE);
		}else{
			listItemView.author.setText(blog.getAuthor()+"   发表于");
		}
		
		return convertView;
	}
}