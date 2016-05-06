package unimelb.distributed_algo_game.network.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.HumanPlayer;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.token.Token;

public class MainGameLoginDealerPanel extends JPanel {
	private JTextField ipTextField;
	private JTextField portTextField;
	private JTextArea textArea;
	private int nodeID = -1;
	private MainGameLoginDealerPanel self;

	public MainGameLoginDealerPanel() {
		self = this;
		setLayout(null);

		ipTextField = new JTextField();
		ipTextField.setBounds(153, 52, 114, 19);
		add(ipTextField);
		ipTextField.setColumns(10);

		portTextField = new JTextField();
		portTextField.setBounds(153, 83, 114, 19);
		add(portTextField);
		portTextField.setColumns(10);

		JLabel lblNewLabel = new JLabel("Dealer IP Address");
		lblNewLabel.setBounds(12, 54, 181, 15);
		add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Dealer Port");
		lblNewLabel_1.setBounds(12, 81, 123, 15);
		add(lblNewLabel_1);

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent action) {
				// TODO get ip and port from textfield and set init server
				// socket
				String ipAddress = ipTextField.getText();
				String port = portTextField.getText();
				if (!ipAddress.equals("") && !port.equals("")) {
					System.out.println("Dealer/Node0 Starts the game");
					String gamePlayerInfo[] = {Integer.toString(nodeID), ipAddress, port};
					// Initialize players
					Player p = new HumanPlayer("Dealer", new GamePlayerInfo(gamePlayerInfo), self);
					p.setDealer(true);
					Thread t = new Thread(p);
					t.start();
					t.setName("Human Player");
					// Initialize game token for mutex
					Token gameToken = new Token();
					// Initialize queue for mutex control
					ArrayList<Player> tokenRequests = new ArrayList<Player>();
				}

			}
		});
		btnStart.setBounds(153, 214, 117, 25);
		add(btnStart);

		textArea = new JTextArea();
		textArea.setBounds(304, 96, 122, 143);
		add(textArea);

		JLabel lblPlayerList = new JLabel("Player List");
		lblPlayerList.setBounds(304, 81, 122, 15);
		add(lblPlayerList);
	}

	public void updatePlayerList(int nodeID) {
		textArea.append("Node" + nodeID + "is joined");
	}
	
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}
}
