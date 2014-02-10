/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.IJ;
import ij.ImageStack;
import inra.ijpb.morphology.Strel;

/**
 * An horizontal linear structuring element of a given length.
 * Provides methods for fast in place erosion and dilation.
 * 
 * @see LinearVerticalStrel
 * @see LinearDiagUpStrel
 * @see LinearDiagDownStrel
 * @author David Legland
 *
 */
public class LinearDepthStrel3D extends AbstractInPlaceStrel3D  {

	// ==================================================
	// Static methods 
	
	public final static LinearDepthStrel3D fromDiameter(int diam) {
		return new LinearDepthStrel3D(diam);
	}
	
	public final static LinearDepthStrel3D fromRadius(int radius) {
		return new LinearDepthStrel3D(2 * radius + 1, radius);
	}
	
	// ==================================================
	// Class variables
	
	/**
	 * Number of element in this structuring element. 
	 * Corresponds to the size in z direction.
	 */
	int length;
	
	/**
	 * Position of the origin within the segment.
	 * Corresponds to the number of elements before the reference element.
	 */
	int offset;
	
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new horizontal linear structuring element of a given size.
	 * @param size the number of pixels in this structuring element
	 */
	public LinearDepthStrel3D(int size) {
		if (size < 1) {
			throw new RuntimeException("Requires a positive size");
		}
		this.length = size;
		
		this.offset = (int) Math.floor((this.length - 1) / 2);
	}
	
	/**
	 * Creates a new horizontal linear structuring element of a given size and
	 * with a given offset. 
	 * @param size the number of pixels in this structuring element
	 * @param offset the position of the reference pixel (between 0 and size-1)
	 */
	public LinearDepthStrel3D(int size, int offset) {
		if (size < 1) {
			throw new RuntimeException("Requires a positive size");
		}
		this.length = size;
		
		if (offset < 0) {
			throw new RuntimeException("Requires a non-negative offset");
		}
		if (offset >= size) {
			throw new RuntimeException("Offset can not be greater than size");
		}
		this.offset = offset;
	}
	
	
	// ==================================================
	// General methods 
	
	/* (non-Javadoc)
	 * @see ijt.morphology.InPlaceStrel#inPlaceDilation(ij.process.ImageStack)
	 */
	@Override
	public void inPlaceDilation(ImageStack stack) {
		// If size is one, there is no need to compute
		if (length <= 1) { 
			return;
		}
		
		// get image size
		int width 	= stack.getWidth(); 
		int height 	= stack.getHeight();
		int depth 	= stack.getSize();
			
		// shifts between reference position and last position
		int shift = this.length - this.offset - 1;
		
		// local histogram
		LocalBufferMax localMax = new LocalBufferMax(length);
		
		// Iterate on image z-columns
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (this.showProgress()) {
					IJ.showProgress(y, height);
				}

				// init local histogram with background values
				localMax.fill(Strel.BACKGROUND);

				// add neighbor values
				for (int z = 0; z < Math.min(shift, depth); z++) {
					localMax.add((int) stack.getVoxel(x, y, z));
				}

				// iterate along "middle" values
				for (int z = 0; z < depth - shift; z++) {
					localMax.add((int) stack.getVoxel(x, y, z + shift));
					stack.setVoxel(x, y, z, localMax.getMax());
				}

				// process pixels at the end of the line
				for (int z = Math.max(0, depth - shift); z < depth; z++) {
					localMax.add(Strel.BACKGROUND);
					stack.setVoxel(x, y, z, localMax.getMax());
				}
			}
		}

		// clear the progress bar
		if (this.showProgress()) {
			IJ.showProgress(1);
		}
		
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.InPlaceStrel#inPlaceErosion(ij.process.ImageStack)
	 */
	@Override
	public void inPlaceErosion(ImageStack stack) {
		// If size is one, there is no need to compute
		if (length <= 1) { 
			return;
		}
		
		// get image size
		int width 	= stack.getWidth(); 
		int height 	= stack.getHeight();
		int depth 	= stack.getSize();
		
		// shifts between reference position and last position
		int shift = this.length - this.offset - 1;
		
		// local histogram
		LocalBufferMin localMin = new LocalBufferMin(length);
		
		// Iterate on image z-columns
		for (int y = 0; y < height; y++) {
			if (this.showProgress()) {
				IJ.showProgress(y, height);
			}
			for (int x = 0; x < width; x++) {

				// init local histogram with background values
				localMin.fill(Strel.FOREGROUND);

				// add neighbor values
				for (int z = 0; z < Math.min(shift, depth); z++) {
					localMin.add((int) stack.getVoxel(x, y, z));
				}

				// iterate along "middle" values
				for (int z = 0; z < depth - shift; z++) {
					localMin.add((int) stack.getVoxel(x, y, z + shift));
					stack.setVoxel(x, y, z, localMin.getMin());
				}

				// process pixels at the end of the line
				for (int z = Math.max(0, depth - shift); z < depth; z++) {
					localMin.add(Strel.FOREGROUND);
					stack.setVoxel(x, y, z, localMin.getMin());
				}
			}
		}
		
		// clear the progress bar
		if (this.showProgress()) {
			IJ.showProgress(1);
		}
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getMask()
	 */
	@Override
	public int[][][] getMask3D() {
		int[][][] mask = new int[this.length][1][1];
		for (int i = 0; i < this.length; i++) {
			mask[i][0][0] = 255;
		}
		
		return mask;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getOffset()
	 */
	@Override
	public int[] getOffset() {
		return new int[]{0, 0, this.offset};
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getShifts()
	 */
	@Override
	public int[][] getShifts3D() {
		int[][] shifts = new int[this.length][3];
		for (int i = 0; i < this.length; i++) {
			shifts[i][0] = 0;
			shifts[i][1] = 0;
			shifts[i][2] = i - this.offset;
		}
		return shifts;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize() {
		return new int[]{1, 1, this.length};
	}

	/**
	 * Returns a linear horizontal line with same size and offset equal to size-offset-1.
	 * @see inra.ijpb.morphology.Strel#reverse()
	 */
	@Override
	public LinearDepthStrel3D reverse() {
		return new LinearDepthStrel3D(this.length, this.length - this.offset - 1);
	}

}
