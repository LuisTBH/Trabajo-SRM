package upv.es.SRM.controller;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.github.sarxos.webcam.Webcam;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Publisher_controller {

	@FXML
	private TextArea consola;

	@FXML
	private TextField status_field;

	@FXML
	private TextField server_uri_field;

	@FXML
	private Button btn_conectar;

	@FXML
	private Button btn_desconectar;

	@FXML
	private TextField clientID_field;

	@FXML
	private TextField topic_field;

	@FXML
	private ComboBox<?> qos_box;

	@FXML
	private TextField time_field;

	// Variables globales broker, clientID, topic, qos
	String broker;
	String clientId;
	String topic;
	int qos = 0;

	int contador_imagenes = 0;
	// La usaremos para mostar la ayuda 3 veces y no más sino es muy coñazo las
	// ventanas emergentes todo el rato.
	private int ayuda = 0;

	int fotos_totales = 0;

	Date date_inicial, date_final;

	int segundos_publicacion = 30;

	// Variables para archivos de log
	File archivo_log;
	FileWriter log;
	BufferedWriter bw;

	Timer timer;

	@FXML
	void accion(ActionEvent event) throws IOException {

		fotos_totales = 0;

		comprobar_fields();

		btn_desconectar.setDisable(false);
		btn_conectar.setDisable(true);

		timer = new Timer();

		TimerTask tarea_publicar = new TimerTask() {

			@Override
			public void run() {
				try {

					System.out.println("ESTE ES IMPORTANTE: " + timer.toString());
					iniciar();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}

			}
		};

		segundos_publicacion = segundos_publicacion * 1000;
		// Le decimos en milisegundos (30000 ms -> 30 segundos) cada cuanto
		// queremos que publique la imagenes el cliente.
		timer.scheduleAtFixedRate(tarea_publicar, 0, segundos_publicacion);
	}

	@FXML
	void desconectar(ActionEvent event) {
		timer.cancel();

		System.out.println("Paramos el timer y deberia coincidir: " + timer.toString());
		System.out.println("DEBERIA DESCONECTAR Y PARAR LA TAREA");
		status_field.setText("Disconnected");
		btn_conectar.setDisable(false);
		btn_desconectar.setDisable(true);
		consola.clear();
		consola.setText("Full Disconnected");

		// Si queremos mostrar el tiempo total de conexion sin que coincida con
		// el tiempo del nombre de la imagen.
		// date_final = new Date();

		DateFormat hourdateFormat_final = new SimpleDateFormat("dd/MM/yyyy 'Hour: 'HH:mm:ss");
		consola.appendText("\nStart: " + hourdateFormat_final.format(date_inicial) + "\nFinal: "
				+ hourdateFormat_final.format(date_final));

		consola.appendText("\nTotal Captured Images: " + fotos_totales);

		consola.appendText("\nPublished by " + clientId + " at " + topic);
		consola.appendText("\nPublished every " + segundos_publicacion / 1000 + " seconds");

		date_inicial = null;

	}

	// Metodo para hacer la captura de la imagen.
	void capturar_imagen() throws IOException {

		// get default webcam and open it
		Webcam webcam = Webcam.getDefault();

		webcam.setViewSize(new Dimension(640, 480));
		webcam.open();

		// get image
		BufferedImage image = webcam.getImage();

		// save image to PNG file
		ImageIO.write(image, "PNG", new File("captura.png"));
		webcam.close();

	}

	private void comprobar_fields() {

		if (server_uri_field.getText().isEmpty()) {
			if (ayuda < 1) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information Dialog");
				alert.setHeaderText("Necesitas indicar la dirección del broker.");
				alert.setContentText("Default: tcp://localhost:1883");

				alert.showAndWait();

			}

			broker = server_uri_field.getPromptText();

		} else {

			broker = server_uri_field.getText();
		}

		if (clientID_field.getText().isEmpty()) {

			if (ayuda < 1) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information Dialog");
				alert.setHeaderText("Necesitas indicar un ID para el cliente.");
				alert.setContentText("Default: JavaPublisherDefaultID");

				alert.showAndWait();
			}
			clientId = clientID_field.getPromptText();

		} else {

			clientId = clientID_field.getText();
		}

		if (topic_field.getText().isEmpty()) {

			if (ayuda < 1) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information Dialog");
				alert.setHeaderText("Necesitas indicar un topic para publicar.");
				alert.setContentText("Default: Planta0/Sensor/Data/");

				alert.showAndWait();

			}
			topic = topic_field.getPromptText();

		} else {

			topic = topic_field.getText();
		}

		if (time_field.getText().isEmpty()) {

			if (ayuda < 1) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information Dialog");
				alert.setHeaderText("Indica un tiempo de publicación.");
				alert.setContentText("Default: 30 seconds.");

				alert.showAndWait();

			}
			segundos_publicacion = Integer.parseInt(time_field.getPromptText());

		} else {

			segundos_publicacion = Integer.parseInt(time_field.getText());

		}

		ayuda++;

	}

	void registro_log(String fecha) throws IOException {

		archivo_log = new File("log.txt");

		if (!archivo_log.exists()) {
			log = new FileWriter(archivo_log);
			bw = new BufferedWriter(log);
			bw.write("LOG DEL CLIENTE DE MQTT PARA DISTRIBUIR IMAGENES");
			bw.newLine();
			bw.write("================================================");
			bw.newLine();
			bw.write("Publicación " + fotos_totales + ":");
			bw.newLine();
			bw.write("Connecting to broker: " + broker);
			bw.newLine();
			bw.write("Connected");
			bw.newLine();
			bw.write("Message Published by " + clientId + " in " + topic + " \n");
			bw.newLine();
			bw.write("Publishing every " + segundos_publicacion / 1000 + " seconds");
			bw.newLine();
			bw.write(fecha);

			bw.close();
			log.close();

		} else {

			log = new FileWriter(archivo_log, true);

			bw = new BufferedWriter(log);
			bw.newLine();
			bw.write("------------------------------------------------");
			bw.newLine();
			bw.write("Publicación " + fotos_totales + ":");
			bw.newLine();
			bw.write("Connecting to broker: " + broker);
			bw.newLine();
			bw.write("Connected");
			bw.newLine();
			bw.write("Message Published by " + clientId + " in " + topic + " \n");
			bw.newLine();
			bw.write("Publishing every " + segundos_publicacion / 1000 + " seconds");
			bw.newLine();
			bw.write(fecha);

			bw.close();
			log.close();
		}

	}

	void iniciar() throws IOException {

		Date date = new Date();

		if (date_inicial == null) {

			date_inicial = date;
		}

		MemoryPersistence persistence = new MemoryPersistence();

		capturar_imagen();

		File file = new File("captura.png");
		byte[] imagen = new byte[(int) file.length()];
		FileInputStream fichero = null;
		try {
			MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting to broker: " + broker);

			consola.clear();

			// FX textarea must be accessed on FX thread, for this
			// Platform.runLater is used.
			Platform.runLater(() -> consola.setText("Connecting to broker: " + broker + "\n"));

			Platform.runLater(() -> consola.appendText("Connected\n"));

			sampleClient.connect(connOpts);
			System.out.println("Connected");

			status_field.setText("Connected");

			fichero = new FileInputStream(file);
			fichero.read(imagen);
			fichero.close();

			fotos_totales++;

			MqttMessage message = new MqttMessage(imagen);
			message.setQos(qos);

			sampleClient.publish(topic, message);

			System.out.println("Message published by " + sampleClient.getClientId() + " in " + topic);

			Platform.runLater(() -> consola
					.appendText("Message Published by " + sampleClient.getClientId() + " in " + topic + " \n"));

			DateFormat hourdateFormat2 = new SimpleDateFormat("dd/MM/yyyy 'Hour: 'HH:mm:ss");
			System.out.println("Date: " + hourdateFormat2.format(date));

			Platform.runLater(() -> consola.appendText("Date: " + hourdateFormat2.format(date) + " \n"));

			Platform.runLater(() -> consola.appendText("Captured Images: " + fotos_totales + " \n"));
			// consola.appendText("Captured Images: " + fotos_totales + " \n");
			// Si queremos que coincida con el momento exacto para llevar mejor
			// control de las imagenes.
			date_final = new Date();

			Platform.runLater(
					() -> status_field.setText("Publishing every " + segundos_publicacion / 1000 + " seconds"));
			Platform.runLater(() -> consola.appendText("Disconnected Publisher"));
			sampleClient.disconnect();

			// AQUI CAPTURAMOS EL REGISTRO DEL LOG DEL PUBLICADOR
			registro_log(hourdateFormat2.format(date));

			// System.exit(0);

		} catch (MqttException me) {
			System.out.println("Reason: " + me.getReasonCode());
			consola.appendText("Reason: " + me.getReasonCode() + "\n");
			System.out.println("Msg: " + me.getMessage());
			consola.appendText("Msg: " + me.getMessage() + "\n");
			System.out.println("Loc: " + me.getLocalizedMessage());
			consola.appendText("Loc: " + me.getLocalizedMessage() + "\n");
			System.out.println("Cause: " + me.getCause());
			consola.appendText("Cause: " + me.getCause() + "\n");
			System.out.println("Excep: " + me);
			consola.appendText("Excep: " + me);
			me.printStackTrace();

		}

	}

}
