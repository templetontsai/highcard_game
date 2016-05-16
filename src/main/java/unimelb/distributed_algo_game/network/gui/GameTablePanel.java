package unimelb.distributed_algo_game.network.gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import unimelb.distributed_algo_game.player.DealerPlayer;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.player.SlavePlayer;
import unimelb.distributed_algo_game.pokers.Card;

class GameTablePanel extends JPanel {

	private List<CardPanel> mPlayerCardPanelList = null;
	private JButton btn = null;
	private GetCardButtonActionListener mGetCardButtonListener = null;
	private DrawButtonActionListener mDrawButtonActionListener = null;
	private Player mPlayer = null;

	private List<CardPanel> mPlayerPanelList = null;
	private boolean isDealer = false;

	public GameTablePanel(List<Integer> mPlayerIDList, boolean isDealer, Player mPlayer) {
		

		this.mPlayer = mPlayer;

		if (mPlayerIDList.size() == 0)
			throw new IllegalArgumentException();
		//this.mPlayerCardPanelList = mPlayerCardPanelList;

		this.isDealer = isDealer;
		initGUI(mPlayerIDList);
		

	}

	private void initGUI(List<Integer> mPlayerIDList) {
		
	
		this.removeAll();
		
		GridLayout gridLayoutGameTable = new GridLayout();
		//gridLayoutGameTable.setRows(3);
		//gridLayoutGameTable.setColumns(2);
		
		mPlayerCardPanelList = new ArrayList<CardPanel>(mPlayerIDList.size());
		for (Integer i : mPlayerIDList) {
			CardPanel p = new CardPanel(i);
			mPlayerCardPanelList.add(p);
			this.add(p);
		}
		
		
		
		
		btn = new JButton();
		if (this.isDealer) {
			mDrawButtonActionListener = new DrawButtonActionListener();
			btn.setText("Draw");
			btn.addActionListener(mDrawButtonActionListener);
		} else {
			mGetCardButtonListener = new GetCardButtonActionListener();
			btn.setText("Get Card");
			btn.addActionListener(mGetCardButtonListener);
		}
		
		this.add(btn);
		this.setLayout(gridLayoutGameTable);
		this.revalidate();
		this.repaint();
	}

	final class DrawButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			((DealerPlayer) mPlayer).dealerDrawnCard();
			// btn.setEnabled(false);
		}

	}

	final class GetCardButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			((SlavePlayer) mPlayer).requestCardFromDealer();
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
		// System.out.println(btn.isEnabled());

	}

	public void updateGameTable(List<Integer> mPlayerIDList) {
		initGUI(mPlayerIDList);
			
	}
	
	public void setDealer(boolean isDealer) {
		this.isDealer = isDealer;
	}


}
