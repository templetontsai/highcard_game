package unimelb.distributed_algo_game.network.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import unimelb.distributed_algo_game.player.AIPlayer;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.pokers.Card;

public class MainGameLoginClientPanel extends JPanel {
	private JTextField nodeField;
	private JTextField ipTextField;
	private JTextField portTextField, serverIPTextField, serverPortTextField;
	private JLabel lblNewLabel_0;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1, serverIPLabel, serverPortLabel;
	private JButton btnStart = null;
	private MainGameLoginClientPanel self = null;
	private StartButtonActionListerner mStartButtonActionListerner = null;
	private MainGameFrameGUI mMainGameFrameGUI = null;
	private Card c;
	private Player p;
	private List<CardPanel> mPlayerPanelList;
	private GameTablePanel gmaeTable;

	

	private int nodeID;

	public MainGameLoginClientPanel(MainGameFrameGUI mainGameFrameGUI) {
		self = this;
		setLayout(null);
		this.mMainGameFrameGUI = mainGameFrameGUI;
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

		mStartButtonActionListerner = new StartButtonActionListerner();
		
		btnStart = new JButton("Start");
		btnStart.addActionListener(mStartButtonActionListerner);
		btnStart.setBounds(12, 225, 117, 25);
		add(btnStart);
		btnStart.setVisible(true);
		
		
		mPlayerPanelList = new ArrayList<CardPanel>();

	}

	final class StartButtonActionListerner implements ActionListener {
		public void actionPerformed(ActionEvent action) {
			// TODO get ip and port from textfield and set init server
			// socket
			
			/*String ipAddress = ipTextField.getText();
			String port = portTextField.getText();
			String serverIPAddress = serverIPTextField.getText();
			String serverPort = serverPortTextField.getText();*/
			String ipAddress = "localhost";
			String port = "500" + nodeID;
			String serverIPAddress = "localhost";
			String serverPort = "5000";
			
			if (!ipAddress.equals("") && !port.equals("") && !serverIPAddress.equals("") && !serverPort.equals("")) {
				String gamePlayerInfo[] = { Integer.toString(nodeID), ipAddress, port };
				String gameServerInfo[] = { "0", serverIPAddress, serverPort };
				System.out.println("Client" + nodeID + " sending connection to dealer");

				p = new AIPlayer("AI", new GamePlayerInfo(gamePlayerInfo), new GamePlayerInfo(gameServerInfo), self);
				Thread t = new Thread(p);
				t.setName("AI Player Thread");
				t.start();
				btnStart.setEnabled(false);

			}
		}

	}


	public void setClientNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public void showGameTable(boolean isEnable, List<Integer> mPlayerIDList) {
		
		for (Integer i : mPlayerIDList) {
			mPlayerPanelList.add(new CardPanel(i));
		}
		
		mMainGameFrameGUI.getContentPane().removeAll();
		gmaeTable = new GameTablePanel(mPlayerPanelList, false, p);
		mMainGameFrameGUI.setContentPane(gmaeTable);
		mMainGameFrameGUI.revalidate();

		System.out.println("Player Ready");
	}
	
	public void updateCard(Card c, int nodeID) {
		gmaeTable.updateCard(c, nodeID);
	}

}
