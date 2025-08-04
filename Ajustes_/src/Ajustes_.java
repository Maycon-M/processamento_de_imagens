import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.DialogListener;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;

public class Ajustes_ implements PlugIn, DialogListener {

    private ImagePlus imp;
    private ColorProcessor original;
    private ColorProcessor preview;
    private ImagePlus previewWindow;

    @Override
    public void run(String arg) {
        imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.error("Abra uma imagem RGB antes.");
            return;
        }
        ImageProcessor ip = imp.getProcessor();
        if (!(ip instanceof ColorProcessor)) {
            IJ.error("A imagem deve ser RGB.");
            return;
        }

        original = (ColorProcessor) ip.duplicate();
        preview  = (ColorProcessor) original.duplicate();
        previewWindow = null;


        GenericDialog gd = new GenericDialog("Ajustes de Imagem");
        gd.addSlider("Brilho:",           -255, 255,  0);
        gd.addSlider("Contraste:",        -100, 100,  0);
        gd.addSlider("Solarização (lim):",   0, 255,255);
        gd.addSlider("Dessaturação (%):",     0,  100, 0);
        gd.addCheckbox("Nova imagem", true);
        gd.addDialogListener(this);
        gd.showDialog();


        if (gd.wasCanceled()) {
            if (previewWindow != null) previewWindow.close();
            imp.setProcessor(original);
            imp.updateAndDraw();
        }
    }

    @Override
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {

        int brightness     = (int) gd.getNextNumber();
        int contrast       = (int) gd.getNextNumber();
        int solarThreshold = (int) gd.getNextNumber();
        double desatFactor = gd.getNextNumber() / 100.0;
        boolean newImage   = gd.getNextBoolean();

        
        preview = (ColorProcessor) original.duplicate();
        processPixels(brightness, contrast, solarThreshold, desatFactor);


        if (newImage) {
            if (previewWindow == null) {
                previewWindow = new ImagePlus(imp.getTitle() + "_preview", preview);
                previewWindow.show();
            } else {
                previewWindow.setProcessor(preview);
                previewWindow.updateAndDraw();
            }

        } else {

            if (previewWindow != null) {
                previewWindow.close();
                previewWindow = null;
            }

            imp.setProcessor(preview);
            imp.updateAndDraw();
        }
        return true;
    }

    private void processPixels(int brightness, int contrast, int solarThreshold, double desatFactor) {
        int w = original.getWidth();
        int h = original.getHeight();
        int[] rgb = new int[3];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                original.getPixel(x, y, rgb);
                int r = rgb[0], g = rgb[1], b = rgb[2];

                r = clamp(r + brightness);
                g = clamp(g + brightness);
                b = clamp(b + brightness);
                
                if (contrast != 0) {
                    double factor = (259.0 * (contrast + 255)) / (255 * (259.0 - contrast));
                    r = clamp((int) (factor * (r - 128) + 128));
                    g = clamp((int) (factor * (g - 128) + 128));
                    b = clamp((int) (factor * (b - 128) + 128));
                }

                if (r > solarThreshold) r = 255 - r;
                if (g > solarThreshold) g = 255 - g;
                if (b > solarThreshold) b = 255 - b;

                if (desatFactor > 0) {
                    int gray = (r + g + b) / 3;
                    r = clamp((int) (r + (gray - r) * desatFactor));
                    g = clamp((int) (g + (gray - g) * desatFactor));
                    b = clamp((int) (b + (gray - b) * desatFactor));
                }
                preview.putPixel(x, y, new int[]{r, g, b});
            }
        }
    }


    private int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
