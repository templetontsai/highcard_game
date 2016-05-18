package unimelb.distributed_algo_game.network.gui;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// TODO: Auto-generated Javadoc
/**
 * This is the main GUI for the entire game that manages both the login panel
 * and the game tabel.
 *
 * @author Lupiya
 */
public class MainGameFrameGUI extends JFrame {

	/** The main panel. */
	// Initialize the main panel
	private MainGamePanel mainPanel;

	/**
	 * Consstructor for the GUI.
	 *
	 * @param name
	 *            the name
	 * @param nodeID
	 *            the node id
	 */
	public MainGameFrameGUI(String name, int nodeID) {
		super(name + ": Node" + nodeID);
		init(nodeID);
	}

	/**
	 * Sets the node ID.
	 *
	 * @param nodeID
	 *            the node id
	 */
	public void init(int nodeID) {
		// Sets a different login panel based on the node ID passed
		if (nodeID == 0) {

			mainPanel = new MainGamePanel(this, true);
			mainPanel.setNodeID(nodeID);

		} else if (nodeID > 0) {
			mainPanel = new MainGamePanel(this, false);
			mainPanel.setNodeID(nodeID);
		}

		this.setContentPane(mainPanel);
		this.setSize(650, 650);
		setResizable(false);
		this.setVisible(true);
	}

}
