package upv.es.SRM.main;

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

public class Main implements MqttCallback {

	Date date;

	public static void main(String[] args) {
		String topic = "Planta1/Sensor/Data/#";
		int qos = 2;
		String broker = "tcp://localhost:1883";
		String clientId = "JavaAsyncSample";
		MemoryPersistence persistence = new MemoryPersistence();

		try {
			MqttAsyncClient sampleClient = new MqttAsyncClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			sampleClient.setCallback(new Main());
			System.out.println("Connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			System.out.println("Connected");
			Thread.sleep(1000);
			sampleClient.subscribe(topic, qos);
			System.out.println("Subscribed");
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

	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.err.println("delivery complete");
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("topic: " + topic);
		System.out.println("message: " + new String(message.getPayload()));

		System.out.println(message.getQos());

		date = new Date();
		// Formateamos fecha para mostrar y guardar el archivo con fecha y hora
		// --> formato largo "(E, dd-MMM-yyyy HH.mm.ss)"
		DateFormat hourdateFormat = new SimpleDateFormat("(dd-MM-yyyy 'at' HH.mm.ss)");

		// Para saber donde se ejecuta el .jar y de esta forma crear carpeta y
		// guardar ahi las imagenes.

		// System.out.println(System.getProperty("user.dir"));
		String ruta_final = System.getProperty("user.dir");

		// Podemos clasificar las fotos por fecha de dia y mes a�o. MEJORA!!!!
		File folder = new File(ruta_final + "/fotos");
		folder.mkdir();
		// System.out.println(folder);

		// C�digo para guardar la imagen en disco.
		// FileOutputStream fileOuputStream = new
		// FileOutputStream("C:/Users/Luis/foto"+hourdateFormat.format(date)+".png");

		FileOutputStream fileOuputStream = new FileOutputStream(
				folder + "/foto" + hourdateFormat.format(date) + ".png");
		fileOuputStream.write(message.getPayload());
		fileOuputStream.close();

	}

}