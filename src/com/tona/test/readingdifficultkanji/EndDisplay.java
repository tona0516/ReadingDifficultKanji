package com.tona.test.readingdifficultkanji;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EndDisplay extends Activity {

	private Button restartButton;
	private Button finishButton;
	private Button returnButton;
	private TextView scoreText;
	private TextView timeText;
	private ListView listView;

	private Integer score;
	private Integer max;
	private Integer level;
	private long time;
	private ArrayList<String> list;

	private static final String DICTIONARY_URL = "http://kotobank.jp/word/REPLACE?dic=daijisen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_end);
		initActivity();
	}

	private void initActivity() {
		Intent i = getIntent();
		score = i.getIntExtra("score", 0);
		max = i.getIntExtra("number", 10);
		level = i.getIntExtra("level", 0);
		time = i.getLongExtra("Resulttime", Long.MAX_VALUE);

		scoreText = (TextView) findViewById(R.id.score);
		timeText = (TextView) findViewById(R.id.time);
		restartButton = (Button) findViewById(R.id.restart);
		restartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Intent i = new Intent();
				String levelStr = new String();
				i.putExtra("number", max.toString());
				if (level.equals(0)) {
					levelStr = "D";
				} else if (level.equals(1)) {
					levelStr = "C";
				} else if (level.equals(2)) {
					levelStr = "B";
				} else if (level.equals(3)) {
					levelStr = "A";
				} else {
					levelStr = "S";
				}
				i.putExtra("level", levelStr);
				i.setClass(getApplicationContext(), MainActivity.class);
				startActivity(i);
			}
		});
		returnButton = (Button) findViewById(R.id.return_title);
		returnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				startActivity(new Intent(getApplicationContext(), StartDisplay.class));
			}
		});
		finishButton = (Button) findViewById(R.id.finish);
		finishButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		if (score.equals(max)) {
			// 全問クリア処理
			double doubleTime = time * 0.001; // /1000にすると小数点以下が無視される
			BigDecimal bigDecimalTime = new BigDecimal(doubleTime).setScale(3, BigDecimal.ROUND_HALF_DOWN);
			timeText.setText(bigDecimalTime + "秒");
		} else {
			// 中断終了処理
			timeText.setVisibility(View.GONE);
		}

		if (max.equals(Integer.MAX_VALUE)) {
			scoreText.setText(score.toString() + "/∞");
		} else {
			scoreText.setText(score.toString() + "/" + max.toString());
		}
		listView = (ListView) findViewById(R.id.kanji_list);
		list = i.getStringArrayListExtra("list");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_at, list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String item = (String) listView.getItemAtPosition(position);
				try {
					createExplanation(item);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
		anlockExtremeMode();
		anlockLunaticMode();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		finish();
		return super.onKeyDown(keyCode, event);
	}

	private void createExplanation(String question) throws UnsupportedEncodingException {
		String encodeKanji = URLEncoder.encode(question, "UTF-8");
		String url = DICTIONARY_URL.replaceAll("REPLACE", encodeKanji);
		AlertDialog.Builder adb = new AlertDialog.Builder(EndDisplay.this);
		WebView mWebView = new WebView(EndDisplay.this);
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

	private void anlockExtremeMode() {
		SharedPreferences sp = getSharedPreferences("preference", MODE_PRIVATE);
		if (!sp.getBoolean("extremeMode", false) && score >= 10 && level == 2) {
			Editor editor = sp.edit();
			Toast.makeText(getApplicationContext(), "難易度Aがアンロックされました！", Toast.LENGTH_LONG).show();
			editor.putBoolean("extremeMode", true);
			editor.commit();
		}
	}

	private void anlockLunaticMode() {
		SharedPreferences sp = getSharedPreferences("preference", MODE_PRIVATE);
		if (!sp.getBoolean("lunaticMode", false) && score >= 10 && level == 3) {
			Editor editor = sp.edit();
			Toast.makeText(getApplicationContext(), "難易度Sがアンロックされました！", Toast.LENGTH_LONG).show();
			editor.putBoolean("lunaticMode", true);
			editor.commit();
		}
	}
}
