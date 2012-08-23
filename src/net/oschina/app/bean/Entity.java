package net.oschina.app.bean;

import java.text.SimpleDateFormat;

/**
 * 实体类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public abstract class Entity extends Base {

	public final static SimpleDateFormat SDF_IN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public final static SimpleDateFormat SDF_OUT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	protected int id;

	public int getId() {
		return id;
	}

}
