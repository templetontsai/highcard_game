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
import unimelb.distributed_algo_game.pokers.Card;
import unimelb.distributed_algo_game.token.Token;

public class MainGameLoginDealerPanel extends JPanel {

	private JTextField ipTextField = null;
	private JTextField portTextField = null;
	private JTextArea textArea = null;
	private int nodeID = -1;


	private JButton btnStart = null;
	private MainGameLoginDealerPanel self = null;
	private MainGameFrameGUI mMainGameFrameGUI = null;
	
	private StartButtonActionListerner mStartButtonActionListerner = null;
	private Card c = null;
	private Player p = null;
	private List<CardPanel> mPlayerPanelList = null;
	private GameTablePanel gameTable = null;

	public MainGameLoginDealerPanel(MainGameFrameGUI mainGameFrameGUI) {

		self = this;
		this.mMainGameFrameGUI = mainGameFrameGUI;

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


		
		mStartButtonActionListerner = new StartButtonActionListerner();
		
		
		btnStart = new JButton("Start");
		btnStart.addActionListener(new StartButtonActionListerner());
		
		btnStart.setBounds(18, 185, 117, 25);
		add(btnStart);

	
	


		textArea = new JTextArea();
		textArea.setBounds(304, 96, 122, 143);
		add(textArea);


		JLabel lblPlayerList = new JLabel("Player List");
		lblPlayerList.setBounds(304, 81, 122, 15);
		add(lblPlayerList);
		
		
		mPlayerPanelList = new ArrayList<CardPanel>();
	}

	final class StartButtonActionListerner implements ActionListener {
		public void actionPerformed(ActionEvent action) {
			// TODO get ip and port from textfield and set init server
			// socket

			/*String ipAddress = ipTextField.getText();
			String port = portTextField.getText();*/

			String ipAddress = "localhost";
			String port = "5000";
			if (!ipAddress.equals("") && !port.equals("")) {
				System.out.println(ipAddress + "-" + port);
				System.out.println("Dealer/Node0 Starts the game");
				String gamePlayerInfo[] = { Integer.toString(nodeID), ipAddress, port };
				// Initialize players
				p = new DealerPlayer("Dealer", new GamePlayerInfo(gamePlayerInfo), self);
				p.setDealer(true);
				p.play();
				textArea.append("Node" + nodeID + " is joined\n");
				// Initialize game token for mutex
				Token gameToken = new Token();
				// Initialize queue for mutex control
				ArrayList<Player> tokenRequests = new ArrayList<Player>();
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
	
	public void showGameTable(boolean isEnable,  List<Integer> mPlayerIDList) {
		
	
		
		mMainGameFrameGUI.getContentPane().removeAll();
		gameTable = new GameTablePanel(mPlayerIDList, true, p);	
		mMainGameFrameGUI.setContentPane(gameTable);
		mMainGameFrameGUI.revalidate();
		System.out.println("Dealer Ready");
	}

	

	public void updateCard(Card c, int nodeID) {
		gameTable.updateCard(c, nodeID);
	}
	
	public void declareWinner(int nodeID) {
		if(nodeID == this.nodeID)
			JOptionPane.showMessageDialog(null, "You win");
		else if(nodeID != -1)
			JOptionPane.showMessageDialog(null, "node" + nodeID + " has won");
		gameTable.newRound();
		
	}
	
	public void updateGameTable(List<Integer> mPlayerIDList) {
		gameTable.updateGameTable(mPlayerIDList);
	}

}
