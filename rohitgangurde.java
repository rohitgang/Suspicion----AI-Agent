import java.util.*;

/** This is the base class for computer player/bots.
 *
 */
public class rohitgangurde extends Bot
{
    Random r = new Random();
    HashMap<String, Piece> pieces; // Keyed off of guest name
    Board board;
    Piece me;
    HashMap<String, Player> players; // Keyed off of player name
    String otherPlayerNames[];
    HashMap<String, Integer> myGems;
    public static class Board
    {
        public Room rooms[][];
        public String gemLocations;

        public class Room
        {
            public final boolean gems[] = new boolean[3];
            public final String[] availableGems;
            public final int row;
            public final int col;
            private HashMap<String, Piece> pieces;

            public void removePlayer(Piece piece)
            {
                removePlayer(piece.name);
                piece.col=-1;
                piece.row=-1;
            }

            public void removePlayer(String name)
            {
                pieces.remove(name);
            }

            public void addPlayer(Piece piece)
            {
                piece.col=this.col;
                piece.row=this.row;
                pieces.put(piece.name, piece);
            }

            public Room(boolean red, boolean green, boolean yellow, int row, int col)
            {
                pieces = new HashMap<String, Piece>();
                this.row = row;
                this.col = col;
                gems[Suspicion.RED]=red;
                gems[Suspicion.GREEN]=green;
                gems[Suspicion.YELLOW]=yellow;
                String temp="";
                if(red) temp += "red,";
                if(green) temp += "green,";
                if(yellow) temp += "yellow,";
                availableGems = (temp.substring(0,temp.length()-1)).split(",");
            }
        }

        public void movePlayer(Piece player, int row, int col)
        {
            rooms[player.row][player.col].removePlayer(player);
            rooms[row][col].addPlayer(player);
        }

        public void clearRooms()
        {
            rooms=new Room[3][4];

            int x=0, y=0;
            boolean red, green, yellow;
            System.out.println(gemLocations);
            for(String gems:gemLocations.trim().split(":"))
            {
                if(gems.contains("red")) red=true;
                else red=false;
                if(gems.contains("green")) green=true;
                else green=false;
                if(gems.contains("yellow")) yellow=true;
                else yellow=false;
                rooms[x][y] = new Room(red,green,yellow,x,y);
                y++;
                x += y/4;
                y %= 4;
            }

        }

        public Board(String piecePositions, HashMap<String, Piece> pieces, String gemLocations)
        {
            Piece piece;
            this.gemLocations=gemLocations;
            clearRooms();
            int col=0;
            int row=0;
            for(String room:piecePositions.split(":",-1)) // Split out each room
            {
                room = room.trim();
                if(room.length()!=0) for(String guest: room.split(",")) // Split guests out of each room
                {
                    guest = guest.trim();
                    piece = pieces.get(guest);
                    rooms[row][col].addPlayer(piece);
                }
                col++;
                row = row + col/4;
                col = col%4;
            }
        }
    }

    public Piece getPiece(String name)
    {
        return pieces.get(name);
    }

    public class Player
    {
        public String playerName;
        public ArrayList<String> possibleGuestNames;

        public void adjustKnowledge(ArrayList<String> possibleGuests)
        {
            Iterator<String> it = possibleGuestNames.iterator();
            while(it.hasNext())
            {
                String g;
                if(!possibleGuests.contains(g=it.next()))
                {
                    it.remove();
                }
            }
        }

        public Player(String name, String[] guests)
        {
            playerName = name;
            possibleGuestNames = new ArrayList<String>();
            for(String g: guests)
            {
                possibleGuestNames.add(g);
            }
        }
    }

    public class Piece
    {
        public int row, col;
        public String name;

        public Piece(String name)
        {
            this.name = name;
        }
    }

    private String[] getPossibleMoves(Piece p)
    {
        LinkedList<String> moves=new LinkedList<String>();
        if(p.row > 0) moves.push((p.row-1) + "," + p.col);
        if(p.row < 2) moves.push((p.row+1) + "," + p.col);
        if(p.col > 0) moves.push((p.row) + "," + (p.col-1));
        if(p.col < 3) moves.push((p.row) + "," + (p.col+1));

        return moves.toArray(new String[moves.size()]);
    }


    public String getPlayerActions(String d1, String d2, String card1, String card2, String board) throws Suspicion.BadActionException
    {
        this.board = new Board(board, pieces, gemLocations);
        String actions = "";
        String [] availableGems;
        availableGems= this.board.rooms[me.row][me.col].availableGems;
        int minVal= 1000;
        String theGemIWant="";
//                    System.out.println("AVAILABLE GEMS:");
        for(String gem : availableGems){
            int count= myGems.get(gem);
            if(count < minVal){
                minVal = count;
                theGemIWant = gem;
            }
        }
        if (theGemIWant.equals("")) theGemIWant= "red";
        myGems.put(theGemIWant, myGems.get(theGemIWant)+1);


        // Random move for dice1
        // THIS TAKES A RANDOM GUEST, CAN IMPROVE HERE
        if(d1.equals("?")) d1 = guestName;
        Piece piece = pieces.get(d1);
        HashMap <String, Integer> myMoves= new HashMap<String, Integer>();
        String[] moves = getPossibleMoves(piece);
        for(String move : moves){
            List<String> gemList= Arrays.asList(this.board.rooms[Integer.parseInt(move.split(",")[0])][Integer.parseInt(move.split(",")[1])].availableGems);
            if (gemList.contains(theGemIWant))
                myMoves.put(move,0);
        }
        if (myMoves.isEmpty() || piece.name != guestName){
            int movei = r.nextInt(moves.length);
            actions += "move," + d1 + "," + moves[movei];
            this.board.movePlayer(piece, Integer.parseInt(moves[movei].split(",")[0]), Integer.parseInt(moves[movei].split(",")[1])); // Perform the move on my board
        }
        else{
            for(String name : guestNames){
                Piece thisPlayer = pieces.get(name);
                String location = thisPlayer.row + ","+thisPlayer.col;
                if (myMoves.keySet().contains(location)){
                    myMoves.put(location, myMoves.get(location) + 1);
                }
            }
            int maxVal=-11111111;
            String theMoveIWant= "";
            for(String move : myMoves.keySet()){
                int count= myMoves.get(move);
                if(count > maxVal){
                    maxVal = count;
                    theMoveIWant = move;
                }
            }
            System.out.println("THE MOVE I WANT "+theMoveIWant);
            actions += "move," + d1 + "," + theMoveIWant;
            System.out.println("THE action I WANT "+actions);
            this.board.movePlayer(piece, Integer.parseInt(theMoveIWant.split(",")[0]), Integer.parseInt(theMoveIWant.split(",")[1])); // Perform the move on my board
        }
        // THIS TAKES RANDOM MOVE, CAN IMPROVE HERE


        // Random move for dice2
        // THIS TAKES A RANDOM GUEST, CAN IMPROVE HERE
        if(d2.equals("?")) d2 = guestName;
        piece = pieces.get(d2);
        myMoves= new HashMap<String, Integer>();
        moves = getPossibleMoves(piece);
        for(String move : moves){
            List<String> gemList= Arrays.asList(this.board.rooms[Integer.parseInt(move.split(",")[0])][Integer.parseInt(move.split(",")[1])].availableGems);
            if (gemList.contains(theGemIWant))
                myMoves.put(move,0);
        }
        if (myMoves.isEmpty() || piece.name != guestName){
            int movei = r.nextInt(moves.length);
            actions += ":move," + d2 + "," + moves[movei];
            this.board.movePlayer(piece, Integer.parseInt(moves[movei].split(",")[0]), Integer.parseInt(moves[movei].split(",")[1])); // Perform the move on my board
        }
        else{
            for(String name : guestNames){
                Piece thisPlayer = pieces.get(name);
                String location = thisPlayer.row + ","+thisPlayer.col;
                if (myMoves.keySet().contains(location)){
                    myMoves.put(location, myMoves.get(location) + 1);
                }
            }
            int maxVal=-11111111;
            String theMoveIWant= "";
            for(String move : myMoves.keySet()){
                int count= myMoves.get(move);
                if(count > maxVal){
                    maxVal = count;
                    theMoveIWant = move;
                }
            }
            actions += ":move," + d2 + "," + theMoveIWant;
            this.board.movePlayer(piece, Integer.parseInt(theMoveIWant.split(",")[0]), Integer.parseInt(theMoveIWant.split(",")[1])); // Perform the move on my board
        }

        // which card, CHOOSING RANDOM CARD, CAN IMPROVE HERE
        int i = r.nextInt(2);
        actions += ":play,card"+(i+1);

        String card = i==0?card1:card2;


        for(String cardAction: card.split(":")) // just go ahead and do them in this order
        {
            if(cardAction.startsWith("move"))
            {
                String guest;
                // IMPROVE HERE
                guest = guestNames[r.nextInt(guestNames.length)];
                piece = pieces.get(guest);
                //moves = getPossibleMoves(piece);
                // CAN IMPROVE HERE
                actions += ":move," + guest + "," + r.nextInt(3) + "," + r.nextInt(4);
            }
            else if(cardAction.startsWith("viewDeck"))
            {
                actions += ":viewDeck";
            }
            else if(cardAction.startsWith("get"))
            {
                // USING RANDOM HERE, CAN IMPROVE
                if(cardAction.equals("get,")){
//                  actions += ":get," + this.board.rooms[me.row][me.col].availableGems[r.nextInt(this.board.rooms[me.row][me.col].availableGems.length)];

                    actions += ":get," + theGemIWant;
                }

                else actions += ":" + cardAction;
            }
            else if(cardAction.startsWith("ask"))
            {
                actions += ":" + cardAction + otherPlayerNames[r.nextInt(otherPlayerNames.length)];
            }
        }
        return actions;
    }

    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String board, String actions)
    {
    }

    private boolean canSee(Piece p1, Piece p2) // returns whether or not these two pieces see each
    {
        return (p1.row==p2.row || p1.col == p2.col);
    }


    public void answerAsk(String guest, String player, String board, boolean canSee)
    {
        Board b = new Board(board, pieces, gemLocations);
        ArrayList<String> possibleGuests = new ArrayList<String>();
        Piece p1 = pieces.get(guest);  // retrieve the guest
        for(String k : pieces.keySet())
        {
            Piece p2 = pieces.get(k);
            if((canSee && canSee(p1,p2)) || (!canSee && !canSee(p1,p2))) possibleGuests.add(p2.name);
        }
        //System.out.println("Adjusting knowledge about " + player + " to : " + possibleGuests);
        players.get(player).adjustKnowledge(possibleGuests);
    }
    public void answerViewDeck(String player)
    {
    }

    public HashMap<String, Integer> countGuestNames()
    {
        HashMap<String, Integer> guestNameCounts = new HashMap<String, Integer>();

        for(String k:players.keySet())
        {
            Player p = players.get(k);
            for(String g: p.possibleGuestNames)
            {
                if(guestNameCounts.containsKey(g))
                {
                    int i = guestNameCounts.get(g);
                    i++;
                    guestNameCounts.replace(g,new Integer(i));
                }
                else
                {
                    guestNameCounts.put(g,new Integer(1));
                }
            }
        }
        return guestNameCounts;
    }


    public void sortPossibleGuestNames(HashMap<String, Integer> guestNameCounts)
    {
        String rval="";
        for(String k:players.keySet())
        {
            Player p = players.get(k);
            boolean bubble=true;
            int y=1;
            while(bubble)
            {
                bubble=false;
                String temp1, temp2;
                for(int x=0;x<p.possibleGuestNames.size()-y;x++)
                {
                    if(guestNameCounts.get(temp1=p.possibleGuestNames.get(x)) <
                            guestNameCounts.get(temp2=p.possibleGuestNames.get(x+1)))
                    {
                        bubble=true;
                        p.possibleGuestNames.set(x,temp2);
                        p.possibleGuestNames.set(x=1,temp1);
                    }
                }
                y++;
            }
        }
    }

    public String reportGuesses()
    {
        HashMap<String, Integer> guestNameCounts = countGuestNames();
        sortPossibleGuestNames(guestNameCounts);

        String rval="";
        for(String k:players.keySet())
        {
            Player p = players.get(k);
            rval += k;
            for(String g: p.possibleGuestNames)
            {
                rval += ","+g;
            }
            rval+=":";
        }
        return rval.substring(0,rval.length()-1);
    }

    public rohitgangurde(String playerName, String guestName, int numStartingGems, String gemLocations, String[] playerNames, String[] guestNames)
    {
        super(playerName, guestName, numStartingGems, gemLocations, playerNames, guestNames);
        pieces = new HashMap<String, Piece>();
        ArrayList<String> possibleGuests = new ArrayList<String>();
        myGems= new HashMap<String, Integer>();
        myGems.put("red",0);
        myGems.put("yellow",0);
        myGems.put("green",0);
        for(String name:guestNames)
        {
            pieces.put(name, new Piece(name));
            if(!name.equals(guestName)) possibleGuests.add(name);
        }
        me = pieces.get(guestName);

        players = new HashMap<String, Player>();
        for(String str: playerNames)
        {
            if(!str.equals(playerName)) players.put(str, new Player(str, possibleGuests.toArray(new String[possibleGuests.size()])));
        }

        otherPlayerNames = players.keySet().toArray(new String[players.size()]);

    }
}







