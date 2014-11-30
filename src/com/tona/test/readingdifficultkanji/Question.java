package com.tona.test.readingdifficultkanji;

import java.util.Arrays;
import java.util.List;

public class Question {
	private String question = null;
	private String correct = null;
	private List<String> correctList = null;

	/**
	 *
	 * @param c
	 *            正解の読み
	 * @param q
	 *            漢字
	 */
	public Question(String c, String q) {
		this.question = q;
		this.setCollect(c);
	}

	public Question(String[] cList, String q) {
		this.question = q;
		correctList = Arrays.asList(cList);
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getCollect() {
		return correct;
	}

	public void setCollect(String collect) {
		this.correct = collect;
	}

	public List<String> getCollectList() {
		return correctList;
	}

	public void setCollectList(List<String> collectList) {
		this.correctList = collectList;
	}

	public String changeHiraganaToKatakana() {
		StringBuffer sb = new StringBuffer(this.correct);
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (c >= 'ぁ' && c <= 'ん') {
				sb.setCharAt(i, (char) (c - 'ぁ' + 'ァ'));
			}
		}
		return sb.toString();
	}

	public String changeKatakanaToHiragana() {
		StringBuffer sb = new StringBuffer(this.correct);
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (c >= 'ァ' && c <= 'ン') {
				sb.setCharAt(i, (char) (c - 'ァ' + 'ぁ'));
			} else if (c == 'ヵ') {
				sb.setCharAt(i, 'か');
			} else if (c == 'ヶ') {
				sb.setCharAt(i, 'け');
			} else if (c == 'ヴ') {
				sb.setCharAt(i, 'う');
				sb.insert(i + 1, '゛');
				i++;
			}
		}
		return sb.toString();
	}

}
