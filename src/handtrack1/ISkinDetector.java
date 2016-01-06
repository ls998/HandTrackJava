package handtrack1;

import org.opencv.core.Mat;

public interface ISkinDetector {
	public void filterSkin(Mat image, Mat binaryImage);
}
