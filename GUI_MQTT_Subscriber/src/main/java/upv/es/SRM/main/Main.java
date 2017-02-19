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
		/*A la hora de trabjar en eclipse hay que quitar el /view */
		/*Ponerlo para exportar*/
		Parent root = FXMLLoader.load(getClass().getResource("visor_imagenes.fxml"));
		Scene scene = new Scene(root);
		
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setTitle("MQTT Subscriber");
		primaryStage.setResizable(false);
		
		primaryStage.getIcons().add(new Image("mqtt_icon.png"));
		
		
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