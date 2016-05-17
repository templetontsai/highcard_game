package unimelb.distributed_algo_game.network.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import unimelb.distributed_algo_game.player.DealerPlayer;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.player.SlavePlayer;
import unimelb.distributed_algo_game.pokers.Card;
import unimelb.distributed_algo_game.token.Token;

public class MainGamePanel extends JPanel {

	private JTextField ipTextField = null;
	private JTextField portTextField = null;
	private JTextField nodeField = null;
	private JTextField serverIPTextField = null;
	private JTextField serverPortTextField = null;
	private JTextArea textArea = null;
	private int nodeID = -1;

	private JButton btnStart = null;
	private MainGamePanel self = null;
	private MainGameFrameGUI mMainGameFrameGUI = null;

	private DealerStartButtonActionListerner mDealerStartButtonActionListerner = null;
	private Card c = null;
	private Player p = null;
	private List<CardPanel> mPlayerPanelList = null;
	private GameTablePanel gameTable = null;
	private boolean isDealer = false;

	private JLabel lblNewLabel_0;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1, serverIPLabel, serverPortLabel;

	private PlayerStartButtonActionListerner mPlayerStartButtonActionListerner = null;

	public MainGamePanel(MainGameFrameGUI mainGameFrameGUI, boolean isDealer) {

		self = this;
		this.mMainGameFrameGUI = mainGameFrameGUI;
		this.isDealer = isDealer;

		setLayout(null);

		if (isDealer) {
			initDealerPanel();
		} else {
			initPlayerPanel();
		}

	}

	public void initDealerPanel() {
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

		mDealerStartButtonActionListerner = new DealerStartButtonActionListerner();

		btnStart = new JButton("Start");
		btnStart.addActionListener(mDealerStartButtonActionListerner);

		btnStart.setBounds(18, 185, 117, 25);
		add(btnStart);

		textArea = new JTextArea();
		textArea.setBounds(304, 96, 122, 143);
		textArea.setEditable(false);
		add(textArea);

		JLabel lblPlayerList = new JLabel("Player List");
		lblPlayerList.setBounds(304, 81, 122, 15);
		add(lblPlayerList);

		mPlayerPanelList = new ArrayList<CardPanel>();
	}

	public void initPlayerPanel() {
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

		mPlayerStartButtonActionListerner = new PlayerStartButtonActionListerner();

		btnStart = new JButton("Start");
		btnStart.addActionListener(mPlayerStartButtonActionListerner);
		btnStart.setBounds(12, 225, 117, 25);
		add(btnStart);
		btnStart.setVisible(true);

		mPlayerPanelList = new ArrayList<CardPanel>();
	}

	final class DealerStartButtonActionListerner implements ActionListener {
		public void actionPerformed(ActionEvent action) {
			// TODO get ip and port from textfield and set init server
			// socket

			// String ipAddress = ipTextField.getText();
			// String port = portTextField.getText();

			String ipAddress = "localhost";
			String port = "5000";

			if (!ipAddress.equals("") && !port.equals("")) {
				System.out.println(ipAddress + "-" + port);
				System.out.println("Dealer/Node0 Starts the game");
				String gamePlayerInfo[] = { Integer.toString(nodeID), ipAddress, port };
				// Initialize players
				p = new DealerPlayer("Dealer", new GamePlayerInfo(gamePlayerInfo, true), self);
				p.setDealer(true);
				p.play();
				textArea.append("Node" + nodeID + " is joined\n");

				btnStart.setEnabled(false);
			}

		}
	}

	final class PlayerStartButtonActionListerner implements ActionListener {
		public void actionPerformed(ActionEvent action) {
			// TODO get ip and port from textfield and set init server
			// socket
			/*
			 * String ipAddress = ipTextField.getText(); String port =
			 * portTextField.getText(); String serverIPAddress =
			 * serverIPTextField.getText(); String serverPort =
			 * serverPortTextField.getText();
			 */

			String ipAddress = "localhost";
			String port = "500" + nodeID;
			String serverIPAddress = "localhost";
			String serverPort = "5000";

			if (!ipAddress.equals("") && !port.equals("") && !serverIPAddress.equals("") && !serverPort.equals("")) {
				String gamePlayerInfo[] = { Integer.toString(nodeID), ipAddress, port };
				String gameServerInfo[] = { "0", serverIPAddress, serverPort };
				System.out.println("Client" + nodeID + " sending connection to dealer");

				p = new SlavePlayer("Node" + nodeID, new GamePlayerInfo(gamePlayerInfo, false),
						new GamePlayerInfo(gameServerInfo, false), self);
				p.setDealer(false);
				p.play();
				btnStart.setEnabled(false);

			}
		}

	}

	public synchronized void updatePlayerList(int nodeID) {
		textArea.append("Node" + nodeID + " is joined\n");
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public void showGameTable(boolean isEnable, List<Integer> mPlayerIDList) {

		for (Integer i : mPlayerIDList) {
			mPlayerPanelList.add(new CardPanel(i));
		}

		mMainGameFrameGUI.getContentPane().removeAll();

		gameTable = new GameTablePanel(mPlayerIDList, isDealer, p);

		mMainGameFrameGUI.setContentPane(gameTable);
		mMainGameFrameGUI.revalidate();

	}

	public void updateCard(Card c, int nodeID) {
		gameTable.updateCard(c, nodeID);
	}

	public void declareWinner(int nodeID) {
		if (nodeID == this.nodeID)
			JOptionPane.showMessageDialog(null, "You win");
		else if (nodeID != -1)
			JOptionPane.showMessageDialog(null, "node" + nodeID + " has won");
		gameTable.newRound();

	}

	public void updateGameTable(List<Integer> mPlayerIDList) {
		gameTable.updateGameTable(mPlayerIDList);
	}

	public void setDealer(boolean isDealer) {
		this.isDealer = isDealer;
		this.gameTable.setDealer(isDealer);
	}

}
