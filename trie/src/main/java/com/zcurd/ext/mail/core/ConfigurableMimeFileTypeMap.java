package com.zcurd.ext.mail.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

public class ConfigurableMimeFileTypeMap extends FileTypeMap {
	private String[] mappings;

	private FileTypeMap fileTypeMap;

	public void setMappings(String[] mappings) {
		this.mappings = mappings;
	}

	public void afterPropertiesSet() {
		getFileTypeMap();
	}

	protected final FileTypeMap getFileTypeMap() {
		if (this.fileTypeMap == null) {
			String name = "mime.types";
			try {
				this.fileTypeMap = createFileTypeMap(getClass().getResourceAsStream(name), this.mappings);
			} catch (IOException ex) {
				throw new IllegalStateException("Could not load specified MIME type mapping file: " + name, ex);
			}
		}
		return this.fileTypeMap;
	}

	protected FileTypeMap createFileTypeMap(InputStream is, String[] mappings) throws IOException {
		MimetypesFileTypeMap fileTypeMap = null;
		if (is != null) {
			try {
				fileTypeMap = new MimetypesFileTypeMap(is);
			} finally {
				is.close();
			}
		} else {
			fileTypeMap = new MimetypesFileTypeMap();
		}
		if (mappings != null) {
			byte b;
			int i;
			String[] arrayOfString;
			for (i = (arrayOfString = mappings).length, b = 0; b < i;) {
				String mapping = arrayOfString[b];
				fileTypeMap.addMimeTypes(mapping);
				b++;
			}
		}
		return fileTypeMap;
	}

	public String getContentType(File file) {
		return getFileTypeMap().getContentType(file);
	}

	public String getContentType(String fileName) {
		return getFileTypeMap().getContentType(fileName);
	}
}
