/**
 * 
 */
package apu.scratch.hax;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.ContentType;

/**
 * @see http://stackoverflow.com/a/8475006/1021196
 */
public class ProgressFileEntity extends org.apache.http.entity.FileEntity {

	public static interface ProgressCallback {
		public void updateProgress(int progress);
	}

	class OutputStreamProgress extends OutputStream {
		private final OutputStream outstream;
		private volatile long bytesWritten = 0;
		private ProgressCallback callback;
		private ProgressFileEntity entity;

		public OutputStreamProgress(OutputStream outstream,
				ProgressCallback callback, ProgressFileEntity entity) {
			this.outstream = outstream;
			this.callback = callback;
			this.entity = entity;
		}

		@Override
		public void write(int b) throws IOException {
			outstream.write(b);
			bytesWritten++;
			updateProgress();
		}

		@Override
		public void write(byte[] b) throws IOException {
			outstream.write(b);
			bytesWritten += b.length;
			updateProgress();
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			outstream.write(b, off, len);
			bytesWritten += len;
			updateProgress();
		}

		@Override
		public void flush() throws IOException {
			outstream.flush();
		}

		@Override
		public void close() throws IOException {
			outstream.close();
		}

		public long getWrittenLength() {
			return bytesWritten;
		}

		private void updateProgress() {
			callback.updateProgress((int) (100 * bytesWritten / entity
					.getContentLength()));
		}
	}

	private OutputStreamProgress outstream;
	private ProgressCallback callback;

	public ProgressFileEntity(File file, String contentType,
			ProgressCallback callback) {
		super(file, ContentType.create(contentType));
		this.callback = callback;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		this.outstream = new OutputStreamProgress(outstream, callback, this);
		super.writeTo(this.outstream);
	}
}
