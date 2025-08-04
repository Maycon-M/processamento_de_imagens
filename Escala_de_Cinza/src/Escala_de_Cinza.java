import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Escala_de_Cinza implements PlugIn {

	@Override
	public void run(String arg) {
		String[] titles = WindowManager.getImageTitles();
		GenericDialog gd = new GenericDialog("Converter para Escala de Cinza");
		gd.addChoice("Imagem:", titles, titles.length>0?titles[0]:"");
		gd.addRadioButtonGroup("Método:", new String[]{
		    "Média (R+G+B)/3",
		    "Lum (0.299, 0.587, 0.114)",
		    "ITU-BT.709 (0.2125, 0.7154, 0.072)"
		}, 3, 1, "Média (R+G+B)/3");
		gd.addCheckbox("Criar nova imagem", true);
		gd.showDialog();
		if (gd.wasCanceled()) return;

		String title    = gd.getNextChoice();
		String method   = gd.getNextRadioButton();
		boolean createNew = gd.getNextBoolean();

		ImagePlus imp = WindowManager.getImage(title);
		if (imp == null) {
		    IJ.error("Imagem selecionada não encontrada.");
		    return;
		}
        ImageProcessor ip = imp.getProcessor();
        if (!(ip instanceof ColorProcessor)) {
            IJ.error("A imagem deve ser do tipo RGB.");
            return;
        }
        ColorProcessor cp = (ColorProcessor)ip;

        int w = imp.getWidth(), h = imp.getHeight();
        ByteProcessor gray = new ByteProcessor(w, h);

        int[] rgb = new int[3];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                cp.getPixel(x, y, rgb);
                int Y;
                switch (method) {
                    case "Média (R+G+B)/3":
                        Y = computeAverage(rgb[0], rgb[1], rgb[2]);
                        break;
                    case "Lum (0.299, 0.587, 0.114)":
                        Y = computeLum(rgb[0], rgb[1], rgb[2]);
                        break;
                    default:
                        Y = computeBT709(rgb[0], rgb[1], rgb[2]);
                        break;
                }
                gray.putPixel(x, y, Y);
            }
        }

        if (createNew) {
            new ImagePlus(imp.getTitle() + "_gray", gray).show();
        } else {
            imp.setProcessor(imp.getTitle(), gray);
            imp.updateAndDraw();
        }
    }

    private int computeAverage(int r, int g, int b) {
        return (r + g + b) / 3;
    }

    private int computeLum(int r, int g, int b) {
        return (int)(0.299 * r + 0.587 * g + 0.114 * b);
    }

    private int computeBT709(int r, int g, int b) {
        return (int)(0.2125 * r + 0.7154 * g + 0.072 * b);
    }

}
