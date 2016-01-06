package handtrack1;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import handtrack1.caching.MappedCacheManager;
import handtrack1.resources.IConsumer;
import handtrack1.resources.ResourceManager;
import handtrack1.settings.IConfigurable;

public class BiggestContour implements IHandContourFinder, IConsumer, IConfigurable {

	private MappedCacheManager<List<MatOfPoint>> listCacheManager;
	private MappedCacheManager<Mat> matCacheManager;
	private double smallest_area;

	@Override
	public void findHandContour(Mat image, MatOfPoint handContour) {
		// get all contours
		List<MatOfPoint> contours = listCacheManager.getObject("contour list");
		Imgproc.findContours(image, contours, matCacheManager.getObject("filler mat"), Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);

		// find biggest
		double maxArea = smallest_area;

		for (MatOfPoint contour : contours) {
			// makes sure isn't empty contour
			if (contour.elemSize() > 0) {
				// calcuate size of contour
				MatOfPoint2f thing = new MatOfPoint2f(contour.toArray());
				RotatedRect box = Imgproc.minAreaRect(thing);
				if (box != null) {
					Size size = box.size;
					double area = size.width * size.height;
					if (area > maxArea) {
						maxArea = area;
						handContour = contour;
					}
				}
			}
		}
	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		listCacheManager = resourceManager.getResource("MappedCacheManager<List<MatOfPoint>>");
		listCacheManager.setReference("contour list", new ArrayList<>());
		matCacheManager.setReference("filler mat", new Mat());
	}

	@Override
	public String getSettingsString() {
		return "" + smallest_area;
	}

	@Override
	public void loadSettings(String settings) {
		smallest_area = Double.parseDouble(settings);
	}

}
