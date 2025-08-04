import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Splitar_RGB implements PlugIn {

	@Override
	public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp.getType() != ImagePlus.COLOR_RGB) {
            IJ.error("Este plugin requer uma imagem RGB.");
            return;
        }
        ImageProcessor ip = imp.getProcessor();
        int width = ip.getWidth();
        int height = ip.getHeight();

        ByteProcessor redProc   = new ByteProcessor(width, height);
        ByteProcessor greenProc = new ByteProcessor(width, height);
        ByteProcessor blueProc  = new ByteProcessor(width, height);

        int[]  rgbPixels = (int[])    ip.getPixels();
        byte[] rPixels   = (byte[]) redProc.getPixels();
        byte[] gPixels   = (byte[]) greenProc.getPixels();
        byte[] bPixels   = (byte[]) blueProc.getPixels();

        for (int i = 0; i < rgbPixels.length; i++) {
            int c = rgbPixels[i];
            rPixels[i] = (byte) ((c & 0xFF0000) >> 16);
            gPixels[i] = (byte) ((c & 0x00FF00) >>  8);
            bPixels[i] = (byte)  (c & 0x0000FF);
        }

        new ImagePlus("Canal Vermelho", redProc).show();
        new ImagePlus("Canal Verde",   greenProc).show();
        new ImagePlus("Canal Azul",    blueProc).show();
	}

}
