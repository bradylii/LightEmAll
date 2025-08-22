import java.awt.Color;
import javalib.worldimages.*;

//to represent a GamePiece
class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  boolean powered;
  
  // a constructor
  GamePiece(int row, int col, boolean left, boolean right, boolean top, boolean bottom) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = false;
    this.powered = false;
  }
  
  // rotates the board clockwise
  void rotate() {
    boolean previousLeft = left;
    left = bottom;
    bottom = right;
    right = top;
    top = previousLeft;
  }
  
  // Method to reset connections of the GamePiece
  void resetConnections() {
    this.top = false;
    this.right = false;
    this.bottom = false;
    this.left = false;
  }
  
  // Method to connect this GamePiece to another GamePiece
  void connect(GamePiece other) {
    int rowDiff = other.row - this.row;
    int colDiff = other.col - this.col;

    if (rowDiff == -1) {
      this.top = true;
      other.bottom = true;
    } else if (rowDiff == 1) {
      this.bottom = true;
      other.top = true;
    } else if (colDiff == -1) {
      this.left = true;
      other.right = true;
    } else if (colDiff == 1) {
      this.right = true;
      other.left = true;
    }
  }
  
  
  
  //Generate an image of this, the given GamePiece.
  // - size: the size of the tile, in pixels
  // - wireWidth: the width of wires, in pixels
  // - wireColor: the Color to use for rendering wires on this
  // - hasPowerStation: if true, draws a fancy star on this tile to represent the power station
  //
  
  //to draw tile
  WorldImage tileImage() {
    // Start tile image off as a blue square with a wire-width square in the middle,
    // to make image "cleaner" (will look strange if tile has no wire, but that can't be)
    
    int size = IConstants.CELL_HEIGHT;
    int wireWidth = 2;
    Color wireColor = Color.gray;
    if (powered) {
      wireColor = Color.yellow;
    }
    boolean hasPowerStation = powerStation;
    WorldImage image = new OverlayImage(
        new RectangleImage(
            wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new RectangleImage(
            size, size, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(
        wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage(
        (size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }
    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);
    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (hasPowerStation) {
      image = new OverlayImage(
                 new OverlayImage(
                     new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
                     new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
                 image);
    }
    return new OverlayImage(
        new RectangleImage(IConstants.CELL_WIDTH, IConstants.CELL_HEIGHT, 
           OutlineMode.OUTLINE, Color.black), image);
  }
}


//to represent edge
class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight; // Random weight assigned for the algorithm
  
  Edge(GamePiece fromNode, GamePiece toNode, int weight) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
    
  }
}

//to represent a UnionFind
class UnionFind {
  int[] parent;

  // Constructor
  UnionFind(int n) {
    parent = new int[n];
    for (int i = 0; i < n; i++) {
      parent[i] = i;
    }
  }

  // Find operation
  int find(int x) {
    if (parent[x] != x) {
      parent[x] = find(parent[x]);
    }
    return parent[x];
  }

  // Union operation
  void union(int x, int y) {
    int rootX = find(x);
    int rootY = find(y);
    if (rootX != rootY) {
      parent[rootX] = rootY;
    }
  }
}