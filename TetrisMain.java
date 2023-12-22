
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

public class TetrisMain {
	
	public static void main(String[] args) {        
        TetrisModel model = new TetrisModel();
        TetrisView view = new TetrisView(model);
        view.setSize(800, 650);
		view.setLocation(100, 100);
		view.setVisible(true);
		
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        view.setLocation( (screensize.width - view.getWidth())/2,
                (screensize.height - view.getHeight())/2 );
        view.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        view.setResizable(false);  
        view.setVisible(true);
        view.getGamePanel().requestFocusInWindow();
    }
}
