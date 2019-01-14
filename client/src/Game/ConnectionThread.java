package Game;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ConnectionThread extends Thread{
	private MyClient client;
	
	public ConnectionThread(MyClient client) throws IOException {
		this.client = client;
	}
	
	@Override
	public void run() {
		String msg;
		int i = 0;
		while(true) {
			if(client.isConnection()) {
				i = 0;
				client.setConnection(false);
				try {
					ConnectionThread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("Thread interrupted");
				}
			}
			else {
				break;
			}
		}
		while(true) {
			if(!client.isConnection()) {
				i++;
				client.getStageController().setStatus("Trying to reconnect " + i+"/120");
				try {
					ConnectionThread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("Thread Interrupted");
				}
				if(i == 120) {
					break;
				}
			}
			else this.run();
		}
		
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setHeaderText("Connection lost");
			alert.setContentText("You lost connection");
			alert.showAndWait();
			client.sendMessage("EXIT;");
			System.exit(0);
		});
		}
	}

