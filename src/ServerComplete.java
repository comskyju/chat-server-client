package csci2020_finalProject1;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
public class ServerComplete extends JFrame {
	
	static int port;
	String portStr = Integer.toString(port);
	String host;
	
	String textLine;
	
	ServerSocket main;
	static DataOutputStream dataOut;
	static DataInputStream dataIn;
	
	//used for the top of the chat area every time a new client/frame is created
	DateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd");
	static Date date1 = new Date();
	String todaysDate = dateFormat1.format(date1);
	
	//used for the name of the conversation transcript file
	DateFormat dateFormat2 = new SimpleDateFormat("yyyy_MM_dd");
	static Date date2 = new Date();
	String todaysDateFile = dateFormat2.format(date2);
	
	//used in every message being sent
	DateFormat dateFormat3 = new SimpleDateFormat("h:mm a");
	static Date date3 = new Date();
	String currentTime = dateFormat3.format(date3);
	
	static List<String> convoTranscript = new ArrayList<String>();
	static List<DataOutputStream> clientsDataOut = new ArrayList<DataOutputStream>();
	//visible to clients as well
	static List<String> usernames = new ArrayList<String>();
	static List<ClientHandler> clients = new ArrayList<ClientHandler>();
	
	static JTextArea chatArea = new JTextArea(20, 40);
	
	static JList<String> connectedUsersList;
	static DefaultListModel<String> connectedUsersModel;
		
	public ServerComplete(String host, String portStr, int port) throws IOException {
		this.host = host;
		this.portStr = portStr;
			
		this.main = new ServerSocket(port);
				
		JFrame serverFrame = new JFrame("Server");
		
		Border transparentBorder = BorderFactory.createEmptyBorder();
		
		JLabel serverNameLabel = new JLabel("    Server Name:");
		JLabel messagePaneLabel = new JLabel("    Message Pane                                                                                                                                  ");
		JLabel connectedUsersLabel = new JLabel("Connected Users");
		JLabel space1 = new JLabel("  ");
		JLabel space2 = new JLabel(" ");
		JLabel space3 = new JLabel("   ");
		JLabel space4 = new JLabel("   ");
						
		JButton disconnect = new JButton("Disconnect");
		JButton removeUser = new JButton("                 Remove User                 ");
		JButton send = new JButton("Send");
		JButton downloadTranscript = new JButton("> Download Conversation Transcript <");
		downloadTranscript.setBorderPainted(false);
		downloadTranscript.setContentAreaFilled(false);
		downloadTranscript.setFocusPainted(false);
		downloadTranscript.setOpaque(false);
			
		JTextField serverNameField = new JTextField(host + " : " + portStr);
		serverNameField.setBorder(transparentBorder);
		serverNameField.setEditable(false);
		final JTextField userInput = new JTextField(55);
		userInput.setEditable(true);		
		
		chatArea.append(todaysDate + "\n");
		chatArea.setWrapStyleWord(true);
		chatArea.setLineWrap(true);
		chatArea.setEditable(false);
		JScrollPane chatScroller = new JScrollPane();
		chatScroller.setBorder(BorderFactory.createTitledBorder("Messages"));
		chatScroller.setViewportView(chatArea);		
		
		connectedUsersModel = new DefaultListModel<String>();
		connectedUsersList = new JList<String>(connectedUsersModel);
		JScrollPane connectedUsersScroller1 = new JScrollPane(connectedUsersList);
		connectedUsersScroller1.setPreferredSize(new Dimension(17, 300));
		connectedUsersScroller1.setBorder(BorderFactory.createTitledBorder("Connected users"));
		connectedUsersScroller1.setViewportView(connectedUsersList);
		
		disconnect.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						chatArea.append(">> You have ended the connection.");
						for (DataOutputStream o : clientsDataOut) {
							o.writeUTF(">> The server has ended the connection.");
							o.flush();
						}
						userInput.setEditable(false);
						dataIn.close();
						dataOut.close();
						main.close();
					} catch (IOException e1) {}
					
				}
			}
		);
		
		removeUser.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int selectedUser = connectedUsersList.getSelectedIndex();
					if (selectedUser >= 0) {
						ClientHandler selectedClient = clients.get(selectedUser);
						String bootedUsername = connectedUsersModel.get(selectedUser);
						connectedUsersModel.removeElement(bootedUsername);
						connectedUsersList.setModel(connectedUsersModel);						
						try {
							for (DataOutputStream o : clientsDataOut) {
								o.writeUTF(">> " + bootedUsername + " has been booted from this chat.\n");
								o.flush();
							}
							selectedClient.dataOutCH.writeUTF(">> You have been removed from this chat.");
							selectedClient.dataInCH.close();
							selectedClient.dataInCH.close();
						} catch (IOException e1) {
							System.out.println("Error removing user from the chat.");
						}
					} else {
						JOptionPane.showMessageDialog(new JFrame(), "No users were selected.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				
			}
		);
		
		send.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String message = "SERVER " + "(" + currentTime + "): " + userInput.getText() + "\n";
					try {
						for (DataOutputStream o : clientsDataOut) {
							o.writeUTF(message);
							o.flush();
						}
						chatArea.append(message);
						convoTranscript.add(message);
						userInput.setText("");
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
				}
			}
		);
		
		userInput.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String message = "SERVER " + "(" + currentTime + "): " + userInput.getText() + "\n";
					try {
						for (DataOutputStream o : clientsDataOut) {
							o.writeUTF(message);
							o.flush();
						}
						chatArea.append(message);
						convoTranscript.add(message);
						userInput.setText("");
					} catch (Exception e1) {
						e1.printStackTrace();
					}
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
		
		JPanel top = new JPanel();
		top.setLayout(new FlowLayout(FlowLayout.LEFT));
		top.add(serverNameLabel);
		top.add(serverNameField);
		top.add(space1);
		top.add(disconnect);
		
		JPanel chatLabels = new JPanel();
		chatLabels.setLayout(new FlowLayout(FlowLayout.LEFT));
		chatLabels.add(messagePaneLabel);
		chatLabels.add(connectedUsersLabel);
		
		JPanel connectedUsers = new JPanel();
		connectedUsers.setLayout(new BoxLayout(connectedUsers, BoxLayout.PAGE_AXIS));
		connectedUsers.setAlignmentY(JPanel.TOP_ALIGNMENT);
		connectedUsers.add(connectedUsersScroller1);
		connectedUsers.add(space4);
		connectedUsers.add(removeUser);
		
		JPanel chat = new JPanel();
		chat.setLayout(new FlowLayout(FlowLayout.CENTER));
		chat.setAlignmentY(JPanel.TOP_ALIGNMENT);
		chat.add(chatScroller);
		chat.add(space2);
		chat.add(connectedUsers);
		
		JPanel userMessage = new JPanel();
		userMessage.add(userInput);
		userMessage.add(space3);
		userMessage.add(send);
		
		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.PAGE_AXIS));
		all.setAlignmentY(JPanel.TOP_ALIGNMENT);
		all.add(top);
		all.add(chat);
		all.add(userMessage);
		all.add(downloadTranscript);
		
		serverFrame.add(all);
		serverFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("serverIcon.png")));
		serverFrame.setVisible(true);
		serverFrame.setSize(730, 490);
		serverFrame.setLocation(50,50);
		serverFrame.setResizable(false);
		serverFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		serverFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    	try {
		    		for (DataOutputStream c : clientsDataOut ) {
		    			c.writeUTF(">> The server has ended the connection.");
						c.flush();
				        System.exit(0);
					}
		    	} catch (IOException e1) {
			        System.exit(0);
		    	}
		    }
		});

	}
	
	protected void downloadFile() {
		String fileName = "convoTranscript_SERVERCOPY_" + todaysDateFile + ".txt";

		try {
			
			FileWriter writer = new FileWriter("C:\\" + fileName);
			
			writer.write("Conversation transcript from: " + todaysDate);
			writer.write("\r\n");
			
			for (String str: convoTranscript) {
				writer.write("\r\n" + str);
			}
			writer.close();
			
			chatArea.append(">> File \" " + fileName + "\" has been downloaded successfully to your Local Disk (C:).\n");
			
		} catch (Exception e) { }
		
	}
	
	static class ClientHandler extends Thread {
		String name;
		DataOutputStream dataOutCH;
		DataInputStream dataInCH;
			
		ObjectOutputStream objOutCH;
		ObjectInputStream objInCH;

		public ClientHandler(int index, DataInputStream in, DataOutputStream out) {
			this.dataInCH = in;
			this.dataOutCH = out;	
		}

		public void run() {	
			while (true) {
				try {
					
					String receivedMessage = dataInCH.readUTF();

					if (receivedMessage.contains(" has joined the chat!")) {
						//when the client sends the server its username, identify it and add its
						//username AND client index to the connected users jlist and update it
						String[] messageTokens = receivedMessage.split(" ");
						String clientUsername = messageTokens[1];
						connectedUsersModel.addElement(clientUsername);
						connectedUsersList.setModel(connectedUsersModel);
						usernames.add(clientUsername);
						chatArea.append(receivedMessage);
						//send the new client username to all clients so that they can add it to
						//their lists
						for (DataOutputStream c : clientsDataOut ) {
							c.writeUTF(receivedMessage);
							c.flush();
						}
					} else if (receivedMessage.contains(" has left the chat.")) {
						String[] messageTokens = receivedMessage.split(" ");
						String clientUsername = messageTokens[1];
						connectedUsersModel.removeElement(clientUsername);
						connectedUsersList.setModel(connectedUsersModel);
						usernames.remove(clientUsername);
						chatArea.append(receivedMessage);
						for (DataOutputStream c : clientsDataOut ) {
							c.writeUTF(receivedMessage);
							c.flush();
						}
					} else {
						//print received message to all connected clients
						for (DataOutputStream c : clientsDataOut ) {
							c.writeUTF(receivedMessage);
							c.flush();
						}
						chatArea.append(receivedMessage);
						convoTranscript.add(receivedMessage);
					}
				} catch (Exception e) {}
			}
		}
	}
	
	// start a serve
	public void serve() throws Exception {
		int index = 1;
		while (true) {
			Socket socket = this.main.accept();
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			DataInputStream dataIn = new DataInputStream(in);
			DataOutputStream dataOut = new DataOutputStream(out);
			clientsDataOut.add(dataOut);
			
					
			// handle the connection
			// keep reading using an infintite loop
			chatArea.append(">> Handling connection to Client " + index + "...\n");
			ClientHandler client = new ClientHandler(index, dataIn, dataOut);
			clients.add(client);
			client.start();
			index += 1; // add one every time a new client is added
			
			//to make sure all clients that joined recently also see the clients that joined
			//previously
			for (int i = 0; i < usernames.size(); i++) {
				dataOut.writeUTF(">> " + usernames.get(i) + " has joined the chat!\n");
			}
		}
	}
		
	public static void main(String[] args) throws Exception {
		ServerComplete server = new ServerComplete("localhost", "3000", 3000);
		chatArea.append(">> Serving...\n");
		server.serve();
	}

}
