/**
 *****************************************************************************
 * Copyright (c) 2015 Daniel Gerighausen, Lydia Mueller, and Dirk Zeckzer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************
 */
package biovis.sierra.data.IO;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import biovis.sierra.data.ConfigObject;
import biovis.sierra.data.DataMapper;
import biovis.sierra.data.Replicate;
import biovis.sierra.data.Version;
import biovis.sierra.data.peakcaller.Peak;
import biovis.sierra.data.peakcaller.PeakList;
import biovis.sierra.data.windows.Window;
import biovis.sierra.data.windows.WindowList;
import biovis.sierra.server.Commander.ServerMapper;

import java.util.List;

/**
 *
 * @author Daniel Gerighausen, Lydia Mueller
 */
public class Exporter {

	/**
	 * Export peak data in bed format.
	 *
	 * @param filename file name
	 * @param pl peak list
	 * @param description description
	 */
	public static void exportBED(String filename, PeakList pl, String description)
	{
		if(!filename.endsWith(".bed")){
			filename += ".bed";
		}

		try (
				BufferedWriter bw = new BufferedWriter(new PrintWriter(filename));
				) {
			for(Peak p : pl.getPeaks()){
				bw.write(p.getChr()+"\t"+p.getStart()+"\t"+p.getEnd()+"\t"+description+"\t"+p.getPvalue());
				bw.newLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void exportCalcParameters(String filename, DataMapper mapper)
	{
		if(!filename.endsWith(".stats")){
			filename += ".stats";
		}
		try (
				BufferedWriter bw = new BufferedWriter(new PrintWriter(filename));
				) {
			bw.write("This file is generated by Sierra Version "
					+ Version.VERSION);
			bw.newLine();
			bw.write("Weighted: " + mapper.isWeighted());
			bw.newLine();
			bw.write("Correcting correlations: " + mapper.isCorrectCorrelation());
			bw.newLine();
			bw.write("Replicates:");
			bw.newLine();
			for(Replicate rep : mapper.getReplicates())
			{
				bw.write("Experiment: " + rep.getExperiment().getDescription());
				bw.newLine();
				bw.write("Background: " + rep.getBackground().getDescription());
				bw.newLine();
				bw.write("Active: " + rep.isActive());
				bw.newLine();
				bw.write("Weight: " + rep.getWeight());
				bw.newLine();
			}
			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Export peak data in csv format.
	 *
	 * @param filename file name
	 * @param pl peak list
	 */
	public static void exportCSV(String filename, PeakList pl) {
		if(!filename.endsWith(".csv")){
			filename += ".csv";
		}

		try (
				BufferedWriter bw = new BufferedWriter(new PrintWriter(filename));
				) {
			for(Peak p : pl.getPeaks()){
				bw.write(p.getChr()+","+p.getStart()+","+p.getEnd()+","+p.getPvalue());
				bw.newLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Export windows.
	 *
	 * @param filename file name
	 * @param windows windows
	 * @param mapper data mapper
	 */
	public static void exportWindows(String filename, WindowList windows, DataMapper mapper)
	{
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		try (
				FileOutputStream fos = new FileOutputStream(filename);
				GZIPOutputStream gzos = new GZIPOutputStream(fos);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzos, "UTF-8"));
				) {


			String stringMapper = gson.toJson(mapper);
			writer.append(stringMapper);
			writer.newLine();


			for(Window w : windows.getWindows())
			{

				String dataString = gson.toJson(w);

				writer.append(dataString);
				writer.newLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void exportServerConfig(String filename, ServerMapper sm)
	{
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		try (
				FileOutputStream fos = new FileOutputStream(filename);
				GZIPOutputStream gzos = new GZIPOutputStream(fos);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzos, "UTF-8"));
				) {


			String stringMapper = gson.toJson(sm);
			writer.append(stringMapper);

			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Export replicates.
	 *
	 * @param filename file name
	 * @param replicates replicates
	 * @param mapper data mapper
	 * @param peakmode peak mode
	 */
	public static void exportReplicates(String filename, List<Replicate> replicates, DataMapper mapper)
	{
		ConfigObject conf = new ConfigObject();

		for(Replicate rep: replicates)
		{
			conf.addReplicate(rep.getExperiment().getDescription(), rep.getBackground().getDescription(), rep.getName());
		}
		//System.err.println("Numcores before export : "+ mapper.getNumCores());
		conf.setSettings(mapper.getWindowsize(), mapper.getOffset(), mapper.getPvaluecutoff(), mapper.getNumCores(), mapper.getJobName());
		//System.err.println("Before Export");
		//System.err.println(mapper.getOffset());
		//System.err.println(mapper.getPvaluecutoff());
		//System.err.println(mapper.getWindowsize());
		//System.err.println("------------------");

		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		try (
				FileOutputStream fos = new FileOutputStream(filename);
				GZIPOutputStream gzos = new GZIPOutputStream(fos);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzos, "UTF-8"));
				) {
			//		 BufferedWriter writer = null;

			String stringMapper = gson.toJson(conf);
			writer.append(stringMapper);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	/**
	 * Export image.
	 *
	 * @param snapshot image
	 * @param height height
	 * @param width width
	 * @param path path
	 */
	public  static void exportImage(WritableImage snapshot, int height, int width, String path) {
		//		  System.err.println("0");
		BufferedImage image;
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image = javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, bufferedImage);
		File file = new File(path);
		//	    System.err.println(path);
		try {
			Graphics2D gd = (Graphics2D) image.getGraphics();
			//	        System.err.println("1");
			gd.translate(width, height);
			//	        System.err.println("2");
			ImageIO.write(image, "png", file);
			//	        System.err.println("3");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Export data mapper.
	 *
	 * @param filename file name
	 * @param mapper data mapper
	 */
	public static void exportMapper(String filename, DataMapper mapper)
	{

		//		Gson gson = new Gson();
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String dataString = gson.toJson(mapper);

		try (
				FileOutputStream fos = new FileOutputStream(filename);
				GZIPOutputStream gzos = new GZIPOutputStream(fos);
				) {
			gzos.write(dataString.getBytes());
			gzos.finish();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Write progress to web page.
	 *
	 * @param filename file name of web page
	 * @param progress progress
	 */
	public static void printHTML(String filename ,Double progress)
	{
		progress = progress * 100.0;

		String percent = String.valueOf(progress);
		percent = percent.substring(0,percent.indexOf(".")+2);

		File f = new File(filename);
		try (
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				) {
			bw.write("<!DOCTYPE html>");
			bw.write("<html>");
			bw.write("<head> <meta http-equiv=\"refresh\" content=\"30\" > </head>");
			bw.write("<body>");
			bw.write("<h1>Sierra Progress</h1>");
			bw.write("<p>"+percent + "% is already calculated! </p>");
			bw.write("</body>");
			bw.write("</html>");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
