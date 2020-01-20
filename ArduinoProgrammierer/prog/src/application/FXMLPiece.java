package application;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public abstract class FXMLPiece {	
	public Node parent;
	
	public FXMLPiece(String path) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
			loader.setController(this);
			loader.load();
		} catch (IOException e) {
			throw (new RuntimeException(e));
		}
	}
	
	abstract public Node getRoot();
}
