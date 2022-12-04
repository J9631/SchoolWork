package application;

//Interface meant to implement the subject end of a observer-subject pattern
public interface LevelSubject {
	public void subscribeObserver(LevelObserver o);
	public void notifyObservers(int switchCase);
}
