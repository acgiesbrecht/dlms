package com.chortitzer.cin.usi.dlms;

import java.io.InputStream;
import java.util.Properties;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class App extends Application {

//    public static EntityManagerFactory factory = Persistence.createEntityManagerFactory("pcb_PU");
    @Override
    public void start(Stage stage) throws Exception {

        //Image img = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/add.png"));
        Parent root = FXMLLoader.load(this.getClass().getResource("/fxml/FXMLDocument.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add((this.getClass().getResource("/com/panemu/tiwulfx/res/tiwulfx.css").toExternalForm()));//load tiwulfx.css
        stage.setScene(scene);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        InputStream resourceAsStream = this.getClass().getResourceAsStream("/version.properties");
        Properties prop = new Properties();
        prop.load(resourceAsStream);

        stage.setOnCloseRequest(e -> Platform.exit());

        stage.setTitle("DLMS Chortitzer " + prop.getProperty("project.version") + "." + prop.getProperty("project.build"));
        stage.show();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        launch(args);
    }
}
