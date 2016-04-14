/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Morphology.Operation;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.util.IJUtils;


/**
 * Various morphological filters for 3D images.
 * 
 * It is possible to specify shapes that correspond to planar structuring
 * elements. In this case, the planar structuring element is stacked as many
 * times as necessary to fit the specified z size.
 * 
 * @author David Legland
 *
 */

public class MorphologicalFilter3DPlugin implements PlugIn 
{
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) 
	{
		if ( IJ.getVersion().compareTo("1.48a") < 0 )
		{
			IJ.error( "Morphological Filter 3D", "ERROR: detected ImageJ version " + IJ.getVersion()  
					+ ".\nThis plugin requires version 1.48a or superior, please update ImageJ!" );
			return;
		}
		
		ImagePlus imagePlus = WindowManager.getCurrentImage();
		if (imagePlus == null) 
		{
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Morphological Filter 3D");
		
		gd.addChoice("Operation", Operation.getAllLabels(), 
				Operation.DILATION.toString());
		gd.addChoice("Element Shape", Strel3D.Shape.getAllLabels(), 
				Strel3D.Shape.CUBE.toString());
		gd.addNumericField("X-Radius (in pixels)", 2, 0);
		gd.addNumericField("Y-Radius (in pixels)", 2, 0);
		gd.addNumericField("Z-Radius (in pixels)", 2, 0);
		gd.addCheckbox("Show Element", false);
		
		// Could also add an option for the type of operation
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		long t0 = System.currentTimeMillis();

		// extract chosen parameters
		Operation op = Operation.fromLabel(gd.getNextChoice());
		Strel3D.Shape strelShape = Strel3D.Shape.fromLabel(gd.getNextChoice());
		int radiusX = (int) gd.getNextNumber();		
		int radiusY = (int) gd.getNextNumber();		
		int radiusZ = (int) gd.getNextNumber();		
		boolean showStrel = gd.getNextBoolean();
		
		// Create structuring element of the given size
		Strel3D strel = strelShape.fromRadiusList(radiusX, radiusY, radiusZ);
		strel.showProgress(true);
		DefaultAlgoListener.monitor(strel);
		
		// Eventually display the structuring element used for processing 
		if (showStrel)
		{
			showStrelImage(strel);
		}
		
		// Execute core of the plugin
		ImagePlus resPlus = process(imagePlus, op, strel);

		if (resPlus == null)
			return;

		// Display the result image
		resPlus.show();
		resPlus.setSlice(imagePlus.getSlice());

		// Display elapsed time
		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, imagePlus);
	}


	/**
	 * Displays the current structuring element in a new ImagePlus. 
	 * @param strel the 3D structuring element to display
	 */
	private void showStrelImage(Strel3D strel) 
	{
		// Size of the strel image (little bit larger than strel)
		int[] dim = strel.getSize();
		int sizeX = dim[0] + 10; 
		int sizeY = dim[1] + 10;
		int sizeZ = dim[2] + 10;
		
		// Creates strel image by dilating a point
		ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		stack.setVoxel(sizeX / 2, sizeY / 2, sizeZ / 2, 255);
		stack = Morphology.dilation(stack, strel);
		
		// Display strel image
		ImagePlus strelImage = new ImagePlus("Structuring Element", stack);
		strelImage.setSlice(((sizeZ - 1) / 2) + 1);
		strelImage.show();
	}

	public ImagePlus process(ImagePlus image, Operation op, Strel3D strel) 
	{
		// Check validity of parameters
		if (image == null)
			return null;
		
		// extract the input stack
		ImageStack inputStack = image.getStack();

		// apply morphological operation
		ImageStack resultStack = op.apply(inputStack, strel);

		// create the new image plus from the processor
		String newName = image.getShortTitle() + "-" + op.toString();
		ImagePlus resultPlus = new ImagePlus(newName, resultStack);
		resultPlus.copyScale(image);
		
		// return the created array
		return resultPlus;
	}
}
