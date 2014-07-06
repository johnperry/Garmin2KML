package org.jp.kml;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.rsna.ui.ColorPane;
import org.rsna.util.FileUtil;
import org.rsna.util.XmlUtil;

import org.w3c.dom.*;

/**
 * A program to convert Garmin path files to KML for Google Earth.
 */
public class Garmin2KML extends JFrame {

	static Color background = Color.getHSBColor(0.58f, 0.17f, 0.95f);

    TextPanel		textPanel;
    FooterPanel		footerPanel;
    String			windowTitle = "Garmin2KML - version 1";
    int 			width = 570;
    int 			height = 700;
    int				termCount = 0;
	ColorPane		text;
	Document		doc;
	JFileChooser		chooser = null;

    public static void main(String args[]) {
        new Garmin2KML();
    }

    public Garmin2KML() {
    	initComponents();
    }

    private void initComponents() {
		setTitle(windowTitle);
		JPanel main = new JPanel();
		main.setLayout(new BorderLayout());
		textPanel = new TextPanel();
		footerPanel = new FooterPanel();
		main.add(textPanel,BorderLayout.CENTER);
		main.add(footerPanel,BorderLayout.SOUTH);
		getContentPane().add(main, BorderLayout.CENTER);
		pack();
		centerFrame();
		this.setVisible(true);
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent evt) { System.exit(0); }
			}
		);
		try {
			doc = XmlUtil.getDocument(getClass().getResourceAsStream("/template.xml"));
			Element coordinates = getElement(doc, "coordinates");
			StringBuffer sb = new StringBuffer();

			File gpxFile = getFile();
			Document gpxDoc = XmlUtil.getDocument(gpxFile);
			Element gpxRoot = gpxDoc.getDocumentElement();
			NodeList nl = gpxRoot.getElementsByTagName("trkpt");
			for (int k=0; k<nl.getLength(); k++) {
				Element pt = (Element)nl.item(k);
				String lat = pt.getAttribute("lat");
				String lon = pt.getAttribute("lon");
				String ele = getElement(pt, "ele").getTextContent().trim();
				sb.append("                    ");
				sb.append(lon+","+lat+","+ele+"\n");
			}
			coordinates.appendChild( doc.createTextNode(sb.toString()) );
			String kml = XmlUtil.toPrettyString(doc);
			text.println(kml);
			File kmlFile = new File(gpxFile.getParentFile(), gpxFile.getName()+".kml");
			FileUtil.setText(kmlFile, kml);
		}
		catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			text.setText(sw.toString());
		}
   }

	private File getFile() {
		if (chooser == null) {
			chooser = new JFileChooser();
			chooser.setDialogTitle("Select the Garmin .gpx file to convert");
		}
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		System.exit(0);
		return null;
	}

	private Element getElement(Node node, String name) {
		try {
			if (node instanceof Document) {
				node = ((Document)node).getDocumentElement();
			}
			NodeList nl = ((Element)node).getElementsByTagName(name);
			if (nl.getLength() > 0) return (Element)nl.item(0);
		}
		catch (Exception unable) { }
		return null;
	}

    private void centerFrame() {
		Toolkit t = getToolkit();
		Dimension scr = t.getScreenSize ();
		setSize(width,height);
		setLocation(
			new Point(
				(scr.width - width)/2,
				(scr.height - height)/2));
    }

	class FooterPanel extends JPanel {
		public JLabel message;
		public FooterPanel() {
			super();
			this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			this.setBackground(background);
			message = new JLabel(" ");
			this.add(message);
		}
		public void setMessage(String msg) {
			final String s = msg;
			Runnable display = new Runnable() {
				public void run() {
					message.setText(s);
				}
			};
			SwingUtilities.invokeLater(display);
		}
	}

	class TextPanel extends JPanel {
		public TextPanel() {
			super();
			setLayout(new BorderLayout());
        	text = new ColorPane();
			JScrollPane jsp = new JScrollPane();
			jsp.setViewportView(text);
			jsp.getViewport().setBackground(Color.white);
        	add(jsp, BorderLayout.CENTER);
		}
	}

}
