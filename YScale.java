
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * Esta clase maneja la escala del eje Y (potencia).
 * 
 * @author Vicente Gonz'alez Ruiz
 * @version 28-Mayo-2004
 */
public class YScale {
    private JFrame frame;
    private JPanel panel;
    private JLabel scaleLabel, stepLabel;

    /* Factor de escala en el eje Y */
    private double scale, step;
 
    class ScaleControl extends JLabel implements MouseWheelListener {
        
        ScaleControl(String str, int horizontalAlignment){
            super(str,horizontalAlignment);
            super.addMouseWheelListener(this);
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int wheelRotationDir = e.getWheelRotation();
            if(wheelRotationDir < 0) {
                scale -= step;
                if(scale <= 1.0) scale = 1.0;
            } else {
                scale += step;
            }
            this.setText("Scale = " + scale);
        }
    }

    class StepControl extends JPanel implements ActionListener {
        
        String initialStepString;
        String x100String = "Step = 100.0";
        String x10String = "Step = 10.0";
        String x1String = "Step = 1.0";
        
        double initialStep;
        
        StepControl(double initialStep) {
            this.initialStep = initialStep;
            Double initialStepObj = new Double(initialStep);
            initialStepString = new String("Step = " + initialStepObj.toString());
            
            // 1. Creating the radio buttons
            JRadioButton initialStepButton = new JRadioButton(initialStepString);
            initialStepButton.setActionCommand(initialStepString);
            initialStepButton.setSelected(true);

            JRadioButton x100Button = new JRadioButton(x100String);
            x100Button.setActionCommand(x100String);
            //x100Button.setMnemonic(KeyEvent.VK_A);
            //x100Button.setSelected(true);
            
            JRadioButton x10Button = new JRadioButton(x10String);
            x10Button.setActionCommand(x10String);

            JRadioButton x1Button = new JRadioButton(x1String);
            x1Button.setActionCommand(x1String);
            
            // 2. Grouping the radio buttons
            ButtonGroup group = new ButtonGroup();
            group.add(initialStepButton);
            group.add(x100Button);
            group.add(x10Button);            
            group.add(x1Button);
            
            // 3. Registering a listener for the radio buttons
            initialStepButton.addActionListener(this);
            x100Button.addActionListener(this);
            x10Button.addActionListener(this);            
            x1Button.addActionListener(this);
            
            // 4. Put the radio buttons in a column in a panel
            JPanel radioPanel = new JPanel(new GridLayout(0,1));
            radioPanel.add(initialStepButton);
            radioPanel.add(x100Button);
            radioPanel.add(x10Button);
            radioPanel.add(x1Button);
            
            add(radioPanel, BorderLayout.LINE_START);
            setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        }
        
        // Listens to the radio buttons
        public void actionPerformed(ActionEvent e) {
            String choice = e.getActionCommand();
            if(choice == x100String) {
                step = 100;
            } else if(choice == x10String) {
                step = 10;
            } else if(choice == x10String) {
                step = 1;
            } else {
                step = initialStep;
            }
        }
    }
    /**
     * Constructor for objects of class YScale
     */
    public YScale(double scale, double step/*, JFrame panel*/) {
        this.scale = scale;
        this.step = step;

        // Create and set up the window
        frame = new JFrame("RTASA - Y-Scale Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(100,50);
        
        // Create and set up the panel
        panel = new JPanel(new GridLayout(1,0));
        
        // Create and add the widgets
        Double scaleObj = new Double(scale);
        Double stepObj = new Double(step);
        ScaleControl scaleLabel2 = new ScaleControl("Scale = " + scaleObj.toString(), SwingConstants.LEFT);
        StepControl stepLabel = new StepControl(step);
        panel.add(scaleLabel2);
        panel.add(stepLabel);
        
        // Add the panel to the window
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        // Display the window
        frame.pack();
        frame.setVisible(true);

        //Register for mouse-wheel events on the text area.
        //scaleLabel.addMouseWheelListener(this);
    }

    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public double getScale() {
        return scale;
    }

}
