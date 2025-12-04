import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;
        char desiredDirection = 'U';

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        initializeGame();
    }

    private void initializeGame() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        loadImages();
        loadMap();
        initializeGhosts();
        startGameLoop();
    }

    private void loadImages() {
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
    }

    private void initializeGhosts() {
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    private void startGameLoop() {
        gameLoop = new Timer(50, this); // 20fps (1000/50)
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        drawPacman(g);
        drawGhosts(g);
        drawWalls(g);
        drawFoods(g);
        drawScore(g);
    }

    private void drawPacman(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
    }

    private void drawGhosts(Graphics g) {
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }
    }

    private void drawWalls(Graphics g) {
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
    }

    private void drawFoods(Graphics g) {
        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
    }

    private void drawScore(Graphics g) {
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + score, tileSize / 2, tileSize / 2);
        } else {
            g.drawString("x" + lives + " Score: " + score, tileSize / 2, tileSize / 2);
        }
    }
    
    // A* Pathfinding classes and methods
    class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        int g, h, f;
        
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f, other.f);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node)obj;
            return x == node.x && y == node.y;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
    
    private Node findPinkGhost() {
        for (Block ghost : ghosts) {
            if (ghost.image == pinkGhostImage) {
                return new Node(ghost.x / tileSize, ghost.y / tileSize);
            }
        }
        return null;
    }
    
    private Node predictPacmanPosition() {
        // Predict 2 tiles ahead in current direction
        int aheadX = pacman.x / tileSize;
        int aheadY = pacman.y / tileSize;
        
        switch (pacman.direction) {
            case 'U': aheadY -= 2; break;
            case 'D': aheadY += 2; break;
            case 'L': aheadX -= 2; break;
            case 'R': aheadX += 2; break;
        }
        
        // Ensure position is within bounds
        aheadX = Math.max(0, Math.min(columnCount - 1, aheadX));
        aheadY = Math.max(0, Math.min(rowCount - 1, aheadY));
        
        return new Node(aheadX, aheadY);
    }
    
    private List<Node> findPath(Node start, Node target) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Map<Node, Integer> gScore = new HashMap<>();
        
        gScore.put(start, 0);
        start.g = 0;
        start.h = heuristic(start, target);
        start.f = start.h;
        openSet.add(start);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.equals(target)) {
                return reconstructPath(cameFrom, current);
            }
            
            closedSet.add(current);
            
            for (Node neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor)) continue;
                
                int tentativeGScore = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;
                
                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                } else if (tentativeGScore >= gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    continue;
                }
                
                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentativeGScore);
                neighbor.g = tentativeGScore;
                neighbor.h = heuristic(neighbor, target);
                neighbor.f = neighbor.g + neighbor.h;
            }
        }
        
        return new ArrayList<>(); // No path found
    }
    
    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, Down, Left, Up
        
        for (int[] dir : directions) {
            int newX = node.x + dir[0];
            int newY = node.y + dir[1];
            
            if (newX >= 0 && newX < columnCount && newY >= 0 && newY < rowCount) {
                if (isWalkable(newY, newX)) {
                    neighbors.add(new Node(newX, newY));
                }
            }
        }
        
        return neighbors;
    }
    
    private int heuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y); // Manhattan distance
    }
    
    private List<Node> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        List<Node> path = new ArrayList<>();
        path.add(current);
        
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }
        
        return path;
    }

    public void move() {
        movePacman();
        moveGhosts();
        checkFoodCollision();
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    private void movePacman() {
        // Turn buffering
        boolean alignedX = (pacman.x % tileSize == 0);
        boolean alignedY = (pacman.y % tileSize == 0);

        if (alignedX && alignedY) {

            int testX = pacman.x;
            int testY = pacman.y;
            int step = tileSize / 4;


            switch (pacman.desiredDirection) {
                case 'U' -> testY -= step;
                case 'D' -> testY += step;
                case 'L' -> testX -= step;
                case 'R' -> testX += step;
            }

            // Test for wall collision
            boolean blocked = false;
            for (Block wall : walls) {
                Block temp = new Block(pacman.image, testX, testY, pacman.width, pacman.height);
                if (collision(temp, wall)) {
                    blocked = true;
                    break;
                }
            }

            // If not blocked turn
            if (!blocked) {
                pacman.updateDirection(pacman.desiredDirection);
                updatePacmanImage();
            }
        }
        // Store current position for wall collision check
        int oldX = pacman.x;
        int oldY = pacman.y;
        
        // Move Pac-Man
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
        
        // Check for tunnel teleportation 
        if (pacman.x + pacman.width < 0) {
            pacman.x = (columnCount - 1) * tileSize;     
            pacman.y = pacman.y - (pacman.y % tileSize); 
        }
        else if (pacman.x > boardWidth) {
            pacman.x = 0;                                
            pacman.y = pacman.y - (pacman.y % tileSize);
        }
        
        // Only check wall collision if we're not in the tunnel
        if (pacman.x > 0 && pacman.x + pacman.width < boardWidth) {
            boolean hitWall = false;
            for (Block wall : walls) {
                if (collision(pacman, wall)) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                pacman.x = oldX;
                pacman.y = oldY;
            }
        }
    }

    private void moveGhosts() {
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                handleGhostCollision();
                return;
            }
            moveGhost(ghost);
        }
    }

    private boolean isAlignedToGrid(Block block) {
        return block.x % tileSize == 0 && block.y % tileSize == 0;
    }

    private void moveGhost(Block ghost) {
        if (ghost.image == pinkGhostImage) {
            if (isAlignedToGrid(ghost)) {
                Node ghostNode = new Node(ghost.x / tileSize, ghost.y / tileSize);
                Node target = predictPacmanPosition();
                List<Node> path = findPath(ghostNode, target);

                if (path.size() > 1) {
                    Node next = path.get(1);
                    int dx = next.x - ghostNode.x;
                    int dy = next.y - ghostNode.y;

                    if (dx > 0) {
                        ghost.updateDirection('R');
                    } else if (dx < 0) {
                        ghost.updateDirection('L');
                    } else if (dy > 0) {
                        ghost.updateDirection('D');
                    } else if (dy < 0) {
                        ghost.updateDirection('U');
                    }
                }
            }
        } else if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
            ghost.updateDirection('U');
        }

        ghost.x += ghost.velocityX;
        ghost.y += ghost.velocityY;
        checkWallCollision(ghost);
    }

    private void checkWallCollision(Block block) {
        for (Block wall : walls) {
            if (collision(block, wall) || block.x <= 0 || block.x + block.width >= boardWidth) {
                block.x -= block.velocityX;
                block.y -= block.velocityY;
                if (block != pacman) {
                    char newDirection = directions[random.nextInt(4)];
                    block.updateDirection(newDirection);
                }
                break;
            }
        }
    }

    private void handleGhostCollision() {
        lives -= 1;
        if (lives == 0) {
            gameOver = true;
        } else {
            resetPositions();
        }
    }

    private void checkFoodCollision() {
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            restartGame();
        } else {
            handleKeyPress(e);
        }
    }

    private void restartGame() {
        loadMap();
        resetPositions();
        lives = 3;
        score = 0;
        gameOver = false;
        gameLoop.start();
    }

    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> pacman.desiredDirection = 'U';
            case KeyEvent.VK_DOWN -> pacman.desiredDirection = 'D';
            case KeyEvent.VK_LEFT -> pacman.desiredDirection = 'L';
            case KeyEvent.VK_RIGHT -> pacman.desiredDirection = 'R';
        }
        updatePacmanImage();
    }

    private void updatePacmanImage() {
        switch (pacman.direction) {
            case 'U' -> pacman.image = pacmanUpImage;
            case 'D' -> pacman.image = pacmanDownImage;
            case 'L' -> pacman.image = pacmanLeftImage;
            case 'R' -> pacman.image = pacmanRightImage;
        }
    }
    
    public boolean isWalkable(int r, int c) {
        int px = c * tileSize;
        int py = r * tileSize;

        // Create a temporary 1-tile block for collision testing
        Block temp = new Block(null, px, py, tileSize, tileSize);

        // If temp collides with any wall block, it's not walkable
        for (Block wall : walls) {
            if (collision(temp, wall)) {
                return false;
            }
        }
        return true;
    }
}
