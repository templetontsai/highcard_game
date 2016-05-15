package unimelb.distributed_algo_game.network.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import unimelb.distributed_algo_game.player.AIPlayer;
import unimelb.distributed_algo_game.player.HumanPlayer;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.pokers.Card;

class GameTablePanel extends JPanel {

	private List<CardPanel> mPlayerCardPanelList = null;
	private JButton btn = null;
	private GetCardButtonActionListener mGetCardButtonListener = null;
	private DrawButtonActionListener mDrawButtonActionListener = null;
	private Player mPlayer = null;


	public GameTablePanel(List<CardPanel> mPlayerCardPanelList, boolean isDealer, Player mPlayer) {
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		this.mPlayer = mPlayer;

		if (mPlayerCardPanelList.size() == 0)
			throw new IllegalArgumentException();
		this.mPlayerCardPanelList = mPlayerCardPanelList;
		
		for (CardPanel cPanel : mPlayerCardPanelList) {

			this.add(cPanel);
		}
		btn = new JButton();
		if (isDealer) {
			mDrawButtonActionListener = new DrawButtonActionListener();
			btn.setText("Draw");
			btn.addActionListener(mDrawButtonActionListener);
		} else {
			mGetCardButtonListener = new GetCardButtonActionListener();
			btn.setText("Get Card");
			btn.addActionListener(mGetCardButtonListener);
		}

		this.add(btn);
		
		//this.revalidate();

	}

	final class DrawButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			((HumanPlayer) mPlayer).dealerDrawnCard();
			//btn.setEnabled(false);
		}

	}

	final class GetCardButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			((AIPlayer) mPlayer).requestCardFromDealer();
			btn.setEnabled(false);

		}

	}

	public void updateCard(Card c, int nodeID) {
		CardPanel panel = mPlayerCardPanelList.get(nodeID);
		panel.setParameters(c, true);
		this.repaint();
	

	}
	
	public void newRound() {
		
		
		for (CardPanel cPanel : mPlayerCardPanelList) {
			cPanel.setGameInProgress(false);
		}
		this.repaint();
		btn.setEnabled(true);
		//System.out.println(btn.isEnabled());
		
	}
}
