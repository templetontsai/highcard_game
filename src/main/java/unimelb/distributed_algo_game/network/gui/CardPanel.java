package unimelb.distributed_algo_game.network.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JPanel;

import unimelb.distributed_algo_game.pokers.Card;

// TODO: Auto-generated Javadoc
/**
 * This class draws the cards on the game table.
 *
 * @author Lupiya
 */
public class CardPanel extends JPanel {

	/** The message. */
	// Initialize all the variables
	private String message; // A message drawn on the canvas, which changes

	/** The big font. */
	private Font bigFont; // Font that will be used to display the message.

	/** The card images. */
	private Image cardImages; // Contains the image of all 52 cards

	/** The board x coords. */
	private int boardXCoords;

	/** The boards y coords. */
	private int boardsYCoords;

	/** The card passed. */
	private Card cardPassed;

	/** The game in progress. */
	private Boolean gameInProgress = false;

	/** The id label. */
	private JLabel idLabel;

	/** The node id. */
	private int nodeID = -1;

	/**
	 * Constructor creates fonts, sets the foreground and background colors and
	 * starts the first game. It also sets a "preferred size" for the panel.
	 * This size is respected when the program is run as an application, since
	 * the pack() method is used to set the size of the window.
	 *
	 * @param nodeID
	 *            the node id
	 */
	public CardPanel(int nodeID) {
		loadImage();
		setBackground(new Color(0, 120, 0));
		setForeground(Color.GREEN);
		bigFont = new Font("Serif", Font.BOLD, 15);
		setPreferredSize(new Dimension(15 + 4 * (15 + 79), 185));
		// setPreferredSize(new Dimension(300, 300));
		idLabel = new JLabel("Node" + nodeID + "\n");
		this.add(idLabel);
		this.nodeID = nodeID;
	} // end constructor

	/**
	 * Sets the game in progress boolean.
	 *
	 * @param gameInProgress
	 *            the new game in progress
	 */
	public void setGameInProgress(boolean gameInProgress) {
		this.gameInProgress = gameInProgress;
	}

	/**
	 * Load the image from the file "cards.png", which must be somewhere on the
	 * classpath for this program. If the file is found, then cardImages will
	 * refer to the Image. If not, then cardImages will be null.
	 */
	private void loadImage() {

		ClassLoader cl = CardPanel.class.getClassLoader();
		URL imageURL = cl.getResource("unimelb/distributed_algo_game/network/gui/cards.png");
		if (imageURL != null)
			cardImages = Toolkit.getDefaultToolkit().createImage(imageURL);
	}

	/**
	 * Paints the card on the table.
	 *
	 * @param g
	 *            the g
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (cardImages == null) {
			g.drawString("Error: Can't get card images!", 10, 30);
			return;
		}
		g.setFont(bigFont);
		// g.drawImage(cardImages, 0, 0, 79, 123, 158, 492, 237, 615, this);
		if (gameInProgress) {
			drawCard(g, cardPassed, boardXCoords, boardsYCoords);
		} else {
			drawCard(g, null, 79, 20);

		}

	}

	/**
	 * Set the dimensions of the card to be drawn.
	 *
	 * @param c
	 *            the c
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param gameState
	 *            the game state
	 */
	public void setParameters(Card c, int x, int y, boolean gameState) {
		boardXCoords = x;
		boardsYCoords = y;
		cardPassed = c;
		gameInProgress = gameState;
	}

	/**
	 * Sets the dimensions of the card to be drawn.
	 *
	 * @param c
	 *            the c
	 * @param gameState
	 *            the game state
	 */
	public void setParameters(Card c, boolean gameState) {
		boardXCoords = 79;
		boardsYCoords = 20;
		cardPassed = c;
		gameInProgress = gameState;
	}

	/**
	 * Draws the card on the table.
	 *
	 * @param g
	 *            the g
	 * @param card
	 *            the card
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	public void drawCard(Graphics g, Card card, int x, int y) {
		int cx; // x-coord of upper left corner of the card inside cardsImage
		int cy; // y-coord of upper left corner of the card inside cardsImage
		if (card == null) {
			cy = 4 * 123; // coords for a face-down card.
			cx = 2 * 79;
		} else {
			cy = 4 * 123; // coords for a face-down card.
			cx = 2 * 79;

			switch (card.getPattern()) {
			case Clubs:
				cx = (card.getCardRank().getCode() - 1) * 79;
				cy = 0;

				break;
			case Diamonds:
				cx = (card.getCardRank().getCode() - 1) * 79;
				cy = 123;

				break;
			case Hearts:
				cx = (card.getCardRank().getCode() - 1) * 79;
				cy = 2 * 123;

				break;
			case Spades:
				cx = (card.getCardRank().getCode() - 1) * 79;
				cy = 3 * 123;

				break;
			default:
				System.out.println("Unknown Pattern");

			}
		}

		g.drawImage(cardImages, x, y, x + 79, y + 123, cx, cy, cx + 79, cy + 123, this);
	}

	/**
	 * Returns the node ID of the card panel.
	 *
	 * @return the node id
	 */
	public int getNodeID() {
		return nodeID;
	}

}
