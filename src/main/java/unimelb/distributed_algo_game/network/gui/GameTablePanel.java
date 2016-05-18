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

// TODO: Auto-generated Javadoc
/**
 * This draws the table of cards for the game.
 *
 * @author Lupiya
 */
public class GameTablePanel extends JPanel {

	/** The m player card panel list. */
	// Initialize all the variables
	private List<CardPanel> mPlayerCardPanelList = null;

	/** The btn. */
	private JButton btn = null;

	/** The m get card button listener. */
	private GetCardButtonActionListener mGetCardButtonListener = null;

	/** The m draw button action listener. */
	private DrawButtonActionListener mDrawButtonActionListener = null;

	/** The m player. */
	private Player mPlayer = null;

	/** The m player panel list. */
	private List<CardPanel> mPlayerPanelList = null;

	/** The is dealer. */
	private boolean isDealer = false;

	/**
	 * Main constructor for this class.
	 *
	 * @param mPlayerIDList
	 *            the m player id list
	 * @param isDealer
	 *            the is dealer
	 * @param mPlayer
	 *            the m player
	 */
	public GameTablePanel(List<Integer> mPlayerIDList, boolean isDealer, Player mPlayer) {

		this.mPlayer = mPlayer;

		if (mPlayerIDList.size() == 0)
			throw new IllegalArgumentException();
		// this.mPlayerCardPanelList = mPlayerCardPanelList;

		this.isDealer = isDealer;
		initGUI(mPlayerIDList);

	}

	/**
	 * Initializes the table using the list of players.
	 *
	 * @param mPlayerIDList
	 *            the m player id list
	 */
	private void initGUI(List<Integer> mPlayerIDList) {

		this.removeAll();

		GridLayout gridLayoutGameTable = new GridLayout();
		// gridLayoutGameTable.setRows(3);
		// gridLayoutGameTable.setColumns(2);

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

	/**
	 * Creates an action listener for the draw card button.
	 *
	 * @author Lupiya
	 */
	final class DrawButtonActionListener implements ActionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
		 * ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			((DealerPlayer) mPlayer).dealerDrawnCard();
			// btn.setEnabled(false);
		}

	}

	/**
	 * Creates am action listener for the get card button.
	 *
	 * @author Lupiya
	 */
	final class GetCardButtonActionListener implements ActionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
		 * ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {

			System.out.println("GameTable, actionPerformed" + mPlayer.getName());
			((SlavePlayer) mPlayer).requestCardFromDealer();
			System.out.println("Requesting from " + mPlayer.getGameServerInfo().getIPAddress() + ":"
					+ mPlayer.getGameServerInfo().getPort());
			btn.setEnabled(false);

		}

	}

	/**
	 * Updates the table with the card drawn by a player.
	 *
	 * @param c
	 *            the c
	 * @param nodeID
	 *            the node id
	 */
	public void updateCard(Card c, int nodeID) {
		int pos = -1;
		int j = 0;
		for (CardPanel i : mPlayerCardPanelList) {
			if (i.getNodeID() == nodeID)
				pos = j;
			j++;
		}
		CardPanel panel = mPlayerCardPanelList.get(pos);

		panel.setParameters(c, true);
		this.repaint();

	}

	/**
	 * Resets the table when a new round begins.
	 */
	public void newRound() {

		for (CardPanel cPanel : mPlayerCardPanelList) {
			cPanel.setGameInProgress(false);
		}
		this.repaint();
		btn.setEnabled(true);
		// System.out.println(btn.isEnabled());

	}

	/**
	 * Updates the game table with the new list of players.
	 *
	 * @param mPlayerIDList
	 *            the m player id list
	 */
	public void updateGameTable(List<Integer> mPlayerIDList) {
		initGUI(mPlayerIDList);

	}

	/**
	 * Sets if the owner of the table is the dealer.
	 *
	 * @param isDealer
	 *            the new dealer
	 */
	public void setDealer(boolean isDealer) {
		this.isDealer = isDealer;
	}

	/**
	 * Sets the reference to the player that the table belongs to.
	 *
	 * @param mPlayer
	 *            the new player
	 */
	public void setPlayer(Player mPlayer) {
		System.out.println("1GameTable " + mPlayer.getName());
		this.mPlayer = mPlayer;
		System.out.println("2GameTable " + this.mPlayer.getName());
	}

}
