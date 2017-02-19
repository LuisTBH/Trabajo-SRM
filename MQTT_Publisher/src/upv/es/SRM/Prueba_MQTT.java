package upv.es.SRM;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
//import java.util.Timer;
//import java.util.TimerTask;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.github.sarxos.webcam.Webcam;

public class Prueba_MQTT {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String topic = "Planta1/Sensor/Data/";
		String content = "Message from MqttPublishSample";
		int qos = 2;
		String broker = "tcp://localhost:1883";
		String clientId = "JavaSample";
		MemoryPersistence persistence = new MemoryPersistence();

		Date date = new Date();
		// Timer timer = new Timer();

		Webcam webcam = Webcam.getDefault();

		webcam.setViewSize(new Dimension(640, 480));
		webcam.open();

		// get image
		BufferedImage image = webcam.getImage();

		// save image to PNG file
		ImageIO.write(image, "PNG", new File("captura.png"));
		webcam.close();

		File file = new File("captura.png");
		byte[] imagen = new byte[(int) file.length()];
		FileInputStream fichero = null;
		try {
			MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			System.out.println("Connected");
			System.out.println("Publishing message: " + content);

			fichero = new FileInputStream(file);
			fichero.read(imagen);
			fichero.close();

			MqttMessage message = new MqttMessage(imagen);
			message.setQos(qos);

			sampleClient.publish(topic, message);
			System.out.println("Message published");

			sampleClient.disconnect();
			System.out.println("Disconnected");

			DateFormat hourdateFormat = new SimpleDateFormat("(dd-MM-yyyy HH.mm.ss)");
			System.out.println("Hora y fecha: " + hourdateFormat.format(date));

			System.exit(0);

		} catch (MqttException me) {
			System.out.println("reason " + me.getReasonCode());
			System.out.println("msg " + me.getMessage());
			System.out.println("loc " + me.getLocalizedMessage());
			System.out.println("cause " + me.getCause());
			System.out.println("excep " + me);
			me.printStackTrace();
		}

	}

}
