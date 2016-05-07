package unimelb.distributed_algo_game.network.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import unimelb.distributed_algo_game.pokers.Card;


public class MainGamePlayDealerPanel extends JPanel implements ActionListener {

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
	MainGamePlayDealerPanel() {
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
		//drawCard(g, c, 158, 0);

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
