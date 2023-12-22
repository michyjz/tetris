import java.awt.event.*;

public class TetrisController implements ActionListener, KeyListener, StatsObserver, BlockObserver {

	private TetrisModel model;
	private TetrisView view;
	
	public TetrisController(TetrisModel m, TetrisView v) {
		this.model = m;
		this.view = v;
		model.registerBlockObserver(this);
		model.registerStatsObserver(this);
		view.addActionListener(this);
		view.addKeyListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent ev) {
		String command = ev.getActionCommand();
		if (command.equals("Main Menu")) {
			view.showPanel("MenuPanel");
			view.setMainMenuButtonVisible(false);
			return;
		}
		if (command.equals("On")) {
			view.startMusic();
			return;
		}
		if (command.equals("Off")) {
			view.muteMusic();
			return;
		}
		if (command.equals("Show")) {
			view.setShowingShadow(true);
			view.repaint();
			return;
		}
		if (command.equals("Hide")) {
			view.setShowingShadow(false);
			view.repaint();
			return;
		}
		
		if (command.equals("Easy")) {
			doNewGame(800);
		} else if (command.equals("Medium")) {
			doNewGame(300);
		} else if (command.equals("Hard")) {
			doNewGame(100);
		} else if (command.equals("Adaptive")) {
			doNewGame(1000);
		}
		view.showPanel("GamePanel");
        view.getGamePanel().requestFocusInWindow();
	}
    
    public void doNewGame(int speed) {
    	model.setUpGame(speed);
        model.setGameInProgress(true);
        view.showPanel("GamePanel");
        if (view.isMusicOn()) {
            view.playMusic("tetristheme.wav");
        }
        view.repaint();
    }
    
    @Override
	public void keyPressed(KeyEvent e) {
		// if game is not started, return
		if (!model.isGameInProgress())
			return;
		
		int code = e.getKeyCode();
		int row = model.getCurrentBlock().getRow();
		int col = model.getCurrentBlock().getCol();
		if (code == KeyEvent.VK_LEFT) {
			model.editBlockPosition(row, col - 1);
		} else if (code == KeyEvent.VK_RIGHT) {
			model.editBlockPosition(row, col + 1);
		} else if (code == KeyEvent.VK_DOWN) {
			model.editBlockPosition(row + 1, col);
		} else if (code == KeyEvent.VK_UP) {
			model.rotateBlock();
		} else if (code == KeyEvent.VK_SPACE) {
			model.hardDrop();
		} else if (code == KeyEvent.VK_C) {
			model.holdBlock();
		}
		view.repaint();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void blocksChanged() {
		view.repaint();
	}

	@Override
	public void statsChanged() {
		view.setStats(model.getGameStats());
		if (!model.isGameInProgress()) {
			view.setMainMenuButtonVisible(true);
		}
		view.repaint();
	}
}
