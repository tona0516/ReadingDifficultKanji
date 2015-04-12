package com.tona.test.readingdifficultkanji;
/*
 * やることメモ
 * リッチなUI()
 * ★答えが複数ある場合にも対応させる
 * ★問題をランダムに
 * ★問題数を選択式に
 * ★時間制限
 * ★今何問目なのか表示する
 * ★出題された漢字の意味を表示する。
 * ★問題数…16452問
 * ハイスコアをStartDisplayに表示
 * 四字熟語…時間制限は大きくとるべきか
 * もっとゲーム性を持たせたい
 *
 * Xperiaバグ
 * ・8問目で打ち始めて5秒くらいでキーボードが消えて答えが出る。カウントはそのまま経っている。
 * カウント0になるとボタンが解説ボタンが表示
 *  */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity implements TextWatcher {
	private TextView questionText;
	private EditText answerEdit;
	private Button retireButton;
	private TextView currectText;
	private TextView yomiganaText;
	private TextView nowNumberText;
	private Button dictionaryButton;
	//private Button feedbackButton;

	private int questionIndex = -1;
	private ArrayList<Question> questionList;
	private static final String TAG = "MainActivity";
	private Handler handler;
	private Runnable runnable;
	private TextView countDownText;
	private CountDownTimer timr;

	private int score = 0;
	private int MAX_NUMBER;
	private int questionLevel;
	private long activityStartTime;

	// Admob関連インスタンス
	LinearLayout layoutMain;
	AdView adView;

	// private static final String dicFileName = "twoWordDic.txt";
	private static final String ELEMENT_FILE = "element.txt";
	private static final String JUNIOR_FILE = "junior.txt";
	private static final String HIGH_FILE = "high.txt";
	private static final String ELEMENT_OKURI_FILE = "elementOkuri.csv";
	private static final String JUNIOR_OKURI_FILE = "juniorOkuri.csv";
	private static final String HIGH_OKURI_FILE = "highOkuri.csv";
	private static final String EXTREME_FILE = "extreme.txt";
	private static final String LUNATIC_FILE = "superExtreme.txt";
	private static final String infinity = "∞";

	public static MainActivity mActivity;
	private static final String DICTIONARY_URL = "http://kotobank.jp/word/REPLACE?dic=daijisen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mActivity = this;
		initActivity();
		initKanjiList();

		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-4176998183155624/5888413997"); // 注1
		adView.setAdSize(AdSize.SMART_BANNER);
		adView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
		layoutMain = (LinearLayout) findViewById(R.id.layout_main);
		layoutMain.addView(adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}

	private void initActivity() {
		Intent i = getIntent();
		String number = i.getStringExtra("number");
		String level = i.getStringExtra("level");

		if (number.equals("制限なし"))
			MAX_NUMBER = Integer.MAX_VALUE;
		else
			MAX_NUMBER = Integer.parseInt(number);

		Log.d("level", level);

		if (level.equals("D")) {
			questionLevel = 0;
		} else if (level.equals("C")) {
			questionLevel = 1;
		} else if (level.equals("B")) {
			questionLevel = 2;
		} else if (level.equals("A")) {
			questionLevel = 3;
		} else {
			questionLevel = 4;
		}

		yomiganaText = (TextView) findViewById(R.id.yomigana);
		questionText = (TextView) findViewById(R.id.kanji_text);
		answerEdit = (EditText) findViewById(R.id.answer_form);
		retireButton = (Button) findViewById(R.id.stop_button);
		currectText = (TextView) findViewById(R.id.show_currect);
		nowNumberText = (TextView) findViewById(R.id.now_number);
		countDownText = (TextView) findViewById(R.id.count_down);
		dictionaryButton = (Button) findViewById(R.id.dictionary);
		//feedbackButton = (Button)findViewById(R.id.feedback);


		retireButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimeupProcessing();
			}
		});
		answerEdit.addTextChangedListener(this);
		dictionaryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					createExplanation();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
//		feedbackButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(getApplicationContext(), "フィードバックを送信しました(仮)", Toast.LENGTH_SHORT).show();
//			}
//		});

		// 起動直後にソフトウェアキーボードを出す
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	public void initKanjiList() {
		questionList = new ArrayList<Question>();
		AssetManager as = getResources().getAssets();
		InputStream is1;
		InputStream is2;
		try {
			if (questionLevel == 0) {
				is1 = as.open(ELEMENT_FILE);
				is2 = as.open(ELEMENT_OKURI_FILE);
				loadQuestionFile(is1);
				loadQuestionFile(is2);
			} else if (questionLevel == 1) {
				is1 = as.open(JUNIOR_FILE);
				is2 = as.open(JUNIOR_OKURI_FILE);
				loadQuestionFile(is1);
				loadQuestionFile(is2);
			} else if (questionLevel == 2) {
				is1 = as.open(HIGH_FILE);
				is2 = as.open(HIGH_OKURI_FILE);
				loadQuestionFile(is1);
				loadQuestionFile(is2);
			} else if (questionLevel == 3) {
				is1 = as.open(EXTREME_FILE);
				loadQuestionFile(is1);
			} else {
				is1 = as.open(LUNATIC_FILE);
				loadQuestionFile(is1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Collections.shuffle(questionList); // Listをシャッフルする

		// 最初の問題を表示
		setNextQuestion();
		activityStartTime = System.currentTimeMillis();
	}

	private void loadQuestionFile(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		String[] splitter;
		String[] splitter2;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("##"))
				continue;
			splitter = line.split("、");
			splitter2 = splitter[0].split("・");
			if (splitter2.length == 1)
				questionList.add(new Question(splitter[0], splitter[1]));
			else
				questionList.add(new Question(splitter2, splitter[1]));
		}
		br.close();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		// Log.d(TAG, questionList.get(questionIndex).getCurrect());
		// Log.d(TAG, answer.getText().toString().trim());

		if (isCollect()) {
			timr.cancel(); // 正解ならば時間をカウントダウンを止める
			score++;
			currectText.setVisibility(View.VISIBLE);
			yomiganaText.setVisibility(View.VISIBLE);
			answerEdit.setText("");
			handler = new Handler();
			runnable = new Runnable() {
				@Override
				public void run() {
					currectText.setVisibility(View.INVISIBLE);
					if ((questionIndex == questionList.size() - 1) || (questionIndex == MAX_NUMBER - 1)) {
						// 終了処理…得点表示など
						endProcessing();
					} else {
						setNextQuestion();
					}
				}
			};
			handler.postDelayed(runnable, 2000);
		}
	}

	private boolean isCollect() {
		String input = answerEdit.getText().toString().trim();
		Question ques;
		if ((ques = questionList.get(questionIndex)) != null) {
			if (input.equals(ques.changeHiraganaToKatakana()) || input.equals(ques.changeKatakanaToHiragana())) {
				return true;
			} else {
				return false;
			}
		} else { // 正解がカタカナ・ひらがな両方を含み複数ある場合はエラーはくかも
			List<String> answerList = questionList.get(questionIndex).getCollectList();
			for (String answer : answerList) {
				if (answer.equals(input)) {
					return true;
				}
			}
			return false;
		}
	}

	public void endProcessing() {
		finish();
		Intent i = new Intent();
		i.putExtra("score", score);
		i.putExtra("number", MAX_NUMBER);
		ArrayList<String> list = new ArrayList<String>(score);
		for (int j = 0; j < score; j++) {
			list.add(questionList.get(j).getQuestion().toString());
		}
		i.putStringArrayListExtra("list", list);
		i.putExtra("level", questionLevel);
		i.putExtra("Resulttime", System.currentTimeMillis() - activityStartTime);
		i.setClass(getApplicationContext(), EndDisplay.class);
		startActivity(i);
	}

	private void setNextQuestion() {
		String collect;
		String answerLine = "";
		questionIndex++;
		if (MAX_NUMBER == Integer.MAX_VALUE) {
			nowNumberText.setText((questionIndex + 1) + "/" + infinity + "問目");
		} else {
			nowNumberText.setText((questionIndex + 1) + "/" + MAX_NUMBER + "問目");
		}
		questionText.setText(questionList.get(questionIndex).getQuestion());
		if ((collect = questionList.get(questionIndex).getCollect()) != null) {
			yomiganaText.setText(collect);
		} else {
			for (String answer : questionList.get(questionIndex).getCollectList()) {
				answerLine += answer + " ";
			}
			Log.d(TAG, answerLine);
			yomiganaText.setText(answerLine);
		}
		yomiganaText.setVisibility(View.INVISIBLE);

		timr = new CountDownTimer(16000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				countDownText.setText("残り" + (millisUntilFinished / 1000) + "秒");
			}
			@Override
			public void onFinish() {
				TimeupProcessing();
			}
		}.start();
	}

	private void TimeupProcessing() {
		if (runnable != null)
			handler.removeCallbacks(runnable);
		if (timr != null)
			timr.cancel();
		answerEdit.setText("");
		answerEdit.setEnabled(false);
		AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
		alpha.setDuration(1000);
		countDownText.setText("");
		yomiganaText.setVisibility(View.VISIBLE);
		yomiganaText.startAnimation(alpha);
		retireButton.setText("NEXT");
		retireButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				endProcessing();
			}
		});
		dictionaryButton.setVisibility(View.VISIBLE);
		dictionaryButton.startAnimation(alpha);
//		feedbackButton.setVisibility(View.VISIBLE);
//		feedbackButton.startAnimation(alpha);
	}

	private void createExplanation() throws UnsupportedEncodingException {
		String encodeKanji = URLEncoder.encode(questionList.get(questionIndex).getQuestion(), "UTF-8");
		String url = DICTIONARY_URL.replaceAll("REPLACE", encodeKanji);
		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
		WebView mWebView = new WebView(MainActivity.this);
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(false);
		settings.setLoadsImagesAutomatically(false);
		mWebView.setWebViewClient(new WebViewClient());
		adb.setView(mWebView);
		adb.setCancelable(true);
		adb.setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		adb.show();
		mWebView.loadUrl(url);
	}
}
