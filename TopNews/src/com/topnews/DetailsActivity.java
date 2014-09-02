package com.topnews;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.social.MissingAuthorizationException;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.web.client.ResourceAccessException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.topnews.base.BaseActivity;
import com.topnews.bean.NewsEntity;
import com.topnews.service.NewsDetailsService;
import com.topnews.tool.BaseTools;
import com.topnews.tool.DataTools;
import com.topnews.tool.DateTools;
import com.yunfox.s4aservicetest.response.Moments;
import com.yunfox.s4aservicetest.response.MomentsComment;
import com.yunfox.s4aservicetest.response.MomentsSupport;
import com.yunfox.s4aservicetest.response.NewsComment;
import com.yunfox.springandroid4healthplus.SpringAndroidService;

@SuppressLint("JavascriptInterface")
public class DetailsActivity extends BaseActivity {
	private TextView title;
	private ProgressBar progressBar;
	private FrameLayout customview_layout;
	private String news_url;
	private String news_title;
	private String news_source;
	private String news_date;
	private NewsEntity news;
	private TextView action_comment_count;
	private ImageView action_write_comment;
	WebView webView;
	private List<NewsComment> newscomment = new ArrayList<NewsComment>();
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 20) {
			if (resultCode == RESULT_OK) {
				new getCommentsAsync().execute("1");
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		setNeedBackGesture(true);// 设置需要手势监听
		getData();
		initView();
		initWebView();
	}

	/* 获取传递过来的数据 */
	private void getData() {
		news = (NewsEntity) getIntent().getSerializableExtra("news");
		news_url = news.getSource_url();
		news_title = news.getTitle();
		news_source = news.getSource();
		news_date = DateTools.getNewsDetailsDate(String.valueOf(news
				.getPublishTime()));

	}

	private void initWebView() {
		webView = (WebView) findViewById(R.id.wb_details);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		if (!TextUtils.isEmpty(news_url)) {
			WebSettings settings = webView.getSettings();
			settings.setJavaScriptEnabled(true);// 设置可以运行JS脚本
			// settings.setTextZoom(120);//Sets the text zoom of the page in
			// percent. The default is 100.
			settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
			// settings.setUseWideViewPort(true); //打开页面时， 自适应屏幕
			// settings.setLoadWithOverviewMode(true);//打开页面时， 自适应屏幕
			settings.setSupportZoom(false);// 用于设置webview放大
			settings.setBuiltInZoomControls(false);
			webView.setBackgroundResource(R.color.transparent);
			// 添加js交互接口类，并起别名 imagelistner
			webView.addJavascriptInterface(new JavascriptInterface(
					getApplicationContext()), "imagelistner");
			webView.setWebChromeClient(new MyWebChromeClient());
			webView.setWebViewClient(new MyWebViewClient());
			new MyAsnycTask().execute(news_url, news_title, news_source + " "
					+ news_date);
		}
	}

	private void initView() {
		title = (TextView) findViewById(R.id.title);
		progressBar = (ProgressBar) findViewById(R.id.ss_htmlprogessbar);
		customview_layout = (FrameLayout) findViewById(R.id.customview_layout);
		// 底部栏目
		action_comment_count = (TextView) findViewById(R.id.action_comment_count);
		// 评论入口
		action_write_comment = (ImageView) findViewById(R.id.action_write_comment);

		progressBar.setVisibility(View.VISIBLE);
		title.setTextSize(13);
		title.setVisibility(View.VISIBLE);
		title.setText(news_url);
		action_comment_count.setText(String.valueOf(news.getCommentNum()));
		action_write_comment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(DetailsActivity.this,
						CommentActivity.class);
				startActivityForResult(intent, 20);
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	private class MyAsnycTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... urls) {
			String data = NewsDetailsService.getNewsDetails(urls[0], urls[1],
					urls[2]);
			return data;
		}

		@Override
		protected void onPostExecute(String data) {
			webView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
		}
	}

	// 注入js函数监听
	private void addImageClickListner() {
		// 这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，在还是执行的时候调用本地接口传递url过去
		webView.loadUrl("javascript:(function(){"
				+ "var objs = document.getElementsByTagName(\"img\");"
				+ "var imgurl=''; " + "for(var i=0;i<objs.length;i++)  " + "{"
				+ "imgurl+=objs[i].src+',';"
				+ "    objs[i].onclick=function()  " + "    {  "
				+ "        window.imagelistner.openImage(imgurl);  "
				+ "    }  " + "}" + "})()");
	}

	// js通信接口
	public class JavascriptInterface {

		private Context context;

		public JavascriptInterface(Context context) {
			this.context = context;
		}

		public void openImage(String img) {

			//
			String[] imgs = img.split(",");
			ArrayList<String> imgsUrl = new ArrayList<String>();
			for (String s : imgs) {
				imgsUrl.add(s);
				Log.i("图片的URL>>>>>>>>>>>>>>>>>>>>>>>", s);
			}
			Intent intent = new Intent();
			intent.putStringArrayListExtra("infos", imgsUrl);
			intent.setClass(context, ImageShowActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	// 监听
	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			view.getSettings().setJavaScriptEnabled(true);
			super.onPageFinished(view, url);
			// html加载完成之后，添加监听图片的点击js函数
			addImageClickListner();
			progressBar.setVisibility(View.GONE);
			webView.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			view.getSettings().setJavaScriptEnabled(true);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			progressBar.setVisibility(View.GONE);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}
	}

	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			// TODO Auto-generated method stub
			if (newProgress != 100) {
				progressBar.setProgress(newProgress);
			}
			super.onProgressChanged(view, newProgress);
		}
	}

	private class getCommentsAsync extends
			AsyncTask<String, Void, List<NewsComment>> {
		private Exception exception;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected List<NewsComment> doInBackground(String... params) {
			try {
				String url = getIntent().getStringExtra("imgurl");
				System.out.println("评论url" + url);
				switch (Integer.parseInt(params[0])) {
				case 1:
					newscomment = SpringAndroidService.getInstance(
							getApplication())
							.getNewsCommentsByScope(url, 0, 10);
					System.out.println("newcomment" + newscomment.size());
					return newscomment;
				
					
				}

			} catch (Exception e) {
				this.exception = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<NewsComment> data) {
			if (exception != null) {
				String message;

				if (exception instanceof DuplicateConnectionException) {
					message = "The connection already exists.";
				} else if (exception instanceof ResourceAccessException
						&& exception.getCause() instanceof ConnectTimeoutException) {
					message = "connect time out";
				} else if (exception instanceof MissingAuthorizationException) {
					message = "please login first";
				} else {
					message = "A problem occurred with the network connection. Please try again in a few minutes.";
				}
			}
			if (data != null && data.size() > 0) {
				/*commentlist = data;
				myAdapter.listaddAdapter(commentlist);
				myAdapter.notifyDataSetChanged();
				comment_listView.onRefreshComplete();*/
			} else {
				// comment_listView.onRefreshComplete();
				Toast.makeText(getApplication(), "没有更多评论", Toast.LENGTH_SHORT)
						.show();
				//comment_listView.onRefreshComplete();
			}
			// TODO 网络数据 adapter
			// myAdapter.notifyDataSetChanged();
		}
	}
	
	
	
	
}
