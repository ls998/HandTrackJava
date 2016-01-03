package handtrack1;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

public interface IHandContourFinder {
	public MatOfPoint findHandContour(Mat image);
}
