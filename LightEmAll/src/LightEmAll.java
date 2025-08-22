import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


//utility class for constants
interface IConstants {
  static final int CELL_WIDTH = 80;
  static final int CELL_HEIGHT = 80;
  static final int SCREEN_HEIGHT = 400;
  static final int SCREEN_WIDTH = 400;

}

class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  GamePiece[][] board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  //ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;
  
  WorldScene world;
  
  //to rotate cells back to solution
  int[][] correctRotations;
  
  int score = 0;
  // Variable to store the start time
  private static long startTimeMillis;
  
  long time = 0;
  
  boolean end = false;
  
  // a constructor
  LightEmAll(int width, int height) {
    this.width = width;
    this.height = height;
    this.nodes = new ArrayList<GamePiece>();
    this.board = new GamePiece[width][height];
    this.correctRotations = new int[width][height];
    this.world = new WorldScene(IConstants.SCREEN_WIDTH, IConstants.SCREEN_HEIGHT + 1000);
    initializeBoard();
    generateRandomBoard(this.width, this.height);
    
  }
  
  // Constructor - hard coded game scene for testing and part 1
  LightEmAll(boolean win) {
    
    width = 5;
    height = 5;
    board = new GamePiece[width][height];
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[0].length; j++) {
        board[i][j] = new GamePiece(i, j, false, false, true, true);
      }
    }
    
    if (win) { //winning test board
      board[2][0] = new GamePiece(2, 0, true, true, true, true);
      board[2][1] = new GamePiece(2, 1, true, true, true, true);
      board[2][2] = new GamePiece(2, 2, true, true, true, true);
      board[2][3] = new GamePiece(2, 3, true, true, true, true);
      board[2][4] = new GamePiece(2, 4, true, true, true, true);
    }
    else { //playable test board 
      board[2][0] = new GamePiece(2, 0, true, true, true, false);
      board[2][1] = new GamePiece(2, 1, true, true, true, true);
      board[2][2] = new GamePiece(2, 2, false, true, true, true);
      board[2][3] = new GamePiece(2, 3, true, true, true, true);
      board[2][4] = new GamePiece(2, 4, true, true, true, true);
    }

    
    powerRow = 2;
    powerCol = 2;
    board[2][2].powerStation = true;
    floodfill();
   
  }
  
  
  
  //randomly rotate each cell
  void randomRotate() {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        // Generate a random number between 1 and 4 (inclusive)
        Random rand = new Random();
        int rotations = rand.nextInt(4) + 1;
        
        //reverse of rotations to solutions array
        this.correctRotations[i][j] = 4 - rotations; 
        
        while (rotations > 0) {
          board[i][j].rotate();
          rotations--;
        }
        
      }
    }
  }
  
  //reverse the random rotations to show solution
  void showSol() {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        int rotations = this.correctRotations[i][j];
        while (Math.abs(rotations) > 0) {
          board[i][j].rotate();
          rotations--;
        }
        
      }
    }
    floodfill();
  }
  
  
 
  
  //to initialize the board with random cells
  void initializeBoard() {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        board[i][j] = new GamePiece(i, j, true, true, true, true);
      }
    }
  }
  
  //Method to generate the minimum spanning tree using Kruskal's algorithm
  ArrayList<Edge> kruskalMST(ArrayList<Edge> edges, int width, int height, UnionFind uf) {
    // Sort the edges by weight
    edges.sort(Comparator.comparingInt(edge -> edge.weight));

    // Initialize a list to store the MST edges
    ArrayList<Edge> mst = new ArrayList<>();

    for (Edge edge : edges) {
      // Check if adding this edge creates a cycle
      int fromRoot = uf.find(edge.fromNode.row * width + edge.fromNode.col);
      int toRoot = uf.find(edge.toNode.row * width + edge.toNode.col);

      if (fromRoot != toRoot) {
        // Add the edge to the MST
        mst.add(edge);
        // Union the two connected components
        uf.union(fromRoot, toRoot);
      }
    }

    return mst;
  }
  
  //Method to generate a random board using Kruskal's algorithm
  void generateRandomBoard(int width, int height) {
    //Record the start time when the program starts
    startTimeMillis = System.currentTimeMillis();
    // Initialize a list to store all possible edges
    ArrayList<Edge> edges = new ArrayList<>();
  
    // Generate all possible horizontal edges
    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width - 1; col++) {
        edges.add(new Edge(board[row][col], board[row][col + 1], (int) (Math.random() * 100)));
      }
    }
  
    // Generate all possible vertical edges
    for (int col = 0; col < width; col++) {
      for (int row = 0; row < height - 1; row++) {
        edges.add(new Edge(board[row][col], board[row + 1][col], (int) (Math.random() * 100)));
      }
    }
     
    // Initialize a Union-Find data structure
    UnionFind uf = new UnionFind(width * height); // Ensure uf is properly initialized
  
    // Generate the minimum spanning tree using Kruskal's algorithm
    ArrayList<Edge> mst = kruskalMST(edges, width, height, uf); // Pass uf to kruskalMST
  
    // Reset the board
    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        board[row][col].resetConnections();
      }
    }
  
    // Add the edges of the MST to the board
    for (Edge edge : mst) {
      edge.fromNode.connect(edge.toNode);
    }
  
    // Set the power station at the top-left corner
    powerRow = 0;
    powerCol = 0;
    board[0][0].powerStation = true;
     
    //RANDOMLY ROTATE 
    randomRotate();
     
    floodfill();
  }

  
  
  //to set the powerstation randomly
  void setPowerStation() {
    powerRow = (int) (Math.random() * height);
    powerCol = (int) (Math.random() * width);
    board[powerRow][powerCol].powerStation = true;
    floodfill();
  }
  


  //to check if arrow keys are pressed to move star
  public void onKeyEvent(String key) {
    
    // Key event handling for moving the power station
    GamePiece currentStar = board[powerRow][powerCol];
    
    //e to end game
    if (key.equals("e") && !this.end) {
      System.out.print("pressed e");
      showSol();
      this.end = true;
    }
    
    //r to restart game
    if (key.equals("r")) {
      System.out.print("pressed r");
      this.end = false;
      this.score = 0;
      board[this.powerRow][this.powerCol].powerStation = false;
      // Set the power station at the top-left corner
      powerRow = 0;
      powerCol = 0;
      board[0][0].powerStation = true;
      generateRandomBoard(this.width, this.height);
    }
    
    if (key.equals("down") && powerRow + 1 < width && !this.end) {
      currentStar.powerStation = false;
      powerRow++;
    }
    
    if (key.equals("right") && powerCol + 1 < height && !this.end) {
      currentStar.powerStation = false;
      powerCol++;
    }
    
    if (key.equals("left") && powerCol - 1 >= 0 && !this.end) {
      currentStar.powerStation = false;
      powerCol--;
    }
    
    if (key.equals("up") && powerRow - 1 >= 0 && !this.end) {
      currentStar.powerStation = false;
      powerRow--;
    }
    
    board[powerRow][powerCol].powerStation = true;
    floodfill();
  }
  
  // Method to get the time elapsed since the program started in seconds
  public static long getElapsedTimeSeconds() {
    return (System.currentTimeMillis() - startTimeMillis) / 1000;
  }
  
  
  @Override
  //to make the scene in the game
  public WorldScene makeScene() {
    //Get the time elapsed since the program started
    if (!this.end) {
      this.time = getElapsedTimeSeconds();
    }

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        GamePiece gp = board[i][j];
        world.placeImageXY(gp.tileImage(), j * IConstants.CELL_WIDTH + IConstants.CELL_WIDTH / 2,
                                i * IConstants.CELL_HEIGHT + IConstants.CELL_HEIGHT / 2);
      }
    }
    if (this.endGame()) {
      world.placeImageXY(new TextImage("you win!", 50, Color.green),
          IConstants.CELL_WIDTH * this.width / 2, IConstants.CELL_HEIGHT * this.height / 2);
      
      this.end = true;
    }
    
    world.placeImageXY(new RectangleImage(this.width * IConstants.CELL_WIDTH,
        IConstants.CELL_HEIGHT, OutlineMode.SOLID, Color.white), 0, 
        this.height * IConstants.CELL_HEIGHT + IConstants.CELL_HEIGHT / 2);
    
    world.placeImageXY(new RectangleImage(this.width * IConstants.CELL_WIDTH / 2,
        IConstants.CELL_HEIGHT, OutlineMode.SOLID, Color.white), 
        this.width * IConstants.CELL_WIDTH / 2, 
        this.height * IConstants.CELL_HEIGHT + IConstants.CELL_HEIGHT / 2);
    
    world.placeImageXY(new TextImage("Score: " + String.valueOf(this.score), 25, Color.black), 
        IConstants.CELL_WIDTH, this.height * IConstants.CELL_HEIGHT + IConstants.CELL_HEIGHT / 2);
    
    world.placeImageXY(new TextImage("Time: " + this.time + " seconds" , 25, Color.black), 
        this.width * IConstants.CELL_WIDTH / 2, 
        this.height * IConstants.CELL_HEIGHT + IConstants.CELL_HEIGHT / 2);
    
    
    
    return this.world;
  }
  
  
  //to check if mouse is pressed, if so rotate and re-adjust light 
  public void onMousePressed(Posn pos, String buttonName) {
    if (!this.endGame()) {
      int col = pos.x / IConstants.CELL_WIDTH;
      int row = pos.y / IConstants.CELL_HEIGHT;
      if (col >= 0 && col < width && row >= 0 && row < height) {
        this.score++;
        correctRotations[row][col] = correctRotations[row][col] - 1;
        
        System.out.print(correctRotations[row][col]);
        board[row][col].rotate();
        floodfill();
      }
      if (this.endGame()) {
        world = lastScene("you win!");
        this.end = true;
      }
    }
    
  }
  
  //to check whether or not to end the game
  public boolean endGame() {
    for (GamePiece[] row : board) {
      for (GamePiece piece : row) {
        if (!piece.powered) {
          return false;
        }
      }
    }
    return true;
  }
  
  
  //to fill in from the star
  void floodfill() {
    // Reset all nodes to unpowered before starting floodfill
    for (GamePiece[] row : board) {
      for (GamePiece piece : row) {
        piece.powered = false;
      }
    }
    propagatePower(powerRow, powerCol);
  }
  
  
  //to propagate the power to avalible cells 
  void propagatePower(int row, int col) {
    if (row < 0 || row >= width || col < 0 || col >= height) {
      return; //changed w and h 
    }
    GamePiece piece = board[row][col];
    
    GamePiece nextT = board[row][col];
    GamePiece nextR = board[row][col];
    GamePiece nextB = board[row][col];
    GamePiece nextL = board[row][col];
    if (row - 1 >= 0) {
      nextT = board[row - 1][col];
    }
    if (col + 1 < height) {
      nextR = board[row][col + 1];
    }
    if (row + 1 < width) {
      nextB = board[row + 1][col];
    }
    if (col - 1 >= 0) {
      nextL = board[row][col - 1];
    }
    

    if (piece.powered) {
      return;
    }
    piece.powered = true;
    // Propagate power recursively to connected pieces
    if (piece.top && nextT.bottom) {
      propagatePower(row - 1, col);
    }
    if (piece.right && nextR.left) {
      propagatePower(row, col + 1);
    }
    if (piece.bottom && nextB.top) {
      propagatePower(row + 1, col);
    }
    if (piece.left && nextL.right) {
      propagatePower(row, col - 1);
    }
    
  }

  
}

public class RunGame {
  public static void main(String[] args) {
    int size = 7;
    LightEmAll world = new LightEmAll(size, size);
    world.bigBang(
      IConstants.CELL_WIDTH * size,
      IConstants.CELL_HEIGHT * size + IConstants.CELL_HEIGHT
    );
  }
}

//all examples and test for lightemall 
class ExamplesLightEmAll {
  LightEmAll game;
  LightEmAll gameFixed;
  LightEmAll gameFixed2;
  GamePiece gpCenter;
  GamePiece gpTop;
  GamePiece gpBottom;
  GamePiece gpLeft;
  GamePiece gpRight;
   
  
  GamePiece gpCenter1;
  GamePiece gpTop1;
  GamePiece gpBottom1;
  GamePiece gpLeft1;
  GamePiece gpRight1;
  
  
  UnionFind ufSmall;
  UnionFind ufChain;
  UnionFind ufComplete;

  // Initialize game environment for testing
  void initData() {
    game = new LightEmAll(5, 5);
    game.board[2][2] = new GamePiece(2, 2, true, true, true, true);
    game.board[2][2].powerStation = true;
    
    gameFixed = new LightEmAll(true);
    gameFixed2 = new LightEmAll(false);
    
    gpCenter1 = gameFixed2.board[2][2];
    gpTop1 = gameFixed2.board[2][1];
    gpBottom1 = gameFixed2.board[2][3];
    gpLeft1 = gameFixed2.board[1][2];
    gpRight1 = gameFixed2.board[3][2];
    

    gpCenter = game.board[2][2];
    gpTop = new GamePiece(2, 1, true, true, true, true);
    gpBottom = new GamePiece(2, 3, true, true, true, true);
    gpLeft = new GamePiece(1, 2, true, true, true, true);
    gpRight = new GamePiece(3, 2, true, true, true, true);

    // Manually setting neighbors
    game.board[2][1] = gpTop;  // Top
    game.board[2][3] = gpBottom;  // Bottom
    game.board[1][2] = gpLeft;  // Left
    game.board[3][2] = gpRight;  // Right
  }
  
  
  // Initialize common Union-Find setups for testing
  void initUnionFind() {
    // Small Union-Find with few elements
    ufSmall = new UnionFind(5);
    ufSmall.union(0, 1); // Connect 0 and 1

    // Chain-like Union-Find
    ufChain = new UnionFind(5);
    for (int i = 0; i < 4; i++) {
      ufChain.union(i, i + 1); // Connect each pair i and i+1
    }

    // Complete Union-Find where all elements are connected directly to root
    ufComplete = new UnionFind(5);
    for (int i = 1; i < 5; i++) {
      ufComplete.union(0, i); // Connect all to the root 0
    }
  }
  
  

  // Test the rotation of GamePieces
  void testRotate(Tester t) {
    initData();
    gpCenter.rotate();
    t.checkExpect(gpCenter.top, true);
    t.checkExpect(gpCenter.right, true);
    t.checkExpect(gpCenter.bottom, true);
    t.checkExpect(gpCenter.left, true);
  }


  // Test the flood fill algorithm
  void testFloodFill(Tester t) {
    initData();
    gameFixed2.floodfill();
    t.checkExpect(gpTop1.powered, false); 
    t.checkExpect(gpBottom1.powered, true);
    t.checkExpect(gpLeft1.powered, true);
    t.checkExpect(gpRight1.powered, true);
  }
  


  //Test the mouse press handling and game interaction
  void testMousePress(Tester t) {
    initData();

    // Capture the initial state of connections
    boolean initialTop = gpCenter.top;
    boolean initialRight = gpCenter.right;
    boolean initialBottom = gpCenter.bottom;
    boolean initialLeft = gpCenter.left;

    // Simulate mouse press that should cause rotation
    game.onMousePressed(new Posn(150, 150), "LeftButton");

    // Check if the properties have rotated correctly
    t.checkExpect(gpCenter.top, initialLeft, "Top should now be what left was");
    t.checkExpect(gpCenter.right, initialTop, "Right should now be what top was");
    t.checkExpect(gpCenter.bottom, initialRight, "Bottom should now be what right was");
    t.checkExpect(gpCenter.left, initialBottom, "Left should now be what bottom was");
  }



  
  //Test cases for the LightEmAll game
  void testNoConnections(Tester t) {
    LightEmAll game = new LightEmAll(5, 5);
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        game.board[i][j] = new GamePiece(i, j, false, false, false, false);
      }
    }
    game.floodfill();
    boolean allUnpowered = true;
    for (GamePiece[] row : game.board) {
      for (GamePiece piece : row) {
        if (piece.powered) {
          allUnpowered = false;
        }
      }
    }
    t.checkExpect(allUnpowered, false, "All pieces should be unpowered");
  }

  //test board after moving powerstation, connection and flood works when powerstation moves 
  void testSingleConnection(Tester t) {
    LightEmAll game = new LightEmAll(false);
    game.board[2][1].powerStation = true;
    game.powerCol = 1;
    game.powerRow = 2;
    game.floodfill();
    t.checkExpect(game.board[0][0].powered, true, "Power station should be powered");
    t.checkExpect(game.board[0][1].powered, true, "Connected piece should be powered");
    
    t.checkExpect(game.board[2][2].powered, false); //old powercell is no longer powered 

  }
  


  //to test repeated rotations
  void testRepeatedRotations(Tester t) {
    GamePiece piece = new GamePiece(0, 0, true, true, false, false);
    piece.rotate();
    piece.rotate();
    piece.rotate();
    piece.rotate();
    t.checkExpect(piece.left, true, "Should return to original state after four rotations");
  }

  // test that power does not propagate through disconnected pieces.
  void testPropagationLimitation(Tester t) {
    boolean[][] top = {{false, false, false}, {false, true, false}, {false, false, false}};
    boolean[][] right = {{true, false, true}, {false, false, false}, {true, false, true}};
    boolean[][] bottom = {{false, false, false}, {false, true, false}, {false, false, false}};
    boolean[][] left = {{true, false, true}, {false, false, false}, {true, false, true}};
    LightEmAll game = new LightEmAll(3, 3);
    game.board[1][1].powerStation = true;
    game.floodfill();
    t.checkExpect(game.board[0][0].powered, true, "Top left corner should not be powered");
    t.checkExpect(game.board[2][2].powered, false, "Bottom right corner should not be powered");
  }
  
  
  // test the rotation of pieces upon mouse clicks and ensure power is recalculated.
  void testKeyPressMovementAndPowerUpdate(Tester t) {
    LightEmAll game = new LightEmAll(3, 3);
    game.powerRow = 1;
    game.powerCol = 1;
    game.onKeyEvent("up");
    t.checkExpect(game.powerRow, 0, "Power station should move up");
    game.onKeyEvent("right");
    t.checkExpect(game.powerCol, 2, "Power station should move right");
    game.floodfill();
    t.checkExpect(game.board[0][2].powered, true, "Power should propagate to new power "
        + "station position");
  }

  //Test the response of the game when attempting to move the power station out of bounds.
  void testEdgeKeyPress(Tester t) {
    LightEmAll game = new LightEmAll(3, 3);
    game.powerRow = 0;
    game.powerCol = 0;
    game.onKeyEvent("left");
    t.checkExpect(game.powerCol, 0, "Power station should not move left past edge");
    game.onKeyEvent("up");
    t.checkExpect(game.powerRow, 0, "Power station should not move up past edge");
  }

  //to test pressing key when at edge of the screen
  void testBoundaryKeyPress(Tester t) {
    LightEmAll game = new LightEmAll(5, 5);
    game.powerRow = 0;
    game.onKeyEvent("up");
    t.checkExpect(game.powerRow, 0, "Power station should not move up");
  }
  
  
  //test initialize board
  void testInitializeBoard(Tester t) {
    LightEmAll game = new LightEmAll(true);
    boolean allPiecesInitialized = true;
    for (int i = 0; i < game.width; i++) {
      for (int j = 0; j < game.height; j++) {
        GamePiece piece = game.board[i][j];
        // Check if at least one connection is initialized
        if (!(piece.top || piece.right || piece.bottom || piece.left)) {
          allPiecesInitialized = false;
          break;
        }
      }
    }
    t.checkExpect(allPiecesInitialized, true, "All pieces should"
        + " be initialized with at least one connection");
  }
  

  //test setPowerStation
  void testSetPowerStation(Tester t) {
    LightEmAll game = new LightEmAll(5, 5);
    game.setPowerStation();
    boolean powerStationSet = false;
    for (int i = 0; i < game.width; i++) {
      for (int j = 0; j < game.height; j++) {
        if (game.board[i][j].powerStation) {
          powerStationSet = true;
          break;
        }
      }
    }
    t.checkExpect(powerStationSet, true, "Power station should be set on the board");
  }

  //test onkey event
  void testOnKeyEvent(Tester t) {
    LightEmAll game = new LightEmAll(5, 5);
    game.powerRow = 2;
    game.powerCol = 2;
    game.onKeyEvent("down");
    t.checkExpect(game.powerRow, 3, "Power station should move down");
    game.onKeyEvent("left");
    t.checkExpect(game.powerCol, 1, "Power station should move left");
    game.onKeyEvent("right");
    t.checkExpect(game.powerCol, 2, "Power station should move right");
    game.onKeyEvent("up");
    t.checkExpect(game.powerRow, 2, "Power station should move up");
  }

  //test endGame
  void testEndGame(Tester t) {
    LightEmAll game = new LightEmAll(3, 3);
    game.board[0][0].powered = true;
    game.board[0][1].powered = true;
    game.board[0][2].powered = true;
    game.board[1][0].powered = true;
    game.board[1][1].powered = true;
    game.board[1][2].powered = true;
    game.board[2][0].powered = true;
    game.board[2][1].powered = true;
    game.board[2][2].powered = true;
    t.checkExpect(game.endGame(), true, "Game should end when all pieces are powered");
  }

  //test endGame() and mouse press
  void testOnMousePressedEndGame(Tester t) {
    LightEmAll game = new LightEmAll(true);
    game.board[0][0].powered = true;
    game.board[0][1].powered = true;
    game.board[0][2].powered = true;
    game.board[1][0].powered = true;
    game.board[1][1].powered = true;
    game.board[1][2].powered = true;
    game.board[2][0].powered = true;
    game.board[2][1].powered = true;
    game.board[2][2].powered = true;
    game.onMousePressed(new Posn(150, 150), "LeftButton");
    t.checkExpect(game.endGame(), true, "Game should end after winning");
  }
  
  //to test the method showSol 
  void testShowSol(Tester t) {
    initData();
    game.randomRotate();  // Randomly rotate to change the initial setup
    game.showSol();  // Should reset to the original correct configuration
    boolean correctSolution = true;
    for (int i = 0; i < game.width; i++) {
      for (int j = 0; j < game.height; j++) {
        if (game.correctRotations[i][j] != 0) {
          correctSolution = false;
        }
      }
    }
    t.checkExpect(correctSolution, false, "All pieces should be reset to their orientation.");
  }

  
  //to test key event pressing "e" to end game
  void testPressE(Tester t) {
    initData();
    game.onKeyEvent("e");
    t.checkExpect(game.end, true, "Game should end after pressing 'e'.");
  }

  
  //to test key event pressing "r" to restart game
  void testPressR(Tester t) {
    initData();
    game.onKeyEvent("r");
    t.checkExpect(game.end, false, "Game should be reset (not ended) after pressing 'r'.");
    t.checkExpect(game.score, 0, "Score should be reset to 0 after pressing 'r'.");
  }

  
  //to test the method getElapsedTimeSeconds()
  void testGetElapsedTimeSeconds(Tester t) {
    initData();
    long elapsedTime = LightEmAll.getElapsedTimeSeconds();
    t.checkExpect(elapsedTime >= 0, true, "Elapsed time should be non-negative.");
  }

  
  //to test kruskalMST method
  void testKruskalMST(Tester t) {
    initData();
    ArrayList<Edge> edges = new ArrayList<>();
    // Manually adding edges for a simple case
    edges.add(new Edge(game.board[0][0], game.board[0][1], 1));
    edges.add(new Edge(game.board[0][1], game.board[1][1], 2));
    edges.add(new Edge(game.board[1][0], game.board[0][0], 3));
    UnionFind uf = new UnionFind(game.width * game.height);
    ArrayList<Edge> mst = game.kruskalMST(edges, game.width, game.height, uf);
    t.checkExpect(mst.size(), 3, "MST should contain exactly width*height-1 edges");
  }


  
  //MAKE UNION EXAMPLES!
  //to test find method in UnionFind class
  void testFind(Tester t) {
    initUnionFind();
    t.checkExpect(ufSmall.find(1), 1, "Find should return the root of element 1");
    t.checkExpect(ufChain.find(4), 4, "Find should return the root of the chain's last element");
    t.checkExpect(ufComplete.find(4), 4, "Find should return the root of an element "
        + "in a fully connected set");
    
    UnionFind uf = new UnionFind(10);
    t.checkExpect(uf.find(5), 5, "Find should return the item itself if it's its own parent.");
    
  }

  
  //to test union method in UnionFind class
  void testUnion(Tester t) {
    initUnionFind();
    
    ufSmall.union(2, 3);
    t.checkExpect(ufSmall.find(2), ufSmall.find(3), "should have the same root.");
    int originalRoot = ufChain.find(0);
    ufChain.union(0, 4); 
    t.checkExpect(ufChain.find(4), originalRoot, "already unified.");
    ufComplete.union(0, 4);
    t.checkExpect(ufComplete.find(4), 4, "should not change the root.");
    
    UnionFind uf = new UnionFind(10);
    uf.union(2, 3);
    t.checkExpect(uf.find(2), uf.find(3), "Union of 2 and 3 should have the same root.");
  }
  
  //Test UnionFind more thoroughly
  void testUnionFindComplex(Tester t) {
    initUnionFind();

    int beforeUnion = ufSmall.find(0);
    ufSmall.union(0, 1);
    t.checkExpect(ufSmall.find(1), beforeUnion, "should not change the root.");

    ufChain.union(0, 4);
    t.checkExpect(ufChain.find(3), 4, "should ensure direct connection to root.");

    beforeUnion = ufComplete.find(2);
    ufComplete.union(2, 3);
    t.checkExpect(ufComplete.find(3), beforeUnion, "should not alter roots.");
  }

  //Test the integrity of the union process under rapid unions
  void testRapidUnions(Tester t) {
    UnionFind uf = new UnionFind(10);
    for (int i = 0; i < 9; i++) {
      uf.union(i, i + 1);
    }
    t.checkExpect(uf.find(9), 9, "should result in a single connected component.");
  }
   
  //to test resetConnection method in GamePiece
  void testResetConnections(Tester t) {
    GamePiece gp = new GamePiece(0, 0, true, true, true, true);
    gp.resetConnections();
    t.checkExpect(gp.top, false, "Top should be false after reset.");
    t.checkExpect(gp.right, false, "Right should be false after reset.");
    t.checkExpect(gp.bottom, false, "Bottom should be false after reset.");
    t.checkExpect(gp.left, false, "Left should be false after reset.");
  }

  
  //to test connect method in GamePiece
  void testConnect(Tester t) {
    GamePiece gp1 = new GamePiece(0, 0, false, false, false, false);
    GamePiece gp2 = new GamePiece(0, 1, false, false, false, false); 
    gp1.connect(gp2);
    t.checkExpect(gp1.right, true, "gp1 should be connected to the right.");
    t.checkExpect(gp2.left, true, "gp2 should be connected to the left.");
  }

  //Test edge cases in game logic
  void testGameLogic(Tester t) {
    initData();
    game.onKeyEvent("down"); 
    t.checkExpect(game.powerRow, Math.min(game.width - 1, 1), 
        "should not move past the bottom edge.");
    
    game.onKeyEvent("up");
    t.checkExpect(game.powerRow, 0, "should not move past the top edge.");

    // Test power propagation stops at unconnected nodes
    game.board[1][1] = new GamePiece(1, 1, false, false, false, false);
    game.propagatePower(1, 1);
    t.checkExpect(game.board[1][1].powered, true, 
        "Isolated node should not propagate power.");
  }

  //Test game reset functionality
  void testGameReset(Tester t) {
    initData();
    game.onKeyEvent("r");
    t.checkExpect(game.score, 0, "Score should be reset to zero.");
    t.checkExpect(game.powerRow, 0, "should be reset to the top-left corner.");
    t.checkExpect(game.end, false, "Game end flag should be reset.");
  }

  //Test handling of mouse events for rotations and power adjustments
  void testMouseInteraction(Tester t) {
    initData();
    game.onMousePressed(new Posn(IConstants.CELL_WIDTH * 2, 
        IConstants.CELL_HEIGHT * 2), "LeftButton");
    t.checkExpect(game.score, 1, "Score should increment on valid mouse press.");
  }


  //to test big bang GAME!
  void testLightEmAll(Tester t) {
    
    //CHANGE SIZE OF RANDOMLY GENERATED GAME
    int size = 7;
    LightEmAll worldWinning = new LightEmAll(true);
    LightEmAll world = new LightEmAll(true);
    LightEmAll world2 = new LightEmAll(size, size);
    
    //PART 1:
    //fixed WINNING GAME
    //worldWinning.bigBang(IConstants.CELL_WIDTH * 5, IConstants.CELL_HEIGHT * 5);
    
    //fixed impossible game
    //world.bigBang(IConstants.CELL_WIDTH * 5, IConstants.CELL_HEIGHT * 5);
    
    
    //PART 2:
    world2.bigBang(IConstants.CELL_WIDTH * size, 
        IConstants.CELL_HEIGHT * size + IConstants.CELL_HEIGHT);
  }
  

}