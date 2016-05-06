package unimelb.distributed_algo_game.network.gui;

import javax.swing.JPanel;
import javax.swing.JTextField;

import unimelb.distributed_algo_game.player.AIPlayer;
import unimelb.distributed_algo_game.player.HumanPlayer;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.token.Token;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

public class MainGameLoginClientPanel extends JPanel {
	private JTextField nodeField;
	private JTextField ipTextField;
	private JTextField portTextField;
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

		JLabel lblNewLabel_0 = new JLabel("NODE ID");
		lblNewLabel_0.setBounds(12, 54, 181, 15);
		add(lblNewLabel_0);
		
		JLabel lblNewLabel = new JLabel("Client IP Address");
		lblNewLabel.setBounds(12, 81, 181, 15);
		add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Client Port");
		lblNewLabel_1.setBounds(12, 111, 123, 15);
		add(lblNewLabel_1);

		JButton btnStart = new JButton("Connect");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent action) {
				// TODO get ip and port from textfield and set init server
				// socket
				String nodeID = nodeField.getText();
				setClientNodeID(Integer.parseInt(nodeID));
				String ipAddress = ipTextField.getText();
				String port = portTextField.getText();
				if (!ipAddress.equals("") && !port.equals("")) {
					System.out.println("Client"+nodeID+" sending connection to dealer");

					Player p = new AIPlayer("AI 1", Integer.parseInt(nodeID));
					Thread t = new Thread(p);
					t.setName("AI Player Thread");
					t.start();
				}

			}
		});
		btnStart.setBounds(153, 214, 117, 25);
		add(btnStart);

	}

	public void setClientNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

}
