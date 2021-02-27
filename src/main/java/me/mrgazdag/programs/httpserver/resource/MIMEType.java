package me.mrgazdag.programs.httpserver.resource;

/**
 * List of common MIME types.
 * Note that this list is not exhaustive.
 * @author Andris
 *
 */
@SuppressWarnings("unused")
public enum MIMEType {
	APPLICATION_JSON("application", "json"),
	APPLICATION_OCTET_STREAM("application","octet-stream"),
	APPLICATION_OGG("application", "ogg"),
	APPLICATION_PDF("application", "pdf"),
	APPLICATION_PKCS8("application", "pkcs8"),
	APPLICATION_X_RAR_COMPRESSED("application", "x-rar-compressed"),
	APPLICATION_ZIP("application", "zip"),

	AUDIO_WAVE("audio", "wave"),
	AUDIO_WEBM("audio", "webm"),
	AUDIO_OGG("audio", "ogg"),
	AUDIO_MPEG("audio", "mpeg"),
	AUDIO_VORBIS("audio", "vorbis"),

	FONT_OTF("font","ttf"),
	FONT_TTF("font","ttf"),
	FONT_WOFF("font","woff"),
	
	IMAGE_APNG("image","apng"),
	IMAGE_BMP("image","bmp"),
	IMAGE_GIF("image","gif"),
	IMAGE_ICO("image","ico"),
	IMAGE_JPEG("image","jpeg"),
	IMAGE_PNG("image","png"),
	IMAGE_SVG_XML("image","svg+xml"),
	IMAGE_TIFF("image","tiff"),
	IMAGE_WEBP("image","webp"),
	
	MODEL_3MF("model","3mf"),
	MODEL_VML("model","vml"),
	
	TEXT_CSS("text","css"),
	TEXT_CSV("text","csv"),
	TEXT_HTML("text","html"),
	TEXT_JAVASCRIPT("text","javascript"),
	TEXT_PLAIN("text","plain"),
	
	VIDEO_MP4("video","mp4"),
	VIDEO_OGG("video", "ogg"),
	VIDEO_WEBM("video", "webm"),
	
	MULTIPART_FORM_DATA("multipart", "form_data"),
	MULTIPART_BYTERANGES("multipart", "byteranges"),
	
	;
	private final String type;
	private final String subtype;
	private final String parameter;
	private final String full;
	MIMEType(String type, String subtype) {
		this(type,subtype,null);
	}
	MIMEType(String type, String subtype, String parameter) {
		this.type = type;
		this.subtype = subtype;
		this.parameter = parameter;
		this.full = type + "/" + subtype + (parameter != null ? ";" + parameter : "");
	}
	public String getType() {
		return type;
	}
	public String getSubtype() {
		return subtype;
	}
	public String getFullString() {
		return full;
	}
	@Override
	public String toString() {
		return full;
	}
	public String getParameter() {
		return parameter;
	}
}
