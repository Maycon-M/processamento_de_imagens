import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;


public class Filtros_Lineares implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.error("Nenhuma imagem aberta.");
            return;
        }

        GenericDialog gd = new GenericDialog("Filtro Espacial (3x3)");
        String[] options = {
            "Passa-Baixas (Média Aritmética)",
            "Passa-Altas (Realce de Bordas)",
            "Filtro de Borda (Sobel Horizontal)"
        };
        gd.addRadioButtonGroup("Escolha o filtro:", options, 1, options.length, options[0]);
        gd.showDialog();

        if (gd.wasCanceled()) return;

        String escolha = gd.getNextRadioButton();
        ImageProcessor ip = imp.getProcessor().duplicate();

        if (!(ip instanceof ij.process.ByteProcessor)) {
            ip = ip.convertToByte(true);
        }

        float[][] kernel = new float[3][3];
        float divisor = 1;

        switch (escolha) {
            case "Passa-Baixas (Média Aritmética)":
                kernel = new float[][] {
                    {1, 1, 1},
                    {1, 1, 1},
                    {1, 1, 1}
                };
                divisor = 9;
                break;

            case "Passa-Altas (Realce de Bordas)":
                kernel = new float[][] {
                    {-1, -1, -1},
                    {-1,  9, -1},
                    {-1, -1, -1}
                };
                
                /*kernel = new float[][] {
                	{1, -2, 1},
                    {-2, 5, -2}, 
                    {1, -2, 1} 
                };*/
                break;

            case "Filtro de Borda (Sobel Horizontal)":
                kernel = new float[][] {
                    {-1, -2, -1},
                    { 0,  0,  0},
                    { 1,  2,  1}
                };
                break;
        }

        aplicarConvolucao(ip, kernel, divisor);
        new ImagePlus("Resultado - " + escolha, ip).show();
    }

    private void aplicarConvolucao(ImageProcessor ip, float[][] kernel, float divisor) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        ImageProcessor original = ip.duplicate();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                float soma = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int px = original.getPixel(x + kx, y + ky);
                        soma += kernel[ky + 1][kx + 1] * px;
                    }
                }

                int novoValor = (int)(soma / divisor);
                novoValor = Math.max(0, Math.min(255, novoValor));
                ip.putPixel(x, y, novoValor);
            }
        }
    }
}
