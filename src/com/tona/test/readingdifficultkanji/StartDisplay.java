package com.tona.test.readingdifficultkanji;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class StartDisplay extends Activity {

	private Button start;
	private Button rule;
	private Button option;
	private Spinner numberSpinner;
	private Spinner levelSpinner;

	private SharedPreferences sp;
	private Editor editor;
	private boolean extremeMode;
	private boolean lunaticMode;

	// Admob関連インスタンス
	LinearLayout layoutStart;
	AdView adView;

	public static final int MENU_OPTION = Menu.FIRST;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_start);
		sp = getSharedPreferences("preference", MODE_PRIVATE);
		if (sp.getBoolean("firstLaunch", false) == false) {
			// 初回起動処理
			initPreference();
		} else {
			// 2回目以降の処理
			loadPrefernce();
		}
		initlayout();

		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-4176998183155624/5888413997"); // 注1
		adView.setAdSize(AdSize.SMART_BANNER);

		adView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

		layoutStart = (LinearLayout) findViewById(R.id.layout_start);
		layoutStart.addView(adView);

		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		/* ここで状態を保存 */
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		/* ここで保存した状態を読み出して設定 */
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// startActivityForResult で起動させたアクティビティが
		// finish() により破棄されたときにコールされる
	}

	private void loadPrefernce() {
		extremeMode = sp.getBoolean("extremeMode", false);
		lunaticMode = sp.getBoolean("lunaticMode", false);
	}

	private void initPreference() {
		editor = sp.edit();
		editor.putBoolean("firstLaunch", true); // 初回起動のフラグ
		editor.putBoolean("extremeMode", false); // エクストリームモードのフラグ
		editor.putBoolean("lunaticMode", false); // ルナティックモードのフラグ
		editor.commit();
	}

	private void initlayout() {
		start = (Button) findViewById(R.id.start_button);
		rule = (Button) findViewById(R.id.rule);
		numberSpinner = (Spinner) findViewById(R.id.number);
		levelSpinner = (Spinner) findViewById(R.id.level);
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				String level = (String) levelSpinner.getSelectedItem();
				if (level.equals("A") && !extremeMode) {
					Toast.makeText(getApplicationContext(), "ロックされています\n「B」を10問モードでクリアするとアンロックされます", Toast.LENGTH_LONG).show();
				} else if (level.equals("S") && !lunaticMode) {
					Toast.makeText(getApplicationContext(), "ロックされています\n「A」を10問モードでクリアするとアンロックされます", Toast.LENGTH_LONG).show();
				} else {
					finish();
					String number = (String) numberSpinner.getSelectedItem();
					Intent i = new Intent();
					i.setClass(getApplicationContext(), MainActivity.class);
					i.putExtra("number", number);
					i.putExtra("level", level);
					startActivity(i);
				}
			}
		});
		rule.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), RuleDisplay.class));
			}
		});
		option = (Button) findViewById(R.id.option);
		option.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getApplicationContext(), Preference.class), 0);
			}
		});
	}

	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		adView.resume();
	}

	@Override
	public void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}
}
