package chordDiagrams;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;

/**
 * This class provides graphical representations of diagrams 
 * (draws diagrams to a bitmap), and provides means to
 * draw collections of diagrams and saving them to files.
 * @author Romwell
 */
public class DiagramDrawer {

	/**
	 * Data structure to store image
	 */
	BufferedImage image;
	
	/**
	 * Graphics object to draw on the image
	 */
	Graphics2D g2;
	
	/**
	 * circle radius
	 */
	int r;
	
	/**
	 *spacing between circles 
	 */
	int space;
		
	/**
	 * Number of rows on the contact sheet
	 */
	int numrows;
	
	/**
	 * Number of columns on the contact sheet
	 */
	int numcols;
	
	
	/**
	 * Spacing between diagrams
	 */
	int dspace;
	
	/**
	 * Dashed stroke for drawing
	 */
	BasicStroke dashedStroke = new BasicStroke(2.0f, 
                                          BasicStroke.CAP_BUTT, 
                                          BasicStroke.JOIN_MITER, 
                                          10.0f, new float[] {5.0f}, 0.0f);
	
	BasicStroke solidStroke = new BasicStroke(2.0f);
	
	
/**
 * Creates a diagram drawer instance
 * @param r
 * diagram radius
 * @param space
 * spacing on the sides and between diagrams
 * @param numrows number of rows on contact sheet
 * @param numcols number of columns on contact sheet
 * @param dspace spacing between diagrams
 */	
public DiagramDrawer(int r, int space, int numrows, int numcols, int dspace)
{
	this.r = r;
	this.space = space;
	this.numrows = numrows;
	this.numcols = numcols;
	this.dspace = dspace;
	image = new BufferedImage(2*(r+space),3*2*(r+space),BufferedImage.TYPE_INT_RGB); 	//default width/height
	g2 = image.createGraphics();
	
}


/**
 * Creates a diagram drawer instance
 * @param r
 * diagram radius
 * @param space
 * spacing on the sides and between diagrams
 */	
public DiagramDrawer(int r, int space)
{
	this(r,space,1,1,0);
}


/**
 * Initializes image and the graphics object and blanks the image
 * @param w diagram width
 * @param h diagram height
 */
public void initImage(int dw, int dh)
{
	int w=dspace+numcols*(dspace+dw);
	int h=dspace+numrows*(dspace+dh);	
	image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
	g2 = image.createGraphics();
}


/**
 * Blanks the image
 */
public void clearImage()
{
	int w = image.getWidth();
	int h = image.getHeight();
	g2.setColor(new Color(255,255,255));
	g2.fill(new Rectangle2D.Double(0,0,w,h));
	g2.setColor(new Color(0,0,0));
	g2.draw(new Rectangle2D.Double(0,0,w,h));	
}



/**
 * Draws k properly spaced circles
 * @param k	number of circles
 * @param offset_x X coordinate of the leftmost circle (UL corner)
 * @param offset_y Y coordinate of the leftmost circle (UL corner) 
 */
public void drawCircles(int k, int off_x, int off_y)
{
	//draw all circles
	g2.setStroke(solidStroke);
	for (int i=0; i<k; i++)
	{
		int ulx = off_x+space+(space+2*r)*i;
		int uly = off_y;
		Ellipse2D circle = new Ellipse2D.Double(ulx,uly,2*r,2*r);
		g2.draw(circle);
	}
}

/**
 * Returns the width of the diagram, in pixels
 * @param g the diagram
 * @return its width in pixels
 */
public int diagWidth(diagram g)
{
	int w = 2*g.k*r+(g.k+1)*space;
	return w;
}


/**
 * Returns the height of the diagram, in pixels
 * @param g the diagram
 * @return its height in pixels
 */
public  int diagHeight(diagram g)
{
	int h = (2*g.k+1)*2*r;
	return h;
}


/**
 * Draws a diagram on the image. Does not erase nor itialize it.
 * Must call initImage() before calling this method.
 * @param g 		the diagram to draw
 * @param off_x		x offset
 * @param off_y		y offset
 * @param drawframe	draw frame around diagram ?
 */
public void drawDiagram(diagram g, int off_x, int off_y, boolean drawframe)
{
	
	int w = diagWidth(g);
	int h = diagHeight(g);
	//set font
	setFontByWidth(g.diagID, (double)diagWidth(g)-space*2,r);
	
	//draw frame
	if (drawframe) 
	{
		g2.setStroke(solidStroke);
		g2.draw(new Rectangle2D.Double(off_x,off_y,w,h));
	}	
	
	
	drawCircles(g.k, off_x,off_y + diagHeight(g)/2 - r);
	//draw all arcs
	for (int i=0;i<g.skipdiag.length;i++)
	{
		if (g.skipdiag[i]>-1)
		{
			if (g.linkcolors[i]==g.linkcolors[i+g.skipdiag[i]])
			{
				drawCurvedInnerChord(g, i, off_x, off_y);
			}
			else
			{
				drawCurvedChord(g, i, off_x, off_y);
			}
		}
	}
	//put title
	g2.drawString(g.toString(), off_x+space, off_y+r);
}

/**
 * Draws straight chord in the diagram g emanating from endpoint t
 * @param g the diagram
 * @param t	endpoint index
 * @param off_x X offset
 * @param off_y Y offset
 */
public void drawChord(diagram g, int i, int off_x, int off_y)
{
	int start = i;
	int end = i + g.skipdiag[i];
	Line2D chord = new Line2D.Double(getPoint(g, start, off_x, off_y),getPoint(g, end, off_x, off_y));
	g2.setStroke(solidStroke);
	g2.draw(chord);	
	drawDot(g, start, off_x, off_y);
	drawDot(g, end, off_x, off_y);
}

/**
 * Draws curved chord in the diagram g emanating from endpoint t
 * @param g
 * @param t
 */
public void drawCurvedChord(diagram g, int i, int off_x, int off_y)
{
	int diagcenter_y = off_y+diagHeight(g)/2; //y coordinate of diagram center
	int start = i;
	int end = i + g.skipdiag[i];
	Point2D p1 = getPoint(g, start, off_x, off_y);
	Point2D p2 = getPoint(g, end, off_x, off_y);
	Double cpx = (p1.getX() + p2.getX())/2;
	Double cpy;
	if ((p1.getY()<=diagcenter_y)&&(p2.getY()<=diagcenter_y))
	{
		cpy=diagcenter_y-(p2.getX()-p1.getX())*1.5;
	}
	else
	{
		cpy=diagcenter_y+(p2.getX()-p1.getX())*1.5;
	}
	QuadCurve2D q = new QuadCurve2D.Double(p1.getX(),p1.getY(),cpx,cpy,p2.getX(),p2.getY());
	g2.setStroke(dashedStroke);
	g2.draw(q);
	drawDot(g, start,off_x,off_y);
	drawDot(g, end,off_x,off_y);
}


/**
 * Draws curved inner chord in the diagram g emanating from endpoint t
 * (inner = not between links)
 * @param g
 * @param t
 */
public void drawCurvedInnerChord(diagram g, int i,int off_x, int off_y)
{
	int start = i;
	int end = i + g.skipdiag[i];
	Point2D p1 = getPoint(g, start, off_x, off_y);
	Point2D p2 = getPoint(g, end, off_x, off_y);
	Point2D c = getCircleCenter(g,g.linkcolors[start], off_x, off_y);

	Double cpx = (p1.getX() + p2.getX())/2;
	Double cpy = (p1.getY() + p2.getY())/2;
	
	Double q = 0.5; //center weight
	
	cpx = c.getX()*q + cpx*(1-q);
	cpy = c.getY()*q + cpy*(1-q);		
	
	QuadCurve2D qc = new QuadCurve2D.Double(p1.getX(),p1.getY(),cpx,cpy,p2.getX(),p2.getY());
	g2.setStroke(solidStroke);
	g2.draw(qc);
	drawDot(g, start, off_x, off_y);
	drawDot(g, end, off_x, off_y);
}



/**
 * Gets the center of the circle that represents the ring t
 * @param t
 * index of the link
 * @return
 * coordinates of the center
 */
public Point2D getCircleCenter(diagram g, int t, int off_x, int off_y)
{
	int ulx = off_x+space+(space+2*r)*t+r;
	int uly = off_y+diagHeight(g)/2;
	Point p = new Point(ulx, uly);
	return p;
}


/**
 * Takes an index of a chord endpoint and produces a point
 * @param g
 * the diagram that contains the point 
 * @param t
 * index of the point
 * @return
 * point that can be drawn in Graphics2D
 * @param off_x X offset
 * @param off_y Y offset
 */
public Point2D getPoint(diagram g, int t, int off_x, int off_y)
{
	Double num = 1.0*g.linkcolors[t];  				//link number
	Double rot = g.posInRing(t)*1.0;   				//poisiton in the link
	Double cx = 1.0*(num*2*r+r+num*space+space);
	Double cy = 1.0*g.k*2*r+r;
	Double angle = rot/g.ringsizes[g.linkcolors[t]]*2*Math.PI;
	Double x = off_x+cx+r*Math.cos(angle);
	Double y = off_y+cy+r*Math.sin(angle);
	Point2D p = new Point(x.intValue(),y.intValue());
	return p;
}

/**
 * Draws a dot that represents an endpoint on a diagram
 * @param g
 * the diagram
 * @param t
 * endpoint index
 */
public void drawDot(diagram g, int t, int off_x, int off_y)
{
	Double dr = r/9.0;
	Point2D p = getPoint(g, t, off_x, off_y);
	
	Double ux = p.getX() - dr;
	Double uy = p.getY() - dr;	
	
	
	g2.setStroke(solidStroke);
	g2.draw(new Ellipse2D.Double(ux,uy,2*dr,2*dr));
	
	//the code below puts labels next to dots. 
	/* 
	Point2D c = getCircleCenter(g.linkcolors[t]);
	Double textx = (c.getX()-p.getX())*0.3 + p.getX()-3;
	Double texty = (c.getY()-p.getY())*0.3 + p.getY()+5;
	g2.drawString(""+g.chords[t], textx.intValue(), texty.intValue()); //puts labels on dots
	*/
}

/**
 * Saves the diagram to a file
 * @param filename
 * The name of the file to save the diagram to (without extension, just the name)
 * Saves to PNG.
 */
public void Save(String filename)
{
	String ext = "png";
	try
	{
	File output = new File(filename+"."+ext);
	ImageIO.write(image, ext, output);
	}
	catch (IOException e) {}
}
	

/**
 * Draws and saves a diagram
 * @param g the diagram to be saved
 * @param filename
 */
public void DrawAndSave(diagram g, String filename)
{
	initImage(diagWidth(g), diagHeight(g));
	clearImage();
	drawDiagram(g,0,0,true);
	Save(filename);
}

/**
 * Draws and saves a subset of a collection of diagrams
 * The filenames are formed using the pattern
 * "name_n-k_#.png", where # stays for index in the list
 * @param hm		hashmap with the diagrams
 * @param keySet	keys for the diagrams to draw and save
 * @param name		base for the filename
 */
public void DrawAndSave(HashMap<String, diagram> hm, Iterable keySet, String name)
{
	int i=0;
	
	Iterator it = keySet.iterator(); 
	Object temp =  it.next();
	diagram sample = hm.get(temp);
	int dw = diagWidth(sample);
	int dh = diagHeight(sample);

	initImage(dw, dh);
	clearImage();
	int sheet = 0;
	
	for (Iterator I = keySet.iterator(); I.hasNext();) 
	{				
		diagram g = hm.get(I.next());		
		drawDiagram(g, getOffX(i, dw), getOffY(i, dh),true);
		i++;
		if ((!I.hasNext())||((i%(numcols*numrows))==0))
		{
			String fname = name+sheet;
			Save(fname);
			clearImage();
			sheet++;
		}
	}	
}


/**
 * Gets X offset of the diagram on the contact sheet 
 * @param i		number of the diagram
 * @param dw	width of  the diagram
 * @return		X offset
 */
private int getOffX(int i, int dw)
{
	int off = dspace+(i % numcols)*(dspace+dw);
	return off;
}


/**
 * Gets Y offset of the diagram on the contact sheet 
 * @param i		number of the diagram
 * @param dh	height of  the diagram
 * @return		Y offset
 */
private int getOffY(int i, int dh)
{
	int off = dspace+((i%(numrows*numcols))/numcols)*(dspace+dh);
	return off;
}



/**
 * Returns the relation depiction width 
 * @param relation the Arraylist that stores relation
 * @param diags	data structure with diagrams diagram
 * @return width of the relation depiction
 */
public int relationDiagramWidth(Relation relation, ArrayList<String> keySet, HashMap<String, diagram>diags)
{
	FontMetrics M = g2.getFontMetrics();	
	int w = 2*space;
	for (Integer I:relation.relation.keySet())
	{
		String ID = keySet.get(I);
		double coeff = relation.relation.get(I);
		w+=diagWidth(diags.get(ID));
		w+=M.stringWidth(""+coefToString(coeff));
	}
	w+=M.stringWidth("=0");
	return w;
}

/**
 * Returns number  as a string, with 1 being empty string and -1 being "-".
 * @param c coefficient
 * @return string representation of coefficient
 */
public String coefToString(double c)
{
	double epsilon = 0.0001;
	long t = Math.round(c);
	double diff = Math.abs(t*1.0-c);
	if (diff<epsilon)
	{
		if (t==1)
		{
			return "+";
		}
		else if (t==-1)
		{
			return "-";
		}
		else  if (t>0)
		{
			return ("+"+Math.abs(t));
		} 
		else
		{
			return ("-"+Math.abs(t));
		}

	}
	else
	{
		if (c>0)
		{
			return ("+"+Math.abs(c));
		} 
		else
		{
			return ("-"+Math.abs(c));
		}
	}
}


/**
 * Returns the relation depiction height 
 * @param relation the Arraylist that stores relation
 * @param diags	data structure with diagrams diagram
 * @return height of the relation depiction
 */
public int relationDiagramHeight(Relation relation, ArrayList<String> keySet, HashMap<String, diagram>diags)
{	
	int maxh = 0;
	for (Integer I:relation.relation.keySet())
	{
		String ID = keySet.get(I);
		int h =diagHeight(diags.get(ID));
		if (h>maxh) {maxh=h;}
	}
	return maxh+2*space;
}

/**
 * Sets font for the graphics 2D object to match the specified pixelheight
 * The font will have the smallest point size that will result in font
 * pixel height exceeding pixelheight parameter
 * @param pixelheight	desired height
 */
public void setFontByMinHeight(Double pixelheight)
{
	Font font = new Font("Monospace", Font.PLAIN, 36);
	int size=0;
	int hgt=0;
	while (hgt < pixelheight)
	{			
		size++;
		font = new Font("Monospace", Font.PLAIN, size);		
		FontMetrics metrics = g2.getFontMetrics(font);
	    hgt = metrics.getAscent();	    
	}
	g2.setFont(font);
}


/**
 * Draws a relation on the image canvas
 * @param relation the relation to draw - an arraylist of relation entries
 * @param keySet data structure with keys; indices in the relation must correspond to entries in keySet
 * @param diags  data structure with diagrams
 * @param off_x x offset 	
 * @param off_y y offset  
 */
public void DrawRelation(Relation relation, ArrayList<String> keySet, HashMap<String, diagram>diags, int off_x, int off_y)
{		
	
	
	setFontByMinHeight(2*r*0.75);
	double fh = g2.getFontMetrics().getAscent();	//large font height;
	fh = fh * 3/4;
	int w = relationDiagramWidth(relation, keySet, diags);
	int h = relationDiagramHeight(relation, keySet, diags);
	initImage(w, h);
	clearImage();	
	
	//	draw frame
	g2.setStroke(solidStroke);
	g2.draw(new Rectangle2D.Double(off_x,off_y,w,h));
	
	int i=0;
	int xpos=off_x+space;		//start x
	int center_y = off_y+h/2;	//center line y coordinate
	for (Integer I:relation.relation.keySet())
	{
		String ID = keySet.get(I);
		diagram g = diags.get(ID);
		double coeff = relation.relation.get(I);
		if (!((coeff==1.0)&&(i==0)))
		{
			String S = coefToString(coeff);
			setFontByMinHeight(2*r*0.75);
			g2.drawString(S,xpos,(float)(center_y+fh/2));
			xpos+=g2.getFontMetrics().stringWidth(S);
		}
		drawDiagram(g, xpos, center_y-diagHeight(g)/2,false);
		xpos+=diagWidth(g);
		i++;
	}	
	setFontByMinHeight(2*r*0.75);
	g2.drawString("=0",xpos,(float)(center_y+fh/2));
}


/**
 * Sets font for the graphics 2D object so that a string 
 * has length not exceeding pixel width and height
 * The font size is largest value so that the string width
 * does not exceed the set pixelwidth/pixelheight
 * @param S the string you want to fit in pixelwidth 
 * @param pixelwidth desired string pixel width
 */
public void setFontByWidth(String S, double pixelwidth, double pixelheight)
{
	Font font = new Font("Monospace", Font.PLAIN, 36);
	int size=0;
	int w=0;
	int h=0;
	while ((w < pixelwidth)&&(h<pixelheight))
	{			
		size++;
		font = new Font("Monospace", Font.PLAIN, size);		
		FontMetrics metrics = g2.getFontMetrics(font);
	    w = metrics.stringWidth(S);
	    h = metrics.getHeight();
	}
	font = new Font("Monospace", Font.PLAIN, size-1);
	g2.setFont(font);
}








}
