package com.baige.imchat;

import java.io.PipedInputStream;

public interface OnShellListener {
	
	int usage(StringBuffer result);
	
	PipedInputStream execute(String command);

	int autoCompletion(StringBuffer result, String command);

	void showError(StringBuffer error);

	String getHeader();
	
	class SimpleOnShellListener implements OnShellListener{

		@Override
		public int usage(StringBuffer result) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public PipedInputStream execute(String command) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int autoCompletion(StringBuffer result, String command) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void showError(StringBuffer error) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getHeader() {
			// TODO Auto-generated method stub
			return "";
		}
	}
}
