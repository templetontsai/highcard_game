package unimelb.distributed_algo_game.network.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import unimelb.distributed_algo_game.player.AIPlayer;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;

public class MainGameLoginClientPanel extends JPanel {
	private JTextField nodeField;
	private JTextField ipTextField;
	private JTextField portTextField, serverIPTextField, serverPortTextField;
	private JLabel lblNewLabel_0;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1, serverIPLabel, serverPortLabel;
	private JButton btnPlay;
	private JButton btnStart;
	
	private int nodeID;

	public MainGameLoginClientPanel() {
		setLayout(null);

		nodeField = new JTextField();
		nodeField.setBounds(153, 52, 114, 19);
		add(nodeField);
		nodeField.setColumns(10);
		
		ipTextField = new JTextField();
		ipTextField.setBounds(153, 83, 114, 19);
		add(ipTextField);
		ipTextField.setColumns(10);
		
		portTextField = new JTextField();
		portTextField.setBounds(153, 113, 114, 19);
		add(portTextField);
		portTextField.setColumns(10);
		
		serverIPTextField = new JTextField();
		serverIPTextField.setBounds(153, 142, 114, 19);
		add(serverIPTextField);
		serverIPTextField.setColumns(10);
		
		serverPortTextField = new JTextField();
		serverPortTextField.setBounds(153, 172, 114, 19);
		add(serverPortTextField);
		serverPortTextField.setColumns(10);

		lblNewLabel_0 = new JLabel("NODE ID");
		lblNewLabel_0.setBounds(12, 54, 181, 15);
		add(lblNewLabel_0);
		
		lblNewLabel = new JLabel("Client IP Address");
		lblNewLabel.setBounds(12, 81, 181, 15);
		add(lblNewLabel);

		lblNewLabel_1 = new JLabel("Client Port");
		lblNewLabel_1.setBounds(12, 111, 123, 15);
		add(lblNewLabel_1);
		
		serverIPLabel = new JLabel("Server IP Address");
		serverIPLabel.setBounds(12, 141, 123, 15);
		add(serverIPLabel);
		
		serverPortLabel = new JLabel("Server Port");
		serverPortLabel.setBounds(12, 171, 123, 15);
		add(serverPortLabel);
		
		btnPlay = new JButton("Play");
		btnPlay.setBounds(153, 214, 117, 25);
		add(btnPlay);
		btnPlay.setVisible(false);
		
		btnStart = new JButton("Connect");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent action) {
				// TODO get ip and port from textfield and set init server
				// socket

				String ipAddress = ipTextField.getText();
				String port = portTextField.getText();
				String serverIPAddress = serverIPTextField.getText();
				String serverPort = serverPortTextField.getText();
				if (!ipAddress.equals("") && !port.equals("") && !serverIPAddress.equals("") && !serverPort.equals("")) {
					String gamePlayerInfo[] = {Integer.toString(nodeID), ipAddress, port};
					String gameServerInfo[] = {"0", serverIPAddress, serverPort};
					System.out.println("Client"+nodeID+" sending connection to dealer");

					Player p = new AIPlayer("AI", new GamePlayerInfo(gamePlayerInfo), new GamePlayerInfo(gameServerInfo));
					Thread t = new Thread(p);
					t.setName("AI Player Thread");
					t.start();
					btnStart.setVisible(false);
					btnPlay.setVisible(true);
				}
			}
		});
		btnStart.setBounds(153, 214, 117, 25);
		add(btnStart);
		
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent action) {
				// TODO get ip and port from textfield and set init server
				// socket
				
			}
		});
		
	}

	public void setClientNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

}
