import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

public class TetrisModel {
	
	private TetrisView view;
	private TetrisController controller;
	
	private ArrayList<StatsObserver> statsObservers;
	private ArrayList<BlockObserver> blockObservers;
	
	private int[][] grid;

	private boolean gameInProgress = false;
			
	private Block currentBlock;
	private ArrayList<Block> nextBlocks;
	private Block heldBlock;
	private Block shadowBlock;
	
	private boolean alreadyHeld;
	private int sevenBag;
	private boolean[] sevenBagUsed;
	
	private TetrisStats gameStats;
	
	private Timer gameTimer;
	
	private boolean adaptive;
	
	public TetrisModel() {
		statsObservers = new ArrayList<StatsObserver>();
		blockObservers = new ArrayList<BlockObserver>();
	}
	
	public void setUpGame(int speed) {
		// clear the board
		grid = new int[21][10];
		// game is in progress
		gameInProgress = true;
		// create the seven bag
		// for every cycle of seven blocks, only one of each type can be created
		sevenBag = 0;
		sevenBagUsed = new boolean[8];
		// choose the starting permutation of blocks from the seven bag
		nextBlocks = new ArrayList<Block>();
		for (int i = 1; i <= 7; i++) {
			// keep randomizing until a block that hasn't been used is found
			int blockType = 0;
			do {
				blockType = (int)(Math.random() * 7) + 1;
			} while (sevenBagUsed[blockType]);
			// if this is the first random block, it is the current block
			if (i == 1)
				currentBlock = new Block(blockType);
			else
				nextBlocks.add(new Block(blockType));
			sevenBagUsed[blockType] = true;
		}
		// reset the seven bag for the next cycle
		for (int i = 1; i <= 7; i++) {
			sevenBagUsed[i] = false;
		}
		// there is no held block at the start of the game
		heldBlock = null;
		alreadyHeld = false;
		// the shadow block shows where the current block will land
		// mimics the current block's type, column position, and orientation
		shadowBlock = updatedShadowBlock();
		drawBlock(shadowBlock);
		// adaptive game mode starts with timer firing every second
		adaptive = (speed == 1000);
		// reset the game stats
		gameStats = new TetrisStats(0, 0, 0, "");
		if (adaptive) {
			gameStats.setLevel(1);
		}
		notifyStatsObservers();
		// start the timer with the given speed
		gameTimer = new Timer(speed, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBlock();
			}
		});
		gameTimer.start();
		
	}
	
	public void updateBlock() {
		// drop the block one row at a time, then create a new one
		if (currentBlock.isActive()) {
			dropBlock();
		} else {
			createNewBlock();
		}
		notifyBlockObservers();
	}
	
	public void gameOver() {
		gameStats.setMessage("GAME OVER!");
		gameInProgress = false;
		gameTimer.stop();
		gameTimer = null;
		notifyStatsObservers();
	}
	
	public boolean validPosition(Block b) {
		// check for out of bounds
		if (b.getBottomRow() > 20 || b.getLeftColumn() < 0 || b.getRightColumn() > 9) {
			return false;
		}
		
		// create a temporary grid without the current block, then add in the current block
		int[][] tempGrid = new int[21][10];
		for (int i = 0; i <= 20; i++) {
			for (int j = 0; j < 10; j++) {
				// if the current block occupies this square, skip
				if (b.getSquareAt(b.getRow() - i, b.getCol() - j) > 0) 
					continue;
				tempGrid[i][j] = grid[i][j];
			}
		}
		
		// check every square that the new block occupies and see if it conflicts with the temp grid
		for (int i = 0; i < b.getDimensions().length; i++) {
			for (int j = 0; j < b.getDimensions()[0].length; j++) {
				if (b.getSquareAt(i, j) > 0) {
					if (i + b.getRow() > 20 || j + b.getCol() > 9) {
						return false;
					}
					if (tempGrid[i + b.getRow()][j + b.getCol()] > 0) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	public void editBlockPosition(int newRow, int newCol) {
		eraseBlock(currentBlock);
		// simulate the move and see if it is legal or not
		if (validPosition(new Block(currentBlock.getBlockType(), 
				newRow, newCol, currentBlock.getDimensions()))) {
			currentBlock.setRow(newRow);
			currentBlock.setCol(newCol);
			eraseBlock(shadowBlock);
			shadowBlock = updatedShadowBlock();
			drawBlock(shadowBlock);
		}
		// the current movement can cause the block to become active again
		if (!currentBlock.hitBlock(grid)) {
			currentBlock.setActive(true);
		}

		drawBlock(currentBlock);
		notifyBlockObservers();
	}
	
	public void clearLines() {
		// calculate how many lines were cleared
		int currLinesCleared = 0;
		for (int i = 1; i <= 20; i++) {
			int count = 0;
			for (int j = 0; j < 10; j++) {
				if (grid[i][j] > 0) {
					count++;
				}
			}
			// if every square in this entire row is filled, clear the line
			if (count == 10) {
				currLinesCleared++;
				shiftRowsDown(i-1);
			}
		}
		// update the game statistics
		gameStats.setLinesCleared(gameStats.getLinesCleared() + currLinesCleared);
		
		int previousScore = gameStats.getScore();
		switch (currLinesCleared) {
			case 1: 
				gameStats.setScore(gameStats.getScore() + 100);
				gameStats.setMessage("SINGLE!");
				break;
			case 2:
				gameStats.setScore(gameStats.getScore() + 300);
				gameStats.setMessage("DOUBLE!!");
				break;
			case 3:
				gameStats.setScore(gameStats.getScore() + 500);
				gameStats.setMessage("TRIPLE!!!");
				break;
			case 4:
				gameStats.setScore(gameStats.getScore() + 800);
				gameStats.setMessage("TETRIS!!!!");
				break;
			default:
				break;
		}
		
		// update the level and speed for the adaptive game mode
		if (adaptive) {
			if (previousScore < 1000 && gameStats.getScore() >= 1000) {
				gameTimer.setDelay(500);
				gameStats.setLevel(2);
				gameStats.setMessage("LEVEL UP!");
			}
			if (previousScore < 2000 && gameStats.getScore() >= 2000) {
				gameTimer.setDelay(300);
				gameStats.setLevel(3);
				gameStats.setMessage("LEVEL UP!");
			} 
			if (previousScore < 3000 && gameStats.getScore() >= 3000) {
				gameTimer.setDelay(200);
				gameStats.setLevel(4);
				gameStats.setMessage("LEVEL UP!");
			}
			if (previousScore < 5000 && gameStats.getScore() >= 5000) {
				gameTimer.setDelay(100);
				gameStats.setLevel(5);
				gameStats.setMessage("LEVEL UP!");
			}
		}
		
		notifyStatsObservers();
	}
	
	public void shiftRowsDown(int lastRow) {
		// when lines are cleared, all the rows above move down
		for (int i = lastRow; i >= 1; i--) {
			for (int j = 0; j < 10; j++) {
				// copy from the square directly above the current one
				grid[i+1][j] = grid[i][j];
			}
		}
		notifyBlockObservers();
	}
	
	public boolean dropBlock() {
		// check if the block can be dropped
		if (currentBlock.hitBlock(grid)) {
			currentBlock.setActive(false);
			return false;
		}
		
		// redraw the block one row lower than it was before
		eraseBlock(currentBlock);
		currentBlock.setRow(currentBlock.getRow() + 1);
		drawBlock(currentBlock);
		
		// if the block has hit another block or the bottom of the grid, deactivate it
		// also check for completed lines
		if (currentBlock.hitBlock(grid)) {
			currentBlock.setActive(false);
			clearLines();
		}
		return true;
	}
	
	public void hardDrop() {
		eraseBlock(currentBlock);
		while (!currentBlock.hitBlock(grid)) {
			currentBlock.setRow(currentBlock.getRow() + 1);
		}
		drawBlock(currentBlock);
		currentBlock.setActive(false);
		clearLines();
		createNewBlock();
	}
	
	public void rotateBlock() {
		// check if the block can rotate
		if (!currentBlock.canRotate(grid)) 
			return;
		
		eraseBlock(currentBlock);
		currentBlock.rotate();
		
		// update the shadow block
		eraseBlock(shadowBlock);
		shadowBlock = updatedShadowBlock();
		drawBlock(shadowBlock);
		
		// the rotation can cause the block to become active again
		if (!currentBlock.hitBlock(grid)) {
			currentBlock.setActive(true);
		}
		drawBlock(currentBlock);
	}
	
	public void holdBlock() {
		// if a block has been held before, you cannot hold it again
		if (alreadyHeld)
			return;
		
		// cannot hold if current block is the same type as the held block
		if (heldBlock != null && heldBlock.getBlockType() == currentBlock.getBlockType())
			return;
		
		// if there is no held block, set the held block to the current block
		// otherwise, use the previously held block and save the current block as the new held block 
		eraseBlock(shadowBlock);
		if (heldBlock == null) {
			heldBlock = new Block(currentBlock.getBlockType());
			eraseBlock(currentBlock);
			createNewBlock();
		} else {
			// if the player has not already held the block, they switch it with the current block
			Block tempBlock = new Block(heldBlock.getBlockType());
			heldBlock = new Block(currentBlock.getBlockType());
			eraseBlock(currentBlock);
			currentBlock = tempBlock;
			alreadyHeld = true;
		}
		shadowBlock = updatedShadowBlock();
		drawBlock(shadowBlock);
		
		notifyBlockObservers();
	}
	
	public void createNewBlock() {
		// the current block is the first block in the nextBlocks list
		currentBlock = nextBlocks.get(0);
		currentBlock.setActive(true);
		nextBlocks.remove(0);
		alreadyHeld = false;
		
		// keep taking a random block until you find an unused one
		int blockType = 0;
		do {
			blockType = (int)(Math.random() * 7) + 1;
		} while (sevenBagUsed[blockType]);
		nextBlocks.add(new Block(blockType));
		sevenBagUsed[blockType] = true;
		sevenBag++;
		
		// if the cycle is complete, create a new cycle
		if (sevenBag == 7) {
			sevenBag = 0;
			for (int i = 1; i <= 7; i++) {
				sevenBagUsed[i] = false;
			}
		}
		
		// check for game over
		if (currentBlock.hitBlock(grid)) {
			// if the block occupies the first row, shift it up
			if (currentBlock.getBottomRow() == 1) {
				currentBlock.setRow(currentBlock.getRow() - 1);
			}
			// if it is not directly stacked on another block after the shift, move it back up one row
			if (!currentBlock.hitBlock(grid)) {
				currentBlock.setRow(currentBlock.getRow() + 1);
			}
			drawBlock(currentBlock);
			gameOver();
		} else {
			shadowBlock = updatedShadowBlock();
			drawBlock(shadowBlock);
		}
	}
	
	public Block updatedShadowBlock() {
		// same block dimensions, same column, same orientation
		Block shadow = new Block(currentBlock.getBlockType(), currentBlock.getOrientation(), true);
		shadow.setRow(currentBlock.getRow());
		shadow.setCol(currentBlock.getCol());
		// keep moving it down until it reaches the bottom or hits another block
		while (!shadow.hitBlock(grid)) {
			shadow.setRow(shadow.getRow() + 1);
		}
		return shadow;
	}
	
	public void eraseBlock(Block b) {
		int r1 = b.getRow();
		int c1 = b.getCol();
		int r2 = r1 + b.getDimensions().length;
		int c2 = c1 + b.getDimensions()[0].length;
		// clear the squares the block originally occupied
		for (int i = r1; i < r2; i++) {
			for (int j = c1; j < c2; j++) {
				// do not fill if the square is out of bounds or not occupied
				if (i < 0 || i > 20 || j < 0 || j >= 10) continue;
				if (b.getDimensions()[i-r1][j-c1] != 0)
					grid[i][j] = 0;
			}
		}
	}
	
	public void drawBlock(Block b) {
		int r1 = b.getRow();
		int c1 = b.getCol();
		int r2 = r1 + b.getDimensions().length;
		int c2 = c1 + b.getDimensions()[0].length;
		// fill in the squares that the block occupies
		for (int i = r1; i < r2; i++) {
			for (int j = c1; j < c2; j++) {
				// do not fill if the square is out of bounds or not occupied
				if (i < 0 || i > 20 || j < 0 || j >= 10 || 
						b.getDimensions()[i-r1][j-c1] == 0) continue;
				grid[i][j] = b.getDimensions()[i-r1][j-c1];
			}
		}
	}
	
	public void registerStatsObserver(StatsObserver observer) {
		statsObservers.add(observer);
	}
		
	public void registerBlockObserver(BlockObserver observer) {
		blockObservers.add(observer);
	}

	public void notifyStatsObservers() {
		for (StatsObserver observer : statsObservers) {
			observer.statsChanged();
		}
	}
	
	public void notifyBlockObservers() {
		for (BlockObserver observer : blockObservers) {
			observer.blocksChanged();
		}
	}
	
    public int blockAt(int row, int col) {
        return grid[row][col];
    }

    // getters and setters

	public int[][] getGrid() {
		return grid;
	}

	public void setGrid(int[][] grid) {
		this.grid = grid;
	}

	public boolean isGameInProgress() {
		return gameInProgress;
	}

	public void setGameInProgress(boolean gameInProgress) {
		this.gameInProgress = gameInProgress;
	}

	public Block getCurrentBlock() {
		return currentBlock;
	}

	public void setCurrentBlock(Block currentBlock) {
		this.currentBlock = currentBlock;
	}

	public ArrayList<Block> getNextBlocks() {
		return nextBlocks;
	}

	public void setNextBlocks(ArrayList<Block> nextBlocks) {
		this.nextBlocks = nextBlocks;
	}

	public Block getHeldBlock() {
		return heldBlock;
	}

	public void setHeldBlock(Block heldBlock) {
		this.heldBlock = heldBlock;
	}

	public TetrisStats getGameStats() {
		return gameStats;
	}
	
	public void setGameStats(TetrisStats gameStats) {
		this.gameStats = gameStats;
	}

}
