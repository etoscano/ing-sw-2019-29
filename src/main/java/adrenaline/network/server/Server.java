package adrenaline.network.server;

// SOCKET
import adrenaline.*;
import adrenaline.gameboard.GameBoard;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// RMI

public class Server {

    private static GameModel model;
    private static Action action;

    private static int currentPlayer = 0;
    private static boolean isFirstTurn = true;

    private static int time = 0;
    private static int connectionsCount = 0;
    private static int boardChosen = 0;
    private static boolean gameIsOn = false;

    // STRING LIST OF THE COLORS A PLAYER CAN CHOOSE AND LIST OF THOSE ALREADY CHOSEN
    private static List<String> possibleColors = Stream.of(Figure.PlayerColor.values())
            .map(Figure.PlayerColor::name)
            .collect(Collectors.toList());
    private static ArrayList<String> colorsChosen = new ArrayList<>();

    // All client names, so we can check for duplicates upon registration.
    private static ArrayList<String> names = new ArrayList<>();

    // The set of all the print writers for all the clients, used for broadcast.
    private static List<PrintWriter> writers = new ArrayList<>();


    private static ServerSocket serverSocket;
    private Server server;
    private ExecutorService threadPool;

    private ObjectOutputStream output;
    private ObjectInputStream input;

    public void setup(String[] args) throws Exception {
        time =Integer.parseInt(args[0]);
        possibleColors.remove("NONE");

        server = new Server();

        start();
    }

    public Server() {
        threadPool = Executors.newCachedThreadPool();
    }


    public static void start() {
        try {
            serverSocket = new ServerSocket(4321);
        } catch (IOException e) {
            e.printStackTrace();
            // Server non si avvia
        }
        System.out.println("SERVER ONLINE");
        while (true) {

            try {
                Socket socket = serverSocket.accept();

                new RequestHandler(socket).start();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
    public static class RequestHandler extends Thread {

        Figure.PlayerColor color;
        String nickname;

        private final transient Socket socket;

        private final transient ObjectInputStream inputStream;

        private final transient ObjectOutputStream outputStream;

        public RequestHandler(Socket socket) throws IOException {


            if(connectionsCount==3){
                Countdown c = new Countdown();
            }

            this.socket = socket;
            this.outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            this.outputStream.flush();
            this.inputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

        }

        @Override
        public void run() {
            try {
                try {

                    while (true) {
                        String msg = (String) inputStream.readObject();
                        System.out.println(msg);
                        clientLogin();


                        if(gameIsOn){
                            addPlayerToGame(nickname, color);

                            handleTurns();
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void sendToClient(String message) throws IOException {
            outputStream.writeObject(message);
            outputStream.flush();

        }
        public void sendListToClient(List<String> messages) throws IOException {
            outputStream.writeObject(messages);
            outputStream.flush();

        }

        public void clientLogin(){
            try {
                nickname = loginName();

                color = checkColor();

                chooseBoard(nickname); // it checks if is firstPlayer and asks board

                sendToClient("ACCEPTED");

                Server.connectionsCount++;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public String loginName() throws Exception {
            sendToClient("LOGIN");
            String name = (String)inputStream.readObject();
            // Keep requesting a name until we get a unique one.
            while (true) {
                if (name != null || !name.equals("null")) {
                    synchronized (names) {
                        if (!isBlank(name) && !names.contains(name)) {
                            names.add(name);
                            return name;
                        } else if (names.contains(name)) {
                            sendToClient("LOGIN");
                            name = (String)inputStream.readObject();
                        }
                    }
                }
            }
        }

        public Figure.PlayerColor checkColor() throws Exception {

            while (true) {
                String csv = String.join(", ", possibleColors);  // SEND POSSIBLE COLORS
                sendToClient("COLOR");
                sendToClient(csv);
                String color = (String)inputStream.readObject();
                if (color != null) {
                    synchronized (possibleColors) {
                        if (!possibleColors.isEmpty() && (possibleColors.contains(color.toUpperCase()) || possibleColors.contains(color))
                                && (colorsChosen.isEmpty() || (!colorsChosen.contains(color) && !colorsChosen.contains(color.toUpperCase())))) {
                            colorsChosen.add(color.toUpperCase());
                            possibleColors.remove(color.toUpperCase());
                            return Figure.PlayerColor.valueOf(color.toUpperCase());
                        } else if ((!possibleColors.contains(color.toUpperCase()) || !possibleColors.contains(color))
                                && !colorsChosen.contains(color.toUpperCase()) && !colorsChosen.contains(color)) {
                            //  ASK AGAIN FOR COLOR, IT WASN'T ACCEPTED

                        } else if (colorsChosen.contains(color.toUpperCase()) || colorsChosen.contains(color)) {
                            //  ASK AGAIN FOR COLOR, IT WAS A DUPLICATE COLOR
                        }
                    }
                }
            }
        }
        public void addPlayerToGame(String name, Figure.PlayerColor color){
            model.addPlayer(new Player(name, color));

            for (PrintWriter writer : writers) {
                writer.println("MESSAGE" + name + " has joined");
            }
        }

        public void chooseBoard(String name) throws Exception {
            if(name.equals(names.get(0))){

                while(true){
                    int result = 0;
                    sendToClient("BOARD");
                    result = (int)inputStream.readObject();
                    if (result == 1 || result == 2 || result == 3 || result == 4) {
                        Server.setBoardChosen(result);
                        System.out.println("BOARD CHOSEN " + result);
                        boardChosen = result;
                        break;
                    } else {
                        //  ASK AGAIN BECAUSE NOT ACCEPTED
                    }

                }
            }
        }
        ////

        public void handleTurns(){
            while(!Server.action.endOfTheGame(model.getMapUsed().getGameBoard())){
                if(Server.model.getPlayers().get(currentPlayer).getName().equals(nickname)){
                    try {
                        if(isFirstTurn){
                            sendToClient("YOURFIRSTTURN");
                            firstTurn();
                        }
                        sendToClient("YOURTURN");
                        String choice = (String) inputStream.readObject();
                        switch (choice) {
                            case "G":
                                //grab(player);
                                break;
                            case "R":
                                playerRun();
                                break;
                            case "S":
                                //shoot(player);
                                break;
                            case "M":
                                // MAP
                                break;
                            case "B":
                                // PLAYER
                                break;
                            case "C":
                                // OTHER PLAYERS
                                break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // END OF TURN
                    nextPlayer();
                }
            }
        }

        public void firstTurn(){
                LinkedList<PowerUpCard> twoCards= new LinkedList<>();
                twoCards.add(model.powerUpDeck.deck.removeFirst());
                twoCards.add(model.powerUpDeck.deck.removeFirst());
                List<String> cards = new LinkedList<>();
                cards.add(0,twoCards.get(0).toString());
                cards.add(1,twoCards.get(1).toString());
            try {
                sendListToClient(cards);
                int x =(int)inputStream.readObject();
                // CHIEDI QUALE CARTA DA TENERE (1 o 2) E QUALE DA USARE COME RESPAWN

                if(x==1){
                    Server.model.getPlayers().get(currentPlayer).getPowerUp().add(twoCards.removeFirst());
                    setInitialPosition(twoCards.removeFirst().getPowerUpColor(), Server.model.getPlayers().get(currentPlayer));
                }else {
                    setInitialPosition(twoCards.removeFirst().getPowerUpColor(), Server.model.getPlayers().get(currentPlayer));
                    Server.model.getPlayers().get(currentPlayer).getPowerUp().add(twoCards.removeFirst());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * Updates index of next Player.
         * If everybody has played, it resets.
         */
        public static void nextPlayer(){
            currentPlayer++;
            if(currentPlayer==model.getPlayers().size()) {
                currentPlayer = 0;
                isFirstTurn= false;
            }
        }

        public void setInitialPosition(AmmoCube.CubeColor c, Player p){
            for (Room r: model.getMapUsed().getGameBoard().getRooms()){
                if(!r.getSpawnpoints().isEmpty() && r.getSpawnpoints().get(0).getColor().equals(c)){
                   CoordinatesWithRoom c1 = new CoordinatesWithRoom(r.getSpawnpoints().get(0).getSpawnpointX(),r.getSpawnpoints().get(0).getSpawnpointY(),r);
                    p.setPlayerPosition(c1);
                    p.setPlayerPositionSpawnpoint(c1);
                }
            }
        };


        public void playerRun(){
            Player player = model.getPlayers().get(currentPlayer);
            LinkedList<CoordinatesWithRoom> cells = action.proposeCellsRun(player.getCoordinatesWithRooms());
            List<String> possibilities = new LinkedList<>();
            for(CoordinatesWithRoom c : cells){
                possibilities.add(c.toString());
            }
            try {
                sendListToClient(possibilities); // RITORNA 1 OPPURE 2 OPPURE 3 ....
                int x = (int)inputStream.readObject();
                x--;
                action.run(player,cells.get(x));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    } // REQUEST HANDLER



    // TIMER
    private static class Countdown{
        public Countdown(){
            final Timer timer = new Timer();
            try {
                timer.scheduleAtFixedRate(new TimerTask() {
                    int i = time;

                    public void run() {
                        System.out.println(i--);
                        if (i< 0 || connectionsCount<3 || connectionsCount==5 && colorsChosen.size()==5) {
                            if(i<0 || connectionsCount==5 && colorsChosen.size()==5) {
                                System.out.println("Game is starting...");

                                Server.gameIsOn = true;
                                Server.startGame();
                            // DO SOMETHING TO START THE GAME
                            }
                            else if(connectionsCount<3){
                                System.out.println("TIMER STOPPED: LESS THAN 3 CONNECTIONS");
                            }
                            timer.cancel();
                        }
                    }
                }, 0, 1000);

            } catch (Exception e) {
                //
            }

        }
    }





    public static boolean isBlank(String str) {
                int strLen;
                    if (str == null || (strLen = str.length()) == 0) {
                        return true;
                    }
                    for (int i = 0; i < strLen; i++) {
                        if ((Character.isWhitespace(str.charAt(i)) == false)) {
                            return false;
                       }
                    }
                    return true;
    }

    public static void setBoardChosen(int i){
        boardChosen = i;
    }

    public static void startGame(){
        model = new GameModel(GameModel.Mode.DEATHMATCH, GameModel.Bot.NOBOT,boardChosen);
        action = new Action(model);

        // TODO SAY number boardChosen TO EVERYBODY
    }



    public void grab(Player player){
        // IF MI DICE GRAB
      //  LinkedList<CoordinatesWithRoom> possibleCells = action.proposeCellsGrab(player.getCoordinatesWithRooms(),model.getMapUsed().getGameBoard());
        // CHIEDI QUALE, RETURN chosenCell
        CoordinatesWithRoom chosenCell = null;

        if(chosenCell.containsSpawnpoint(model)) {
            /*
            * controllo io se può pagarla + ricarica ma tu devi:
            * 0-controllare che ti dica o effetto base o effetto alt nient'altro altrimenti non funziona il metodo x
            * 1-controllare che abbia meno di 3 carte in mano x
            * 2-una volta raccolta devi rimuoverla dalle carte dello spawnpoint
            * 3-una volta finito cio setta la posizione del player nel punto in cui ha scelto di raccogliere
            * se scarta una carta rimettila nel deck x
            * */
            // cerca nel model se c'è uno spawnpoint lì o no
            //  TODO se spawn fai scegliere quale e mettila in card
            Spawnpoint s = chosenCell.getSpawnpoint(model);
            LinkedList<WeaponCard> weaponCards = s.getWeaponCards();
            LinkedList<PowerUpCard>powerUpCards=new LinkedList<>();
            // TODO SCEGLI CARTA, mettila in card
            WeaponCard card = null;
            //TODO CHIEDI EFFETTO BASE/ALT
            AmmoCube.Effect effect=null;
            //TODO CHIEDI METODO PAGAMENTO
            Action.PayOption payOption=null;  //payOption= Action.PayOption.AMMO OR   payOption= Action.PayOption.AMMOPOWER
            if(payOption.equals(Action.PayOption.AMMOPOWER)){
                //TODO CHIEDI CON CHE POWERUPS VUOLE PAGARE
                //powerUpCards.add ...
            }
            //TODO CONTROLLA SE PUO' PAGARE L'EFFETTO BASE O ALT SE NO ESCI E ANNULLA AZIONE
            if(!action.canPayCard(card,player,payOption,effect,powerUpCards))
                return;
            // TODO CONTROLLA SE PUO RACCOGLIERE ALTRIMENTI RICHIEDI DROP ARMA, METTILA IN DCARD
            if(!player.canGrabWeapon()){
                WeaponCard dCard=null;
                model.weaponDeck.getList().addLast(dCard);
                player.getHand().remove(dCard);
            }
            //TODO DOVRAI FARTI DARE UN NUMERO DALLE CARTE PER EFFECT&NUMBER
            int number=0;
            action.payAmmo(player,card,effect,number);

            // ADDING TO PLAYER'S HAND + REMOVE FROM SPAWNPOINT + CHANGE POSITION

            player.getHand().add(card);
            s.getWeaponCards().remove(card);
            action.run(player,card.getCoordinatesOnMap());

            //TODO INCREMENTA TURNO
        }
        else {
            //se non spawnpoint
            //raccolgo un AmmoTile
            /*
            * qui faccio direttamente io l'assegnazione della posizione del player  e rimuovo gia' l'ammotile
            * dalla mappa e ne aggiungo subito un altro
            * già incluso pescaggio power up
            * */
            action.grabTile(player, chosenCell);
            //TODO INCREMENTA TURNO
        }
    }



    public void shoot(Player player){
        /*
        * a meno di errori quando fai shoot controlla prima che la lista degli effetti pagati non sia nulla*/
        //TODO CHIEDI PAGAMENTO
        Action.PayOption payOption=null;
        LinkedList <EffectAndNumber> paidEffect=new LinkedList<>();
        LinkedList <PowerUpCard> powers=new LinkedList<>();
        int number;
        //SAVING PLAYER INITIAL POSITION IN CASE HE CAN'T SHOOT/HE DOESN'T SHOOT
        CoordinatesWithRoom positionBeforeShoot=player.getCoordinatesWithRooms();
        //TODO GIOCATORE SI VUOLE SPOSTARE PRIMA?
        boolean moves=true;
          if(moves==true){
              LinkedList<CoordinatesWithRoom> possibleCells= action.proposeCellsRunBeforeShoot(player);
              //TODO CHIEDI CELLA E METTILA IN PLAYER POSITION
              CoordinatesWithRoom playerPosition=null;
              //set new position
              action.run(player,playerPosition);
          }

       //TODO MANDA MANO WEAPON AL GIOCATORE
       //TODO SCEGLI ARMA DA USARE E METTILA IN weaponCard
       //TODO RICEVI UN NUMERO DA METODI CARTE
        WeaponCard weaponCard =null;
           number=0;
          //if weapon is already reloaded or reloaded alt, player can request other effect

        if(!weaponCard.getReloadAlt()&&!weaponCard.getReload()){
            //TODO CHIEDI SE VUOLE EFFETTO BASE O ALTERNATIVO
            AmmoCube.Effect effect=null;
            //send message and delete the action
            if(!effect.equals(AmmoCube.Effect.BASE)&&!effect.equals(AmmoCube.Effect.ALT))
                return;
            //TODO CHIEDI COME PAGARE
            //payOption= ...
            if(payOption.equals(Action.PayOption.AMMOPOWER))
                {
                    //TODO CHIEDI POWERUP

                }

            if(action.canPayCard(weaponCard,player,payOption,effect,powers))
                {
                   paidEffect.add(action.paidEffect(weaponCard,player,payOption,effect,powers,number));
                    if(effect.equals(AmmoCube.Effect.BASE))
                        weaponCard.setReload();
                    if(effect.equals(AmmoCube.Effect.ALT))
                        weaponCard.setReloadAlt(true);
                }
            else {
                action.run(player,positionBeforeShoot);
                return; // delete action plus send message
            }}
        //TODO VUOI ALTRI EFFETTI?
        boolean otherEffect=true;
        if(otherEffect){
        if(weaponCard.getReload()||weaponCard.getReloadAlt()) {
            //TODO CONTROLLA CHE L'EFFETTO NON SIA NE BASE NE ALT
            LinkedList<AmmoCube.Effect> effect = new LinkedList<>();
            LinkedList<Action.PayOption> payOptions = new LinkedList<>();
            //TODO PER OGNI EFFETTO CHIEDI METODO PAGAMENTO
            for (AmmoCube.Effect e : effect) {
                //chiedi pagamento
                //payOption.add ...
            }

            for (AmmoCube.Effect e : effect
            ) {
                //TODO BETTER
                if(payOptions.get(effect.indexOf(e)).equals(Action.PayOption.AMMOPOWER))
                {
                    //TODO CHIEDI POWER DA USARE
                    //powers.add
                    //TODO ad ogni giro togli il powerUps aggiunti dalla mano del player
                }
               if( !action.canPayCard(weaponCard,player,payOptions.get(effect.indexOf(e)),e,powers)){
                   //TODO RIAGGIUNGI POWERUPS NON USATI ALLA MANO DEL PLAYER E TOGLILI DA powers
               }
                    else {
                        player.getPowerUp().removeAll(powers);
                        paidEffect.add(action.paidEffect(weaponCard,player,payOptions.get(effect.indexOf(e)),e,powers,number));
               }

            }
        }
            //send possible target
            LinkedList<Object>targets=new LinkedList<>();

            // SOME EFFECTS REQUIRE A CHECK THAT TARGETS ARE DIFFERENT FROM EFFECT TO EFFECT
            // I HAVE TO PASS THE OLD TARGETS
            List<Object> pastTargets = null;

            for (EffectAndNumber e:paidEffect) {
                pastTargets = requestsForEveryWeapon(e, weaponCard, player, model.getMapUsed().getGameBoard(), model, pastTargets);
            }
            for (EffectAndNumber e: paidEffect
            ) {
                action.shoot(weaponCard,player,e,targets);
            }
        }


        weaponCard.setNotReload();
        weaponCard.setReloadAlt(false);
    }

    public List<Object> requestsForEveryWeapon(EffectAndNumber e, WeaponCard w, Player p, GameBoard g, GameModel model, List<Object> pastTargets){
        CoordinatesWithRoom playerPosition = p.getCoordinatesWithRooms();
        List<CoordinatesWithRoom> cells;
        List<Object> targets;
        switch (w.toString()){

            case "Cyberblade":
                if(e.getEffect()== AmmoCube.Effect.BASE || e.getEffect()== AmmoCube.Effect.OP2){
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    // ASK WHICH 1 TARGET TO DAMAGE, REMOVE THE OTHERS

                    // TODO CHECK TARGETS OP1 AND BASE DIFFERENTI (RITORNA IL GIOCATORE COLPITO)
                    // SE PASTTARGETS VUOTO OK, SE PIENO TARGET SCELTO DEVE ESSERE DIVERSO DA QUELLO

                    w.applyDamage(targets,p,e);
                }
                if(e.getEffect()== AmmoCube.Effect.OP1){
                    // TODO MOVE 1 SQUARE
                }
                break;

            case "Electroscythe":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                w.applyDamage(targets,p,e);
                break;

            case "Flamethrower":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                // TODO ASK PLAYER TO CHOSE ONE OR TWO SQUARES (CHECK FIRST DISTANT 1, SECOND DISTANT 2, SAME DIR)

                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);

                if(e.getEffect()== AmmoCube.Effect.BASE){
                    // TODO ASK 1 TARGET PER OGNI SQUARE (2 FORSE)
                }
                w.applyDamage(targets,p,e);
                break;

            case "Furnace":
                if(e.getEffect()== AmmoCube.Effect.BASE) {
                    LinkedList<Room> possibleRooms = new LinkedList<>();

                    for (int k = 0; k < g.getDoors().size(); k++) {
                        if (playerPosition.getX() == g.getDoors().get(k).getCoordinates1().getX() &&
                                playerPosition.getY() == g.getDoors().get(k).getCoordinates1().getY() &&
                                playerPosition.getRoom().getToken() == g.getDoors().get(k).getCoordinates1().getRoom().getToken()) {

                            possibleRooms.add(g.getDoors().get(k).getCoordinates2().getRoom());
                        }

                        if (playerPosition.getX() == g.getDoors().get(k).getCoordinates2().getX() &&
                                playerPosition.getY() == g.getDoors().get(k).getCoordinates2().getY() &&
                                playerPosition.getRoom().getToken() == g.getDoors().get(k).getCoordinates2().getRoom().getToken()) {

                            possibleRooms.add(g.getDoors().get(k).getCoordinates1().getRoom());

                        }
                    }

                    // TODO ASK PLAYER FOR A ROOM (DIFFERENT) - METTILA NELLA COORDINATA ROOM
                    // SEND PLAYER LIST OF ROOMS possibleRooms APPENA CREATA
                    CoordinatesWithRoom room = new CoordinatesWithRoom();

                    cells = w.getPossibleTargetCells(room, e, g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    w.applyDamage(targets,p,e);
                }
                if(e.getEffect()== AmmoCube.Effect.ALT) {
                    cells = w.getPossibleTargetCells(playerPosition,e,g);

                    // TODO ASK PLAYER WHICH TILE, GET ONE BACK IN THAT LIST

                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    w.applyDamage(targets,p,e);
                }
                break;

            case "GrenadeLauncher":
                if(e.getEffect()== AmmoCube.Effect.BASE) {
                    cells = w.getPossibleTargetCells(playerPosition, e, g);
                    targets = w.fromCellsToTargets(cells, playerPosition, g, p, model, e);
                    // TODO ASK QUALE TARGET COLPIRE (1)
                    w.applyDamage(targets, p, e);
                    // TODO ASK SE VUOLE MUOVERE IL TARGET DI 1
                }
                if(e.getEffect()== AmmoCube.Effect.OP1) {
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    // TODO ASK UNA CELLA ANCHE LA TUA
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    w.applyDamage(targets,p,e);
                }
                break;

            case "Heatseeker":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);

                // TODO ASK 1 TARGET
                w.applyDamage(targets,p,e);
                break;
            case "Hellion":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);

                // TODO ASK 1 TARGET - SAVE IT FOR LATER
                Object target = new Object();

                CoordinatesWithRoom c = ((Player)target).getCoordinatesWithRooms(); // COORD DEL TARGET
                List<CoordinatesWithRoom> newList = new LinkedList<>();
                newList.add(c); // IT HAS JUST COORD TARGET
                targets = w.fromCellsToTargets(newList,playerPosition,g,p,model,e);

                for(Object o : targets){
                    if(o instanceof Player && ((Player) o).getColor()==((Player) target).getColor()){
                        targets.remove(o);
                        break;
                    }
                }
                targets.add(0,target);  // TARGET HAS TO BE IN FRONT

                w.applyDamage(targets,p,e);
                break;

            case "LockRifle":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                // TODO ASK 1 TARGET TO DAMAGE

                // TODO CHECK TARGETS OP1 AND BASE DIFFERENTI (RITORNA IL GIOCATORE COLPITO COSì LO SALVI IN SHOOT)
                // SE PASTTARGETS VUOTO OK, SE PIENO TARGET SCELTO DEVE ESSERE DIVERSO DA QUELLO

                w.applyDamage(targets,p,e);
                break;

            case "MachineGun":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                // TODO ASK TO CHOOSE 1 OR 2 TARGETS
                // TODO CHECK TARGETS OP1 AND OP2 DIFFERENTI
                // SE PASTTARGETS VUOTO OK, SE PIENO TARGET SCELTO DEVE ESSERE DIVERSO DA QUELLO
                // TODO ASK TO SHOOT THE OTHER OR AND SOMEONE ELSE
                w.applyDamage(targets,p,e);
                break;

            case "PlasmaGun":
                if(e.getEffect()== AmmoCube.Effect.BASE || e.getEffect()== AmmoCube.Effect.OP2) {
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    // TODO ASK 1 TARGET (SAVE IT FOR THE OTHER EFFECT)
                    w.applyDamage(targets,p,e);
                }
                if(e.getEffect()== AmmoCube.Effect.OP1){
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    // TODO ASK WHERE TO MOVE
                    // MOVE
                }
                break;

            case "PowerGlove":
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    // TODO ASK 1 TARGET
                    w.applyDamage(targets,p,e);

                    CoordinatesWithRoom c0 = playerPosition; // SAVED PLAYER'S POSITION
                    // TODO MOVE PLAYER TO TARGET'S SQUARE

                // ALSO
                if(e.getEffect()== AmmoCube.Effect.ALT){
                    CoordinatesWithRoom c2 = c0.getNextCell(c0,playerPosition,g,false);
                    if(c2.getX()!=0){
                        // TODO ASK IF PLAYER WANTS TO MOVE THERE

                        // TO BE CONTINUED...
                        // POSSO MUOVERE SOLAMENTE IN ALT O DEVO PER FORZA COLPIRE GLI AVVERSARI NELLA MOSSA?????????
                    }
                }
                break;

            case "Railgun":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);

                // TODO ASK TO CHOOSE 1 (BASE) OR 1-2 (ALT) TARGETS, REMOVE OTHERS

                if(e.getEffect()== AmmoCube.Effect.ALT && targets.size()==2) {

                    // 2 TARGETS OF ALT EFFECT HAVE TO BE IN THE SAME DIRECTION!!!!!
                    // TODO ASK AGAIN IF THIS IS FALSE
                    playerPosition.checkSameDirection(((Player)targets.get(0)).getCoordinatesWithRooms(),((Player)targets.get(1)).getCoordinatesWithRooms(),10,g,false);
                }
                w.applyDamage(targets,p,e);
                break;

            case "RocketLauncher":
                if(e.getEffect()== AmmoCube.Effect.BASE) {
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    // TODO ASK 1 TARGET (SAVE IT - POSITION - FOR OP2 EFFECT)
                    w.applyDamage(targets,p,e);
                    // TODO ASK IF MOVE TARGET

                    // PRIMA BASE- SE NON VIENE PASSATO NULLA RITORNA IL GIOCATORE COLPITO
                    // PRIMA OP2- SE GLI PASSANO UN GIOCATORE ALLORA PRENDI LA SUA POS, METTILA IN CELLS TO TARGETS, FAI SCEGLIERE UN TARGET E COLPISCILO E MUOVI SE VUOI
                }
                if(e.getEffect()== AmmoCube.Effect.OP2) {
                    // PRIMA BASE- PRENDI POSIZIONE VECCHIA DI TARGET (PASSATA COME GIOCATORE)
                    // PRIMA OP2- (NULLA PASSATO) TODO SCEGLI UNA CELLA, COLPISCILI E POI RITORNA LA CELLA (COME GIOCATORE)
                }
                if(e.getEffect()== AmmoCube.Effect.OP1){
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    // TODO ASK WHERE TO MOVE
                    // MOVE
                }
                break;

            case "Shockwave":
                if(e.getEffect()== AmmoCube.Effect.BASE) {
                    cells = w.getPossibleTargetCells(playerPosition, e, g);
                    targets = w.fromCellsToTargets(cells, playerPosition, g, p, model, e);

                    // TODO ASK WHICH TARGETS TO DAMAGE - UP TO 3
                    boolean ok = false;
                    while (!ok) {
                        if ((targets.size() == 3 &&
                                !((Player) targets.get(0)).getCoordinatesWithRooms().equals(((Player) targets.get(1)).getCoordinatesWithRooms())
                                && !((Player) targets.get(0)).getCoordinatesWithRooms().equals(((Player) targets.get(2)).getCoordinatesWithRooms())
                                && !((Player) targets.get(1)).getCoordinatesWithRooms().equals(((Player) targets.get(2)).getCoordinatesWithRooms())) ||
                                (targets.size() == 2 &&
                                        !((Player) targets.get(0)).getCoordinatesWithRooms().equals(((Player) targets.get(1)).getCoordinatesWithRooms())
                                )) {
                            ok = true;
                        } else {
                            // TODO ASK AGAIN
                        }
                    }
                    w.applyDamage(targets, p, e);
                }
                if(e.getEffect()== AmmoCube.Effect.ALT) {
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    w.applyDamage(targets,p,e);
                }
                break;

            case "Shotgun":
                if(e.getEffect()== AmmoCube.Effect.BASE) {
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    // TODO CHOOSE 1 TARGET
                    w.applyDamage(targets,p,e);
                    // TODO ASK TO MOVE THAT TARGET 1 SQUARE
                }
                if(e.getEffect()== AmmoCube.Effect.ALT) {
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    // TODO CHOOSE 1 TARGET
                    w.applyDamage(targets,p,e);
                }
                break;

            case "Sledgehammer":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                // TODO CHOOSE 1 TARGET
                w.applyDamage(targets,p,e);

                if(e.getEffect()== AmmoCube.Effect.ALT) {
                    // TODO ASK TO MOVE THAT TARGET 0-1-2 SQUARE SAME DIRECTION
                    // FROM LIST
                    List<CoordinatesWithRoom> possibleCells = playerPosition.tilesSameDirection(2,g,false);
                }
                break;

            case "Thor":
                if(e.getEffect()== AmmoCube.Effect.BASE) {
                    cells = w.getPossibleTargetCells(playerPosition, e, g);
                    targets = w.fromCellsToTargets(cells, playerPosition, g, p, model, e);
                    // TODO ASK 1 TARGET
                    w.applyDamage(targets, p, e);
                    // RETURN THE TARGET (YOU'LL NEED TO CHECK THEY ARE DIFFERENT)
                }
                if(e.getEffect()== AmmoCube.Effect.OP1 || e.getEffect()== AmmoCube.Effect.OP2) {
                    // PUT HERE TARGET'S POSITION, GLIEL'HA PASSATA
                    CoordinatesWithRoom targetPos = new CoordinatesWithRoom();

                    cells = w.getPossibleTargetCells(targetPos, e, g);
                    targets = w.fromCellsToTargets(cells, playerPosition, g, p, model, e);
                    // TODO ASK 1 TARGET
                    w.applyDamage(targets, p, e);
                    // RETURN THE TARGET (YOU'LL NEED TO CHECK THEY ARE DIFFERENT)
                }
                break;

            case "TractorBeam":
                if(e.getEffect()== AmmoCube.Effect.BASE) {
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    // TODO ASK 1 TARGET
                    CoordinatesWithRoom c1 = new CoordinatesWithRoom(); // SALVACI LA POS DEL TARGET

                    // TODO ASK NUOVA POSIZIONE (QUESTA VOLTA NON PROPONGO NULLA)

                    // CHECK CELLS.CONTAINS(NEWPOS) &&
                    // CHECK possibleMoves.CONTAINS(NEWPOS)
                    // SE VA BENE MUOVI IL TARGET E APPLYDAMAGE, ALTRIMENTI CHIEDI ANCORA

                    List<CoordinatesWithRoom> possibleMoves;
                    possibleMoves = c1.oneTileDistant(g, false);
                    possibleMoves.addAll(c1.xTilesDistant(g, 2));
                    possibleMoves.add(c1);


                    w.applyDamage(targets,p,e);

                }
                if(e.getEffect()== AmmoCube.Effect.ALT) {
                    cells = w.getPossibleTargetCells(playerPosition,e,g);
                    targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                    // TODO CHOOSE 1 TARGET AND MOVE IT TO YOUR SQUARE
                    w.applyDamage(targets,p,e);
                }
                break;

            case "VortexCannon":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                // TODO CHOOSE A VORTEX

                CoordinatesWithRoom vortex = new CoordinatesWithRoom();
                List<CoordinatesWithRoom> list;
                list = vortex.oneTileDistant(g,false);
                list.add(vortex);

                targets = w.fromCellsToTargets(list,playerPosition,g,p,model,e);
                // TODO ASK FOR 1 TARGET IF BASE OR UP TO 2 IF OP1
                // CHECK DIFFERENTI DA QUELLI PASSATI
                // MOVE EVERY TARGET ON THE VORTEX

                w.applyDamage(targets,p,e);
                // RETURN THE TARGETS
                break;

            case "Whisper":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                // TODO CHOOSE 1 TARGET
                w.applyDamage(targets,p,e);
                break;

            case "Zx_2":
                cells = w.getPossibleTargetCells(playerPosition,e,g);
                targets = w.fromCellsToTargets(cells,playerPosition,g,p,model,e);
                // TODO ASK FOR 1 TARGET IF BASE OR UP TO 3 IF ALT
                w.applyDamage(targets,p,e);
                break;
        }
        return Collections.emptyList(); // SE NON DIVERSAMENTE SPECIFICATO
    }







}

