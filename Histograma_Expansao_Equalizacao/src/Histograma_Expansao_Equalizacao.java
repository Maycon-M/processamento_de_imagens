import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Histograma_Expansao_Equalizacao implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.error("Nenhuma imagem aberta.");
            return;
        }

        GenericDialog gd = new GenericDialog("Operações de Histograma");
        String[] options = {"Expansão", "Equalização"};
        gd.addRadioButtonGroup("Escolha a operação:", options, 2, 1, "Expansão");
        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }

        String choice = gd.getNextRadioButton();
        ImageProcessor ip = imp.getProcessor().duplicate();
        
        if (!(ip instanceof ij.process.ByteProcessor)) {
            ip = ip.convertToByte(true);
        }


        if (choice.equals("Expansão")) {
            aplicarExpansao(ip);
            IJ.showMessage("Expansão aplicada.");
        } else if (choice.equals("Equalização")) {
            aplicarEqualizacao(ip);
            IJ.showMessage("Equalização aplicada.");
        }

        new ImagePlus("Resultado - " + choice, ip).show();
    }

    private void aplicarExpansao(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();

        int min = 255;
        int max = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = ip.getPixel(x, y);
                if (val < min) min = val;
                if (val > max) max = val;
            }
        }


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = ip.getPixel(x, y);
                int newVal = (int)(((p - min) * 255.0) / (max - min));
                ip.putPixel(x, y, newVal);
            }
        }
    }

    private void aplicarEqualizacao(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int totalPixels = width * height;

        int[] hist = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                hist[ip.getPixel(x, y)]++;
            }
        }

        int[] cdf = new int[256];
        cdf[0] = hist[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + hist[i];
        }

        int[] eq = new int[256];
        for (int i = 0; i < 256; i++) {
            eq[i] = (int)(255.0 * cdf[i] / totalPixels);
        }


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = ip.getPixel(x, y);
                ip.putPixel(x, y, eq[p]);
            }
        }
    }
}
