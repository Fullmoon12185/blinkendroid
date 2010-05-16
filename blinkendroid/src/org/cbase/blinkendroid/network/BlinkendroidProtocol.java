package org.cbase.blinkendroid.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import org.cbase.blinkendroid.Constants;

import android.util.Log;

public class BlinkendroidProtocol {
	public final static String PROTOCOL_PLAYER="P";
	public final static String PROTOCOL_INIT="I";

	public final static String COMMAND_PLAYER_TIME="T";

	private boolean server;
	PrintWriter out;
	BufferedReader in;
	GlobalTimerThread globalTimerThread;

	private final HashMap<String, ICommandHandler> handlers = 
	    new HashMap<String, ICommandHandler>();

	public BlinkendroidProtocol(Socket socket, boolean server){
		try {
			this.out=new PrintWriter(socket.getOutputStream(), true);

		this.in=new BufferedReader( new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(42);
		}
		this.server=server;
		new InputThread().start();
	}
	
	public void registerHandler(String proto, ICommandHandler handler) {
	    handlers.put(proto, handler);
	}
	
	public void startTimerThread(){
//		if(null==globalTimerThread)
		globalTimerThread=new GlobalTimerThread();
		globalTimerThread.start();
	}
	
	public void stopTimerThread() {
		if(null!=globalTimerThread){
			globalTimerThread.end();
			globalTimerThread.interrupt();
		}
	}
	public void close(){
		out.close();
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private class InputThread extends Thread {
//		private boolean running = true;

		@Override
		public void run() {
			Log.i(Constants.LOG_TAG, "InputThread started "+(server?"server":"client"));
			String inputLine;
			try {
				while ((inputLine = in.readLine()) != null) {
					Log.i(Constants.LOG_TAG, "InputThread recieved: "+inputLine);
					String proto = inputLine.substring(0, 1);
					ICommandHandler handler	=	handlers.get(proto);
					if(null!=handler)
						handler.handle(inputLine.substring(1).getBytes());
				}
			} catch (NumberFormatException e) {
				Log.e(Constants.LOG_TAG, "InputThread fucked " +(server?"server":"client") ,e);
			} catch (IOException e) {
				Log.e(Constants.LOG_TAG, "InputThread fucked " +(server?"server":"client") ,e);
			}	
			Log.i(Constants.LOG_TAG, "InputThread ended!!!!!!! " +(server?"server":"client") );
		}
	}
	
	private class GlobalTimerThread extends Thread {
		private boolean running = true;

		@Override
		public void run() {
			Log.i(Constants.LOG_TAG, "GlobalTimerThread started");
			while (running) {
				try {
					GlobalTimerThread.sleep(5000);
				} catch (InterruptedException e) {
				}
				long t = System.currentTimeMillis();
				Log.i(Constants.LOG_TAG, "GlobalTimerThread ping " + t);
				out.write(PROTOCOL_PLAYER+COMMAND_PLAYER_TIME+Long.toString(t) + '\n');
				out.flush();
			}
		}
		
		public void end(){
			running=false;
		}
	}



}