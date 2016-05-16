package unimelb.distributed_algo_game.network.gui;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainGameFrameGUI extends JFrame {
	private MainGamePanel mainPanel;



	public MainGameFrameGUI(String name, int nodeID) {
		super(name + ": Node" + nodeID);
		init(nodeID);
	}

	public void init(int nodeID) {
		if (nodeID == 0) {

			mainPanel = new MainGamePanel(this, true);
			mainPanel.setNodeID(nodeID);

			
		} else if (nodeID > 0) {
			mainPanel = new MainGamePanel(this, false);
			mainPanel.setNodeID(nodeID);
		}
		
		this.setContentPane(mainPanel);
		this.setSize(650, 650);
		setResizable(false);
		this.setVisible(true);
	}


}
