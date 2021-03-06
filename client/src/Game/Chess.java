package Game;
import java.util.List;
import java.util.Scanner;

import GUI.*;
import enums.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Chess extends Application{
	
	Stage stage;
	StageController stageController;
	MyClient client;
	
	public static void main(String argv[]) throws Exception{
		launch(argv);
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		String host = "127.0.0.1";
		int port = 10000;
		List<String> parameters = this.getParameters().getRaw();
		for (String string : parameters) {
			System.out.println(string);
		}
		if(parameters.size()>0) {
			host = parameters.get(0);
			try {
				port = Integer.valueOf(parameters.get(1));
			} catch(Exception e) {
				 System.out.println("Chybně zadané parametry");
				 System.out.println("1. parametr - adress");
				 System.out.println("2. parametr - port");
				 System.out.println("př.: java -jar UPS-1.0-SNAPSHOT.jar 127.0.0.1 10000");
				 return;
			}
		}
		System.out.println("Připojuji se na ip: " + host + " a port: " + port);
		
		stageController = new StageController(primaryStage);
		client = new MyClient(host, port, stageController);		
		
		client.getStageController().initLoginStage();
		client.getStageController().getStage().show();
		
		/*System.out.print("Enter your name: ");
		String clientName = sc.nextLine();*/
		
		/*
		client.sendMessage("CONNECT;OK;" + clientName + ";");
		
		while (client.getState() == ClientState.LOBBY){
			System.out.print("Vytvořit hru? [Y,N]");
			con = sc.nextLine();
			if (con.equals("Y")) {
				client.sendMessage("CREATE_MATCH;");
			}
			else if(con.equals("N")){
				System.out.println("Chcete se připojit do hry? [Y,N]");
				con=sc.nextLine();
				if(con.equals("Y")) {
					client.sendMessage("FIND_MATCH;");
					System.out.print("Zadej ID hry: ");
					con = sc.nextLine();
					client.sendMessage("JOIN_MATCH;" + con + ";");
				}
				else if(con.equals("N")) {
					System.out.println("Nechci vybrat hru");
				}
			}
		}*/
	}
	
	public Stage initLoginStage(Stage stage, MyClient client) {
		return stage;
	}
}
