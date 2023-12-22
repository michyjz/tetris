
public class TetrisStats {

	private int score;
	private int linesCleared;
	private int level;
	private String message;
	
	public TetrisStats(int score, int linesCleared, int level, String message) {
		this.score = score;
		this.linesCleared = linesCleared;
		this.level = level;
		this.message = message;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getLinesCleared() {
		return linesCleared;
	}

	public void setLinesCleared(int linesCleared) {
		this.linesCleared = linesCleared;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
