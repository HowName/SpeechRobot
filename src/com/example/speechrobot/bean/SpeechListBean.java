package com.example.speechrobot.bean;

public class SpeechListBean {

	public String askText;
	public String answerText;

	public SpeechListBean() {

	}

	public SpeechListBean(String askText, String answerText) {
		super();
		this.askText = askText;
		this.answerText = answerText;
	}

	public String getAskText() {
		return askText;
	}

	public void setAskText(String askText) {
		this.askText = askText;
	}

	public String getAnswerText() {
		return answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

}
