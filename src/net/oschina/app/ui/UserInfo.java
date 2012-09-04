package net.oschina.app.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.R;
import net.oschina.app.bean.FriendList;
import net.oschina.app.bean.MyInformation;
import net.oschina.app.bean.Result;
import net.oschina.app.common.FileUtils;
import net.oschina.app.common.ImageUtils;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.LoadingDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 用户资料
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UserInfo extends Activity{	
	
	private ImageView back;
	private ImageView refresh;
	private ImageView face;
	private ImageView gender;
	private Button editer;
	private TextView name;
	private TextView jointime;
	private TextView from;
	private TextView devplatform;
	private TextView expertise;
	private TextView followers;
	private TextView fans;
	private TextView favorites;
	private LinearLayout favorites_ll;
	private LinearLayout followers_ll;
	private LinearLayout fans_ll;
	private LoadingDialog loading;
	private MyInformation user;
	private Handler mHandler;
		
	private final static int CROP = 200;
	private final static String FILE_SAVEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OSChina/Portrait/";
	private File protraitFile;
	private Bitmap protraitBitmap;
	private String protraitPath;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_info);		
		
		//初始话视图控件
		this.initView();
		//初始化视图数据
		this.initData();
	}
	
	private void initView(){
		back = (ImageView)findViewById(R.id.user_info_back);
		refresh = (ImageView)findViewById(R.id.user_info_refresh);
		editer = (Button)findViewById(R.id.user_info_editer);
		back.setOnClickListener(UIHelper.finish(this));
		refresh.setOnClickListener(refreshClickListener);
		editer.setOnClickListener(editerClickListener);
		
		face = (ImageView)findViewById(R.id.user_info_userface);
		gender = (ImageView)findViewById(R.id.user_info_gender);
		name = (TextView)findViewById(R.id.user_info_username);
		jointime = (TextView)findViewById(R.id.user_info_jointime);
		from = (TextView)findViewById(R.id.user_info_from);
		devplatform = (TextView)findViewById(R.id.user_info_devplatform);
		expertise = (TextView)findViewById(R.id.user_info_expertise);
		followers = (TextView)findViewById(R.id.user_info_followers);
		fans = (TextView)findViewById(R.id.user_info_fans);
		favorites = (TextView)findViewById(R.id.user_info_favorites);
		favorites_ll = (LinearLayout)findViewById(R.id.user_info_favorites_ll);
		followers_ll = (LinearLayout)findViewById(R.id.user_info_followers_ll);
		fans_ll = (LinearLayout)findViewById(R.id.user_info_fans_ll);
	}
	
	private void initData(){
		mHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(loading != null)	loading.dismiss();
				if(msg.what == 1 && msg.obj != null){
					user = (MyInformation)msg.obj;
					
					//加载用户头像
					UIHelper.showUserFace(face, user.getFace());
					
					//用户性别
					if(user.getGender() == 1)
						gender.setImageResource(R.drawable.widget_gender_man);
					else
						gender.setImageResource(R.drawable.widget_gender_woman);
					
					//其他资料
					name.setText(user.getName());
					jointime.setText(StringUtils.friendly_time(user.getJointime()));
					from.setText(user.getFrom());
					devplatform.setText(user.getDevplatform());
					expertise.setText(user.getExpertise());
					followers.setText(user.getFollowerscount()+"");
					fans.setText(user.getFanscount()+"");
					favorites.setText(user.getFavoritecount()+"");
					
					favorites_ll.setOnClickListener(favoritesClickListener);
					fans_ll.setOnClickListener(fansClickListener);
					followers_ll.setOnClickListener(followersClickListener);
					
				}else if(msg.obj != null){
					((AppException)msg.obj).makeToast(UserInfo.this);
				}
			}
		};		
		this.loadUserInfoThread(false);
	}
	
	private void loadUserInfoThread(final boolean isRefresh){
		loading = new LoadingDialog(this);		
		loading.show();
		
		new Thread(){
			public void run() {
				Message msg = new Message();
				try {
					MyInformation user = ((AppContext)getApplication()).getMyInformation(isRefresh);
					msg.what = 1;
	                msg.obj = user;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	                msg.obj = e;
	            }
				mHandler.sendMessage(msg);
			}
		}.start();
	}
	
	private View.OnClickListener editerClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			CharSequence[] items = {
					getString(R.string.img_from_album),
					getString(R.string.img_from_camera)
			};
			imageChooseItem(items);
		}
	};
	
	private View.OnClickListener refreshClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			loadUserInfoThread(true);
		}
	};
	
	private View.OnClickListener favoritesClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			UIHelper.showUserFavorite(v.getContext());
		}
	};
	
	private View.OnClickListener fansClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			int followers = user!=null ? user.getFollowerscount() : 0;
			int fans = user!=null ? user.getFanscount() : 0;
			UIHelper.showUserFriend(v.getContext(), FriendList.TYPE_FANS, followers, fans);
		}
	};
	
	private View.OnClickListener followersClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			int followers = user!=null ? user.getFollowerscount() : 0;
			int fans = user!=null ? user.getFanscount() : 0;
			UIHelper.showUserFriend(v.getContext(), FriendList.TYPE_FOLLOWER, followers, fans);
		}
	};
	
	/**
	 * 操作选择
	 * @param items
	 */
	public void imageChooseItem(CharSequence[] items )
	{
		AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle("上传头像").setIcon(android.R.drawable.btn_star).setItems(items,
			new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int item)
				{	
					//判断是否挂载了SD卡
					String storageState = Environment.getExternalStorageState();		
					if(storageState.equals(Environment.MEDIA_MOUNTED)){
						File savedir = new File(FILE_SAVEPATH);
						if (!savedir.exists()) {
							savedir.mkdirs();
						}
					}					
					else{
						UIHelper.ToastMessage(UserInfo.this, "无法保存上传的头像，请检查SD卡是否挂载");
						return;
					}

					//输出裁剪的临时文件
					String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
					String fileName = "osc_" + timeStamp + ".jpg";//照片命名
					protraitFile = new File(FILE_SAVEPATH, fileName);
					Uri uri = Uri.fromFile(protraitFile);
					
					protraitPath = FILE_SAVEPATH + fileName;//该照片的绝对路径
					
					//手机选图
					if( item == 0 )
					{
						Intent intent = new Intent(Intent.ACTION_PICK);
						intent.setType("image/*");
						intent.putExtra("output", uri);
						intent.putExtra("crop", "true");
						intent.putExtra("aspectX", 1);// 裁剪框比例
						intent.putExtra("aspectY", 1);
						intent.putExtra("outputX", CROP);// 输出图片大小
						intent.putExtra("outputY", CROP);
						startActivityForResult(Intent.createChooser(intent, "选择图片"),ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD); 
					}
					//拍照
					else if( item == 1 )
					{	
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra("output", uri);
						intent.putExtra("crop", "true");
						intent.putExtra("aspectX", 1);// 裁剪框比例
						intent.putExtra("aspectY", 1);
						intent.putExtra("outputX", CROP);// 输出图片大小
						intent.putExtra("outputY", CROP);
						startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
					}   
				}}).create();
		
		 imageDialog.show();
	}
	
	@Override 
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{ 
    	if(resultCode != RESULT_OK) return;
		
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				if(loading != null)	loading.dismiss();
				if(msg.what == 1 && msg.obj != null){
					Result res = (Result)msg.obj;
					//提示信息
					UIHelper.ToastMessage(UserInfo.this, res.getErrorMessage());
					if(res.OK()){
						//显示新头像
						face.setImageBitmap(protraitBitmap);
					}
				}else if(msg.what == -1 && msg.obj != null){
					((AppException)msg.obj).makeToast(UserInfo.this);
				}
			}
		};
			
		if(loading != null){
			loading.setLoadText("正在上传头像···");
			loading.show();	
		}
		
		new Thread(){
			public void run() 
			{
		        if(requestCode == ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD || requestCode == ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA) 
		        {   
		        	//获取头像缩略图
		        	if(!StringUtils.isEmpty(protraitPath) && protraitFile.exists())
		        	{
		        		protraitBitmap = ImageUtils.loadImgThumbnail(protraitPath, 200, 200);
		        	}
		        }
		        
				if(protraitBitmap != null)
				{	
					Message msg = new Message();
					try {
						Result res = ((AppContext)getApplication()).updatePortrait(protraitFile);
						if(res!=null && res.OK()){
							//保存新头像到缓存
							String filename = FileUtils.getFileName(user.getFace());
							ImageUtils.saveImage(UserInfo.this, filename, protraitBitmap);
						}
						msg.what = 1;
						msg.obj = res;
					} catch (AppException e) {
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
					} catch(IOException e) {
						e.printStackTrace();
					}				
					handler.sendMessage(msg);
				}				
			};
		}.start();
    }
}
