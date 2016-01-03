package handtrack1;

import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class ConvexityDefects {
	public class FoldInfo {
		public Point foldPt;
		public Point startPt;
		public Point endPt;

		public FoldInfo(Point foldPt, Point startPt, Point endPt) {
			this.foldPt = foldPt;
			this.startPt = startPt;
			this.endPt = endPt;
		}
	}

	public List<FoldInfo> findDefects(MatOfPoint contour) {

	}
}
