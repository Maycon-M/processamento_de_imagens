import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Extrator_de_ROIs implements PlugIn {
		
    @Override
    public void run(String arg) {
     
        JFileChooser inputChooser = new JFileChooser();
        inputChooser.setDialogTitle("Selecione o diretório de entrada");
        inputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (inputChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File inputDir = inputChooser.getSelectedFile();

     
        JFileChooser outputChooser = new JFileChooser();
        outputChooser.setDialogTitle("Selecione o diretório de saída");
        outputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (outputChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File outputDir = outputChooser.getSelectedFile();

       
        File[] files = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".tif") || 
                                                         name.toLowerCase().endsWith(".gif") || 
                                                         name.toLowerCase().endsWith(".png") || 
                                                         name.toLowerCase().endsWith(".jpg") ||
                                                         name.toLowerCase().endsWith(".jpeg"));
        if (files == null || files.length == 0) {
            IJ.showMessage("Nenhuma imagem encontrada no diretório de entrada.");
            return;
        }

        for (File file : files) {
            IJ.log("Processando imagem: " + file.getName());
            processImage(file, outputDir);
        }

        IJ.showMessage("Extração de ROIs concluída!");
    }

        private void processImage(File file, File outputDir) {
      
        ImagePlus originalImg = IJ.openImage(file.getAbsolutePath());
        if (originalImg == null) {
            IJ.log("Erro ao abrir imagem: " + file.getName());
            return;
        }

        ImagePlus img = originalImg.duplicate(); 
        IJ.run(img, "Subtract Background...", "rolling=20 light sliding");
        
        IJ.run(img, "Gaussian Blur...", "sigma=5");
        
        IJ.run(img, "8-bit", "");
        
        IJ.setAutoThreshold(img, "Otsu dark");
        
        IJ.run(img, "Convert to Mask", "");
        
        IJ.run(img, "Invert", "");
        
        IJ.run(img, "Fill Holes", "");
        
        IJ.run(img, "Remove Outliers...", "radius=2 threshold=30 which=Bright");
        
        IJ.run(img, "Analyze Particles...", 
        	       "size=5000-Infinity add");
        
        RoiManager roiManager = RoiManager.getInstance();
        if (roiManager == null) roiManager = new RoiManager();

        Roi[] rois = roiManager.getRoisAsArray();

      
        for (int i = 0; i < rois.length; i++) {
            
            originalImg.setRoi(rois[i]);
            ImageProcessor processor = originalImg.getProcessor().crop(); 
            ImagePlus roiImage = new ImagePlus("ROI_" + i, processor);

            
            File outputFile = new File(outputDir, file.getName().replaceAll("\\.\\w+$", "") + "_ROI" + (i + 1) + ".png");
            
            try {
                ImageIO.write(roiImage.getBufferedImage(), "PNG", outputFile);
                IJ.log("ROI salva em RGB: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                IJ.log("Erro ao salvar ROI: " + e.getMessage());
            }
        }

     
        roiManager.reset();
        originalImg.close();
        img.close();
    }
}