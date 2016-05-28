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
				
				//�Ѳ����ļ�����������Ķ����װ��ViewHolder������
				myHolder = new ViewHolder();
				myHolder.tv_title = (TextView) v.findViewById(R.id.tv_title);
				myHolder.tv_detail = (TextView) v.findViewById(R.id.tv_detail);
				myHolder.tv_comment = (TextView) v.findViewById(R.id.tv_comment);
				myHolder.siv = (SmartImageView) v.findViewById(R.id.siv);
				//��ViewHolder�����װ��View������
				v.setTag(myHolder);
			} else {
				v = convertView;
				myHolder = (ViewHolder) v.getTag();
			}


			myHolder.tv_title.setText(news.getTitle());
			myHolder.tv_detail.setText(news.getDetail());
			myHolder.tv_comment.setText(news.getComment() + "������");
			myHolder.siv.setImageUrl(news.getImageUrl());

			return v;
		}

		
		class ViewHolder{
			
			//������Ŀ���������������Ϊ����
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
				
//				//����GET��ʽ�ύ�������ֱ����path����ƴ�ӣ�������Ҫ����
//				String path = "http://127.0.0.1:8080/library/loginServlet?name=" + URLEncoder.encode(name) +"&pass=" + pass;
				
				String path = "http://192.168.1.101:8080/news.xml";
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);
//					
//					//POST�����轫����ĳ�"POST",��ƴ�ӳ��ַ������ټ������ֶ�,��ȡ���������������д��ȥ
//					String data = "name=" + URLEncoder.encode(name) +"&pass=" + pass;
//					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//					conn.setRequestProperty("Content-Length", data.length() + "");
//					//�򿪳������
//					conn.setDoOutput(true);
//					//�õ������
//					OutputStream os = conn.getOutputStream();
//					//ʹ����������������ύ����
//					os.write(data.getBytes());
//					
					if (conn.getResponseCode() == 200) {
						InputStream is = conn.getInputStream();
						// ʹ��pull�����������������
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

			// ����Ϣ�������߳�����newslist��������
			// ��һ������Ϣ��һ���źţ�
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
