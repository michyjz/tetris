import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class TetrisView extends JFrame {
	
	private TetrisModel model;
	private TetrisController controller;
	
	private CardLayout cardLayout;
	private JPanel mainPanel;
	private JPanel gamePanel;
	
	private TetrisGrid grid;
	
	private JButton[] difficultyButtons;
	private JButton mainMenuButton;
	
	private JPanel statsPanel;
	private Font mainFont;
	
	private int score = 0;
	private JLabel scoreLabel;
	
	private int linesCleared = 0;
	private JLabel linesClearedLabel;
	
	private int level = 0;
	private JLabel levelLabel;
		
	private int message = 0;
	private JLabel messageLabel;
	
	private Clip clip;
	private long clipTimePosition = 0;
	private boolean musicOn = true;
	private boolean showingShadow = true;

	public TetrisView(TetrisModel m) {
		// setup the JFrame with a CardLayout
		super("Tetris");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);	
		this.setContentPane(mainPanel);
		
		// create the menu panel, and add buttons onto the button panel
		MenuPanel menuPanel = new MenuPanel(new BorderLayout());
		mainPanel.add(menuPanel, "MenuPanel");
		
		// create the button panel
		JPanel buttonPanel = new JPanel();
		
		menuPanel.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(new JLabel("Choose your level of difficulty"), BorderLayout.NORTH);
		
		String[] buttonLabels = {"Easy", "Medium", "Hard", "Adaptive"};
		difficultyButtons = new JButton[buttonLabels.length];
		for (int i = 0; i < buttonLabels.length; i++) {
			difficultyButtons[i] = new JButton(buttonLabels[i]);
			buttonPanel.add(difficultyButtons[i]);
		}
		
		// create the game panel
		gamePanel = new JPanel(new BorderLayout());
		mainPanel.add(gamePanel, "GamePanel");
		
		// add the grid to the game panel
		grid = new TetrisGrid();
		gamePanel.add(grid, BorderLayout.CENTER);
		
		// add the stats panel to the game panel
		statsPanel = new JPanel(new GridLayout(5, 1));
		
		// within the stats panel, create the score, lines cleared, and message labels
		scoreLabel = new JLabel("SCORE: " + score);
		scoreLabel.setFont(new Font("Futura", Font.PLAIN, 24));
		statsPanel.add(scoreLabel);
		mainFont = new Font("Futura", Font.PLAIN, 24);
		
		linesClearedLabel = new JLabel("LINES CLEARED: " + linesCleared);
		linesClearedLabel.setFont(mainFont);
		statsPanel.add(linesClearedLabel);
		
		levelLabel = new JLabel();
		levelLabel.setFont(mainFont);
		statsPanel.add(levelLabel);
		
		messageLabel = new JLabel();
		messageLabel.setFont(mainFont);
		statsPanel.add(messageLabel);
		
		mainMenuButton = new JButton("Main Menu");
		statsPanel.add(mainMenuButton);
		mainMenuButton.setVisible(false);
		
		gamePanel.add(statsPanel, BorderLayout.WEST);
		
		// set the view to the main menu
		cardLayout.show(mainPanel, "MainMenu");
		mainPanel.setVisible(true);
		mainPanel.requestFocusInWindow();
		
		// attach the references to the model and controller
    	this.model = m;
    	this.controller = new TetrisController(model, this);
    	
    	// create the menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu musicMenu = new JMenu("Music");
		
		JMenuItem musicOn = new JMenuItem("On");
		JMenuItem musicOff = new JMenuItem("Off");
		musicOn.addActionListener(controller);
		musicOff.addActionListener(controller);
		musicMenu.add(musicOn);
		musicMenu.add(musicOff);
		
		JMenu shadowBlockMenu = new JMenu("Shadow Block");
		JMenuItem shadowShow = new JMenuItem("Show");
		JMenuItem shadowHide = new JMenuItem("Hide");
		shadowShow.addActionListener(controller);
		shadowHide.addActionListener(controller);
		shadowBlockMenu.add(shadowShow);
		shadowBlockMenu.add(shadowHide);
		
		menuBar.add(musicMenu);
		menuBar.add(shadowBlockMenu);
		this.setJMenuBar(menuBar);
    	
    } // end constructor
	
	// switches to a different panel
	public void showPanel(String panelName) {
		cardLayout.show(mainPanel, panelName);
	}
	
	// update the stats
	public void setStats(TetrisStats stats) {
		scoreLabel.setText("SCORE: " + stats.getScore());
		linesClearedLabel.setText("LINES CLEARED: " + stats.getLinesCleared());
		if (stats.getLevel() > 0) {
			levelLabel.setText("LEVEL " + stats.getLevel());
		} else {
			levelLabel.setText("");
		}
		messageLabel.setText(stats.getMessage());
		if (stats.getMessage().equals("GAME OVER!")) {
			stopMusic();
		}
		repaint();
	}
	
	public void setMainMenuButtonVisible(boolean visible) {
		mainMenuButton.setVisible(visible);
	}
	
	public void addActionListener(ActionListener listener) {
		for (JButton button : difficultyButtons) {
			button.addActionListener(listener);
		}
		mainMenuButton.addActionListener(listener);
	}
	
	@Override
	public void addKeyListener(KeyListener listener) {
		super.addKeyListener(listener);
		gamePanel.addKeyListener(listener);
	}
	
	private class MenuPanel extends JPanel {
		
		MenuPanel(BorderLayout b) {
			super(b);
			JLabel logo = null;  
			// try to load the image of the tetris logo
			try {
				Image i = ImageIO.read(new File("tetrislogo.png"));
				logo = new JLabel(new ImageIcon(i.getScaledInstance(450, 300, 0)));
			} catch (IOException e) {
				e.printStackTrace();
				logo = new JLabel("Tetris");
			}			
			this.add(logo);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
	
    private class TetrisGrid extends JPanel {
    	
    	TetrisGrid() {
	        setBackground(Color.WHITE);
    	}

        public void paintComponent(Graphics g) {
            // grid
            for (int row = 1; row <= 20; row++) {
                for (int col = 0; col < 10; col++) {
                	g.setColor(convertBlockToColor(model.blockAt(row, col)));
                	g.fillRect(col*30, (row-1)*30, 30, 30);
                }
            }
            
            // next blocks
        	g.setColor(Color.BLACK);
        	g.setFont(mainFont);
            g.drawString("NEXT", 330, 30);
            int currentY = 60;
    		for (Block b : model.getNextBlocks()) {
    			if (b.getBlockType() == Block.I_BLOCK)
    				currentY -= 30;
    			for (int i = 0; i < b.getDimensions().length; i++) {
    				for (int j = 0; j < b.getDimensions()[0].length; j++) {
    					Color c = convertBlockToColor(b.getSquareAt(i, j));
        				if (c == Color.BLACK) continue; 
    					g.setColor(c);
                		g.fillRect(330 + 30*j, currentY + 30*i, 30, 30);
    				}
    			}
    			currentY += 30 * (b.getDimensions().length);
    			if (b.getBlockType() == Block.O_BLOCK)
    				currentY += 30;
    			if (b.getBlockType() == Block.I_BLOCK)
    				currentY -= 30;
    		}
   		
    		// held block
    		g.setColor(Color.BLACK);
        	g.setFont(mainFont);
            g.drawString("HOLD", 470, 30);
    		if (model.getHeldBlock() != null) {
        		int[][] held = model.getHeldBlock().getDimensions();
        		currentY = 60;
        		if (model.getHeldBlock().getBlockType() == Block.I_BLOCK)
        			currentY -= 30;
        		for (int i = 0; i < held.length; i++) {
        			for (int j = 0; j < held[0].length; j++) {
        				Color c = convertBlockToColor(held[i][j]);
        				if (c == Color.BLACK) continue; 
    					g.setColor(c);
    					g.fillRect(470 + 30*j, currentY + 30*i, 30, 30);
        			}
        		}
    		} 
 
        }  // end paintComponent()

    } // end nested class TetrisGrid
    
    public Color convertBlockToColor(int blockType) {
    	switch (blockType) {
    		case Block.SHADOW_BLOCK:
    			if (showingShadow)
    				return Color.GRAY;
    			else
    				return Color.BLACK;
			case Block.I_BLOCK:
				return Color.CYAN;
			case Block.J_BLOCK:
				return Color.BLUE;
			case Block.L_BLOCK:
				return Color.ORANGE;
			case Block.O_BLOCK:
				return Color.YELLOW;
			case Block.S_BLOCK:
				return Color.GREEN;
			case Block.T_BLOCK:
				return Color.MAGENTA;
			case Block.Z_BLOCK:
				return Color.RED;
			default:
				return Color.BLACK;
    	}
    }
    
    // plays music when the game starts
    public void playMusic(String fileName) {
		try {
			File soundFile = new File(fileName); // Open an audio input stream from a wave File
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
			clip = AudioSystem.getClip(); // Get a sound clip resource.
			clip.open(audioIn); // Open audio clip and load samples from the audio input stream.
			clip.setMicrosecondPosition(0);
			clip.start();
			clip.loop(clip.LOOP_CONTINUOUSLY);
		} 
		catch (Exception e) { 
			e.printStackTrace(); 
		} 
	}
    
    // user clicks the music on option
    public void startMusic() {
    	if (musicOn)
    		return;
    	musicOn = true;
    	if (model.isGameInProgress()) {
    		if (clip != null) {
    			clip.setMicrosecondPosition(clipTimePosition);
		    	clip.start();
		    	clip.loop(clip.LOOP_CONTINUOUSLY);
    		} else {
    			playMusic("tetristheme.wav");
    		}
    	}
    }
    
    // user clicks the music off option
    public void muteMusic() {
    	if (!musicOn)
    		return;
    	musicOn = false;
    	stopMusic();
    	if (clip != null) {
    		clipTimePosition = clip.getMicrosecondPosition();
    	}
    }
    
    // the difference in this method is that the music will still play after a new game starts
    public void stopMusic() {
    	if (clip != null) {
    		clip.stop();
    		clip.flush();
    		clipTimePosition = 0;
    	}
    }
    
    public boolean isMusicOn() {
    	return musicOn;
    }
    
    public void setShowingShadow(boolean showingShadow) {
    	this.showingShadow = showingShadow;
    }

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public void setMainPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public JPanel getGamePanel() {
		return gamePanel;
	}

	public void setGamePanel(JPanel gamePanel) {
		this.gamePanel = gamePanel;
	}
    
}
