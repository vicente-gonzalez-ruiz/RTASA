/* Real Time Audio Spectrum Analyzer. */

/*
 * Versi'on inicial creada por Vicente Gonz'alez Ruiz <vruiz@ual.es>.
 *
 * Mejoras introducidas por Manuel Mar'in <niram@eresmas.net>
 *   referentes al c'alculo de la frecuencia con el puntero del rat'on.
 *
 * gse. 2006.
 */

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.DatagramPacket;
import java.io.IOException;

public class RTASA
    extends JComponent
    implements Runnable,
	       ComponentListener,MouseMotionListener {
    
    // Variables y metodos relacionados con el rat'on.  
    
    //Variables para almacenar la posici'on actual del rat'on
    Dimension d;
    int posX = 0, posY=0; // Posici'on del rat'on
    int visualiza_frec;
    
    // Inicializacion de la funcion de rastreo del raton.
    public void initMouse() {
	d = this.getSize();
	addMouseMotionListener(this);
    }

    // M'etodo de realizaci'on de acciones en el caso de arrastrar y
    // soltar.
    public void mouseDragged(MouseEvent me) {}
    
    // M'etodo de realizaci'on de acciones en el caso de movimiento
    // del rat'on dentro de la pantalla gr'afica. Dichas acciones
    // consisten en calcular el valor de la frecuencia sobre el que
    // esta situado el puntero del rat'on. Para realizar el calculo,
    // se multiplica la variable bandWidth por la coordenada X de la
    // posici'on actual del rat'on en ese momento, restandole a dicho
    // resultado el desplazamiento de 20 pixeles existente, ya que la
    // abcisa empieza en el pixel 20. Para el c'alculo del valor de
    // este desplazamiento se multiplica la variable bandWidth por
    // 20. El valor final de toda esta operaci'on lo recoge la
    // variable visualiza_frec.
    public void mouseMoved(MouseEvent me) {
	// Si el raton est'a situado a la derecha de los 20 pixeles de
	// xoffset.
	if (me.getX()>=20){ 
	    // Obtiene la frecuencia con la coordenada x.
	    visualiza_frec = (int)(bandWidth*me.getX()-(bandWidth*20)); 
	    // Escribe en la consola de texto el valor en Hz. Se puede
	    // prescindir de esta l'inea ya que dicho valor ya aparece
	    // en la pantalla grafica.
	    //System.err.println("Frecuencia: " + visualiza_frec +" Hz");
	}
	// Si se sale del margen izquierdo, para evitar escribir
	// valores negativos.
	else { 
	    visualiza_frec = 0;    
	    System.err.println("Frecuencia: " + visualiza_frec +" Hz");
	}
	
	// Se almacenan las coordenadas actuales "x" e "y" del rat'on.
	posX = me.getX();
	posY = me.getY();  
	
	//Actualizamos la pantalla gr'afica.
	this.repaint(); 
    }
    
    /* Fin de las variables y m'etodos relacionados con el rat'on.

    /* N'umero de bandas (por defecto). */
    static int NUMBER_OF_BANDS = 512;
    
    /* N'umero de muestras por segundo (por defecto). */
    static int SAMPLE_RATE = 44100;

    /* A FFT will be performed each STEP samples. */
    static int STEP = 512;
    
    /* Altura inicial de la ventana. */
    static int WINDOW_HEIGHT = 512;
    
    /* Dimensiones de la ventana. */
    int windowHeight, windowWidth;

    JFrame frame;
    Thread thread; /* Este hilo */

    /* N'umero de bytes en cada muestra de audio. */
    int bytesPerSample = 2;

    /* N'umero de canales. */
    int channels = 2;

    /* N'umero de muestras por segundo (frecuencia de muestreo). */
    float sampleRate;

    /* Ancho de banda mostrado. */
    float totalBandWidth;

    /* Anchura de cada banda. */
    float bandWidth;
 
    YScale yScale;
    ElasticityControl ec;
    double elasticity;
 
    /* Factor de escala en el eje Y. */
    //float scale;
    
    /* Buffer de audio. */
    byte[] audioBuffer;

    /* Tama~no del buffer de audio en bytes. */
    int audioBufferSize;

    double[] leftSpectrum;
    double[] rightSpectrum;
    double[][] leftBuffer;
    double[][] rightBuffer;
    double[] window;
    
    /* Arena encima del espectro */
    int[] leftSand, rightSand;

    /* Gravedad aplicada a la arena (0 -> ausencia de gravedad, 1 ->
     * gravedad infinita). */
    double sandGravity=0.1;

    /* Tama~no del grano de arena */
    int sandSize = 10;

    /* N'umero de bandas. */
    int numberOfBands;

    int step_bytes;

    /* N'umero de muestras de audio. */
    int numberOfSamples;

    /* Number of samples avaiable from the last read. */
    int availableSamples;

    //int numberOfSpectrumsPerPacket;

    //static final int PACKET_SIZE = 1024;
    //static final int SIZEOF_SAMPLE = 2;
    //static final int BUF_SIZE = 4096;

    //static final int PORT = 6789;
    //DatagramSocket socket;
    //DatagramPacket sendPacket;
    //DatagramPacket receivePacket;

    /* Un color. */
    Color rojoOscuro;

    public RTASA(int numberOfBands, float sampleRate, int step) throws IOException {
        System.err.println("+-----------------------------------+");
        System.err.println("| Real Time Audio Spectrum Analizer |");
        System.err.println("+-----------------------------------+");
        this.sampleRate = sampleRate;
        System.err.println("Sample Rate = " + sampleRate + " samples/second");
	this.numberOfBands = numberOfBands;
	step_bytes = step * 4; // 2 bytes/sample, 2 channels

	/* Comprobamos que el n'umero de bandas sea una potencia de dos. */ {
	    int tmp = numberOfBands;
	    boolean numberOfBandsIsAPowerOfTwo = true;
	    int i = 0;
	    while(tmp>1) {
		if((tmp%2)==1) {
		    numberOfBandsIsAPowerOfTwo = false;
		    break;
		}
		tmp >>= 1;
		i++;
	    }
	    if(numberOfBandsIsAPowerOfTwo==false) {
		System.err.println("I need a number of bands power of two ...");
		numberOfSamples = (numberOfSamples>>i);
	    }
	}

        System.err.println("Number of bands = " + numberOfBands);
        numberOfSamples = numberOfBands*2;
	System.err.println("Number of samples = " + numberOfSamples);

	audioBufferSize = channels * numberOfSamples * bytesPerSample;
	audioBuffer = new byte[audioBufferSize];
	//numberOfSpectrumsPerPacket = BUF_SIZE/numberOfSamples/2;
        leftBuffer = new double[numberOfSamples][2];
        rightBuffer = new double[numberOfSamples][2];
        leftSpectrum = new double[numberOfSamples];
        rightSpectrum = new double[numberOfSamples];
        leftSand = new int[numberOfSamples];
        rightSand = new int[numberOfSamples];

	/* Calculamos los coeficientes de la ventana temporal. */
        //computeWindow(1);
        window = new double[numberOfSamples];
	Window.computeCoefficients(1/* Ventana tipo 1*/, window);

        totalBandWidth = computeTotalBandWidth(sampleRate);
        System.err.println("Total Band Width = " + totalBandWidth + " Hz");
        bandWidth = computeBandWidth(sampleRate, numberOfBands);
        System.err.println("Band Width = " + bandWidth + " Hz");
        //setDoubleBuffered(true);
        
        // Create and set up the RTASA window
        frame = new JFrame("RTASA - Spectrum");
        
        //scale = numberOfSamples;
        // Controlador de la scala en el eje Y
        yScale = new YScale(numberOfSamples, numberOfSamples/10);

        frame.getContentPane().add(this);
	windowWidth = numberOfBands + 10;
	windowHeight = WINDOW_HEIGHT;
        frame.setSize(windowWidth, windowHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.addComponentListener(this);
	Container content = frame.getContentPane();
	content.setBackground(Color.black);
        frame.setVisible(true)/*show()*/;
        
        // Controlador de la elasticidad del espectro azul. */
        ec = new  ElasticityControl();

	rojoOscuro = new Color(100, 0, 0);
	
	//Capturar movimiento del raton//
	initMouse();
    }

    /* Lanza el hilo */
    public void start() {
        thread = new Thread(this);
        thread.setName("RealTimeAudioSpectrumAnalizer");
        thread.start();
	System.err.println("Yo puedo seguir haciendo cosas aqu'i!");
	// for(;;) {
	//     int total = 0;
	//     while(total<audioBufferSize) {
	// 	try {
	// 	    System.out.write(audioBuffer,total,256);
	// 	    total += System.in.read(audioBuffer,total,/*audioBufferSize-total*/256);
	// 	    //System.err.print(total + " ");
	// 	    System.err.print(".");
	// 	    //Thread.sleep(10);
	// 	} catch (IOException e) {
	// 	    System.err.println("Error in the pipe.");
	// 	} /*catch (InterruptedException e) {
	// 	    System.err.println("InterruptedException.");
	// 	    }*/
	// 	availableSamples = total/4; /* 2 bytes/sample, 2 channels */
	// 	//ystem.out.write(audioBuffer,total,audioBufferSize-total);
	//     }
	//     //System.out.write(audioBuffer,0,audioBufferSize);
	// }
    }

    /* Detiene el hilo */
    public void stop() {
        thread = null;
    }

    public float computeTotalBandWidth(float sampleRate) {
        return sampleRate/2;
    }

    public float computeBandWidth(float sampleRate, int numberOfBands) {
        return (sampleRate/2)/numberOfBands;
    }

    public void setGravity(float gravity) {
        this.sandGravity = gravity;
	System.out.println(gravity);
    }

    public void run() {
	for(;;) {
	    
	    int total = 0;
	    while(total<audioBufferSize) {
		try {
		    if(step_bytes < (audioBufferSize-total)) {
			System.out.write(audioBuffer,total,step_bytes);
			total += System.in.read(audioBuffer,total,step_bytes);
		    } else {
			System.out.write(audioBuffer,total,audioBufferSize-total);
			total += System.in.read(audioBuffer,total,audioBufferSize-total);
		    }
		    //System.err.print(total + " ");
		    //System.err.print(".");
		    //Thread.sleep(10);
		} catch (IOException e) {
		    System.err.println("Error in the pipe.");
		} /*catch (InterruptedException e) {
		    System.err.println("InterruptedException.");
		    }*/
		availableSamples = total/4; /* 2 bytes/sample, 2 channels */

		/* We have availableSamples from the last read. These
		 * samples are going to be placed at the end of the arrays
		 * while the rest of data remain of previous reads. */
		for(int i=availableSamples; i<numberOfSamples; i++) {
		    leftBuffer[i-availableSamples][0]
			= (double)(audioBuffer[4*i+1]*256 +
				   audioBuffer[4*i]);
		    /* Parte imaginaria, muestra izquierda. */
		    leftBuffer[i-availableSamples][1] = 0.0;
		    /* Parte real, muestra derecha. */
		    rightBuffer[i-availableSamples][0]
			= (double)(audioBuffer[4*i+3]*256 +
				   audioBuffer[4*i+2]);
		    /* Parte imaginaria, muestra derecha. */
		    rightBuffer[i-availableSamples][1] = 0;
		}
		
		/* Now, we concatenate the new samples. */
		for(int i=0; i<availableSamples; i++) {
		    /* Parte real, muestra izquierda. */
		    leftBuffer[i+numberOfSamples-availableSamples][0]
			= (double)(audioBuffer[4*i+1]*256 +
				   audioBuffer[4*i]);
		    /* Parte imaginaria, muestra izquierda. */
		    leftBuffer[i+numberOfSamples-availableSamples][1] = 0.0;
		    /* Parte real, muestra derecha. */
		    rightBuffer[i+numberOfSamples-availableSamples][0]
			= (double)(audioBuffer[4*i+3]*256 +
				   audioBuffer[4*i+2]);
		    /* Parte imaginaria, muestra derecha. */
		    rightBuffer[i+numberOfSamples-availableSamples][1] = 0;
		}
		
		/* Multiplicamos cada muestra con el correspondiente
		 * coeficiente de la ventana temporal. */
		for(int i=0; i<numberOfSamples; i++) {
		    leftBuffer[i][0] *= window[i];
		    rightBuffer[i][0] *= window[i];
		}
		
		/* Transformada de Fourier del canal izquierdo. */
		FFT.direct(leftBuffer);
		/* Transformada de Fourier del canal derecho. */
		FFT.direct(rightBuffer);
		
		/* Obtenemos la elasticitad de la arena. */
		elasticity = ec.getElasticity();
		//System.err.println(elasticity);

		
		/* Calculamos el espectro (m'odulo). */
		for(int i=0; i<numberOfSamples; i++) {
		    leftSpectrum[i]
			= Math.sqrt(leftBuffer[i][0]*leftBuffer[i][0] +
				    leftBuffer[i][1]*leftBuffer[i][1]);
		    rightSpectrum[i]
			= Math.sqrt(rightBuffer[i][0]*rightBuffer[i][0] +
				    rightBuffer[i][1]*rightBuffer[i][1]);
		}
		
		/* Calculamos la arena. */
		for(int i=0; i<numberOfSamples; i++) {
		    leftSand[i]
			= (int)((1-elasticity)*leftSand[i] +
				elasticity*leftSpectrum[i]);
		    rightSand[i]
			= (int)((1-elasticity)*rightSand[i] +
				elasticity*rightSpectrum[i]);
		}
		
		/* Pintamos. */
		repaint();
	    }
	    
	}
    }
    
    // Este m'etodo no deber'ia estar aqu'i. Si creamos una clase para
    // controlar el tama~no de la ventana deber'ia de llamarse desde
    // all'i. Lo mismo deber'ia de ocurrir si creamos una clase para
    // controlar la frecuencia de muestreo.  Esto se debe de hacer as'i
    // porque s'olo cuando estas clases est'an trabajando es cuando debe
    // de cambiar la escala y no deber'ia de pintarse siempre que se
    // presenta el espectro.
    void drawHz(Graphics g) {
	Color color = Color.black;
	g.setColor(color);
	for(int i=10; i<numberOfSamples/2; i+=50) {
	    g.drawString(i*bandWidth + "", i, 10);
	}
    }
     
    /* Pinta la ventana */
    public void paintComponent(Graphics g) {
	int xOffset = 20;
	int yOffset = 25;
	Color color = Color.red;
	//g.setColor(color);

	/* Pintamos el espectro del canal izquiero arriba. */
	//color = new Color(255,0,0);
	g.setXORMode(new Color(255,0,0));
	for(int i=0; i<numberOfSamples/2/*256*/; i++) {
	    //Double y = new Double(spectrum[i]/numberOfSamples);
	    //int x = y.intValue();
	    int x = (int)(leftSpectrum[i]/yScale.getScale());
	    //System.err.print(spectrum[i] + " " + x + " ");
	    g.drawLine(i+xOffset, /*460*/yOffset, i+xOffset, /*460-x*/x+yOffset);
	    //paintLine(i,yOffset,i,x+yOffset,g);
	    //int val_i = (data[2*i]*256+data[2*i+1])/256;
	    //int val_i1 = (data[2*(i+1)]*256+data[2*(i+1)+1])/256;
	    //g.drawLine(i,val_i+128,i+1,val_i1+128);
	}

	/* Pintamos el espectro del canal derecho abajo. */
	g.setXORMode(/*Color.green*/new Color(0,255,0));
	for(int i=0; i<numberOfSamples/2/*256*/; i++) {
	    //Double y = new Double(spectrum[i]/numberOfSamples);
	    //int x = y.intValue();
	    int x = (int)(rightSpectrum[i]/yScale.getScale());
	    //System.err.print(rightSpectrum[i] + " " + x + " ");
	    g.drawLine(i+xOffset, /*460*/yOffset+windowHeight-85, i+xOffset, /*460-x*/windowHeight-85+yOffset-x);
	    //paintLine(i,yOffset,i,x+yOffset,g);
	    //int val_i = (data[2*i]*256+data[2*i+1])/256;
	    //int val_i1 = (data[2*(i+1)]*256+data[2*(i+1)+1])/256;
	    //g.drawLine(i,val_i+128,i+1,val_i1+128);
	}

	//color = Color.blue;
	//g.setColor(color);
	g.setXORMode(/*Color.blue*/new Color(0,0,255));
	for(int i=0; i<numberOfSamples/2; i++) {
	    int x = (int)(leftSand[i]/yScale.getScale());
	    g.drawLine(i+xOffset, /*460-x*/x+yOffset, i+xOffset, /*450-x*/x+sandSize+yOffset);
	}
	//color = Color.green;
	//g.setColor(color);

	//color = Color.cyan;
	//g.setColor(color);
	//g.setXORMode(Color.cyan);
	//g.setColor(Color.cyan);
	//color = new Color(10,10,200);
	//g.setXORMode(color);
	for(int i=0; i<numberOfSamples/2; i++) {
	    int x = (int)(rightSand[i]/yScale.getScale());
	    g.drawLine(i+xOffset, /*460-x*/yOffset+windowHeight-85-x, i+xOffset, /*450-x*/yOffset+windowHeight-85-sandSize-x);
	}
	//if(dakl) drawHz(g);
	g.setXORMode(Color.white);
	for(int i=0; i<numberOfBands; i+= 50) {
	    g.drawString("" + (int)(i*bandWidth), i+xOffset, 15);
	    g.drawString("" + (int)(i*bandWidth), i+xOffset, /*505*/windowHeight-40);
	}

	//g.setColor(Color.red);
	for(int i=0; i<numberOfBands; i+= 50) {
	    g.drawLine(i+xOffset,18,i+xOffset,21);
	    g.drawLine(i+xOffset,windowHeight-84+yOffset+3,i+xOffset,windowHeight-84+yOffset+6);
	}

	//Mostrar la frecuencia siguiendo al puntero del raton//
	if (posX>=xOffset && posX<=windowWidth){ //para no salirse de los m'argenes establecidos//
	  g.drawString("" +visualiza_frec + " Hz", posX, posY);
	  //Mostrar linea vertical cuyas coordenadas de inicio y fin son los siguientes dos puntos:
	  //  origen(posX, 0)
	  //  fin(posX, windowHeight) 
	  g.drawLine(posX,0,posX,windowHeight);
	}
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        Component c = e.getComponent();
	windowHeight = c.getSize().height;
	//System.err.println(c.getSize().width + " " + c.getSize().height);
    }

    public void componentShown(ComponentEvent e) {
    }

    public static void main(String[] args) throws Exception {
	int numberOfBands = NUMBER_OF_BANDS;
	float sampleRate = SAMPLE_RATE;
	int step = STEP;
	if(args.length>=1) {
	    try {
		numberOfBands = new Integer(args[0]).intValue();
	    }  catch (NumberFormatException e) {
		System.err.println("Error parsing \"" + args[1] + "\"");
	    }
	}
	if(args.length>=2) {
	    try {
		sampleRate = new Float(args[1]).floatValue();
	    }  catch (NumberFormatException e) {
		System.err.println("Error parsing \"" + args[2] + "\"");
	    }
	}
	if(args.length>=3) {
	    try {
		step = new Float(args[2]).intValue();
	    }  catch (NumberFormatException e) {
		System.err.println("Error parsing \"" + args[3] + "\"");
	    }
	}
	RTASA analizador = new RTASA(numberOfBands, sampleRate, step);
	analizador.start();
    }
}
    
