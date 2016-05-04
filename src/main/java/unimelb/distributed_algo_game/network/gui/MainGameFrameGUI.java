package unimelb.distributed_algo_game.network.gui;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainGameFrameGUI extends JFrame{
	private MainGamePanel mainPanel;
	public MainGameFrameGUI(String name) {
		super(name);
		init();
	}
	
	private void init() {
		mainPanel = new MainGamePanel();
		this.setContentPane(mainPanel);
		this.pack();
		this.setVisible(true);
	}
	
	

}
