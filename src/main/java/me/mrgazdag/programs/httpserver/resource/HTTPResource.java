package me.mrgazdag.programs.httpserver.resource;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link HTTPResource} is any resource that can
 * be reached from the internet. An implementation
 * should handle writing the data, and supply an
 * appropriate MIME type. Unknown objects should use
 * {@link MIMEType#APPLICATION_OCTET_STREAM application/octet-stream}.
 * @author Andris
 *
 */
public interface HTTPResource {
	/**
	 * The MIME type used. Implementations can use a 
	 * common {@link MIMEType} using {@link MIMEType#getFullString()}.
	 * @return the content MIME type
	 */
	public String getMimeType();
	/**
	 * Implementations of this method should write the whole
	 * content of the specified resource. Caching is allowed.
	 * @param stream the stream to write into.
	 * @throws IOException if any exceptions occur during writing
	 */
	public void write(OutputStream stream) throws IOException;
	/**
	 * Implementations should return the length of the resource
	 * in the way applicable (bytes, characters)
	 * @return the content length
	 */
	public long length();
}
