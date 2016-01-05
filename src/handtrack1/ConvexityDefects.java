package handtrack1;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class ConvexityDefects {
	public class FoldInfo {
		public Point foldPt;
		public Point startPt;
		public Point endPt;
		public double depth;

		public FoldInfo(Point foldPt, Point startPt, Point endPt, double depth) {
			this.foldPt = foldPt;
			this.startPt = startPt;
			this.endPt = endPt;
			this.depth = depth;
		}
	}

	private static int max_points;

	public List<FoldInfo> findDefects(MatOfPoint contour) {
		List<FoldInfo> folds = new ArrayList<ConvexityDefects.FoldInfo>();

		MatOfInt hull = new MatOfInt();

		Imgproc.convexHull(contour, hull, false);

		MatOfInt4 defects = new MatOfInt4();
		Imgproc.convexityDefects(contour, hull, defects);

		long numPoints = defects.total();
		if (numPoints > max_points) {
			System.out.println("Processing " + max_points + " defect pts");
			numPoints = max_points;
		}

		for (int i = 0; i < numPoints; i++) {
			double[] dat = defects.get(i, 0);

			double[] startdat = contour.get((int) dat[0], 0);

			Point startPt = new Point(startdat[0], startdat[1]);

			double[] enddat = contour.get((int) dat[1], 0);
			Point endPt = new Point(enddat[0], enddat[1]);

			double[] folddat = contour.get((int) dat[2], 0);
			Point foldPt = new Point(folddat[0], folddat[1]);

			double depth = dat[3];

			folds.add(new FoldInfo(foldPt, startPt, endPt, depth));
		}

		return folds;
	}
}
