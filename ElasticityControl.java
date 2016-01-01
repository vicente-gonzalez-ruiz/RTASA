import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.NumberFormatter;
import java.beans.*;
import java.util.*;

/**
 * Esta clase maneja la elasticidad del espectro azul.
 * 
 * @author Vicente Gonz'alez Ruiz
 * @version 1-Junio-2004
 */

/*
 * SliderDemo3.java is a 1.4 application that requires all the
 * files in the images/doggy directory.  It adds a formatted text
 * field to SliderDemo.java.
 */
public class ElasticityControl extends JPanel implements /*ActionListener,*/ WindowListener, ChangeListener, PropertyChangeListener {

    static final int ELASTICITY_MIN = 0;
    static final int ELASTICITY_MAX = 1000;
    static final int ELASTICITY_INIT = 50;    // Initial elasticity
    double elasticity = (double)ELASTICITY_INIT/(double)ELASTICITY_MAX;
    JFormattedTextField textField;
    JSlider slider;

    public ElasticityControl() {
        // Colocamos el textField y el slider uno encima de otro
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // Creamos el label del slider
        JLabel sliderLabel = new JLabel("Elasticity (x1000): ", JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create the formatted text field and its formatter.
        java.text.NumberFormat numberFormat = java.text.NumberFormat.getIntegerInstance();
        NumberFormatter formatter = new NumberFormatter(numberFormat);
        formatter.setMinimum(new Double((double)ELASTICITY_MIN/(double)ELASTICITY_MAX));
        formatter.setMaximum(new Double((double)ELASTICITY_MAX));
        textField = new JFormattedTextField(formatter);
        textField.setValue(new Integer(ELASTICITY_INIT));
        textField.setColumns(3); //get some space
        textField.addPropertyChangeListener(this);

        //React when the user presses Enter.
        textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),"check");
        textField.getActionMap().put("check", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!textField.isEditValid()) { //The text is invalid.
                    Toolkit.getDefaultToolkit().beep();
                    textField.selectAll();
                } else try {                    //The text is valid,
                    textField.commitEdit();     //so use it.
                } catch (java.text.ParseException exc) { }
            }
        });
        
        //Create the slider.
        slider = new JSlider(JSlider.HORIZONTAL, ELASTICITY_MIN, ELASTICITY_MAX, ELASTICITY_INIT);
        slider.addChangeListener(this);

        //Turn on labels at major tick marks.
        slider.setMajorTickSpacing(500);
        slider.setMinorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

        // Le redefinimos las etiquetas al slider
        Dictionary labelTable = slider.getLabelTable();
        JLabel cero = new JLabel("0.0");
        JLabel puntocinco = new JLabel("0.5");
        JLabel uno = new JLabel("1.0");
        labelTable.put(new Integer(ELASTICITY_MIN), cero);
        labelTable.put(new Integer((ELASTICITY_MAX - ELASTICITY_MIN)/2), puntocinco);
        labelTable.put(new Integer(ELASTICITY_MAX), uno);
        slider.setLabelTable(labelTable);

        // Create a subpanel for the label and text field.
        JPanel labelAndTextField = new JPanel(); //use FlowLayout
        labelAndTextField.add(sliderLabel);
        labelAndTextField.add(textField);

        // Put everything together.
        add(labelAndTextField);
        add(slider);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        //createAndShowGUI();
        
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        
        //Create and set up the window.
        JFrame frame = new JFrame("RTASA - Elasticity Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Create and set up the content pane.
        //ElasticityControl animator = new ElasticityControl();
        this.setOpaque(true); //content panes must be opaque
        frame.setContentPane(this);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    double getElasticity() {
        return elasticity;
   }

    /** Add a listener for window events. */
    void addWindowListener(Window w) {}

    //React to window events.
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    /** Listen to the slider. */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        int elast = (int)source.getValue();
        elasticity = (double)elast/(double)ELASTICITY_MAX;
        if (!source.getValueIsAdjusting()) { //done adjusting
            textField.setValue(new Integer(elast));
        } else { //value is adjusting; just set the text
            textField.setText(String.valueOf(elast));
        }
    }

    /**
     * Listen to the text field.  This method detects when the
     * value of the text field (not necessarily the same
     * number as you'd get from getText) changes.
     */
    public void propertyChange(PropertyChangeEvent e) {
        if ("value".equals(e.getPropertyName())) {
            Number value = (Number)e.getNewValue();
            if (slider != null && value != null) {
                slider.setValue(value.intValue());
            }
        }
    }
}


