package com.example.x13_news;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.example.x13_news.domain.News;
import com.loopj.android.image.SmartImageView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

	List<News> newsList;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			ListView lv = (ListView) findViewById(R.id.lv);
			lv.setAdapter(new MyAdapter());
		}
	};

	class MyAdapter extends BaseAdapter {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			News news = newsList.get(position);

			ViewHolder myHolder;
			View v = null;
			if (convertView == null) {
				v = View.inflate(MainActivity.this, R.layout.item_news, null);
				
				//把布局文件中所有组件的对象封装到ViewHolder对象中
				myHolder = new ViewHolder();
				myHolder.tv_title = (TextView) v.findViewById(R.id.tv_title);
				myHolder.tv_detail = (TextView) v.findViewById(R.id.tv_detail);
				myHolder.tv_comment = (TextView) v.findViewById(R.id.tv_comment);
				myHolder.siv = (SmartImageView) v.findViewById(R.id.siv);
				//把ViewHolder对象封装到View对象中
				v.setTag(myHolder);
			} else {
				v = convertView;
				myHolder = (ViewHolder) v.getTag();
			}


			myHolder.tv_title.setText(news.getTitle());
			myHolder.tv_detail.setText(news.getDetail());
			myHolder.tv_comment.setText(news.getComment() + "条评论");
			myHolder.siv.setImageUrl(news.getImageUrl());

			return v;
		}

		
		class ViewHolder{
			
			//定义条目布局中所有组件成为属性
			TextView tv_title;
			TextView tv_detail;
			TextView tv_comment;
			SmartImageView siv;
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return newsList.size();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getNewsInfo();

	}

	private void getNewsInfo() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
//				//若有GET方式提交表单，则可直接在path里面拼接，中文需要处理
//				String path = "http://127.0.0.1:8080/library/loginServlet?name=" + URLEncoder.encode(name) +"&pass=" + pass;
				
				String path = "http://192.168.1.101:8080/news.xml";
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);
//					
//					//POST请求需将上面改成"POST",先拼接成字符串，再加两个字段,再取得输出流，把请求写进去
//					String data = "name=" + URLEncoder.encode(name) +"&pass=" + pass;
//					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//					conn.setRequestProperty("Content-Length", data.length() + "");
//					//打开出输出流
//					conn.setDoOutput(true);
//					//拿到输出流
//					OutputStream os = conn.getOutputStream();
//					//使用输出流往服务器提交数据
//					os.write(data.getBytes());
//					
					if (conn.getResponseCode() == 200) {
						InputStream is = conn.getInputStream();
						// 使用pull解析器，解析这个流
						parseNewsXml(is);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		thread.start();

	}

	private void parseNewsXml(InputStream is) {
		XmlPullParser xp = Xml.newPullParser();
		try {

			News news = null;

			xp.setInput(is, "utf-8");
			int type = xp.getEventType();
			while (type != XmlPullParser.END_DOCUMENT) {
				switch (type) {
				case XmlPullParser.START_TAG:
					if ("newslist".equals(xp.getName())) {
						newsList = new ArrayList<News>();
					} else if ("news".equals(xp.getName())) {
						news = new News();
					} else if ("title".equals(xp.getName())) {
						news.setTitle(xp.nextText());
					} else if ("detail".equals(xp.getName())) {
						news.setDetail(xp.nextText());
					} else if ("comment".equals(xp.getName())) {
						news.setComment(xp.nextText());
					} else if ("image".equals(xp.getName())) {
						news.setImageUrl(xp.nextText());
					}

					break;

				case XmlPullParser.END_TAG:
					if ("news".equals(xp.getName())) {
						newsList.add(news);
					}
					break;
				}

				type = xp.next();
			}

			// 发消息，让主线程设置newslist的适配器
			// 发一个空消息（一个信号）
			handler.sendEmptyMessage(1);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
