package handtrack1;

import org.opencv.core.Mat;

public interface ISkinDetector {
	public Mat filterSkin(Mat image);
}
