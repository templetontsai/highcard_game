package unimelb.distributed_algo_game.network.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JButton;

class GameTablePanel extends JPanel {

	private List<CardPanel> mPlayerList = null;
	private JButton btn = null;
	private GetCardButtonActionListener mGetCardButtonListener = null;
	private DrawButtonActionListener mDrawButtonActionListener = null;
	public GameTablePanel(List<CardPanel> playerList) {
		setLayout(null);

		if(playerList.size() == 0)
			throw new IllegalArgumentException();
		mPlayerList = playerList;
		for(CardPanel cPanel : mPlayerList) {
			this.add(cPanel);
		}

	}
	
	public void setDealer(boolean isDealer) {
		
		btn = new JButton();
		btn.setBounds(166, 263, 117, 25);
		
		if(isDealer) {
			mDrawButtonActionListener = new DrawButtonActionListener();
			btn.setText("Draw");
		} else {
			mGetCardButtonListener = new GetCardButtonActionListener();
			btn.setText("Get Card");
		}
		
		add(btn);
	}
	
	final class DrawButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			
		}
		
	}
	
	final class GetCardButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			
		}
		
	}

}

