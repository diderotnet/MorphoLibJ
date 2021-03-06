/**
 * <p>A collection of Structuring Element ("Strel") implementations for mathematical morphology.</p> 
 * 
 * Contains the implementation of several types of structuring elements. 
 * The packages tries to takes advantage of the separability property of most 
 * structuring elements.<p>
 * 
 * The package can be divided into:
 * <ul>
 * <li>Specialization Strel interfaces: {@link inra.ijpb.morphology.strel.SeparableStrel}, 
 * 	{@link inra.ijpb.morphology.strel.InPlaceStrel}</li>
 * <li>Abstract classes for facilitating implementations: {@link inra.ijpb.morphology.strel.AbstractStrel}, 
 * 	{@link inra.ijpb.morphology.strel.AbstractSeparableStrel}, 
 * 	{@link inra.ijpb.morphology.strel.AbstractInPlaceStrel}</li>
 * <li>Final Strel implementations: {@link inra.ijpb.morphology.strel.SquareStrel}, 
 * 	{@link inra.ijpb.morphology.strel.OctagonStrel}, {@link inra.ijpb.morphology.strel.DiamondStrel}, 
 * 	{@link inra.ijpb.morphology.strel.Cross3x3Strel}...</li>
 * <li>Utility classes that manage local extremum: {@link inra.ijpb.morphology.strel.LocalExtremum}, 
 * 	{@link inra.ijpb.morphology.strel.LocalExtremumBufferGray8},
 * {@link inra.ijpb.morphology.strel.LocalExtremumBufferDouble}</li> 
 * </ul>
 */
package inra.ijpb.morphology.strel;


