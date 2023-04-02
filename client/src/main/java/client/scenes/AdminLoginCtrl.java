package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Modality;

public class AdminLoginCtrl {
    @FXML
    private TextField psswdField;

    private ServerUtils server;
    private MainCtrl mainCtrl;

    @Inject
    public AdminLoginCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    public void login() {
        String psswd = "";
        boolean res = false;
        try {
            psswd = psswdField.getText();
            res = server.checkPsswd(psswd);
        }
        catch(Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setContentText("Error logging in: " + e.getMessage());
            alert.showAndWait();
        }

        if(res) {
            System.out.println("ok");
            //mainCtrl.showDashboard();
        }
    }
}
