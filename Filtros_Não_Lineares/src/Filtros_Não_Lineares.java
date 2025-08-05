import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Filtros_Não_Lineares implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.error("Nenhuma imagem aberta.");
            return;
        }

        GenericDialog gd = new GenericDialog("Filtros Não Lineares");
        String[] options = {"Sobel", "Mediana"};
        gd.addRadioButtonGroup("Escolha o filtro:", options, 1, options.length, options[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        String escolha = gd.getNextRadioButton();
        ImageProcessor ip = imp.getProcessor().duplicate();
        if (!(ip instanceof ij.process.ByteProcessor)) {
            ip = ip.convertToByte(true);
        }

        if (escolha.equals("Sobel")) {
            aplicarSobel(ip);
        } else if (escolha.equals("Mediana")) {
            aplicarMediana(ip);
        }
    }

    private void aplicarSobel(ImageProcessor ip) {
        float[][] sobelX = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        };
        float[][] sobelY = {
            {-1, -2, -1},
            { 0,  0,  0},
            { 1,  2,  1}
        };

        int width = ip.getWidth();
        int height = ip.getHeight();

        ImageProcessor gx = ip.duplicate();
        ImageProcessor gy = ip.duplicate();

        ImageProcessor original = ip.duplicate();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                float somaX = 0;
                float somaY = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) { 
                        int px = original.getPixel(x + kx, y + ky);
                        somaX += sobelX[ky + 1][kx + 1] * px;
                        somaY += sobelY[ky + 1][kx + 1] * px;
                    }
                }

                int valX = Math.min(255, Math.max(0, Math.round(Math.abs(somaX))));
                int valY = Math.min(255, Math.max(0, Math.round(Math.abs(somaY))));
                gx.putPixel(x, y, valX);
                gy.putPixel(x, y, valY);
            }
        }

        new ImagePlus("Sobel - Horizontal (Gx)", gx).show();
        new ImagePlus("Sobel - Vertical (Gy)", gy).show();

        ImageProcessor gFinal = ip.duplicate();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pxX = gx.getPixel(x, y);
                int pxY = gy.getPixel(x, y);
                int val = (int) Math.min(255, Math.round(Math.sqrt(pxX * pxX + pxY * pxY)));
                gFinal.putPixel(x, y, val);
            }
        }

        new ImagePlus("Sobel - Combinado", gFinal).show();
    }

    private void aplicarMediana(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        ImageProcessor original = ip.duplicate();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] vizinhos = new int[9];
                int idx = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        vizinhos[idx++] = original.getPixel(x + kx, y + ky); //adicionando a matriz 3x3 em vetor
                    }
                }

                Arrays.sort(vizinhos); //ordenação do vetor
                int mediana = vizinhos[4]; 
                ip.putPixel(x, y, mediana);
            }
        }

        new ImagePlus("Filtro de Mediana", ip).show();
    }
}
