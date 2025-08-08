// Pong.java
// Um Pong simples usando Swing — W/S para jogador esquerdo, ↑/↓ para jogador direito, Space para pausar.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Pong extends JPanel implements ActionListener {
    // Janela
    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;

    // Paddles
    private final int PADDLE_WIDTH = 12;
    private final int PADDLE_HEIGHT = 80;
    private int leftY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private int rightY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private final int PADDLE_SPEED = 6;

    // Bola
    private int ballX = WIDTH / 2;
    private int ballY = HEIGHT / 2;
    private int ballSize = 14;
    private int ballVX = 5; // velocidade inicial x
    private int ballVY = 3; // velocidade inicial y

    // Pontuação
    private int leftScore = 0;
    private int rightScore = 0;

    // Estados de controle
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean paused = false;

    // Timer (game loop)
    private Timer timer;

    public Pong() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Game loop ~60 FPS
        timer = new Timer(1000 / 60, this);
        timer.start();

        // Input
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> wPressed = true;
                    case KeyEvent.VK_S -> sPressed = true;
                    case KeyEvent.VK_UP -> upPressed = true;
                    case KeyEvent.VK_DOWN -> downPressed = true;
                    case KeyEvent.VK_SPACE -> paused = !paused;
                    case KeyEvent.VK_R -> resetScores();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> wPressed = false;
                    case KeyEvent.VK_S -> sPressed = false;
                    case KeyEvent.VK_UP -> upPressed = false;
                    case KeyEvent.VK_DOWN -> downPressed = false;
                }
            }
        });
    }

    private void resetScores() {
        leftScore = 0;
        rightScore = 0;
        resetBall(true);
    }

    private void resetBall(boolean toRight) {
        ballX = WIDTH / 2 - ballSize / 2;
        ballY = HEIGHT / 2 - ballSize / 2;
        // define direção aleatória vertical pequena e horizontal dependendo de quem venceu
        ballVX = (toRight ? 5 : -5);
        ballVY = (Math.random() > 0.5 ? 3 : -3);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused) {
            updatePaddles();
            updateBall();
        }
        repaint();
    }

    private void updatePaddles() {
        if (wPressed) leftY -= PADDLE_SPEED;
        if (sPressed) leftY += PADDLE_SPEED;
        if (upPressed) rightY -= PADDLE_SPEED;
        if (downPressed) rightY += PADDLE_SPEED;

        // Limites
        if (leftY < 0) leftY = 0;
        if (leftY + PADDLE_HEIGHT > HEIGHT) leftY = HEIGHT - PADDLE_HEIGHT;
        if (rightY < 0) rightY = 0;
        if (rightY + PADDLE_HEIGHT > HEIGHT) rightY = HEIGHT - PADDLE_HEIGHT;
    }

    private void updateBall() {
        ballX += ballVX;
        ballY += ballVY;

        // Colisão superior/inferior
        if (ballY <= 0) {
            ballY = 0;
            ballVY = -ballVY;
        } else if (ballY + ballSize >= HEIGHT) {
            ballY = HEIGHT - ballSize;
            ballVY = -ballVY;
        }

        // Colisão com paddles - checagem AABB simples ajustada
        Rectangle ballRect = new Rectangle(ballX, ballY, ballSize, ballSize);
        Rectangle leftPaddle = new Rectangle(20, leftY, PADDLE_WIDTH, PADDLE_HEIGHT);
        Rectangle rightPaddle = new Rectangle(WIDTH - 20 - PADDLE_WIDTH, rightY, PADDLE_WIDTH, PADDLE_HEIGHT);

        if (ballRect.intersects(leftPaddle)) {
            ballX = 20 + PADDLE_WIDTH; // empurra para fora do paddle
            ballVX = Math.abs(ballVX) + 1; // acelera levemente
            // ajustar VY baseado no ponto de impacto (controle de "efeito")
            int paddleCenter = leftY + PADDLE_HEIGHT / 2;
            int relativeY = ballY + ballSize / 2 - paddleCenter;
            ballVY = relativeY / 7; // divisor para controlar sensibilidade
            if (ballVY == 0) ballVY = (Math.random() > 0.5 ? 1 : -1);
        } else if (ballRect.intersects(rightPaddle)) {
            ballX = WIDTH - 20 - PADDLE_WIDTH - ballSize;
            ballVX = -Math.abs(ballVX) - 1;
            int paddleCenter = rightY + PADDLE_HEIGHT / 2;
            int relativeY = ballY + ballSize / 2 - paddleCenter;
            ballVY = relativeY / 7;
            if (ballVY == 0) ballVY = (Math.random() > 0.5 ? 1 : -1);
        }

        // Se passou pela esquerda ou direita => ponto
        if (ballX < -ballSize) {
            rightScore++;
            resetBall(true);
        } else if (ballX > WIDTH + ballSize) {
            leftScore++;
            resetBall(false);
        }

        // Limitar velocidade máxima para evitar runaway
        int maxSpeed = 14;
        if (ballVX > maxSpeed) ballVX = maxSpeed;
        if (ballVX < -maxSpeed) ballVX = -maxSpeed;
        if (ballVY > maxSpeed) ballVY = maxSpeed;
        if (ballVY < -maxSpeed) ballVY = -maxSpeed;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Antialias e melhor renderização
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background já preto
        // Linha central tracejada
        g2.setColor(Color.DARK_GRAY);
        for (int y = 0; y < HEIGHT; y += 20) {
            g2.fillRect(WIDTH / 2 - 2, y, 4, 12);
        }

        // Desenhar paddles e bola
        g2.setColor(Color.WHITE);
        g2.fillRect(20, leftY, PADDLE_WIDTH, PADDLE_HEIGHT);
        g2.fillRect(WIDTH - 20 - PADDLE_WIDTH, rightY, PADDLE_WIDTH, PADDLE_HEIGHT);
        g2.fillOval(ballX, ballY, ballSize, ballSize);

        // Pontuação
        g2.setFont(new Font("Consolas", Font.BOLD, 36));
        String scoreText = leftScore + "   " + rightScore;
        FontMetrics fm = g2.getFontMetrics();
        int tx = WIDTH / 2 - fm.stringWidth(scoreText) / 2;
        g2.drawString(scoreText, tx, 50);

        // Mensagens
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("W/S - Jogador Esquerdo   |   ↑/↓ - Jogador Direito   |   Space - Pausar   |   R - Reset", 12, HEIGHT - 12);

        // Pausa
        if (paused) {
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            String p = "PAUSED";
            int px = WIDTH / 2 - g2.getFontMetrics().stringWidth(p) / 2;
            g2.drawString(p, px, HEIGHT / 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pong - Java (simples)");
            Pong game = new Pong();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

