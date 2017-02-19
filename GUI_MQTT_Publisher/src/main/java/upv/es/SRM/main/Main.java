package upv.es.SRM.main;
import javafx.application.*;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.*;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		/*A la hora de trabajar en eclipse hay que quitar el /view */
		/*Ponerlo para exportar*/
		Parent root = FXMLLoader.load(getClass().getResource("GUI_Publisher.fxml"));
		Scene scene = new Scene(root);
		
		primaryStage.setScene(scene);
		
		primaryStage.setTitle("MQTT Publisher");
		primaryStage.setResizable(false);
		
		primaryStage.getIcons().add(new Image("mqtt_dash.png"));
		
		primaryStage.show();
		/*
		 * De esta forma sale el icono al exportar el .jar
		 * 
		 * primaryStage.getIcons().add(new Image("/view/mqtt_dash.png"));
		 * 
		 * */
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		      public void handle(WindowEvent we) {
		          System.out.println("Stage is closing");
		          System.exit(0);
		      }
		  }); 
	}
	
	public static void main(String[] args) {
		launch(args);
	} 
}
