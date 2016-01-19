package edu.utk.biodynamics.icloudecg.GraphingUtils;

/**
 * Created by DSClifford on 8/8/2015.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class LineGraph extends View {
	
	private ArrayList<Line> lines = new ArrayList<Line>();
	private Paint paint = new Paint();
	private Paint txtPaint = new Paint();
	private float minY = 0, minX = 0;
	private float maxY = 0, maxX = 0;
	private boolean isMaxYUserSet = false;
	private int lineToFill = -1;
	private int indexSelected = -1;
	private OnPointClickedListener listener;
	private Bitmap fullImage;
	private boolean shouldUpdate = false;
	private boolean showMinandMax = true;
	private boolean showHorizontalGrid = true;
	private int gridColor = 0x000000;
	private int fillColor = Color.BLACK;
	private int fillAlpha = 30;
	private float fillStrokeWidth= 2;
	
	public LineGraph(Context context){
		this(context,null);
	}
	public LineGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		txtPaint.setColor(0xD50929);
		txtPaint.setTextSize(20);
		txtPaint.setAntiAlias(true);
	}
	public void setGridColor(int color)
	{
		gridColor = color;
	}
	public void setFillColor(int color)
	{
	  fillColor = color;
	}
	public void setFillAlpha(int alpha)
	{
	  fillAlpha = alpha;
	}
	public void setFillStrokeWidth(float strokeWidth)
	{
	  fillStrokeWidth = strokeWidth;
	}
	public void showHorizontalGrid(boolean show)
	{
		showHorizontalGrid = show;
	}
	public void showMinAndMaxValues(boolean show)
	{
		showMinandMax = show;
	}
	public void setTextColor(int color)
	{
		txtPaint.setColor(color);
	}
	public void setTextSize(float s)
	{
		txtPaint.setTextSize(s);
	}
	public void setMinY(float minY){
		this.minY = minY;
	}
	
	public void update()
	{
		shouldUpdate = true;
		postInvalidate();
	}
	public void removeAllLines(){
		while (lines.size() > 0){
			lines.remove(0);
		}
		shouldUpdate = true;
		postInvalidate();
	}
	
	public void addLine(Line line) {
		lines.add(line);
		shouldUpdate = true;
		postInvalidate();
	}
	public ArrayList<Line> getLines() {
		return lines;
	}
	public void setLineToFill(int indexOfLine) {
		this.lineToFill = indexOfLine;
		shouldUpdate = true;
		postInvalidate();
	}
	public int getLineToFill(){
		return lineToFill;
	}
	public void setLines(ArrayList<Line> lines) {
		this.lines = lines;
	}
	public Line getLine(int index) {
		return lines.get(index);
	}
	public int getSize(){
		return lines.size();
	}
	
	public void setRangeY(float min, float max) {
		minY = min;
		maxY = max;
		isMaxYUserSet = true;
	}
	public float getMaxY(){
		if (isMaxYUserSet){
			return maxY;
		} else {
			maxY = lines.get(0).getPoint(0).getY();
			for (Line line : lines){
				for (LinePoint point : line.getPoints()){
					if (point.getY() > maxY){
						maxY = point.getY();
					}
				}
			}
			return maxY;
		}
		
	}
	public float getMinY(){
		if (isMaxYUserSet){
			return minY;
		} else {
			float min = lines.get(0).getPoint(0).getY();
			for (Line line : lines){
				for (LinePoint point : line.getPoints()){
					if (point.getY() < min) min = point.getY();
				}
			}
			minY = min;
			return minY;
		}
	}
	public float getMaxX(){
		float max = lines.get(0).getPoint(0).getX();
		for (Line line : lines){
			for (LinePoint point : line.getPoints()){
				if (point.getX() > max) max = point.getX();
			}
		}
		maxX = max;
		return maxX;
		
	}
	public float getMinX(){
		float max = lines.get(0).getPoint(0).getX();
		for (Line line : lines){
			for (LinePoint point : line.getPoints()){
				if (point.getX() < max) max = point.getX();
			}
		}
		maxX = max;
		return maxX;
	}
	
	public void onDraw(Canvas ca) {
		if (fullImage == null || shouldUpdate) {
			fullImage = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(fullImage);
			String max = (int)maxY+"";// used to display max
			String min = (int)minY+"";// used to display min
			paint.reset();
			Path path = new Path();

			float bottomPadding = 0, topPadding = 1;
			float sidePadding = 10;
			if(this.showMinandMax)
				sidePadding = txtPaint.measureText(max);
			
			float usableHeight = getHeight() - bottomPadding - topPadding;
			float usableWidth = getWidth() - sidePadding;
			float horzSpace = usableWidth/50;
			float vertLineCount = usableHeight/horzSpace;

            paint.setStyle( Paint.Style.STROKE );
            paint.setStrokeWidth( 1.0f );
            canvas.drawRect(0, 0, usableWidth, usableHeight, paint);

			int lineCount = 0;
			for (Line line : lines){
				int count = 0;
				float firstXPixels = 0, lastXPixels = 0, newYPixels = 0;
				float lastYPixels = 0, newXPixels = 0;
				float maxY = getMaxY();
				float minY = getMinY();
				float maxX = getMaxX();
				float minX = getMinX();
				
				if (lineCount == lineToFill){
					paint.setColor(fillColor);
					paint.setAlpha(fillAlpha);
					paint.setStrokeWidth(fillStrokeWidth);
					for (int i = 10; i-getWidth() < getHeight(); i = i+20){
						canvas.drawLine(i, getHeight()-bottomPadding, 0, getHeight()-bottomPadding-i, paint);
					}
					
					paint.reset();
					
					paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
					for (LinePoint p : line.getPoints()){
						float yPercent = (p.getY()-minY)/(maxY - minY);
						float xPercent = (p.getX()-minX)/(maxX - minX);
						if (count == 0){
							lastXPixels = sidePadding + (xPercent*usableWidth);
							lastYPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
							firstXPixels = lastXPixels;
							path.moveTo(lastXPixels, lastYPixels);
						} else {
							newXPixels = sidePadding + (xPercent*usableWidth);
							newYPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
							path.lineTo(newXPixels, newYPixels);
							Path pa = new Path();
							pa.moveTo(lastXPixels, lastYPixels);
							pa.lineTo(newXPixels, newYPixels);
							pa.lineTo(newXPixels, 0);
							pa.lineTo(lastXPixels, 0);
							pa.close();
							canvas.drawPath(pa, paint);
							lastXPixels = newXPixels;
							lastYPixels = newYPixels;
						}
						count++;
					}
					
					path.reset();
					
					path.moveTo(0, getHeight()-bottomPadding);
					path.lineTo(sidePadding, getHeight()-bottomPadding);
					path.lineTo(sidePadding, 0);
					path.lineTo(0, 0);
					path.close();
					canvas.drawPath(path, paint);
					
					path.reset();
					
					path.moveTo(getWidth(), getHeight()-bottomPadding);
					path.lineTo(getWidth()-sidePadding, getHeight()-bottomPadding);
					path.lineTo(getWidth()-sidePadding, 0);
					path.lineTo(getWidth(), 0);
					path.close();
					
					canvas.drawPath(path, paint);
					
				}
				
				lineCount++;
			}
			
			paint.reset();
			
			paint.setColor(this.gridColor);
			paint.setAlpha(50);
			paint.setAntiAlias(true);
			//canvas.drawLine(sidePadding, getHeight() - bottomPadding, getWidth(), getHeight()-bottomPadding, paint);
			if(this.showHorizontalGrid)
				for(int i=1;i<=vertLineCount; i++)
				{
					if (i % 5 == 0) {
						paint.setStrokeWidth(fillStrokeWidth*3);
						canvas.drawLine(0, getHeight() - bottomPadding - (i * horzSpace), usableWidth, getHeight() - bottomPadding - (i * horzSpace), paint);
					}else{
						paint.setStrokeWidth(fillStrokeWidth);
						canvas.drawLine(0, getHeight() - bottomPadding - (i * horzSpace), usableWidth, getHeight() - bottomPadding - (i * horzSpace), paint);
					}
				}
			for(int i=1;i<=50;i++)
			{
				if (i % 5 == 0) {
					paint.setStrokeWidth(fillStrokeWidth*3);
					canvas.drawLine(usableWidth-(i*horzSpace),bottomPadding,usableWidth-(i*horzSpace),getHeight(),paint);
				}else {
					paint.setStrokeWidth(fillStrokeWidth);
					canvas.drawLine(usableWidth-(i*horzSpace),bottomPadding,usableWidth-(i*horzSpace),getHeight(),paint);

			}
			}
			paint.setAlpha(255);
			
			
			for (Line line : lines){
				int count = 0;
				float lastXPixels = 0, newYPixels = 0;
				float lastYPixels = 0, newXPixels = 0;
				float maxY = getMaxY();
				float minY = getMinY();
				float maxX = getMaxX();
				float minX = getMinX();
				
				paint.setColor(line.getColor());
				paint.setStrokeWidth(6);
				
				for (LinePoint p : line.getPoints()){
					float yPercent = (p.getY()-minY)/(maxY - minY);
					float xPercent = (p.getX()-minX)/(maxX - minX);
					if (count == 0){
						lastXPixels = sidePadding + (xPercent*usableWidth);
						lastYPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
					} else {
						newXPixels = sidePadding + (xPercent*usableWidth);
						newYPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
						canvas.drawLine(lastXPixels, lastYPixels, newXPixels, newYPixels, paint);
						lastXPixels = newXPixels;
						lastYPixels = newYPixels;
					}
					count++;
				}
			}
			
			
			int pointCount = 0;
			
			for (Line line : lines){
				float maxY = getMaxY();
				float minY = getMinY();
				float maxX = getMaxX();
				float minX = getMinX();
				
				paint.setColor(line.getColor());
				paint.setStrokeWidth(6);
				paint.setStrokeCap(Paint.Cap.ROUND);
				
				if (line.isShowingPoints()){
					for (LinePoint p : line.getPoints()){
						float yPercent = (p.getY()-minY)/(maxY - minY);
						float xPercent = (p.getX()-minX)/(maxX - minX);
						float xPixels = sidePadding + (xPercent*usableWidth);
						float yPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
						
						//paint.setColor(Color.WHITE);
						//canvas.drawCircle(xPixels, yPixels, 10, paint);
						paint.setColor(Color.RED);
						canvas.drawCircle(xPixels, yPixels, 5, paint);
						
						Path path2 = new Path();
						path2.addCircle(xPixels, yPixels, 30, Direction.CW);
						p.setPath(path2);
						p.setRegion(new Region((int)(xPixels-30), (int)(yPixels-30), (int)(xPixels+30), (int)(yPixels+30)));
						
						if (indexSelected == pointCount && listener != null){
							paint.setColor(Color.parseColor("#33B5E5"));
							paint.setAlpha(100);
							canvas.drawPath(p.getPath(), paint);
							paint.setAlpha(255);
						}
						
						pointCount++;
					}
				}
			}
			
			shouldUpdate = false;
			if(this.showMinandMax)
			{
				ca.drawText(max, 0, txtPaint.getTextSize(), txtPaint);
				ca.drawText(min,0,this.getHeight(),txtPaint);
			}
		}
		
		ca.drawBitmap(fullImage, 0, 0, null);
		
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

	    Point point = new Point();
	    point.x = (int) event.getX();
	    point.y = (int) event.getY();
	    
	    int count = 0;
	    int lineCount = 0;
	    int pointCount = 0;
	    
	    Region r = new Region();
	    for (Line line : lines){
	    	pointCount = 0;
	    	for (LinePoint p : line.getPoints()){
	    		
	    		if (p.getPath() != null && p.getRegion() != null){
	    			r.setPath(p.getPath(), p.getRegion());
			    	if (r.contains((int)point.x,(int) point.y) && event.getAction() == MotionEvent.ACTION_DOWN){
			    		indexSelected = count;
			    	} else if (event.getAction() == MotionEvent.ACTION_UP){
			    		if (r.contains((int)point.x,(int) point.y) && listener != null){
			    			listener.onClick(lineCount, pointCount);
			    		}
			    		indexSelected = -1;
			    	}
	    		}
		    	
		    	pointCount++;
			    count++;
	    	}
	    	lineCount++;
	    	
	    }
	    
	    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP){
	    	shouldUpdate = true;
	    	postInvalidate();
	    }
	    
	    

	    return true;
	}
	
	public void setOnPointClickedListener(OnPointClickedListener listener) {
		this.listener = listener;
	}
	
	public interface OnPointClickedListener {
		abstract void onClick(int lineIndex, int pointIndex);
	}
}
