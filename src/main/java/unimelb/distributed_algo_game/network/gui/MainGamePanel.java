package unimelb.distributed_algo_game.network.gui;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainGamePanel extends JPanel {
	public MainGamePanel() {
		setLayout(null);
		
		JButton btnPlay = new JButton("Dealer");
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnPlay.setBounds(29, 244, 117, 25);
		add(btnPlay);
		
		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnExit.setBounds(290, 244, 117, 25);
		add(btnExit);
		
		JButton btnNewButton = new JButton("Player");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton.setBounds(161, 244, 117, 25);
		add(btnNewButton);
	}
}
