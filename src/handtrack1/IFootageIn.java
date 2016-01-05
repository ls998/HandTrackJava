package handtrack1;

import org.opencv.core.Mat;

public interface IFootageIn {
	public static class FootageEndedException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8653263521552031355L;

		public FootageEndedException() {
			super("Input footage has ended");
		}
	}

	public void getFrame(Mat frame) throws FootageEndedException;

	public void open();

	public void close();
}
