package csci2020_finalProject1;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class ClientComplete extends JFrame {
	
	String portStr = "";
	String host = "";
	static String username = "";
	static String messageLine;
	
	static Socket socket;
	
    static Pattern usernamePattern = Pattern.compile("[A-Z]([a-z]+)([0-9]{3})");
    
	static JTextField userInput = new JTextField(53);
	static JTextArea chatArea = new JTextArea(18, 40);
	
	DataInputStream dataIn;
    static DataOutputStream dataOut;
	
	DateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd");
	static Date date1 = new Date();
	String todaysDate = dateFormat1.format(date1);
	
	DateFormat dateFormat2 = new SimpleDateFormat("yyyy_MM_dd");
	static Date date2 = new Date();
	String todaysDateFile = dateFormat2.format(date2);
	
	DateFormat dateFormat3 = new SimpleDateFormat("h:mm a");
	static Date date3 = new Date();
	String currentTime = dateFormat3.format(date3);
	
	static List<String> convoTranscript = new ArrayList<String>();
	static List<DataOutputStream> clientsDataOut = new ArrayList<DataOutputStream>();
	static List<ObjectOutputStream> clientsObjectOut = new ArrayList<ObjectOutputStream>();
	
	static JList<String> connectedUsersList;
	static DefaultListModel<String> connectedUsersModel;
    
	public ClientComplete(String host, String portStr) throws UnknownHostException, IOException {
		
		this.host = host;
		this.portStr = portStr;
		int port = Integer.parseInt(this.portStr);
			
        socket = new Socket(host, port);
			
		final JFrame clientFrame = new JFrame("Chat Client");
		
		Border transparentBorder = BorderFactory.createEmptyBorder();
		
		JLabel loggedInAsLabel = new JLabel("    Logged in as:");
		JLabel serverNameLabel = new JLabel("    Server Name:");
		JLabel messagePaneLabel = new JLabel("    Message Pane                                                                                                                                   ");
		JLabel connectedUsersLabel = new JLabel("Connected Users");
		JLabel space1 = new JLabel("  ");
		JLabel space2 = new JLabel("  ");
		JLabel space3 = new JLabel("   ");
		JLabel space4 = new JLabel("   ");
		
		JButton disconnect = new JButton("Disconnect");
		JButton send = new JButton("Send");
		JButton downloadTranscript = new JButton("> Download Conversation Transcript <");
		downloadTranscript.setBorderPainted(false);
		downloadTranscript.setContentAreaFilled(false);
		downloadTranscript.setFocusPainted(false);
		downloadTranscript.setOpaque(false);
		
		JTextField loggedInAsField = new JTextField(username);
		loggedInAsField.setBorder(transparentBorder);
		loggedInAsField.setEditable(false);
		loggedInAsField.validate();
		JTextField serverNameField = new JTextField(host + " : " + port);
		serverNameField.setBorder(transparentBorder);
		serverNameField.setEditable(false);
		userInput.setEditable(false);
		
		chatArea.append(todaysDate + "\n");
		chatArea.setWrapStyleWord(true);
		chatArea.setLineWrap(true);
		chatArea.setEditable(false);
		JScrollPane chatScroller = new JScrollPane();
		chatScroller.setBorder(BorderFactory.createTitledBorder("Messages"));
		chatScroller.setViewportView(chatArea);		
		
		connectedUsersModel = new DefaultListModel<String>();
		connectedUsersList = new JList<String>(connectedUsersModel);
		connectedUsersList.setSelectionModel(new DisabledItemSelectionModel());
		JScrollPane connectedUsersScroller = new JScrollPane(connectedUsersList);
		connectedUsersScroller.setPreferredSize(new Dimension(200, 310));
		connectedUsersScroller.setBorder(BorderFactory.createTitledBorder("Connected users"));
		connectedUsersScroller.setViewportView(connectedUsersList);
		
		disconnect.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						chatArea.append("You have been disconnected from the chat.");
						dataOut.writeUTF(">> " + username + " has left the chat.");
						userInput.setEditable(false);
						dataIn.close();
						dataOut.close();
						socket.close();
					} catch (IOException e1) {}
					
				}
			}
		);
		
		send.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					messageLine = username + " (" + currentTime + "): " + userInput.getText() + "\n";
					try {
						dataOut.writeUTF(messageLine);
						dataOut.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					userInput.setText("");
				}
			}
		);
			
		userInput.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					messageLine = username + " (" + currentTime + "): " + userInput.getText() + "\n";
					try {
						dataOut.writeUTF(messageLine);
						dataOut.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					userInput.setText("");
				}				
			}
		);
		
		downloadTranscript.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					downloadFile();
				}				
			}
		);
		
		JPanel top1 = new JPanel();
		top1.setLayout(new FlowLayout(FlowLayout.LEFT));
		top1.add(serverNameLabel);
		top1.add(serverNameField);
		top1.add(loggedInAsLabel);
		top1.add(loggedInAsField);
		top1.add(space1);
		top1.add(disconnect);
		
		JPanel chatLabels = new JPanel();
		chatLabels.setLayout(new FlowLayout(FlowLayout.LEFT));
		chatLabels.add(messagePaneLabel);
		chatLabels.add(connectedUsersLabel);
		
		JPanel connectedUsers = new JPanel();
		connectedUsers.setLayout(new BoxLayout(connectedUsers, BoxLayout.PAGE_AXIS));
		connectedUsers.setAlignmentY(JPanel.TOP_ALIGNMENT);
		connectedUsers.add(connectedUsersScroller);
		
		JPanel chat = new JPanel();
		chat.setLayout(new FlowLayout(FlowLayout.CENTER));
		chat.setAlignmentY(JPanel.TOP_ALIGNMENT);
		chat.add(chatScroller);
		chat.add(space2);
		chat.add(connectedUsers);
		
		JPanel userMessage = new JPanel();
		userMessage.add(userInput);
		userMessage.add(space4);
		userMessage.add(send);
		
		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.PAGE_AXIS));
		all.setAlignmentY(JPanel.TOP_ALIGNMENT);
		all.add(top1);
		all.add(chat);
		all.add(space3);
		all.add(userMessage);
		all.add(downloadTranscript);
	
		clientFrame.add(all);
		clientFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("clientIcon.png")));
		clientFrame.setLocation(425, 100);
		clientFrame.setVisible(true);
		clientFrame.setSize(700, 460);
		clientFrame.setResizable(false);
		clientFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		clientFrame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    	try {
					dataOut.writeUTF(">> " + username + " has left the chat.\n");
			        System.exit(0);
		    	} catch (IOException e1) {}
		    }
		});

	}
	
	//pop-up to input username
	private static void usernamePopup() {
		
		while (username.isEmpty() || username.toLowerCase().contains("server")) {
			
			JTextField instructions = new JTextField("Your username should follow the form: \"Username123\" ");
			instructions.setBorder(BorderFactory.createEmptyBorder());
			instructions.setEditable(false);
			final JTextField userText = new JTextField(10);
			
			
			JLabel usernamePopupLabel = new JLabel("Username: ");
			
			JPanel userInput = new JPanel();
			userInput.setLayout(new FlowLayout(FlowLayout.CENTER));
			userInput.add(usernamePopupLabel);
			userInput.add(userText);
			
			JPanel all = new JPanel();
			all.setLayout(new BoxLayout(all, BoxLayout.PAGE_AXIS));
			all.add(instructions);
			all.add(userInput);
			
			Object [] options = {"OK", "CANCEL"};
			JOptionPane jop = new JOptionPane(all, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, null);
			final JDialog dialog = jop.createDialog(null, "Login Window");
			dialog.setLocation(585, 300);
			dialog.setVisible(true);
			String choice = (String) jop.getValue();
			
			try {
				if (choice.equals("OK")) {
					username = userText.getText();
					Matcher usernameMatcher = usernamePattern.matcher(username);
					if (usernameMatcher.matches()) {
						if (username.length() >= 44) {
							JOptionPane.showMessageDialog(new JFrame(), "The username entered is too long.\nPlease, try again.", "Error", JOptionPane.ERROR_MESSAGE);
							username = "server";
						} else {
							dialog.dispose();
						}
					} else {
						JOptionPane.showMessageDialog(new JFrame(), "The username entered is not in the correct form or\nthe username you have picked is reserved (i.e. \"Server\").\nPlease, try again.", "Error", JOptionPane.ERROR_MESSAGE);
						username = "server";
					}
				} else if (choice.equals("CANCEL")) {
					System.exit(0);
				}
				
			} catch (Exception e) {}
						
		}
	}
	
	class Sender extends Thread {
        DataOutputStream dataOut;
        public Sender(DataOutputStream dataOut) {
          this.dataOut = dataOut;
        }
        
        public void run() {
                try {
                    dataOut.writeUTF(messageLine);
                    dataOut.flush();
                } catch (Exception e) {}
            }
    }

    static class Receiver extends Thread {
      DataInputStream dataIn;
        public Receiver(DataInputStream dataIn) {
            this.dataIn = dataIn;           
        }
        
		public void run() {
        	while(true){
        		try {
        			String msg = dataIn.readUTF();
					
        			if (msg.contains(" has joined the chat!")) {
        				String[] messageTokens = msg.split(" ");
						String clientUsername = messageTokens[1];
        				connectedUsersModel.addElement(clientUsername);
						connectedUsersList.setModel(connectedUsersModel);
						chatArea.append(msg);
        			} else if (msg.contains(" has left the chat.")) {
						String[] messageTokens = msg.split(" ");
						String clientUsername = messageTokens[1];
						connectedUsersModel.removeElement(clientUsername);
						connectedUsersList.setModel(connectedUsersModel);
						chatArea.append(msg);
        			} else if (msg.equals(">> You have been removed from this chat.")) {
        				chatArea.append(msg);
        				dataIn.close();
        				dataOut.close();
        				socket.close();
        				userInput.setEditable(false);
        			} else if (msg.equals(">> The server has ended the connection.")) {
        				chatArea.append(msg);
        				dataIn.close();
        				dataOut.close();
        				socket.close();
        				userInput.setEditable(false);
        			} else if (msg.contains(" has been booted from this chat.")) {
        				String[] messageTokens = msg.split(" ");
						String clientUsername = messageTokens[1];
						connectedUsersModel.removeElement(clientUsername);
						connectedUsersList.setModel(connectedUsersModel);
						chatArea.append(msg);
        			} else {
        				chatArea.append(msg);
            			convoTranscript.add(msg);
        			}
           		} catch(Exception e) {}
        	}
        } 
    }
    
    public void connect() throws Exception {
    	chatArea.append(">> Attempting connection...\n");
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        dataIn = new DataInputStream(in);
        dataOut = new DataOutputStream(out);
        
        chatArea.append(">> Setting up data input and output streams...\n");
    	chatArea.append(">> Handling connection to " + host + ":" + Integer.parseInt(this.portStr) + "...\n");
 
        Thread sender = new Sender(dataOut);
        Thread receiver = new Receiver(dataIn);
        
        chatArea.append(">> Streams and threads have been set-up successfully.\n");
        chatArea.append(">> You are now connected!\n");
		//will send this client's username to the server. the server will
		//store it to the connected users list
		dataOut.writeUTF(">> " + username + " has joined the chat!\n");
  
        userInput.setEditable(true);
        
        sender.start();
        receiver.start();
        
        sender.join();
        receiver.join();

    }
    
    //called when the user clicks "download conversation transcript"
    protected void downloadFile() {
		String fileName = "convoTranscript_" + todaysDateFile + ".txt";

		try {
			
			FileWriter writer = new FileWriter("C:\\" + fileName);
			
			writer.write("Conversation transcript from: " + todaysDate);
			writer.write("\r\n");
			
			for (String str: convoTranscript) {
				writer.write("\r\n" + str);
			}
			writer.close();
			
			chatArea.append(">> The file \" " + fileName + "\" has been downloaded successfully to your Local Disk (C:).\n");
			
		} catch (Exception e) { }
		
	}
	
    class DisabledItemSelectionModel extends DefaultListSelectionModel {

        public void setSelectionInterval(int index0, int index1) {
            super.setSelectionInterval(-1, -1);
        }
    }
    
	public static void main(String[] args) throws Exception {
		usernamePopup();
		ClientComplete client = new ClientComplete("localhost", "3000");
		client.connect();
	}
}
