/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.metacsp.utility.timelinePlotting;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.symbolsAndTime.SymbolicTimeline;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.PlotBoxTLSmall;
import org.metacsp.utility.logging.MetaCSPLogging;

/**
 * A utility class that that extracts {@link SymbolicTimeline}s from components and publishes them as images.
 * @author Jonas Ullberg, Federico Pecora
 */
public final class TimelinePublisher
{
	private final TimelineEncoder imageEncoder = new TimelineEncoder(4096/*768*/, 80, "png", "TimeLine", "TimeLines");

	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());

	private String[] components;
	private ConstraintNetwork an;
	private long min = Long.MAX_VALUE;
	private long max = Long.MIN_VALUE;
	private Bounds bounds;
	private boolean slidingWindow = false;
	private TimelineVisualizer viz = null;
	private long timeNow = 0;
	private long temporalResolution = 1;
	private long origin = 0;

	private void computeOrigin() {
		ArrayList<Long> startTimes = new ArrayList<Long>();
		for (Variable v : an.getVariables()) {
			if (v instanceof Activity) {
				startTimes.add(((Activity) v).getTemporalVariable().getEST());
			}
		}
		if (!startTimes.isEmpty()) {
			Collections.sort(startTimes);
			this.origin = startTimes.get(0);
		}
		else {
			this.origin = 0;
		}
	}
	
	/**
	 * @param an The {@link ConstraintNetwork} used to calculate the {@link SymbolicTimeline}s.
	 * @param bounds The range in which to plot {@link SymbolicTimeline}s.
	 * @param slidingWindow If <code>true</code>, the bounds are interpreted as a sliding window.
	 * @param components List of components for which to publish {@link SymbolicTimeline}s.
	 */
	public TimelinePublisher(ConstraintNetwork an, Bounds bounds, boolean slidingWindow, String ... components)
	{
		this.components = components;
		this.an = an;
		this.bounds = bounds;
		this.imageEncoder.start();
		this.slidingWindow = slidingWindow;
		computeOrigin();
	}

	/**
	 * @param ans The {@link ActivityNetworkSolver} used to calculate the {@link SymbolicTimeline}s.
	 * @param bounds The range in which to plot {@link SymbolicTimeline}s.
	 * @param slidingWindow If <code>true</code>, the bounds are interpreted as a sliding window.
	 * @param components List of components for which to publish {@link SymbolicTimeline}s.
	 */
	@Deprecated
	public TimelinePublisher(ActivityNetworkSolver ans, Bounds bounds, boolean slidingWindow, String ... components)
	{
		this.components = components;
		this.an = ans.getConstraintNetwork();
		this.bounds = bounds;
		this.imageEncoder.start();
		this.slidingWindow = slidingWindow;
		this.origin = ans.getOrigin();
	}

	/**
	 * @param an The {@link ConstraintNetwork} used to calculate the {@link SymbolicTimeline}s.
	 * @param bounds The range in which to plot {@link SymbolicTimeline}s.
	 * @param components List of components for which to publish {@link SymbolicTimeline}s.
	 */
	public TimelinePublisher(ConstraintNetwork an, Bounds bounds, String ... components)
	{
		this.components = components;
		this.an = an;
		this.bounds = bounds;
		this.imageEncoder.start();
		computeOrigin();
	}
	
	/**
	 * @param ans The {@link ActivityNetworkSolver} used to calculate the {@link SymbolicTimeline}s.
	 * @param bounds The range in which to plot {@link SymbolicTimeline}s.
	 * @param components List of components for which to publish {@link SymbolicTimeline}s.
	 */
	@Deprecated
	public TimelinePublisher(ActivityNetworkSolver ans, Bounds bounds, String ... components)
	{
		this.components = components;
		this.an = ans.getConstraintNetwork();
		this.bounds = bounds;
		this.imageEncoder.start();
		this.origin = ans.getOrigin();
	}
	
	/**
	 * @param an The {@link ConstraintNetwork} used to calculate the {@link SymbolicTimeline}s.
	 * @param components List of components for which to publish {@link SymbolicTimeline}s.
	 */
	public TimelinePublisher(ConstraintNetwork an, String ... components) {
		this(an,null,components);
	}

	/**
	 * @param ans The {@link ActivityNetworkSolver} used to calculate the {@link SymbolicTimeline}s.
	 * @param components List of components for which to publish {@link SymbolicTimeline}s.
	 */
	@Deprecated
	public TimelinePublisher(ActivityNetworkSolver ans, String ... components) {
		this(ans,null,components);
	}
	
	/**
	 * Sets the temporal resolution of this publisher (default is milliseconds).
	 * @param temporalResolution The desired temporal resolution of this publisher (e.g., seconds if <code>temporalResolution = 1000</code>).
	 */
	public void setTemporalResolution(long temporalResolution) {
		this.temporalResolution = temporalResolution;
	}

	/**
	 * Make a given {@link TimelineVisualizer} display the {@link SymbolicTimeline}s published
	 * by this {@link TimelinePublisher}.  Note that newly instantiated {@link TimelineVisualizer}s are
	 * automatically registered to the {@link TimelinePublisher} given in the constructor, so there is
	 * usually no need to call this method.
	 * @param viz The {@link TimelineVisualizer} that should display the {@link SymbolicTimeline}s published
	 * by this {@link TimelinePublisher}.
	 */
	public void registerTimelineVisualizer(TimelineVisualizer viz) {
		this.viz = viz;
	}
	
	/**
	 * Set the list of components for which to publish {@link SymbolicTimeline}s.
	 * @param components The list of components for which to publish {@link SymbolicTimeline}s.
	 */
	public void setComponents(String ... components) {
		this.components = components;
	}
		
	private final ArrayList<SymbolicTimeline> timelinesToRefresh = new ArrayList<SymbolicTimeline>();
	
	/**
	 * Publish the {@link SymbolicTimeline}s.  This method renders in a background thread (unless blocking behavior
	 * is requested through parameter <code>block</code>).
	 * @param block Set to <code>true</code> if the call should block until {@link SymbolicTimeline}s are rendered.
	 * @param skip Set to <code>true</code> if this call should be skipped in case previous rendering is still in progress.
	 */
	public void publish(boolean block, boolean skip) {
		
		//Sort components so we always get the same timline order...
//		Arrays.sort(components);
		
		//If blocking behavior has been requested, wait until any previous call 
		//to the image encoding thread has finished. The rest of the
		//code in this function is written from an "encode if possible" perspective.
		if (!skip) imageEncoder.waitUntilFinished();

		if(!imageEncoder.isWorking()) {
			timelinesToRefresh.clear();
			if (bounds != null) {
				min = bounds.min+origin;
				max = bounds.max+origin;
			}
			else {
				min = origin;
				max = Long.MIN_VALUE;
			}
			for(int tl = 0; tl < components.length; tl++) {
				String comp = components[tl];
				SymbolicTimeline stl  = null;
				//synchronized(ans) {
				synchronized(an) {
					//stl = new SymbolicTimeline(ans, comp);
					stl = new SymbolicTimeline(an, comp);
				}
				if (bounds == null) {
					if (stl.getPulses()[stl.getPulses().length-1] > max) max = stl.getPulses()[stl.getPulses().length-1];
				}
				timelinesToRefresh.add(stl);
			}
			long delta = 0;
			double portionBefore = 0.9;
			if (slidingWindow && bounds != null) {
				delta = bounds.max-bounds.min;				
				if (((double)timeNow)/temporalResolution > origin+((double)delta)*portionBefore) {
					min = (long)( ((double)timeNow)/(double)temporalResolution  -  ((double)delta)*portionBefore);
					max = (long)( ((double)timeNow)/(double)temporalResolution  +  ((double)delta)*(1-portionBefore));
				}
			}
			imageEncoder.encodeTimelines(timelinesToRefresh);
			logger.finest("Image being rendered...");
		}
		else {
			logger.finest("Skipped rendering image.");
		}
		
		//If blocking behavior has been requested, do not return until
		//the image encoding thread has finished.
		if (block) imageEncoder.waitUntilFinished();
	}	
	
	/**
	 * Encodes time line images in the background and publishes them to files in a directory called "Timelines".
	 * @author Jonas Ullberg, Federico Pecora
	 */
	private final class TimelineEncoder extends Thread
	{
		private ArrayList<SymbolicTimeline> currentTimelines = null;
		private final Object criticalSection = new Object(); 
		private final Semaphore runSemaphore = new Semaphore(0);
		private final Semaphore waitingSemaphore = new Semaphore(1);
		private int numTimeLinesPublished = 0;
		
		private final int imageWidth;
		private final int subImageHeight;
		private final String imageType;
		
		private final String filePath;

		/**
		 * @param imageWidth the width of the timeline image
		 * @param subImageHeight the width of each component row in the time line image
		 * @param imageType the image type, either "png" or "jpg"
		 * @param peisKey the key to write time lines to, e.g. "TimeLine" or null to suppress
		 * writing graphical time lines to PEIS.
		 * @param filePath the path to write the encoded time lines to, e.g. "./TimeLines" or null to suppress writing
		 * to file.
		 */
		public TimelineEncoder(int imageWidth, int subImageHeight, String imageType, String peisKey, String filePath)
		{
			this.imageWidth = imageWidth;
			this.subImageHeight = subImageHeight;
			this.imageType = imageType;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
			Date date = new Date(Calendar.getInstance().getTimeInMillis());
			String time = sdf.format(date);
			this.filePath = filePath+"-"+time;
		}
				
		@Override
		public void run()
		{
			boolean tryCreateOutputDir = true;
			BufferedImage mergedImage = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream(50*1024);
			long startTime = Calendar.getInstance().getTimeInMillis();
			for(;;)
			{
				timeNow = origin*temporalResolution+(Calendar.getInstance().getTimeInMillis()-startTime);				
				//Wait until we are cleared to encode an image
				try { runSemaphore.acquire(); }
				catch (InterruptedException e) { break; }
				
				if(mergedImage == null || mergedImage.getHeight() != subImageHeight * currentTimelines.size())
				{
					mergedImage = new BufferedImage(imageWidth, subImageHeight * currentTimelines.size(), BufferedImage.TYPE_INT_RGB);
				}
				
				for(int tl = 0; tl < currentTimelines.size(); tl++)
				{
					SymbolicTimeline stl = currentTimelines.get(tl);
					if(stl != null)
					{
						BufferedImage img = (new PlotBoxTLSmall(stl, stl.getComponent(), false, false, min, max)).getBufferedImage(imageWidth, subImageHeight);
						Graphics2D g2 = mergedImage.createGraphics();
						g2.drawImage(img, 0, tl*subImageHeight, null);
						g2.dispose();
					}
				}
				
				baos.reset();
				viz.setImage(mergedImage);
				try
				{
					ImageIO.write(mergedImage, imageType, baos);
				} catch (IOException e) { e.printStackTrace(); System.exit(1); }
				
				if(filePath != null)
				{
					if(tryCreateOutputDir)
					{
						new File(filePath).mkdirs();
						tryCreateOutputDir = false;
					}
					
					try
					{
						String fileName = String.format("%s" + File.separator + "TL_#%04d.%s", filePath, numTimeLinesPublished++, imageType);						
						FileOutputStream fos = new FileOutputStream(fileName);
						fos.write(baos.toByteArray());
						fos.close();
					}
					catch(Exception e) { e.printStackTrace(); }
				}
				
				//Release any thread waiting to encode
				synchronized(criticalSection)
				{
					currentTimelines = null;
					waitingSemaphore.release();
				}
			}
		}
		
		/**
		 * Assess whether the image rendering thread is working.
		 * @return <code>true</code> if the {@link TimelineEncoder} is currently
		 * encoding an image.
		 */
		public boolean isWorking()
		{
			synchronized(criticalSection) { return currentTimelines != null; }
		}
		
		public boolean encodeTimelines(List<SymbolicTimeline> timelines)
		{
			//Wait until the job is finished
			waitingSemaphore.acquireUninterruptibly();
			
			//Set the encoding target
			synchronized(criticalSection)
			{
				assert currentTimelines == null;
				currentTimelines = new ArrayList<SymbolicTimeline>(timelines); 
			}

			//Release the encoding thread
			runSemaphore.release();
			return true;
		}
		
		/**
		 * Waits until the {@link TimelineEncoder} is ready to encode another set of time lines
		 */
		void waitUntilFinished()
		{
			waitingSemaphore.acquireUninterruptibly();
			waitingSemaphore.release();
		}
	}
}
