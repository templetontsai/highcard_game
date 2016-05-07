package unimelb.distributed_algo_game.network.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.HumanPlayer;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.pokers.Card;
import unimelb.distributed_algo_game.token.Token;

public class MainGameLoginDealerPanel extends JPanel {
	private JTextField ipTextField;
	private JTextField portTextField;
	private JTextArea textArea;
	private int nodeID = -1;

	private JButton btnPlay;
	private MainGameLoginDealerPanel self;
	private static int playerCount = 0;
	Card c;
	public MainGameLoginDealerPanel(final MainGameFrameGUI mainGameFrameGUI) {

		self = this;

		setLayout(null);

		ipTextField = new JTextField();
		ipTextField.setBounds(153, 52, 114, 19);
		add(ipTextField);
		ipTextField.setColumns(10);

		portTextField = new JTextField();
		portTextField.setBounds(153, 83, 114, 19);
		add(portTextField);
		portTextField.setColumns(10);

		JLabel lblNewLabel = new JLabel("Dealer IP Address");
		lblNewLabel.setBounds(12, 54, 181, 15);
		add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Dealer Port");
		lblNewLabel_1.setBounds(12, 81, 123, 15);
		add(lblNewLabel_1);

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent action) {
				// TODO get ip and port from textfield and set init server
				// socket
				textArea.append("arjun \n");
				textArea.append("arjun \n");
				textArea.append("arjun \n");
				String ipAddress = ipTextField.getText();
				String port = portTextField.getText();
				if (!ipAddress.equals("") && !port.equals("")) {
					System.out.println("Dealer/Node0 Starts the game");
					String gamePlayerInfo[] = { Integer.toString(nodeID), ipAddress, port };
					// Initialize players
					Player p = new HumanPlayer("Dealer", new GamePlayerInfo(gamePlayerInfo), self);
					p.setDealer(true);
					Thread t = new Thread(p);
					t.start();
					t.setName("Human Player");
					// Initialize game token for mutex
					Token gameToken = new Token();
					// Initialize queue for mutex control
					ArrayList<Player> tokenRequests = new ArrayList<Player>();
				}

			}
		});
		btnStart.setBounds(153, 214, 117, 25);
		add(btnStart);

		btnPlay = new JButton("Play");
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent action) {
				// TODO get ip and port from textfield and set init server
				// socket\
				System.out.println("inside play");
				mainGameFrameGUI.getContentPane().removeAll();

				CardPanel board = new CardPanel();
				// add(board, BorderLayout.CENTER);
				Player p=new HumanPlayer("arjun", new GamePlayerInfo(), self);
				p.setDealer(true);
				
				 c=p.getCard(4);
				 System.out.println(c.getPattern());
				 System.out.println(c.getPattern().getCode());
				 
				mainGameFrameGUI.setContentPane(board);// Adding to
														// content pane,
														// not to Frame
				mainGameFrameGUI.setSize(500, 500);
				mainGameFrameGUI.setVisible(true);
				mainGameFrameGUI.invalidate();
				mainGameFrameGUI.validate();
			 
				// mainGameFrameGUI.printAll(getGraphics());
			}
		});
		btnPlay.setBounds(153, 290, 117, 25);
		add(btnPlay);
		btnPlay.setVisible(false);

		textArea = new JTextArea();
		textArea.setBounds(304, 96, 122, 143);
		add(textArea);

		textArea.getDocument().addDocumentListener(new MyDocumentListener());

		JLabel lblPlayerList = new JLabel("Player List");
		lblPlayerList.setBounds(304, 81, 122, 15);
		add(lblPlayerList);
	}

	public void updatePlayerList(int nodeID) {
		textArea.append("Node" + nodeID + " is joined\n");
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	class MyDocumentListener implements DocumentListener {
		String newline = "\n";

		public void insertUpdate(DocumentEvent e) {
			updateLog(e, "inserted into");
		}

		public void removeUpdate(DocumentEvent e) {
			// updateLog(e, "removed from");
			// System.out.println("deleted someting");
		}

		public void changedUpdate(DocumentEvent e) {
			// Plain text components do not fire these events
			// System.out.println("updated someting");
			// updateLog(e, "update");
		}

		public void updateLog(DocumentEvent e, String action) {
			
			playerCount++;
			if (playerCount == 3) {
				System.out.println("button visible");
				btnPlay.setVisible(true);
			}

		}
	}

	private class CardPanel extends JPanel implements ActionListener {

		String message; // A message drawn on the canvas, which changes

		Font bigFont; // Font that will be used to display the message.

		Image cardImages; // Contains the image of all 52 cards

		/**
		 * Constructor creates fonts, sets the foreground and background colors
		 * and starts the first game. It also sets a "preferred size" for the
		 * panel. This size is respected when the program is run as an
		 * application, since the pack() method is used to set the size of the
		 * window.
		 */
		CardPanel() {
			loadImage();
			setBackground(new Color(0, 120, 0));
			setForeground(Color.GREEN);
			bigFont = new Font("Serif", Font.BOLD, 15);
			setPreferredSize(new Dimension(15 + 4 * (15 + 79), 185));
		} // end constructor

		/**
		 * Load the image from the file "cards.png", which must be somewhere on
		 * the classpath for this program. If the file is found, then cardImages
		 * will refer to the Image. If not, then cardImages will be null.
		 */
		private void loadImage() {
			ClassLoader cl = MainGameLoginDealerPanel.class.getClassLoader();
			// URL imageURL =
			// cl.getResource("/main/java/unimelb/distributed_algo_game/network/gui/cards.png");
			// if (imageURL != null)
			cardImages = Toolkit.getDefaultToolkit()
					.createImage("src/main/java/unimelb/distributed_algo_game/network/gui/cards.png");
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (cardImages == null) {
				g.drawString("Error: Can't get card images!", 10, 30);
				return;
			}
			g.setFont(bigFont);
			//g.drawImage(cardImages, 0, 0, 79, 123, 158, 492, 237, 615, this);
			drawCard(g, null, 0, 0);
			drawCard(g, null, 79, 0);
			drawCard(g, c, 158, 0);

		}
		public void drawCard(Graphics g, Card card, int x, int y) {
	         int cx;    // x-coord of upper left corner of the card inside cardsImage
	         int cy;    // y-coord of upper left corner of the card inside cardsImage
	         if (card == null) {
	            cy = 4*123;   // coords for a face-down card.
	            cx = 2*79;
	         }
	         else {
	            cx = (card.getPattern().getCode()-1)*79;
	           System.out.println(card.getPattern().getCode());
	           System.out.println(card.getPattern());
	            switch (card.getPattern()) {
	            case Clubs:    
	               cy = 0; 
	               break;
	            case Diamonds: 
	               cy = 123; 
	               break;
	            case Hearts:   
	               cy = 2*123; 
	               break;
	            default:  // spades   
	               cy = 3*123; 
	               break;
	            }
	         }
	         System.out.println("we got" +" "+x+","+y+","+(x+79)+","+(y+123)+","+cx+","+cy+","+(cx+79)+","+(cy+123));
	        // g.drawImage(cardImages,15, 15 ,250,250,this);
	         g.drawImage(cardImages,x,y,x+79,y+123,cx,cy,cx+79,cy+123,this);
	      }


		public void actionPerformed(ActionEvent evt) {
			String command = evt.getActionCommand();
			if (command.equals("Higher"))
				System.out.println("Higher");
			else if (command.equals("Lower"))
				System.out.println("Higher");
			else if (command.equals("New Game"))
				System.out.println("Higher");
		}

	}
}
