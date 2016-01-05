package handtrack1;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

public class OpenCVFootageIn implements IFootageIn {
	private VideoCapture capture;

	public OpenCVFootageIn() {
		capture = new VideoCapture(0);
	}

	@Override
	public void getFrame(Mat frame) throws FootageEndedException {
		if (!capture.isOpened())
			throw new FootageEndedException();
		capture.read(frame);
	}

	@Override
	public void open() {
		capture.open(0);
	}

	@Override
	public void close() {
		capture.release();
	}

}
