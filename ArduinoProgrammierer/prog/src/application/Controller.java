package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class Controller extends FXMLPiece{

	
	
    public Controller() {
		super("/application/main.fxml");
		// TODO Auto-generated constructor stub
	}


	@FXML
    private BorderPane root;
	
	
	public Node getRoot() {
		return root;
	};
	
    @FXML
    private TextField PasswordTextField;

    @FXML
    private TextField SSIDTextFiield;

    @FXML
    private TextField IDTextField;

    @FXML
    void programClicked(ActionEvent event) {
    	sendCommand("setPass "+PasswordTextField.getText()+" \n");
    	sendCommand("setSSID "+SSIDTextFiield.getText()+" \n");
    	sendCommand("setID "+IDTextField.getText()+" \n");
    }
    
    
    public void sendCommand(String cmd) {
    	System.out.println(cmd);
    	
    };
}
