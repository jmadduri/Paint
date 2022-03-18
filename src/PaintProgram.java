import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import javax.swing.event.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PaintProgram extends JPanel implements MouseMotionListener, ActionListener, MouseListener, AdjustmentListener, ChangeListener, KeyListener
{
    JFrame frame;
    ArrayList<Point> points;
    Color currentColor, backgroundColor, oldColor;
    JMenuBar bar;
    Shape currentShape;
    JMenu colorMenu, fileMenu;
    JMenuItem[] colorOptions;
    JMenuItem save, load, clear, exit;
    Color[] colors;
    Stack<Object> shapes, undoRedoStack;
    int penWidth, currentX, currentY, currentWidth, currentHeight;
    JScrollBar penWidthBar;
    JColorChooser colorChooser;
    BufferedImage loadedImage;
    JFileChooser fileChooser;
    JButton freeLineButton, rectangleButton, ovalButton, undoButton, redoButton, eraser;
    ImageIcon freeLineImage, rectangleImage, ovalImage, loadImage, saveImage, undoImage, redoImage, eraserImage;
    boolean drawingLine = true, drawingRectangle = false, drawingOval = false, eraserOn = false;
    boolean firstClick = true;
    boolean shiftPressed;

    public PaintProgram()
    {
        frame = new JFrame("Paint");
        frame.add(this);
        bar = new JMenuBar();
        colorMenu = new JMenu("Color Options");
        colors = new Color[]{Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA};
        colorOptions = new JMenuItem[colors.length];
        colorMenu.setLayout(new GridLayout(7, 1));
        for(int i = 0; i<colorOptions.length; i++)
        {
            colorOptions[i] = new JMenuItem();
            colorOptions[i].putClientProperty("colorIndex", i);
            colorOptions[i].setBackground(colors[i]);
            colorOptions[i].addActionListener(this);
            colorOptions[i].setPreferredSize(new Dimension(50, 30));
            colorOptions[i].setFocusable(false);
            colorMenu.add(colorOptions[i]);
        }
        currentColor = colors[0];
        colorChooser = new JColorChooser();
        colorChooser.getSelectionModel().addChangeListener(this);
        colorMenu.add(colorChooser);
        points = new ArrayList<>();
        shapes = new Stack<Object>();
        undoRedoStack = new Stack<>();

        penWidthBar = new JScrollBar(JScrollBar.HORIZONTAL, 1, 0, 1, 40);
        penWidthBar.addAdjustmentListener(this);
        penWidthBar.setFocusable(false);
        penWidth = penWidthBar.getValue();

        fileMenu = new JMenu("File");
        save = new JMenuItem("Save", KeyEvent.VK_S);
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

        load = new JMenuItem("Load", KeyEvent.VK_L);
        load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));

        clear = new JMenuItem("New");
        exit = new JMenuItem("Exit");

        saveImage = new ImageIcon("src/saveImg.png");
        saveImage = new ImageIcon(saveImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        loadImage = new ImageIcon("src/loadImg.png");
        loadImage = new ImageIcon(loadImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        save.setIcon(saveImage);
        load.setIcon(loadImage);

        save.addActionListener(this);
        load.addActionListener(this);
        clear.addActionListener(this);
        exit.addActionListener(this);

        fileMenu.add(clear);
        fileMenu.add(load);
        fileMenu.add(save);
        fileMenu.add(exit);

        String currentDir = System.getProperty("user.dir");
        fileChooser = new JFileChooser(currentDir);

        freeLineImage = new ImageIcon("src/freeLineImg.png");
        freeLineImage = new ImageIcon(freeLineImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        freeLineButton = new JButton();
        freeLineButton.setIcon(freeLineImage);
        freeLineButton.setFocusPainted(false);
        freeLineButton.setBackground(Color.LIGHT_GRAY);
        freeLineButton.setFocusable(false);
        freeLineButton.addActionListener(this);

        rectangleImage = new ImageIcon("src/rectImg.png");
        rectangleImage = new ImageIcon(rectangleImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        rectangleButton = new JButton();
        rectangleButton.setIcon(rectangleImage);
        rectangleButton.setFocusPainted(false);
        rectangleButton.setFocusable(false);
        //rectangleButton.setBackground(Color.LIGHT_GRAY);
        rectangleButton.addActionListener(this);

        ovalImage = new ImageIcon("src/ovalImg.png");
        ovalImage = new ImageIcon(ovalImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        ovalButton = new JButton();
        ovalButton.setIcon(ovalImage);
        ovalButton.setFocusPainted(false);
        ovalButton.setFocusable(false);
        //ovalButton.setBackground(Color.LIGHT_GRAY);
        ovalButton.addActionListener(this);

        eraserImage = new ImageIcon("src/eraserImg.png");
        eraserImage = new ImageIcon(eraserImage.getImage().getScaledInstance(20,20, Image.SCALE_SMOOTH));
        eraser = new JButton();
        eraser.setIcon(eraserImage);
        eraser.setFocusPainted(false);
        eraser.setFocusable(false);
        eraser.addActionListener(this);

        undoImage = new ImageIcon("src/undoImg.png");
        undoImage = new ImageIcon(undoImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        undoButton = new JButton();
        undoButton.setIcon(undoImage);
        undoButton.setFocusPainted(false);
        undoButton.setFocusable(false);
        undoButton.addActionListener(this);

        redoImage = new ImageIcon("src/redoImg.png");
        redoImage = new ImageIcon(redoImage.getImage().getScaledInstance(20,20, Image.SCALE_SMOOTH));
        redoButton = new JButton();
        redoButton.setIcon(redoImage);
        redoButton.setFocusPainted(false);
        redoButton.setFocusable(false);
        redoButton.addActionListener(this);

        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        bar.add(fileMenu);
        bar.add(colorMenu);
        bar.add(freeLineButton);
        bar.add(rectangleButton);
        bar.add(ovalButton);
        bar.add(eraser);
        bar.add(undoButton);
        bar.add(redoButton);
        bar.add(penWidthBar);
        shiftPressed = false;
        backgroundColor = Color.WHITE;
        frame.addKeyListener(this);
        frame.add(bar, BorderLayout.NORTH);
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(backgroundColor);
        g2.fillRect(0, 0, frame.getWidth(), frame.getHeight());
        if(loadedImage != null)
        {
            g2.drawImage(loadedImage, 0, 0, null);
        }
        Iterator iterator = shapes.iterator();
        while(iterator.hasNext())
        {
            Object next = iterator.next();
            if(next instanceof Rectangle)
            {
                Rectangle temp = (Rectangle)next;
                g2.setColor(temp.getColor());
                g2.setStroke(new BasicStroke(temp.getPenWidth()));
                g2.draw(temp.getShape());
            }
            else if(next instanceof Oval)
            {
                Oval temp = (Oval) next;
                g2.setColor(temp.getColor());
                g2.setStroke(new BasicStroke(temp.getPenWidth()));
                g2.draw(temp.getShape());
            }
            else
            {
                ArrayList<?> temp = (ArrayList<?>)next;
                if(temp.size() > 0)
                {
                    g2.setStroke(new BasicStroke(((Point)temp.get(0)).getPenWidth()));
                    for (int i = 0; i < temp.size() - 1; i++) {
                        Point p1 = (Point)(temp.get(i));
                        Point p2 = (Point)(temp.get(i + 1));
                        g2.setColor(p1.getColor());
                        g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                    }
                }
            }
        }
        if(drawingLine && points.size() > 0)
        {
            g2.setStroke(new BasicStroke(points.get(0).getPenWidth()));
            g2.setColor(points.get(0).getColor());
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }
    }
    public void save()
    {
        FileFilter filter = new FileNameExtensionFilter("*.png", "png");
        fileChooser.setFileFilter(filter);
        if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            try
            {
                String str = file.getAbsolutePath();
                if(str.indexOf(".png") >= 0)
                {
                    str = str.substring(0, str.length()-4);
                }
                ImageIO.write(createImage(), "png", new File(str + ".png"));
            }catch(IOException ioException){}
        }
    }
    public void load()
    {
        fileChooser.showSaveDialog(null);
        File imageFile = fileChooser.getSelectedFile();
        if(imageFile != null && imageFile.toString().indexOf(".png") >= 0)
        {
            try
            {
                loadedImage = ImageIO.read(imageFile);
            }catch (IOException ioException){}
            shapes = new Stack<>();
            repaint();
        }
        else
        {
            if(imageFile != null)
            {
                JOptionPane.showMessageDialog(null, "Wrong File Type. Please select a .PNG file.");
            }
        }
    }
    public void undo()
    {
        if(!shapes.isEmpty())
        {
            undoRedoStack.push(shapes.pop());
            repaint();
        }
    }
    public void redo()
    {
        if(!undoRedoStack.isEmpty())
        {
            shapes.push(undoRedoStack.pop());
            repaint();
        }
    }
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == save)
        {
            save();
        }
        else if(e.getSource() == load)
        {
            load();
        }
        else if(e.getSource() == clear)
        {
            shapes = new Stack<>();
            loadedImage = null;
            repaint();
        }
        else if(e.getSource() == exit)
        {
            System.exit(0);
        }
        else if(e.getSource() == freeLineButton)
        {
            drawingLine = true;
            drawingRectangle = false;
            drawingOval = false;
            eraserOn = false;
            freeLineButton.setBackground(Color.LIGHT_GRAY);
            rectangleButton.setBackground(null);
            ovalButton.setBackground(null);
            eraser.setBackground(null);
            currentColor = oldColor;
        }
        else if(e.getSource() == rectangleButton)
        {
            drawingLine = false;
            drawingRectangle = true;
            drawingOval = false;
            eraserOn = false;
            freeLineButton.setBackground(null);
            rectangleButton.setBackground(Color.LIGHT_GRAY);
            ovalButton.setBackground(null);
            eraser.setBackground(null);
            currentColor = oldColor;
        }
        else if(e.getSource() == ovalButton)
        {
            drawingLine = false;
            drawingRectangle = false;
            drawingOval = true;
            eraserOn = false;
            freeLineButton.setBackground(null);
            rectangleButton.setBackground(null);
            ovalButton.setBackground(Color.LIGHT_GRAY);
            eraser.setBackground(null);
            currentColor = oldColor;
        }
        else if(e.getSource() == eraser)
        {
            drawingLine = false;
            drawingRectangle = false;
            drawingOval = true;
            eraserOn = true;
            freeLineButton.setBackground(null);
            rectangleButton.setBackground(null);
            ovalButton.setBackground(null);
            eraser.setBackground(Color.LIGHT_GRAY);
            oldColor = currentColor;
            currentColor = backgroundColor;
        }
        else if(e.getSource() == undoButton)
        {
            undo();
        }
        else if(e.getSource() == redoButton)
        {
            redo();
        }
        else
        {
            if(eraserOn)
            {
                drawingLine = true;
                drawingRectangle = false;
                drawingOval = false;
                eraserOn = false;
                freeLineButton.setBackground(Color.LIGHT_GRAY);
                rectangleButton.setBackground(null);
                ovalButton.setBackground(null);
                eraser.setBackground(null);
            }
            int index = (int) ((JMenuItem) e.getSource()).getClientProperty("colorIndex");
            currentColor = colors[index];
        }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if(drawingRectangle || drawingOval)
        {
            if(firstClick)
            {
                currentX = e.getX();
                currentY = e.getY();
                if(drawingRectangle)
                {
                    currentShape = new Rectangle(currentX, currentY, currentColor, penWidth, 0, 0);
                }
                else
                {
                    currentShape = new Oval(currentX, currentY, currentColor, penWidth, 0, 0);
                }
                firstClick = false;
                shapes.push(currentShape);
            }
            else
            {
                currentWidth = Math.abs(e.getX() - currentX);
                currentHeight = Math.abs(e.getY() - currentY);
                currentShape.setWidth(currentWidth);
                currentShape.setHeight(currentHeight);
                if(currentX <= e.getX() && currentY >= e.getY())
                {
                    currentShape.setY(e.getY());
                }
                else if(currentX >= e.getX() && currentY <= e.getY())
                {
                    currentShape.setX(e.getX());
                }
                else if(currentX >= e.getX() && currentY >= e.getY())
                {
                    currentShape.setX(e.getX());
                    currentShape.setY(e.getY());
                }
            }
        }
        else
        {
            points.add(new Point(e.getX(), e.getY(), currentColor, penWidth));
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {

    }
    public static void main(String[]args)
    {
        PaintProgram app = new PaintProgram();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(drawingRectangle || drawingOval)
        {
            firstClick = true;
        }
        else
        {
            shapes.push(points);
            points = new ArrayList<>();
        }
        undoRedoStack = new Stack<>();
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        penWidth = penWidthBar.getValue();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        currentColor = colorChooser.getColor();
    }
    public BufferedImage createImage()
    {
        int width=this.getWidth();
        int height=this.getHeight();
        BufferedImage img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2=img.createGraphics();
        this.paint(g2);
        g2.dispose();
        return img;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.isControlDown())
        {
            if(e.getKeyCode() == KeyEvent.VK_Z)
            {
                undo();
            }
            if(e.getKeyCode() == KeyEvent.VK_Y)
            {
                redo();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        shiftPressed = false;
    }

    public class Point
    {
        int x, y, penWidth;
        Color color;
        public Point(int x, int y, Color color, int penWidth)
        {
            this.x = x;
            this.y = y;
            this.color = color;
            this.penWidth = penWidth;
        }
        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }
        public Color getColor() {
            return color;
        }
        public int getPenWidth() {
            return penWidth;
        }
        public void setX(int x)
        {
            this.x = x;
        }
        public void setY(int y)
        {
            this.y = y;
        }
    }
    public class Shape extends Point
    {
        private  int width, height;
        public Shape(int x, int y, Color color, int penWidth, int width, int height)
        {
            super(x,y,color,penWidth);
            this.width = width;
            this.height = height;
        }
        public int getWidth()
        {
            return width;
        }
        public int getHeight()
        {
            return height;
        }
        public void setWidth(int width)
        {
            this.width = width;
        }
        public void setHeight(int height)
        {
            this.height = height;
        }
    }
    public class Rectangle extends Shape
    {
        public Rectangle(int x, int y, Color color, int penWidth, int width, int height)
        {
            super(x, y, color, penWidth, width, height);
        }
        public Rectangle2D.Double getShape()
        {
            return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
        }
    }
    public class Oval extends Shape
    {
        public Oval(int x, int y, Color color, int penWidth, int width, int height)
        {
            super(x, y, color, penWidth, width, height);
        }
        public Ellipse2D.Double getShape()
        {
            return new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight());
        }
    }
}
