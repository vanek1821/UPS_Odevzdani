package Game;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;

import enums.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


public class ReaderThread extends Thread{

	private BufferedReader br;
	private String cbuf;// = new char[1024];
	private MyClient client;
	
	public ReaderThread(Socket socket, MyClient client) throws IOException {
		this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.client = client;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				cbuf = br.readLine();
				if(cbuf!=null) {
					System.out.println("přijal jsem: " + new String(cbuf) );
					if (!Charset.forName("US-ASCII").newEncoder().canEncode(cbuf)){
						client.getSocket().close();
						Platform.runLater(()->{
							Alert alert = new Alert(AlertType.WARNING);
							alert.setHeaderText("Bad messages");
							alert.setContentText("Client recieved wrong messages from server");
							alert.showAndWait();
							System.exit(0);
						});
						break;
					}
					decodeMessage(cbuf);
				}
				else {
					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setHeaderText("Server failed");
			alert.setContentText("Something happened on server. ");
			alert.showAndWait();
			System.exit(0);
			return;
		});
	}
	
	public void decodeMessage(String cbuf) throws IOException {
		String message = new String(cbuf);
		
		String[] splitMes = message.split(";");
		
		if(splitMes[0].equals("PING") && client.getState()!=ClientState.START) {
			client.setConnection(true);
			client.sendMessage("PING;" + client.getName()+";");
		}
		else if(splitMes[0].equals("RECONNECT")) {
			if(splitMes[1].equals("LOBBY")) {
				client.setClientState(ClientState.LOBBY);
				Platform.runLater(() -> {
					client.getStageController().initLobbyStage();
					client.sendMessage("FIND_MATCH;");
					client.getStageController().setStatus("Reconnect Succesfull");
				});
			}
			else if(splitMes[1].equals("ROOM")) {
				Platform.runLater(() -> {
					client.setClientState(ClientState.ROOM);
					client.getStageController().initWaitStage();
					client.getStageController().setStatus("Reconnect Succesfull");
				});
			}
			else if(splitMes[1].equals("GAME")) {
				Platform.runLater(() -> {
					client.setClientState(ClientState.GAME);
					if(splitMes[2].equals("Y")) {
						client.setTurn(true);
						client.getStageController().setStatus("Your turn");
					}
					
					else {
						client.setTurn(false);
						client.getStageController().setStatus("Opponents turn");
					}
					client.getStageController().initGameStage();
					client.getStageController().clearBoard();
				});
			}
			else if(splitMes[1].equals("REPLAY")) {
				Platform.runLater(()-> {
					client.setClientState(ClientState.REPLAY);
					if(splitMes[2].equals("W")) client.getStageController().initResultStage(true);
					else client.getStageController().initResultStage(false);
					if(splitMes[3].equals("W")) client.setColor(Color.WHITE);
					else client.setColor(Color.BLACK);
				});
			}
		}
		else if(client.getState() == ClientState.START) {
			if(splitMes[0].equals("CONNECT")) {
				if(splitMes[1].equals("OK")) {
					System.out.println("Uživatel úspěšně připojen");
					client.setClientState(ClientState.LOBBY);
					Platform.runLater(() -> {
						client.getStageController().initLobbyStage();
						client.getStageController().setStatus("Login succesfull");
						client.sendMessage("FIND_MATCH;");
					});
				}
				else if(splitMes[1].equals("FAIL")) {
					System.out.println("Připojení uživatele se nezdařilo.");
				}
			}
		} 
		else if (client.getState() == ClientState.LOBBY ) {
			if(splitMes[0].equals("CREATE_MATCH")) {
				if(splitMes[1].equals("OK")) {
					System.out.println("Hra byla vytvořena");
					System.out.println("Vyčkejte na hráče");
					client.getStageController().setStatus("Wait for second player");
					client.setClientState(ClientState.ROOM);
					Platform.runLater(() -> {
						client.getStageController().initWaitStage();
					});
				}
			}
			else if (splitMes[0].equals("FOUND_MATCH")) {
				ObservableList<String> games = FXCollections.observableArrayList();
				if(splitMes.length>1) {
					String[] gameIDs = splitMes[1].split("-");
					for (String s: gameIDs) {
						if(!(s.isEmpty())) {
							games.add(s);
							System.out.println("Můžete se připojit ke hře s ID:" + s);
						}
					}
				}
				else {
						games.clear();
					}
				Platform.runLater(() ->{ 
					client.getStageController().gameList.setItems(games);
					client.getStageController().gameList.refresh();
					client.getStageController().setStatus("Matches found");
				});
			}
			else if (splitMes[0].equals("JOIN_MATCH")) {
				if(splitMes[1].equals("OK")) {
					System.out.println("Jste připojen do hry");
					client.setClientState(ClientState.GAME);
					Platform.runLater(() -> {
						System.out.println("Vytvarim gamestage");
						client.getStageController().initGameStage();
						client.setColor(Color.BLACK);
						client.setTurn(false);
						client.getStageController().setStatus("Opponents turn");
					});
				}
				else if(splitMes[1].equals("FAIL")) {
					System.out.println("Do hry se nelze připojit. Je buď obsazena nebo neexistuje.");
					client.getStageController().setStatus("Can`t connect to match. Full or doesn`t exist");
				}
			}
 		}
		else if (client.getState() == ClientState.ROOM) {
			if(splitMes[0].equals("JOIN_MATCH")) {
				System.out.println("Jste připojen do hry");
				client.setClientState(ClientState.GAME);
				client.setOpponentConnected(true);
				Platform.runLater(() -> {
					System.out.println("Vytvarim gamestage");
					client.getStageController().initGameStage();
					client.setColor(Color.WHITE);
					client.setTurn(true);
					client.getStageController().setStatus("Your turn");
				});
			}
		}
		else if(client.getState() == ClientState.GAME) {
			if(splitMes[0].equals("PAUSE")){
				client.getStageController().setStatus("Opponent disconnected. Wait.");
				client.setOpponentConnected(false);
			}
			if(splitMes[0].equals("BACK")) {
				client.getStageController().setStatus("Opponents reconnected");
				client.setOpponentConnected(true);
			}
			if(splitMes[0].equals("TURN")) {
				if(splitMes[1].equals("T")) client.setTurn(true);
				else if(splitMes[1].equals("F")) client.setTurn(false);
				
			}
			if(splitMes[0].equals("MOVE")) {
				if(splitMes[1].equals("OK")) {
					Platform.runLater(() -> {
						client.getStageController().move(new Move(Integer.valueOf(splitMes[2]),Integer.valueOf(splitMes[3]),Integer.valueOf(splitMes[4]),Integer.valueOf(splitMes[5])));	
					});
					if(client.isTurn()) {
						client.setTurn(false);
						client.getStageController().setStatus("Opponents turn");
					}
					else {
						client.setTurn(true);
						client.getStageController().setStatus("Your turn");
					}
				}
				else {
					client.getStageController().setStatus("Wrong move");
				}
			}
			else if(splitMes[0].equals("GAME")) {
				if(splitMes[1].equals("WIN")) {
					Platform.runLater(()->{
						client.getStageController().initResultStage(true);
						client.setClientState(ClientState.REPLAY);
						client.getStageController().setStatus("You won the game");
					});
				}
				else {
					Platform.runLater(()->{
						client.getStageController().initResultStage(false);
						client.setClientState(ClientState.REPLAY);
						client.getStageController().setStatus("You lost the game");
					});
				}
			}
			else if(splitMes[0].equals("YIELD")){
				client.setClientState(ClientState.LOBBY);
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setHeaderText("Yield");
					alert.setContentText("Your opponent left the game. You WIN");
					alert.setOnCloseRequest(event-> {
						client.getStageController().backToLobby();
						client.getStageController().setStatus("Matches found");
					});
					alert.show();
					return;
				});
			}
			else if(splitMes[0].equals("LEFT")) {
				Platform.runLater(() -> {
					client.setClientState(ClientState.LOBBY);
					Alert alert = new Alert(AlertType.WARNING);
					alert.setHeaderText("Disconnected");
					alert.setContentText("Your opponent was disconnected from server.");
					alert.setOnCloseRequest(event-> {
						client.getStageController().backToLobby();
						client.getStageController().setStatus("Matches found");
					});
					alert.show();
					return;
				});
			}
			else if(splitMes[0].equals("PIECE")) {
				String[] pieces = splitMes[1].split("-");
				Platform.runLater(() -> {
					client.getStageController().addPiece(pieces[0], pieces[1], pieces[2], pieces[3]);
				});
			}
		}
		else if(client.getState() == ClientState.REPLAY) {
			if(splitMes[0].equals("REPLAY")) {
				if(splitMes[1].equals("YES")) {
					client.setClientState(ClientState.GAME);
					if(client.getColor() == Color.WHITE) {
						client.setTurn(true);
					}
					else {
						client.setTurn(false);
					}
					Platform.runLater(()->{
						client.getStageController().initGameStage();
					});
					
				}
				else if(splitMes[1].equals("NO")) {
					client.setClientState(ClientState.LOBBY);
					client.setColor(null);
					client.setMove(null);
					Platform.runLater(()->{
						client.getStageController().initLobbyStage();
					});
				}
			}
		}
		else {
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.WARNING);
				alert.setHeaderText("Bad messages");
				alert.setContentText("Client recieved wrong messages from server");
				alert.showAndWait();
				System.exit(0);
			});
		}
		
		return;
	}

	
}
