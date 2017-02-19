package upv.es.SRM.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class Subscriber_Controller {

	@FXML
	private ImageView visor_imagenes;

	@FXML
	private Button btn_conectar;

	@FXML
	private TextField texto_prueba;

	@FXML
	private Text archivo_name;

	@FXML
	private TextField server_uri_field;

	@FXML
	private TextField clientID_field;

	@FXML
	private TextField topic_field;

	@FXML
	private TextField status_field;

	@FXML
	private TextArea consola;

	// Variables globales broker, clientID, topic, qos
	String broker;
	String clientId;
	String topic;
	int qos = 0;

	SubscriberMQTT subscriptor;

	Image imagen;

	@FXML
	void conectar_accion(ActionEvent event) throws InterruptedException {

		subscriptor = new SubscriberMQTT();
		subscriptor.iniciar();

	}

	private class SubscriberMQTT implements MqttCallback {

		Date date;

		Image imagen;

		public void iniciar() {

			if (server_uri_field.getText().isEmpty()) {

				broker = server_uri_field.getPromptText();
			} else {
				broker = server_uri_field.getText();
			}

			if (clientID_field.getText().isEmpty()) {

				clientId = clientID_field.getPromptText();
			} else {
				clientId = clientID_field.getText();
			}

			if (topic_field.getText().isEmpty()) {

				topic = topic_field.getPromptText();
			} else {
				topic = topic_field.getText();
			}

			qos = 2;

			// topic = "Planta0/Sensor/Data/#";
			// broker = "tcp://localhost:1883";
			// clientId = "JavaAsyncSample";

			MemoryPersistence persistence = new MemoryPersistence();

			try {
				MqttAsyncClient sampleClient = new MqttAsyncClient(broker, clientId, persistence);
				MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setCleanSession(true);
				sampleClient.setCallback(new SubscriberMQTT());
				System.out.println("Connecting to broker: " + broker);

				Platform.runLater(() -> consola.setText("Connecting to broker: " + broker + "\n"));

				sampleClient.connect(connOpts);
				System.out.println("Connected");

				Platform.runLater(() -> consola.appendText("Connected\n"));
				Platform.runLater(() -> status_field.setText("Connected\n"));

				Platform.runLater(() -> consola.appendText("Client Subscriber: " + clientId + "\n"));

				Thread.sleep(1000);
				sampleClient.subscribe(topic, qos);
				System.out.println("Subscribed");

				Platform.runLater(() -> consola.appendText("Subscribed to " + topic + "\n"));
				Platform.runLater(() -> status_field.setText("Subscribed"));

			} catch (Exception me) {
				if (me instanceof MqttException) {
					System.out.println("reason " + ((MqttException) me).getReasonCode());
				}
				System.out.println("msg " + me.getMessage());
				System.out.println("loc " + me.getLocalizedMessage());
				System.out.println("cause " + me.getCause());
				System.out.println("excep " + me);
				me.printStackTrace();
			}
		}

		public void connectionLost(Throwable arg0) {
			System.err.println("connection lost");
			Platform.runLater(() -> status_field.setText("connection lost"));

		}

		public void deliveryComplete(IMqttDeliveryToken arg0) {
			System.err.println("delivery complete");
		}

		public void messageArrived(String topic, MqttMessage message) throws Exception {

			date = new Date();
			// Formateamos fecha para mostrar y guardar el archivo con fecha y
			// hora --> formato largo "(E, dd-MMM-yyyy HH.mm.ss)"
			DateFormat hourdateFormat = new SimpleDateFormat("(dd-MM-yyyy 'at' HH.mm.ss)");

			DateFormat hourdateFormat2 = new SimpleDateFormat("dd/MM/yyyy 'Hour: 'HH:mm:ss");
			Platform.runLater(() -> consola
					.appendText("Imagen capturada y guardada.\nDate: " + hourdateFormat2.format(date) + "\n"));

			// Para saber donde se ejecuta el .jar y de esta forma crear carpeta
			// y guardar ahi las imagenes.

			String ruta_final = System.getProperty("user.dir");

			// Podemos clasificar las fotos por fecha de dia y mes año.
			// MEJORA!!!!
			File folder = new File(ruta_final + "/fotos");
			folder.mkdir();

			// Código para guardar la imagen en disco.
			// FileOutputStream fileOuputStream = new
			// FileOutputStream("C:/Users/Luis/foto"+hourdateFormat.format(date)+".png");
			String path = folder + "/Captura_foto " + hourdateFormat.format(date) + ".png";

			FileOutputStream fileOuputStream = new FileOutputStream(
					folder + "/Captura_foto " + hourdateFormat.format(date) + ".png");
			fileOuputStream.write(message.getPayload());
			fileOuputStream.flush();
			fileOuputStream.close();

			imagen = new Image("file:" + path);

			Double ancho = imagen.getWidth();
			Double alto = imagen.getHeight();

			visor_imagenes.setFitWidth(ancho);
			visor_imagenes.setFitHeight(alto);

			visor_imagenes.setImage(imagen);

		}

	}

}
