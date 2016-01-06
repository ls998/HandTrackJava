package handtrack1;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

public interface IHandContourFinder {
	public void findHandContour(Mat image, MatOfPoint handContour);
}
