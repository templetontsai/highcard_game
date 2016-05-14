package unimelb.distributed_algo_game.network.gui;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainGameFrameGUI extends JFrame {
	private MainGamePanel mainPanel;
	private MainGameLoginDealerPanel mainLoginPanel;
	private MainGameLoginClientPanel mainLoginClientPanel;

	public MainGameFrameGUI(String name, int nodeID) {
		super(name + ": Node" + nodeID);
		init(nodeID);
	}

	public void init(int nodeID) {
		if (nodeID == 0) {

			mainLoginPanel = new MainGameLoginDealerPanel(this);
			mainLoginPanel.setNodeID(nodeID);

			this.setContentPane(mainLoginPanel);
			this.setSize(700, 700);
			setResizable(false);
			this.setVisible(true);
		} else if (nodeID > 0) {
			mainLoginClientPanel = new MainGameLoginClientPanel(this);
			mainLoginClientPanel.setClientNodeID(nodeID);
			this.setContentPane(mainLoginClientPanel);
			this.setSize(700, 700);
			setResizable(false);
			this.setVisible(true);
		}
	}


}
