package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class KartenEditor extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener,
    KeyListener {

    public JFrame parent;

    public double deltaX = 0;
    public double deltaY = 0;

    public int dragX;
    public int dragY;

    public Punkt dragKnoten = null;

    public double zoom = 1;

    public double masstab = 1;

    public String karteDatei = "pergament.png";

    public BufferedImage karte;

    public List<Punkt> punkte = new ArrayList<>();
    public List<Linie> linien = new ArrayList<>();

    public boolean zeichneKarte = true;
    public boolean zeichnePunkte = true;
    public boolean zeichneLinien = true;
    public boolean zeichneNamen = true;

    public Punkt startPunkt = null;
    public Punkt zielPunkt = null;
    public List<Linie> route = new ArrayList<>();

    public boolean navigation = false;

    public boolean streckeMessen = false;

    public Punkt[] vorgaenger;

    public Gelaendetyp[] gelaendetypen = new Gelaendetyp[] {
        new Gelaendetyp(180,170,170, "Weg", 1),
        new Gelaendetyp(255,255,255, "Strasse", .5),
        new Gelaendetyp(255,255,0, "Wüste", 2),
        new Gelaendetyp(0,0,255, "Meer", .3),
        new Gelaendetyp(80,90,255, "Fluss", .4),
        new Gelaendetyp(220,220,255, "Eis", 3),
        new Gelaendetyp(40,220,45, "Sumpf", 2),
        new Gelaendetyp(120,120,120, "Gebirge", 4),
        new Gelaendetyp(220,0,0, "Geheimgang", 1.5),
        new Gelaendetyp(255,255,255, "Sprung", 0),
    };

    public int aktuellerGelaendeTyp = 0;

    public KartenEditor(JFrame parent) {
        this.parent = parent;
        ladeBild();
        lade();
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if( zeichneKarte ) {
            g.drawImage(karte, (int) (deltaX * zoom), (int) (deltaY * zoom), (int) (karte.getWidth() * zoom),
                (int) (karte.getHeight() * zoom), null);
        }
        if(zeichneLinien) {
            g.setColor(Color.BLACK);
            ((Graphics2D) g).setStroke(new BasicStroke(5));
            for (Linie l : linien) {
                g.drawLine((int) ((l.start.x + deltaX) * zoom), (int) ((l.start.y + deltaY) * zoom),
                    (int) ((l.ende.x + deltaX) * zoom), (int) ((l.ende.y + deltaY) * zoom));
            }
            g.setColor(new Color(255, 225, 50));
            ((Graphics2D) g).setStroke(new BasicStroke(2));
            for (Linie l : linien) {
                if( gelaendetypen[l.gelaendeyp]!=null )
                    g.setColor(new Color(
                        gelaendetypen[l.gelaendeyp].rot,
                        gelaendetypen[l.gelaendeyp].gruen,
                        gelaendetypen[l.gelaendeyp].blau
                    ));
                g.drawLine((int) ((l.start.x + deltaX) * zoom), (int) ((l.start.y + deltaY) * zoom),
                    (int) ((l.ende.x + deltaX) * zoom), (int) ((l.ende.y + deltaY) * zoom));
            }
        }
        if( zeichnePunkte) {
            for (Punkt p : punkte) {
                g.setColor(Color.BLACK);
                g.drawOval((int) ((p.x + deltaX) * zoom - 5), (int) ((p.y + deltaY) * zoom - 5), 10, 10);
                g.setColor(p.name==null ? Color.GREEN: Color.blue);
                g.fillOval((int) ((p.x + deltaX) * zoom - 5), (int) ((p.y + deltaY) * zoom - 5), 10, 10);
            }
        }
        if(navigation) {
            g.setColor(Color.BLACK);
            ((Graphics2D) g).setStroke(new BasicStroke(6));
            for (Linie l : route) {
                g.drawLine((int) ((l.start.x + deltaX) * zoom), (int) ((l.start.y + deltaY) * zoom),
                    (int) ((l.ende.x + deltaX) * zoom), (int) ((l.ende.y + deltaY) * zoom));
            }
            g.setColor(new Color(255, 0,0));
            ((Graphics2D) g).setStroke(new BasicStroke(4));
            for (Linie l : route) {
                g.drawLine((int) ((l.start.x + deltaX) * zoom), (int) ((l.start.y + deltaY) * zoom),
                    (int) ((l.ende.x + deltaX) * zoom), (int) ((l.ende.y + deltaY) * zoom));
            }
        }
        if( zeichneNamen) {
            g.setFont(new Font("Gentium Book Basic", Font.BOLD,(int)(16*zoom)));
            for (Punkt p : punkte) {
                if( p.name!=null ) {
                    g.setColor(Color.BLACK);
                    ((Graphics2D) g).setStroke(new BasicStroke(4));
                    g.drawString(p.name, (int) ((p.x + deltaX) * zoom + 4), (int) ((p.y + deltaY) * zoom + 4));
                    g.drawString(p.name, (int) ((p.x + deltaX) * zoom + 6), (int) ((p.y + deltaY) * zoom + 4));
                    g.drawString(p.name, (int) ((p.x + deltaX) * zoom + 4), (int) ((p.y + deltaY) * zoom + 6));
                    g.drawString(p.name, (int) ((p.x + deltaX) * zoom + 6), (int) ((p.y + deltaY) * zoom + 6));
                    g.setColor(Color.WHITE);
                    ((Graphics2D) g).setStroke(new BasicStroke(1));
                    g.drawString(p.name, (int) ((p.x + deltaX) * zoom + 5), (int) ((p.y + deltaY) * zoom + 5));

                }
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DSA-Navigation");
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setLocationRelativeTo(null);
        frame.add(new KartenEditor(frame));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if( e.getClickCount()==2 ) {
            Punkt p = naechsterPunkt(e.getX(), e.getY());
            if(p!=null) {
                p.name = JOptionPane.showInputDialog(this, "Name des Punkts",
                    p.name);
                if(p.name.isBlank())
                    p.name=null;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if((e.getModifiersEx()&MouseEvent.CTRL_DOWN_MASK)==MouseEvent.CTRL_DOWN_MASK)
            return;

        dragX = e.getX();
        dragY = e.getY();
        dragKnoten = naechsterPunkt(e.getX(), e.getY());

        if( navigation ) {
             if( startPunkt==null || zielPunkt!=null) {
                startPunkt = dragKnoten;
                zielPunkt = null;
                starteNavigation();
                repaint();
                return;
            } else {
                zielPunkt = dragKnoten;
                return;
            }
        }

        if( (e.getModifiersEx()&MouseEvent.BUTTON1_DOWN_MASK)!=0 ) {
            if( dragKnoten==null)
                teileLinie(e.getX(), e.getY());
            if( dragKnoten==null ) {
                Punkt p = new Punkt(e.getX() / zoom - deltaX, e.getY() / zoom - deltaY);
                punkte.add(p);
                dragKnoten = p;
                repaint();
            }
        } else if((e.getModifiersEx()&MouseEvent.BUTTON3_DOWN_MASK)!=0 ) {
            if( dragKnoten==null ) {
                teileLinie(e.getX(),e.getY());
                if( dragKnoten==null ) {
                    dragKnoten = new Punkt(e.getX() / zoom - deltaX, e.getY() / zoom - deltaY);
                    punkte.add(dragKnoten);
                }
            }
            Punkt p = new Punkt(e.getX()/zoom-deltaX, e.getY()/zoom-deltaY);
            punkte.add(p);
            Linie l = new Linie(p, dragKnoten, aktuellerGelaendeTyp);
            linien.add(l);
            dragKnoten = p;
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if( navigation || (e.getModifiersEx()&MouseEvent.CTRL_DOWN_MASK)==MouseEvent.CTRL_DOWN_MASK)
            return;

        if(dragKnoten!=null) {
            Punkt p = naechsterPunkt(e.getX(), e.getY(), dragKnoten);
            if( p!=null ) {
                punkte.remove(dragKnoten);
                for( int i=linien.size()-1; i>=0; i-- ) {
                    Linie l = linien.get(i);
                    if( l.start==dragKnoten )
                        l.start=p;
                    if( l.ende==dragKnoten )
                        l.ende = p;
                    if( l.start==l.ende )
                        linien.remove(i);
                }
            }
            repaint();
        }

        if( streckeMessen ) {
            streckeMessen=false;
            try {
                Linie l = linien.get(linien.size()-1);
                String value = JOptionPane.showInputDialog("Länge der Strecke");
                punkte.remove(l.start);
                punkte.remove(l.ende);
                linien.remove(l);
                masstab = Double.parseDouble(value)/l.laenge();
                repaint();
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

        if( navigation )
            return;

        if( (e.getModifiersEx()&MouseEvent.BUTTON1_DOWN_MASK)!=0 || (e.getModifiersEx()&MouseEvent.BUTTON3_DOWN_MASK)!=0) {
            if((e.getModifiersEx()&MouseEvent.CTRL_DOWN_MASK)==MouseEvent.CTRL_DOWN_MASK) {
                double px = e.getX()/zoom-deltaX;
                double py = e.getY()/zoom-deltaY;
                for( Linie l:linien ) {
                    double dx = l.ende.x-l.start.x;
                    double dy = l.ende.y-l.start.y;
                    double a = (px-l.start.x)*dx+(py-l.start.y)*dy;
                    double length2=dx*dx+dy*dy;
                    double t = a/length2;

                    Punkt neu = new Punkt(l.start.x+dx*t, l.start.y+dy*t);
                    if( (neu.x-px)*(neu.x-px)+(neu.y-py)*(neu.y-py)<100/zoom/zoom && t>0 && t<1) {
                        l.gelaendeyp = aktuellerGelaendeTyp;
                    }
                }
                repaint();
                return;
            }
            if( dragKnoten!=null ) {
                dragKnoten.x = e.getX()/zoom-deltaX;
                dragKnoten.y = e.getY()/zoom-deltaY;
                repaint();
            }
        } else if( (e.getModifiersEx()&MouseEvent.BUTTON2_DOWN_MASK)!=0 ) {
            deltaX += (e.getX() - dragX) / zoom;
            deltaY += (e.getY() - dragY) / zoom;
            dragX = e.getX();
            dragY = e.getY();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Punkt p = naechsterPunkt(e.getX(),e.getY());
        setCursor(new Cursor(p==null ? Cursor.DEFAULT_CURSOR:(navigation ? Cursor.HAND_CURSOR:Cursor.CROSSHAIR_CURSOR)));
        if(p!=null)
            setToolTipText(p.name);
        if( navigation && zielPunkt==null )
            navigiere(p);

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double f = Math.pow(1.1, e.getPreciseWheelRotation());
        deltaX += e.getX()/zoom*(1-1/f);
        deltaY += e.getY()/zoom*(1-1/f);
        zoom /= f;
        repaint();
    }

    public Punkt naechsterPunkt(int x, int y) {
        return naechsterPunkt(x,y,null);
    }
    public Punkt naechsterPunkt(int x, int y, Punkt ignoriere) {
        Punkt bester = null;
        double dist = 100/zoom/zoom;
        for( Punkt p:punkte ) {
            double d = Math.pow(x/zoom-deltaX-p.x,2)+Math.pow(y/zoom-deltaY-p.y,2);
            if( d<dist && p!=ignoriere)
            {
                bester = p;
                dist = d;
            }
        }
        return bester;
    }

    public void starteNavigation() {
        if (startPunkt == null)
            return;

        double[] distanz = new double[punkte.size()];
        vorgaenger = new Punkt[punkte.size()];
        for (int i = 0; i < distanz.length; i++) {
            distanz[i] = Double.MAX_VALUE;
            vorgaenger[i] = null;
        }
        distanz[punkte.indexOf(startPunkt)] = 0;
        List<Integer> offen = new ArrayList<>();
        offen.addAll(IntStream.range(0, punkte.size()).boxed().toList());
        while (!offen.isEmpty()) {
            if( offen.size()%100==0)
                System.out.println(offen.size());
            offen.sort(((a, b) -> (int) Math.signum(distanz[a] - distanz[b])));
            Integer p = offen.get(0);
            Punkt tmp = punkte.get(p);
            offen.remove(p);
            for (Linie l : linien) {
                if (l.start == tmp || l.ende == tmp) {
                    Punkt p2 = l.start == tmp ? l.ende : l.start;
                    Integer index2 = punkte.indexOf(p2);
                    if (offen.contains(punkte.indexOf(p2))) {
                        double abstand = l.kosten(gelaendetypen) + distanz[p];
                        if (abstand < distanz[index2]) {
                            distanz[index2] = abstand;
                            vorgaenger[index2] = tmp;
                        }
                    }
                }
            }
        }
    }

    public void navigiere(Punkt ziel) {
        if( startPunkt==null || vorgaenger==null )
            return;
        route.clear();
        double laenge = 0;
        double zeit = 0;
        while( ziel!=startPunkt ) {
            if( punkte.indexOf(ziel)==-1 )
                break;
            Punkt tmp = vorgaenger[punkte.indexOf(ziel)];
            for( Linie l:linien )
                if( l.start==tmp && l.ende==ziel || l.start==ziel && l.ende==tmp ) {
                    route.add(l);
                    laenge += l.laenge()*masstab;
                    zeit += l.laenge()*masstab*gelaendetypen[l.gelaendeyp].zeitProWeg;
                }
            ziel=tmp;
        }
        repaint();
        parent.setTitle("Gesamtlänge: "+ new DecimalFormat("0.00").format (laenge)
            +"   Gesamtzeit: "+new DecimalFormat("0.00").format (zeit));
    }

    public void teileLinie(int x, int y) {
        double px = x/zoom-deltaX;
        double py = y/zoom-deltaY;
        for( Linie l:linien ) {
            double dx = l.ende.x-l.start.x;
            double dy = l.ende.y-l.start.y;
            double a = (px-l.start.x)*dx+(py-l.start.y)*dy;
            double length2=dx*dx+dy*dy;
            double t = a/length2;

            Punkt neu = new Punkt(l.start.x+dx*t, l.start.y+dy*t);
            if( (neu.x-px)*(neu.x-px)+(neu.y-py)*(neu.y-py)<100/zoom/zoom && t>0 && t<1) {
                punkte.add(neu);
                Linie neueLinie = new Linie(neu, l.ende, l.gelaendeyp);
                l.ende = neu;
                linien.add(neueLinie);
                dragKnoten = neu;
                return;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if( e.getKeyCode()==KeyEvent.VK_F1 )
            zeichneKarte=!zeichneKarte;
        if( e.getKeyCode()==KeyEvent.VK_F2 )
            zeichnePunkte=!zeichnePunkte;
        if( e.getKeyCode()==KeyEvent.VK_F3 )
            zeichneLinien=!zeichneLinien;
        if( e.getKeyCode()==KeyEvent.VK_F4 )
            zeichneNamen=!zeichneNamen;
        if( e.getKeyCode()==KeyEvent.VK_SPACE )
            navigation = !navigation;
        if( e.getKeyCode()==KeyEvent.VK_F5 )
            speichere();
        if( e.getKeyCode()==KeyEvent.VK_F6 )
            lade();
        if( e.getKeyCode()==KeyEvent.VK_F9 )
            gelaendeTypDialog();
        if( e.getKeyCode()==KeyEvent.VK_LEFT )
            wechsleKarte(-1);
        if( e.getKeyCode()==KeyEvent.VK_RIGHT )
            wechsleKarte(1);
        if( e.getKeyCode()==KeyEvent.VK_F10 )
            streckeMessen = true;
        if( e.getKeyCode()>=KeyEvent.VK_0 && e.getKeyCode()<=KeyEvent.VK_9) {
            aktuellerGelaendeTyp = e.getKeyCode() - KeyEvent.VK_0;
            parent.setTitle(gelaendetypen[aktuellerGelaendeTyp].name);
        }
        if(e.getKeyCode()==KeyEvent.VK_DELETE)
        {
            if( dragKnoten!=null ) {
                punkte.remove(dragKnoten);
                for( int i=linien.size()-1; i>=0; i-- ) {
                    Linie l = linien.get(i);
                    if(l.start==dragKnoten || l.ende==dragKnoten )
                    {
                        linien.remove(l);
                    }
                }
                dragKnoten=null;
            }
        }
        repaint();
    }

    public void speichere() {
        try {
            ObjectMapper mapper = new JsonMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(karteDatei.substring(0, karteDatei.lastIndexOf(".")) + ".json"),
                new Karte(karteDatei, masstab, punkte, linien, gelaendetypen));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void lade() {
        try {
            ObjectMapper mapper = new JsonMapper();
            Karte k = mapper.readValue(new File(karteDatei.substring(0, karteDatei.lastIndexOf(".")) + ".json"),
                Karte.class);
            this.masstab = k.masstab;
            this.punkte = k.punkte;
            this.linien = new ArrayList<>();
            for( int i=0; k.gelaendetypen!=null && i<k.gelaendetypen.length; i++)
                if( k.gelaendetypen[i]!=null )
                    this.gelaendetypen[i] = k.gelaendetypen[i];
            for( Linie l:k.linien ) {
                if( punkte.contains(l.start) && punkte.contains(l.ende)) {
                    this.linien.add(new Linie(this.punkte.get(this.punkte.indexOf(l.start)),
                        this.punkte.get(this.punkte.indexOf(l.ende)), l.gelaendeyp));
                }
            }
            repaint();
        } catch (Exception e) {
            this.masstab = 1;
            this.punkte = new ArrayList<>();
            this.linien = new ArrayList<>();
            e.printStackTrace();
        }
    }

    public void ladeBild() {
        try {
            karte = ImageIO.read(new File(karteDatei));
            if( getSize()!=null && getSize().width!=0 && getSize().height!=0 )
                zoom = Math.min(1.0*getSize().width/karte.getWidth(),1.0*getSize().height/karte.getHeight())*.9;
            deltaX=(getSize().width-karte.getWidth()*1.0*zoom)/2/zoom;
            deltaY=(getSize().height-karte.getHeight()*1.0*zoom)/2/zoom;
            repaint();
        } catch ( Exception e ) {
            System.out.println("Karte nicht gefunden! "+karteDatei);
            e.printStackTrace();
        }
    }

    public void wechsleKarte(int offset) {
        var files = new File(".").list((dir, filename) -> filename.toString().toLowerCase().endsWith(".png"));
        int fileIndex=-1;
        for(int i=0; i<files.length; i++ )
        {
            if(files[i].equals(karteDatei))
                fileIndex = i;
        }
        if( fileIndex>=0 )
        {
            karteDatei = files[(fileIndex+offset+files.length)%files.length];
            ladeBild();
            lade();
            route.clear();
            repaint();
        }
    }

    public void gelaendeTypDialog() {
        JDialog dialog = new JDialog(parent, "Geländetypen", true);
        JPanel panel1 = new JPanel(new GridLayout(10,3,10,10));
        dialog.add(panel1);
        panel1.setBorder(new EmptyBorder(10,10,10,10));
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,10));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Abbrechen");
        panel2.add(okButton);
        panel2.add(cancelButton);
        dialog.add(panel2,BorderLayout.SOUTH);
        JTextField[] gelaendeText = new JTextField[10];
        JTextField[] zeitProWegText = new JTextField[10];
        JButton[] farbeButton = new JButton[10];
        for( int i=0; i<10; i++ ) {
            gelaendeText[i] = new JTextField(gelaendetypen[i].name);
            zeitProWegText[i] = new JTextField(gelaendetypen[i].zeitProWeg+"");
            farbeButton[i] = new JButton();
            Color farbe = new Color(gelaendetypen[i].rot, gelaendetypen[i].gruen, gelaendetypen[i].blau);
            farbeButton[i].setBackground(farbe);
            int j = i;
            panel1.add(gelaendeText[i]);
            panel1.add(zeitProWegText[i]);
            panel1.add(farbeButton[i]);
            farbeButton[i].addActionListener(e->{
                farbeButton[j].setBackground(JColorChooser.showDialog(dialog, "Farbe für "+gelaendeText[j].getText(),farbeButton[j].getBackground()));
            });
        }
        okButton.addActionListener(e->{
            for( int i=0; i<10; i++ ) {
                gelaendetypen[i].name = gelaendeText[i].getText();
                try {
                    gelaendetypen[i].zeitProWeg = Double.parseDouble(zeitProWegText[i].getText());
                } catch (Exception ex) {}
                gelaendetypen[i].rot = farbeButton[i].getBackground().getRed();
                gelaendetypen[i].gruen = farbeButton[i].getBackground().getGreen();
                gelaendetypen[i].blau = farbeButton[i].getBackground().getBlue();
            }
            repaint();
            dialog.setVisible(false);
        });
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static class Punkt {
        public double x;
        public double y;
        public String name;
        public Punkt() {
        }
        public Punkt(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Punkt punkt = (Punkt) o;
            return Double.compare(x, punkt.x) == 0 && Double.compare(y, punkt.y) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public static class Linie {
        public Punkt start;
        public Punkt ende;
        public int gelaendeyp = 0;
        public Linie() {
        }
        public Linie(Punkt start, Punkt ende, int gelaendeyp) {
            this.start = start;
            this.ende = ende;
            this.gelaendeyp = gelaendeyp;
        }

        public double laenge() {
            return Math.sqrt(Math.pow(start.x-ende.x,2)+Math.pow(start.y-ende.y,2));
        }

        public double kosten(Gelaendetyp[] gelaendetypen) { return laenge()*gelaendetypen[gelaendeyp].zeitProWeg;}
    }

    public static class Gelaendetyp {
        public int rot=0;
        public int gruen = 0;
        public int blau = 0;
        public String name = "";
        public double zeitProWeg=1;

        public Gelaendetyp() {
        }
        public Gelaendetyp(int rot, int gruen, int blau, String name, double zeitProWeg) {
            this.rot = rot;
            this.gruen = gruen;
            this.blau = blau;
            this.name=name;
            this.zeitProWeg=zeitProWeg;
        }
    }

    public static class Karte {
        public String dateiName;
        public double masstab = 1;
        public List<Punkt> punkte = new ArrayList<>();
        public List<Linie> linien = new ArrayList<>();
        public Gelaendetyp[] gelaendetypen = new Gelaendetyp[10];
        public Karte() {
        }
        public Karte(String dateiName, double masstab, List<Punkt> punkte, List<Linie> linien, Gelaendetyp[] gelaendetypen) {
            this.dateiName = dateiName;
            this.masstab = masstab;
            this.punkte = punkte;
            this.linien = linien;
            this.gelaendetypen = gelaendetypen;
        }
    }
}