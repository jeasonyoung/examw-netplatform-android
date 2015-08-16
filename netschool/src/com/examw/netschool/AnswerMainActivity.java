package com.examw.netschool;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.examw.netschool.adapter.ProblemListAdapter;
import com.examw.netschool.entity.Problem;
import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;
/**
 * 答案主界面
 * @author jeasonyoung
 *
 */
public class AnswerMainActivity extends ListActivity implements OnClickListener{   
	private ImageButton returnbtn,refreshbtn;
	private LinearLayout progressLayout,contentLayout;
	private TextView answerInfosText;
	private Button askBtn;
	private String username;
    private LinkedList<Problem> mListItems;
    private ProblemListAdapter mAdapter;
    private View lvNews_footer;
    private int uid;
    private Gson gson;
    private ProgressBar lvNews_foot_progress;
	private TextView lvNews_foot_more;
	private static final int PAGESIZE = 10;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_main);
        this.returnbtn = (ImageButton) this.findViewById(R.id.returnbtn);
        this.progressLayout = (LinearLayout) this.findViewById(R.id.progressLayout);
        this.contentLayout = (LinearLayout) this.findViewById(R.id.contextLayout);
        this.answerInfosText = (TextView) this.findViewById(R.id.answerInfos_text);
        this.askBtn = (Button) this.findViewById(R.id.ask_btn);
        this.refreshbtn = (ImageButton) this.findViewById(R.id.refreshbtn);
        lvNews_footer = LayoutInflater.from(this).inflate(R.layout.listview_footer, null);
        this.lvNews_foot_more = (TextView) lvNews_footer.findViewById(R.id.listview_foot_more);
		this.lvNews_foot_progress = (ProgressBar) lvNews_footer.findViewById(R.id.listview_foot_progress);
        gson = new Gson();
        Intent intent = this.getIntent();
        this.username = intent.getStringExtra("username");
        this.uid = intent.getIntExtra("uid", 0);
        // Set a listener to be invoked when the list should be refreshed.
//        ((PullToRefreshListView) getListView()).setOnRefreshListener(new OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                // Do work to refresh the list here.
//            	new GetDataTask().execute(Constant.DOMAIN_URL+"mobile/MyQuestions?username="+username);
//            }
//        });
        this.askBtn.setOnClickListener(this);
        this.returnbtn.setOnClickListener(this);
        this.refreshbtn.setOnClickListener(this);
        this.lvNews_footer.setOnClickListener(this);
        getListView().addFooterView(lvNews_footer);
    }
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	this.progressLayout.setVisibility(View.VISIBLE);
        this.contentLayout.setVisibility(View.GONE);
        new GetDataTask().execute(Constant.DOMAIN_URL+"mobile/MyQuestions?username="+username);
    }
    private class GetDataTask extends AsyncTask<String, Void, String> {
    	@Override
    	protected void onPreExecute() {
    		// TODO Auto-generate method stub
    		super.onPreExecute();
    	}
        @Override
        protected String doInBackground(String... params) {
            // Simulates a background job.
            try {
            	String result = null;
            	result = HttpConnectUtil.httpGetRequest(AnswerMainActivity.this, params[0]);
            	return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //解析json
        	LinkedList<Problem> list = parseJson(result);
        	if(mListItems!=null)
        	{
        		mListItems.clear();
        		mListItems.addAll(list);
        	}else
        	{
        		mListItems = list;
        	}
        	answerInfosText.setText(username+"您有"+mListItems.size()+"个提问");
        	if(mAdapter==null)
        	{
        		initFooter(list);
        		mAdapter = new ProblemListAdapter(AnswerMainActivity.this, mListItems);
        		setListAdapter(mAdapter);
        	}
        	else
        	{
        		mAdapter.notifyDataSetChanged();
        	}
        	contentLayout.setVisibility(View.VISIBLE);
        	progressLayout.setVisibility(View.GONE);
            // Call onRefreshComplete when the list has been refreshed.
//            ((PullToRefreshListView) getListView()).onRefreshComplete();

            super.onPostExecute(result);
        }
    }
    private void initFooter(LinkedList<Problem> list)
    {
    	int size = list.size();
    	if(size == 0) lvNews_footer.setVisibility(View.GONE);
    	else if(size>=PAGESIZE)
    	{
    		lvNews_footer.setVisibility(View.VISIBLE);
			lvNews_foot_more.setText("更多");
			lvNews_foot_progress.setVisibility(View.GONE);
			lvNews_footer.setTag(0);
    	}else
		{
			lvNews_footer.setVisibility(View.VISIBLE);
			lvNews_foot_more.setText("已加载全部");
			lvNews_foot_progress.setVisibility(View.GONE);
			lvNews_footer.setTag(1);
		}
    }
    private LinkedList<Problem> parseJson(String result)
    {
    	LinkedList<Problem> list = new LinkedList<Problem>();
    	try{
    		JSONArray json = new JSONArray(result);
    		int length = json.length();
    		for(int i=0;i<length;i++)
    		{
    			JSONObject obj = json.getJSONObject(i);
    			//String content, String title, String path, String addTime,String answersJson
    			String time = obj.getString("questionAddTime").split("T")[0];
    			Problem p = new Problem(obj.getString("questionContent"),obj.getString("questionTitle"),obj.getString("questionPath"),time,obj.getString("tbAnswers"));
    			list.add(p);
    		}
    		return list;
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    		return list;
    	}
    }
    @Override
    public void onClick(View v) {
    	// TODO Auto-generated method stub
    	switch(v.getId())
    	{
    	case R.id.returnbtn:
    		this.finish();
    		return;
    	case R.id.refreshbtn:
    		refresh();
    		return;
    	case R.id.ask_btn:
    		ask();
    		return;
    	}
    }
    @SuppressLint("HandlerLeak") 
    private void footerClick()
	{
		//已加载全部
		if(Integer.valueOf(1).equals((Integer)lvNews_footer.getTag()))
		{
			return;
		}
		lvNews_foot_more.setText("玩命加载中");
		lvNews_foot_progress.setVisibility(View.VISIBLE);
		final Handler handler = new Handler(){
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch(msg.what)
				{
				case 1:
					LinkedList<Problem> list = (LinkedList<Problem>) msg.obj;
					if(list.size()==0 || list.size()<PAGESIZE)
					{
						lvNews_footer.setVisibility(View.VISIBLE);
						lvNews_foot_more.setText("已加载全部");
						lvNews_foot_progress.setVisibility(View.GONE);
						lvNews_footer.setTag(1);
					}else
					{
						lvNews_footer.setVisibility(View.VISIBLE);
						lvNews_foot_more.setText("更多");
						lvNews_foot_progress.setVisibility(View.GONE);
						lvNews_footer.setTag(0);
					}
					//刷新数据
					mListItems.addAll(list);
					mAdapter.notifyDataSetChanged();
					break;
				case -1:
					lvNews_footer.setVisibility(View.VISIBLE);
					lvNews_foot_more.setText("加载失败,点击加载");
					lvNews_foot_progress.setVisibility(View.GONE);
					lvNews_footer.setTag(0);
					break;
				}
			}
		};
		new Thread(){
			public void run() {
				try
				{
					String url = Constant.DOMAIN_URL+"mobile/MyQuestions?username="+username+"&page="+mListItems.size()/PAGESIZE+1;
					String result = HttpConnectUtil.httpGetRequest(AnswerMainActivity.this, url);
					LinkedList<Problem> list = parseJson(result);
					Message msg = handler.obtainMessage();
					msg.what = 1;
					msg.obj = list;
					handler.sendMessage(msg);
				}catch(Exception e)
				{
					e.printStackTrace();
					handler.sendEmptyMessage(-1);
				}
			};
		}.start();
	}
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
    	//带刷新的list,position从1开始,带了header
    	if(position>mListItems.size())
		{
			footerClick();
			return;
		}
    	Problem p = this.mListItems.get(position-1);
    	Intent intent = new Intent(this,AnswerInfoActivity.class);
    	intent.putExtra("problem", gson.toJson(p));
    	this.startActivity(intent);
	}

	private void refresh()
    {
		this.progressLayout.setVisibility(View.VISIBLE);
        this.contentLayout.setVisibility(View.GONE);
		new GetDataTask().execute(Constant.DOMAIN_URL+"mobile/MyQuestions?username="+username);
    }
    private void ask()
    {
    	Intent mIntent = new Intent(this,AnswerAskActivity.class);
    	mIntent.putExtra("username", username);
    	mIntent.putExtra("uid", uid);
    	this.startActivity(mIntent);
    }
    @Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		
	}
}