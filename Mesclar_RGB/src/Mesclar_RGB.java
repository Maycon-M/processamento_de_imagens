import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;

public class Mesclar_RGB implements PlugIn {

	@Override
	public void run(String arg) {
		int[] ids = WindowManager.getIDList();
        if (ids == null || ids.length < 3) {
            IJ.error("É necessário ter pelo menos três imagens 8-bits abertas.");
            return;
        }
        String[] titles = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            titles[i] = WindowManager.getImage(ids[i]).getTitle();
        }

        GenericDialog gd = new GenericDialog("Mesclar Canais RGB");
        gd.addChoice("Vermelho:", titles, titles[0]);
        gd.addChoice("Verde:   ", titles, titles[1]);
        gd.addChoice("Azul:    ", titles, titles[2]);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        ImagePlus impR = WindowManager.getImage(ids[gd.getNextChoiceIndex()]);
        ImagePlus impG = WindowManager.getImage(ids[gd.getNextChoiceIndex()]);
        ImagePlus impB = WindowManager.getImage(ids[gd.getNextChoiceIndex()]);

        int w = impR.getWidth(), h = impR.getHeight();
        ColorProcessor cp = new ColorProcessor(w, h);
        byte[] r = (byte[]) impR.getProcessor().getPixels();
        byte[] g = (byte[]) impG.getProcessor().getPixels();
        byte[] b = (byte[]) impB.getProcessor().getPixels();

        for (int i = 0; i < r.length; i++) {
            int rr = r[i] & 0xFF;
            int gg = g[i] & 0xFF;
            int bb = b[i] & 0xFF;
            cp.set(i % w, i / w, (rr << 16) | (gg << 8) | bb);
        }

        new ImagePlus("RGB Mesclado", cp).show();
    }

}
