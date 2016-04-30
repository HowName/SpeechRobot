package com.example.speechrobot;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speechrobot.bean.AnswerBean;
import com.example.speechrobot.bean.SpeechListBean;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

public class MainActivity extends Activity {

	private static String TAG = MainActivity.class.getSimpleName();
	private Toast mToast;
	private ListView lvSpeechList;
	private Button btStartSpeech;
	private ArrayList<SpeechListBean> speechList = new ArrayList<SpeechListBean>();
	private MyListAdapter myListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lvSpeechList = (ListView) findViewById(R.id.lvSpeechList);
		btStartSpeech = (Button) findViewById(R.id.btStartSpeech);

		btStartSpeech.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSpeech();
			}
		});

		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

		// 初始化创建语音配置对象
		SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5722603f");

		speechList.add(new SpeechListBean("你好.我是你的语音小助手", null));
		myListAdapter = new MyListAdapter();
		lvSpeechList.setAdapter(myListAdapter);
	}

	/**
	 * 开始讲话,语音语义理解
	 */
	private void startSpeech() {
		// 1.创建RecognizerDialog对象
		RecognizerDialog mDialog = new RecognizerDialog(this,
				new MyInitListener());
		// 2.设置accent、language等参数
		mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
		// 若要将UI控件用于语义理解， 必须添加以下参数设置， 设置之后onResult回调返回将是语义理解结果
		mDialog.setParameter("asr_sch", "1");
		mDialog.setParameter("nlp_version", "2.0");
		// 3.设置回调接口
		mDialog.setListener(new MyRecognizerDialogListener());
		// 4.显示dialog，接收语音输入
		mDialog.show();
	}

	/**
	 * 初始化监听
	 */
	private class MyInitListener implements InitListener {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码：" + code);
			}
		}
	}

	/**
	 * 结果回调
	 */
	private class MyRecognizerDialogListener implements
			RecognizerDialogListener {

		@Override
		public void onError(SpeechError arg0) {
			System.out.println("onError");
		}

		@Override
		public void onResult(RecognizerResult result, boolean isLast) {
			if (result == null) {
				showTip("抱歉,没查询到结果");
			} else {
				String resultString = result.getResultString();
				System.out.println("resultString..." + resultString);
				parseAnswer(resultString);
			}
		}
	}

	/**
	 * 解析答复json
	 * 
	 * @param resultString
	 */
	private void parseAnswer(String resultString) {
		Gson gson = new Gson();
		AnswerBean answerBean = gson.fromJson(resultString, AnswerBean.class);

		if (answerBean.answer != null && answerBean.rc.equals("0")) {
			String askText = answerBean.text;// 提问内容
			String answerText = answerBean.answer.text;// 回答内容
			
			// 播放回答
			Text2Voice(answerText);

			SpeechListBean bean = new SpeechListBean(askText, answerText);
			speechList.add(bean);
		} else {
			speechList.add(new SpeechListBean("抱歉,未能识别到你的意思", null));
		}

		// 刷新数据
		myListAdapter.notifyDataSetChanged();
		lvSpeechList.setSelection(speechList.size() - 1);
	}

	// 合成- 文字转语音
	private void Text2Voice(String str) {
		// 1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
		SpeechSynthesizer mTts = SpeechSynthesizer
				.createSynthesizer(this, null);
		// 2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
		mTts.setParameter(SpeechConstant.VOICE_NAME, "vixm");// 设置发音人
		mTts.setParameter(SpeechConstant.SPEED, "50");// 设置语速
		mTts.setParameter(SpeechConstant.VOLUME, "80");// 设置音量，范围0~100
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置云端

		// 3.开始合成
		mTts.startSpeaking(str, new MySynListener());
	}

	// 合成监听器
	private class MySynListener implements SynthesizerListener {
		// 会话结束回调接口，没有错误时，error为null
		public void onCompleted(SpeechError error) {
		}

		// 缓冲进度回调
		// percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
		}

		// 开始播放
		public void onSpeakBegin() {
		}

		// 暂停播放
		public void onSpeakPaused() {
		}

		// 播放进度回调
		// percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
		}

		// 恢复播放回调接口
		public void onSpeakResumed() {
		}

		// 会话事件回调接口
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
		}

	};

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	/**
	 * ListView 适配器
	 */
	private class MyListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return speechList.size();
		}

		@Override
		public SpeechListBean getItem(int position) {
			return speechList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(MainActivity.this,
						R.layout.speech_item, null);
				holder.tvAsk = (TextView) convertView.findViewById(R.id.tvAsk);
				holder.tvAnswer = (TextView) convertView
						.findViewById(R.id.tvAnswer);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			SpeechListBean item = getItem(position);

			holder.tvAsk.setText(item.askText);

			if (TextUtils.isEmpty(item.answerText)) {
				holder.tvAnswer.setVisibility(View.GONE);
			} else {
				holder.tvAnswer.setVisibility(View.VISIBLE);
				holder.tvAnswer.setText(item.answerText);
			}

			return convertView;
		}

	}

	private class ViewHolder {
		public TextView tvAsk;
		public TextView tvAnswer;
	}

}
