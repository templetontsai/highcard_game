package unimelb.distributed_algo_game.network.gui;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainGameFrameGUI extends JFrame{
	private MainGamePanel mainPanel;
	private MainGameLoginDealerPanel mainLoginPanel;
	public MainGameFrameGUI(String name) {
		super(name);
		init();
	}
	
	private void init() {
		mainLoginPanel = new MainGameLoginDealerPanel();
		this.setContentPane(mainLoginPanel);
		this.setSize(500, 500);
		this.setVisible(true);
	}
	
	public void setNodeID(int nodeID) {
		mainLoginPanel.setNodeID(nodeID);
	}
	
	

}