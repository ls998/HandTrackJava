package handtrack1;

import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class HandInfo {
	public class Finger {
		public String name;
		public Point location;
	}

	public List<Finger> fingers;

	public Point center;
	public double angle;

	public List<ConvexityDefects.FoldInfo> folds;
	
	public MatOfPoint polyApproximation;
}
