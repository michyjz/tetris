
public class Block {
	
	// constants of block types
	static final int 
				SHADOW_BLOCK = -1,
				I_BLOCK = 1,
				J_BLOCK = 2,
				L_BLOCK = 3,
				O_BLOCK = 4,
				S_BLOCK = 5,
				T_BLOCK = 6,
				Z_BLOCK = 7;
	
	private int blockType;
	
	private int row, col;
	
	private int[][] dimensions;
	
	private int orientation;
	
	private boolean active;
		
	public Block(int blockType) {
		// each block starts at the top row in the middle column
		this.blockType = blockType;
		this.row = 0;
		this.col = 4;
		orientation = 0;
		active = true;
		
		switch(blockType) {
			case I_BLOCK:
				dimensions = new int[4][4];
				dimensions[1][0] = blockType;
				dimensions[1][1] = blockType;
				dimensions[1][2] = blockType;
				dimensions[1][3] = blockType;
				break;
			case J_BLOCK: 
				dimensions = new int[3][3];
				dimensions[0][0] = blockType;
				dimensions[1][0] = blockType;
				dimensions[1][1] = blockType;
				dimensions[1][2] = blockType;
				break;
			case L_BLOCK:
				dimensions = new int[3][3];
				dimensions[0][2] = blockType;
				dimensions[1][0] = blockType;
				dimensions[1][1] = blockType;
				dimensions[1][2] = blockType;
				break;
			case O_BLOCK:
				dimensions = new int[2][2];
				dimensions[0][0] = blockType;
				dimensions[0][1] = blockType;
				dimensions[1][0] = blockType;
				dimensions[1][1] = blockType;
				break;
			case S_BLOCK:
				dimensions = new int[3][3];
				dimensions[0][1] = blockType;
				dimensions[0][2] = blockType;
				dimensions[1][0] = blockType;
				dimensions[1][1] = blockType;
				break;
			case T_BLOCK:
				dimensions = new int[3][3];
				dimensions[0][1] = blockType;
				dimensions[1][0] = blockType;
				dimensions[1][1] = blockType;
				dimensions[1][2] = blockType;
				break;
			case Z_BLOCK:
				dimensions = new int[3][3];
				dimensions[0][0] = blockType;
				dimensions[0][1] = blockType;
				dimensions[1][1] = blockType;
				dimensions[1][2] = blockType;
				break;
			default:
				// should be unreachable
				dimensions = new int[1][1];
				break;
		}
	}
	
	// alternate constructor used to make custom blocks to test rotation
	public Block(int blockType, int row, int col, int[][] dimensions) {
		this(blockType);
		this.row = row;
		this.col = col;
		this.dimensions = dimensions;
	}
	
	// alternate constructor used to make the shadow block
	public Block(int blockType, int orientation, boolean shadow) {
		this(blockType);
		for (int i = 0; i < orientation; i++) {
			this.rotate();
		}
		if (shadow) {
			this.blockType = SHADOW_BLOCK;
			for (int i = 0; i < dimensions.length; i++) {
				for (int j = 0; j < dimensions[0].length; j++) {
					if (dimensions[i][j] > 0) {
						dimensions[i][j] = SHADOW_BLOCK;
					}
				}
			}
		}
	}
	
	// finds the row of the block closest to the bottom
	public int getBottomRow() {
		int bottom = 0;
		for (int i = 0; i < dimensions.length; i++) {
			for (int j = 0; j < dimensions[0].length; j++) {
				if (dimensions[i][j] == blockType) {
					bottom = i;
				}
			}
		}
		return row + bottom;
	}
	
	// finds the left-most column that the block occupies
	public int getLeftColumn() {
		int left = 0;
		for (int j = dimensions[0].length-1; j >= 0; j--) {
			for (int i = 0; i < dimensions.length; i++) {
				if (dimensions[i][j] == blockType) {
					left = j;
				}
			}
		}
		return col + left;
	}
	
	// finds the right-most column that the block occupies
	public int getRightColumn() {
		int right = 0;
		for (int j = 0; j < dimensions[0].length; j++) {
			for (int i = 0; i < dimensions.length; i++) {
				if (dimensions[i][j] == blockType) {
					right = j;
				}
			}
		}
		return col + right;
	}
	
	public boolean hitBlock(int[][] grid) {
		// check if the block is on the bottom of the grid
		if (getBottomRow() > 20) {
			return true;
		}
		// iterate by column first
		for (int j = 0; j < dimensions[0].length; j++) {
			// find the last index of this column that contains a square
			int colBottom = -1;
			for (int i = 0; i < dimensions.length; i++) {
				if (dimensions[i][j] == blockType) {
					colBottom = i;
				}
			}
			if (colBottom == -1) continue;
			// check if the square directly under this one is occupied
			if (row + colBottom + 1 > 20 || row + colBottom + 1 < 0
					|| grid[row + colBottom + 1][col + j] > 0) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canRotate(int[][] grid) {
		// create a temporary block that simulates the rotation on a temporary board
		int[][] tempDim = new int[dimensions.length][dimensions[0].length];
		for (int i = 0; i < dimensions.length; i++) {
			for (int j = 0; j < dimensions[0].length; j++) {
				tempDim[j][dimensions[0].length - i - 1] = dimensions[i][j];
			}
		}
		Block tempBlock = new Block(blockType, row, col, tempDim);
		if (tempBlock.getLeftColumn() < 0 || tempBlock.getRightColumn() > 9
				|| tempBlock.getBottomRow() >= 19)
			return false;
		
		// check if the rotation is possible
		for (int i = 0; i < dimensions.length; i++) {
			for (int j = 0; j < dimensions[0].length; j++) {
				// if this square was not previously occupied by this block
				// and the grid already has this square occupied, return false
				if (tempBlock.getSquareAt(i, j) == blockType && dimensions[i][j] != blockType 
						&& grid[i+row][j+col] > 0) {
					return false;
				}
			}
		}
				
		return true;
	}
	
	public void rotate() {
		int[][] tempDim = new int[dimensions.length][dimensions[0].length];
		for (int i = 0; i < dimensions.length; i++) {
			for (int j = 0; j < dimensions[0].length; j++) {
				tempDim[j][dimensions[0].length - i - 1] = dimensions[i][j];
			}
		}
		Block tempBlock = new Block(blockType, row, col, tempDim);
		
		// update the block info
		orientation = (orientation + 1) % 4;
		col = tempBlock.getCol();
		for (int i = 0; i < dimensions.length; i++) {
			for (int j = 0; j < dimensions[0].length; j++) {
				dimensions[i][j] = tempBlock.getSquareAt(i, j);
			}
		}
	}
	
	
	public int getBlockType() {
		return blockType;
	}

	public void setBlockType(int blockType) {
		this.blockType = blockType;
	}
	
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int[][] getDimensions() {
		return dimensions;
	}

	public void setDimensions(int[][] dimensions) {
		this.dimensions = dimensions;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public int getSquareAt(int r, int c) {
		if (r < 0 || c < 0 || r >= dimensions.length || c >= dimensions[0].length)
			return 0;
		return dimensions[r][c];
	}
	
	// for debugging purposes
	public void printBlock() {
		System.out.println("type: " + blockType);
		System.out.println("location: " + row + " " + col);
		for (int i = 0; i < dimensions.length; i++) {
			for (int j = 0; j < dimensions[0].length; j++) {
				System.out.print(dimensions[i][j]);
			}
			System.out.println();
		}
	}
}
