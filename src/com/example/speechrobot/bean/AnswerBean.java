package com.example.speechrobot.bean;

import java.util.ArrayList;

public class AnswerBean {

	public Answer answer;
	public String operation;
	public String rc;
	public String service;
	public String text; //提问内容
	public ArrayList<MoreResults> moreResults;

	public class Answer {
		public String text; //回答内容
		public String type;
		
		@Override
		public String toString() {
			return "Answer [text=" + text + "]";
		}
	}

	public class MoreResults {
		public MoreAnswer answer;
		public String operation;
		public String rc;
		public String service;
		public String text;
	}

	public class MoreAnswer {
		public String text;
		public String type;
	}

	@Override
	public String toString() {
		return "AnswerBean [answer=" + answer + ", rc=" + rc + ", text=" + text
				+ "]";
	}
	
	

}
