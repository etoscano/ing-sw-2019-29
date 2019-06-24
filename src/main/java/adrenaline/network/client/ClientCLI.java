package adrenaline.network.client;

// SOCKET
import adrenaline.Figure;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ClientCLI extends Client{

    static ClientCLI cli;
    public static Socket socket;

    public static Scanner scanner = new Scanner(System.in);

    private static boolean quit = false;

    private ObjectOutputStream output;

    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) throws Exception {


        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }
        String serverAddress = args[0];

        //String serverAddress = "192.168.43.171";//todo change every time
        int socketPort = 4321, rmiPort = 59002;

        cli = new ClientCLI(serverAddress, socketPort, rmiPort);

        new ClientThread(socket,cli).run();
    }

    public static void startClient(String serverAddress, int socketPort, int rmiPort){
        startSocketClient(serverAddress,socketPort);

    }

    public static void startSocketClient(String serverAddress, int socketPort){
        try {
            socket = new Socket(serverAddress,socketPort);
            System.out.println(ANSI_YELLOW +
                    "          _____  _____  ______ _   _          _      _____ _   _ ______ \n" +
                    "    /\\   |  __ \\|  __ \\|  ____| \\ | |   /\\   | |    |_   _| \\ | |  ____|\n" +
                    "   /  \\  | |  | | |__) | |__  |  \\| |  /  \\  | |      | | |  \\| | |__   \n" +
                    "  / /\\ \\ | |  | |  _  /|  __| | . ` | / /\\ \\ | |      | | | . ` |  __|  \n" +
                    " / ____ \\| |__| | | \\ \\| |____| |\\  |/ ____ \\| |____ _| |_| |\\  | |____ \n" +
                    "/_/    \\_\\_____/|_|  \\_\\______|_| \\_/_/    \\_\\______|_____|_| \\_|______|"+ ANSI_RESET);

            System.out.println("Socket Connection");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ClientCLI(String serverAddress, int socketPort, int rmiPort) {
        startClient(serverAddress,socketPort,rmiPort);

    }
    public void getOutput(ObjectOutputStream o){
        this.output=o;
    }

    public void sendToServer(String message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void sendIntToServer(int number){
        try {
            output.writeObject(number);
            output.flush();
        } catch (IOException e){
            e.printStackTrace();

        }
    }
    public void login(){
         System.out.println("Enter nickname: ");

          sendToServer(scanner.nextLine());

     }
    public String view() {
        return "CLI";
    }

    public void chooseColor(String possibleColors
     ){
         System.out.println("Choose a color: ");
         System.out.println(possibleColors);
        sendToServer(scanner.nextLine());

     }

    public void chooseBoard(){

         System.out.println("Choose board number: 1, 2, 3, 4");
         sendIntToServer(scanner.nextInt());
         scanner.nextLine();
     }


    public void printMessage(String message){
         System.out.println(message);
     }

     public void firstTurn(List<String> list){
         System.out.println("\nChoose one card to keep, the color of the other one will decide your respawn coordinates:");
         System.out.println(list.get(0) + " [1] [Default] or " + list.get(1) + " [2]\n");
         if(scanner.hasNextInt()){
             int x = scanner.nextInt();
             scanner.nextLine();
             if(x==2||x==1){
                 sendIntToServer(x);
             }else sendIntToServer(1);
         }else sendIntToServer(1);
     }

    public void showMainMenu() {
            System.out.println("Main Menu:\n" +
                    //"Z: Exit\n" +
                    "M: Show Map Info\n" +
                    "B: Show your player board\n" +
                    "C: Show other players' information\n" +
                            "S: Shoot [Default]\n" +
                            "G: Grab\n" +
                            "R: Run\n"
                    );
            String s = scanner.nextLine();
            s = s.trim().toUpperCase();
                switch (s) {
                    case "M":
                    case "B":
                    case "C":
                    case "S":
                    case "G":
                    case "R":
                        sendToServer(s);
                        break;
                    default:
                        sendToServer("R");
                        break;
                }

    }

    public static void back() {
        // GO BACK TO MAIN MENU
    }

    public void run(List<String> list) {
        System.out.println("\nChoose a cell (first is default):");
        for(int i=0; i<list.size();i++) {
            System.out.println("[" + (i + 1) + "]  " +list.get(i).toString());
        }
        if(scanner.hasNextInt()){
            int x = scanner.nextInt();
            scanner.nextLine();
            if(x>0 && x<list.size()+1){
                sendIntToServer(x);
            }else sendIntToServer(1);
        }else sendIntToServer(1);
    }

    public void grab(List<String> items,List<String> cells) {
        System.out.println("\nChoose an item cell (first is default):");
        for(int i=0; i<items.size();i++) {
            System.out.println("[" + (i + 1) + "]  " +items.get(i).toString()+" in "+cells.get(i).toString());
        }
        if(scanner.hasNextInt()){
            int x = scanner.nextInt();
            scanner.nextLine();
            if(x>0 && x<items.size()+1){
                sendIntToServer(x);
            }else sendIntToServer(1);
        }else sendIntToServer(1);
    }
    public void payment(){
        System.out.println("\nDo you want to pay with just ammo [1] or also with powerups [2]? ([1] is default): ");
        if(scanner.hasNextInt()){
            int x = scanner.nextInt();
            scanner.nextLine();
            if(x==1||x==2){
                sendIntToServer(x);
            }else sendIntToServer(1);
        }else sendIntToServer(1);
    }

    public void payWithPowerup(String powerup){
        System.out.println("\nDo you want to pay with this powerup, [Y] or [N]? ([Y] is default): "+powerup);
        if(scanner.hasNextLine()){
            String x = (String)scanner.nextLine();
            if(x.toUpperCase().equals("Y") ||x.toUpperCase().equals("N")){
                sendToServer(x);
            }else sendToServer("Y");
        }else sendToServer("Y");
    }

    public void grabWeapon(String weapons){
        String[] list = weapons.split(" ");
        System.out.println("\nChoose a weapon to grab (first is default):");
        System.out.println(list[0]+" [1] " +list[1]+" [2] "+list[2]+" [3]: ");
        if(scanner.hasNextInt()){
            int x = scanner.nextInt();
            scanner.nextLine();
            if(x>0 && x<4){
                sendIntToServer(x);
            }else sendIntToServer(1);
        }else sendIntToServer(1);
    }

    public void dropWeapon(List<String> weapons){
        System.out.println("\nChoose one of your weapons to drop (first is default):");
        for(int i =0;i<weapons.size();i++) {
            System.out.println(weapons.get(i) + " ["+i+1+"] ");
        }
        if(scanner.hasNextInt()){
            int x = scanner.nextInt();
            scanner.nextLine();
            if(x>0 && x<4){
                sendIntToServer(x);
            }else sendIntToServer(1);
        }else sendIntToServer(1);
    }
    public void chooseWeapon(List<String> weapons){
        System.out.println("\nChoose one of your weapons (first is default):");
        for(int i =0;i<weapons.size();i++) {
            System.out.println(weapons.get(i) + " ["+i+1+"] ");
        }
        if(scanner.hasNextInt()){
            int x = scanner.nextInt();
            scanner.nextLine();
            if(x>0 && x<4){
                sendIntToServer(x);
            }else sendIntToServer(1);
        }else sendIntToServer(1);
    }

    public void chooseTarget(List<String> weapons){
        System.out.println("\nChoose one target (first is default):");
        for(int i =0;i<weapons.size();i++) {
            System.out.println(weapons.get(i) + " ["+i+1+"] ");
        }
        if(scanner.hasNextInt()){
            int x = scanner.nextInt();
            scanner.nextLine();
            if(x>0 && x<4){
                sendIntToServer(x);
            }else sendIntToServer(1);
        }else sendIntToServer(1);
    }

    public void chooseAnother(){
        System.out.println("\nDo you want to choose another one, [Y] or [N]? ([Y] is default): ");
        if(scanner.hasNextLine()){
            String x = (String)scanner.nextLine();
            if(x.toUpperCase().equals("Y") ||x.toUpperCase().equals("N")){
                sendToServer(x);
            }else sendToServer("Y");
        }else sendToServer("Y");
    }
    public void chooseCell(List<String> weapons){
        System.out.println("\nChoose a cell (first is default):");
        for(int i =0;i<weapons.size();i++) {
            System.out.println(weapons.get(i) + " ["+i+1+"] ");
        }
        if(scanner.hasNextInt()){
            int x = scanner.nextInt();
            scanner.nextLine();
            if(x>0 && x<4){
                sendIntToServer(x);
            }else sendIntToServer(1);
        }else sendIntToServer(1);
    }


    public static void shoot() {
    }

    public static void showOtherPlayers() {
        // SHOW INFO ABOUT OTHER PLAYERS
    }

    public static void showPlayerBoard() {
        // SHOW INFO ABOUT PLAYER, CARDS ETC...
    }

    public static void showMapInfo() {
        // TODO PRINT BOARD OR SOMETHING
    }

    public void waitStart(){
        System.out.println("Waiting for other players...");
    }

    public static void printPlayerDetails(String playerName, int score, Figure.PlayerColor color) {
        System.out.println("Player: "+"\n"+"Name: " + playerName+"\n"+"Color: " + color+"\n"+"Score: " + score);
    }

}