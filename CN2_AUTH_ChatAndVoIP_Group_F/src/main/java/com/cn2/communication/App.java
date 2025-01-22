package com.cn2.communication;

import java.io.*;
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.sound.sampled.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import java.lang.Thread;

public class App extends Frame implements WindowListener, ActionListener {

	/*
	 * Definition of the app's fields
	 */
	static TextField inputTextField;		
	static JTextArea textArea;				 
	static JFrame frame;					
	static JButton sendButton;				
	static JTextField meesageTextField;		  
	public static Color gray;				
	final static String newline="\n";		
	static JButton callButton;				
	
	// TODO: Please define and initialize your variables here...
	public String ipAddress = "192.168.1.8";
	public int textChatPort = 12345;
	public int voiceChatPort = 12346;
	/**
	 * Construct the app's frame and initialize important parameters
	 */
	public App(String title) {
		
		/*
		 * 1. Defining the components of the GUI
		 */
		
		// Setting up the characteristics of the frame
		super(title);									
		gray = new Color(254, 254, 254);		
		setBackground(gray);
		setLayout(new FlowLayout());			
		addWindowListener(this);	
		
		// Setting up the TextField and the TextArea
		inputTextField = new TextField();
		inputTextField.setColumns(20);
		
		// Setting up the TextArea.
		textArea = new JTextArea(10,40);			
		textArea.setLineWrap(true);				
		textArea.setEditable(false);			
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		//Setting up the buttons
		sendButton = new JButton("Send");			
		callButton = new JButton("Call");			
						
		/*
		 * 2. Adding the components to the GUI
		 */
		add(scrollPane);								
		add(inputTextField);
		add(sendButton);
		add(callButton);
		
		/*
		 * 3. Linking the buttons to the ActionListener
		 */
		sendButton.addActionListener(this);			
		callButton.addActionListener(this);	

		
	}
	
	public void sendMessage(String message) {
		try {
			DatagramSocket socket = new DatagramSocket();
			InetAddress address = InetAddress.getByName(ipAddress);
			
			byte[] buffer = message.getBytes();
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, textChatPort);
			socket.send(packet);
			
			textArea.append("Me: "+message+"\n");
//			socket.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public void startReceiver() {
		new Thread(() -> {
			try {
				DatagramSocket socket = new DatagramSocket(textChatPort);
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				System.out.println("Waiting for messages...");
				while(true) {
					socket.receive(packet);
					String message = new String(packet.getData(), 0, packet.getLength());
					textArea.append("You: "+message+"\n");
				}
			} catch(Exception ex) {
				System.out.println(ex);
			}
		}).start();
	}
	
	public void startCall() throws Exception{
		AudioFormat format = new AudioFormat(8000.0f, 8, 1, true, true);
		
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
		System.out.println(microphone);
		if(!AudioSystem.isLineSupported(info)) {
			System.out.println("Not supported");
		}
		microphone.open(format);
		microphone.start();
		
		DatagramSocket socket = new DatagramSocket();
		InetAddress targetAddress = InetAddress.getByName(ipAddress);
		
		byte[] buffer = new byte[1024];
		
		System.out.println("Call started...");
		
		while(true) {
			//Get the audio from the microphone
//			System.out.println("MIC WORKING");
			int bytesRead = microphone.read(buffer, 0, buffer.length);
			
			//Send audio data
			DatagramPacket packet = new DatagramPacket(buffer, bytesRead, targetAddress, voiceChatPort);
			socket.send(packet);
			
		}
		
	}
	
	public void startCallReceiver(){
		new Thread(() -> {
			try {
				AudioFormat format = new AudioFormat(8000.0f, 8, 1, true, true);
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
				SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(info);
				speakers.open(format);
				speakers.start();
				
				DatagramSocket socket = new DatagramSocket(voiceChatPort);
				
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				System.out.println("Waiting for voice...");
				
				while(true) {
//					System.out.println("DOULEVEI");
					socket.receive(packet);
					speakers.write(packet.getData(), 0, packet.getLength());
//					System.out.println("Audio packet size: "+packet.getLength());
				}
			}catch(Exception ex) {
				System.out.println(ex);
			}
		}).start();
		
	}
	
	/**
	 * The main method of the application. It continuously listens for
	 * new messages.
	 */
	public static void main(String[] args){
	
		/*
		 * 1. Create the app's window
		 */
		App app = new App("CN2 - AUTH");  // TODO: You can add the title that will displayed on the Window of the App here																		  
		app.setSize(500,250);				  
		app.setVisible(true);				  
		
		
		
		app.startCallReceiver();
		app.startReceiver();
		/*
		 * 2. 
		 */
		
		do{		
//			app.startReceiver();
//			app.startCallReceiver();
			// TODO: Your code goes here...
		}while(true);
	}
	
	/**
	 * The method that corresponds to the Action Listener. Whenever an action is performed
	 * (i.e., one of the buttons is clicked) this method is executed. 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
	

		/*
		 * Check which button was clicked.
		 */
		if (e.getSource() == sendButton){
			
			// The "Send" button was clicked
			
			// TODO: Your code goes here...
			String message = inputTextField.getText();
			if(!message.isEmpty()) {
				sendMessage(message);
				inputTextField.setText("");
			}
		
			
		}else if(e.getSource() == callButton){
			
			// The "Call" button was clicked
			
			// TODO: Your code goes here...
			new Thread(() -> {
				try {
					startCall();
				} catch(Exception ex) {
					System.out.println(ex);
				}
			}).start();
			
		}
	}

	/**
	 * These methods have to do with the GUI. You can use them if you wish to define
	 * what the program should do in specific scenarios (e.g., when closing the 
	 * window).
	 */
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		dispose();
        	System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub	
	}
}
