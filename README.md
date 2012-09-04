android-app
===========

# **开源中国社区 Android 客户端项目简析** #

*注：本文假设你已经有Android开发环境*

启动Eclipse，点击菜单并导入Android客户端项目，请确保你当前的Android SDK是最新版。<br>
如果编译出错，请修改项目根目录下的 project.properties 文件。<br>
推荐使用Android 4.0 以上版本的SDK：

> target=android-15

**本项目采用 GPL 授权协议，欢迎大家在这个基础上进行改进，并与大家分享。**

下面将简单的解析下项目：

## **一、项目的目录结构** ##
> 根目录<br>
> ├ src<br>
> ├ libs<br>
> ├ res<br>
> ├ AndroidManifest.xml<br>
> ├ LICENSE.txt<br>
> ├ proguard.cfg<br>
> └ project.properties<br>

**1、src目录**<br>
src目录用于存放项目的包及java源码文件。

下面是src目录的子目录：
> src<br>
> ├ com.weibo.net<br>
> ├ greendroid.widget<br>
> ├ net.oschina.app<br>
> ├ net.oschina.app.adapter<br>
> ├ net.oschina.app.api<br>
> ├ net.oschina.app.bean<br>
> ├ net.oschina.app.common<br>
> ├ net.oschina.app.ui<br>
> └ net.oschina.app.widget<br>

- com.weibo.net — 新浪微博SDK源码包
- greendroid.widget — 快捷菜单栏组件(国外UI库[GreenDroid](http://www.oschina.net/p/greendroid))
- net.oschina.app — APP启动及管理包
- net.oschina.app.adapter — APP列表适配器包
- net.oschina.app.api — API访问包
- net.oschina.app.bean — APP实体包
- net.oschina.app.common — APP工具包
- net.oschina.app.ui — APP界面包
- net.oschina.app.widget — APP控件包


**2、libs目录**<br>
libs目录用于存放项目引用到的jar包文件。

下面是libs目录里的jar包文件：
> libs<br>
> └ commons-httpclient-3.1.jar<br>

- commons-httpclient-3.1.jar — Apache的HttpClient包

**3、res目录**<br>
res目录用于存放项目的图片、布局、样式等资源文件。

下面是res目录的子目录：
> res<br>
> ├ anim<br>
> ├ color<br>
> ├ drawable<br>
> ├ drawable-hdpi<br>
> ├ drawable-ldpi<br>
> ├ drawable-mdpi<br>
> ├ layout<br>
> ├ menu<br>
> ├ raw<br>
> ├ values<br>
> └ xml<br>

- anim — 动画效果
- color — 颜色
- drawable/drawable-hdpi/drawable-ldpi/drawable-mdpi — 图标、图片
- layout — 界面布局
- menu — 菜单
- raw — 通知音
- values — 语言包和样式
- xml — 系统设置

**4、AndroidManifest.xml**<br>
AndroidManifest.xml用于设置应用程序的版本、主题、用户权限及注册Activity等。

## **二、项目的功能流程** ##

#### 1、APP启动流程 ####
AndroidManifest.xml注册的启动界面为"AppStart"，具体文件为net.oschina.app\AppStart.java文件。启动显示欢迎界面之后，通过意图(Intent)跳转到首页（net.oschina.app.ui\Main.java）。<br>
*注：除启动界面之外，其他所有界面都放在src\net.oschina.app.ui包中。*

#### 2、APP访问API流程 ####

以首页资讯列表显示访问API数据为例：

**1) 初始化控件**<br>
首页Activity(Main.java)在onCreate()方法里面加载布局文件(Main.xml)，对下拉刷新列表控件(PullToRefreshListView)进行了初始化，并设置了数据适配器(ListViewNewsAdapter)。<br>
*注：Main.xml布局文件在res\layout目录下；PullToRefreshListView控件在net.oschina.app.widget包；ListViewNewsAdapter适配器在net.oschina.app.adapter包。*

**2) 异步线程访问**<br>
列表控件初始化后，开启一个线程方法(loadLvNewsData())，该方法中调用全局应用程序类(AppContext)来访问API客户端类(ApiClient)。通过ApiClient以http方式请求服务器的API。返回响应的XML数据，再通过实体Bean(NewsList)解析XML，返回实体(NewsList)给UI控件(PullToRefreshListView)展示。<br>
*注：AppContext全局应用程序类在net.oschina.app包；ApiClient API客户端类在net.oschina.app.api包。*

**3) 解析数据显示**<br>
服务得到请求，将返回对应的资讯XML数据，再通过资讯实体类(NewsList)解析XML，返回实体(NewsList)给UI控件(PullToRefreshListView)展示。<br>
*注：NewsList实体类在net.oschina.app.bean包。*
