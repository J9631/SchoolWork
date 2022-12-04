package application;

//Interface meant to implement the observer end of a observer-subject pattern
public interface LevelObserver {
	public void update(int switchCase, double score, double time);
}
