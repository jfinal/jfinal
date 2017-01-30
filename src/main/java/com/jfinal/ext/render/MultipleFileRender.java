package com.jfinal.ext.render;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;
import com.jfinal.render.RenderManager;

/**
 * 多文件压缩后Render 具体使用请参考各构造函数</br>
 * .
 *
 * @author lizhao@gmail.com
 *
 */
public class MultipleFileRender extends Render {

	static class FileEntry {
		private final String absolutePath;
		private final String showName;

		public FileEntry(final String showName, final String absolutePath) {
			this.showName = showName;
			this.absolutePath = absolutePath;
		}

		public String getAbsolutePath() {
			return this.absolutePath;
		}

		public String getShowName() {
			return this.showName;
		}
	}

	public static class MultipleFileRenderBuilder {
		private final List<FileEntry> files;

		private String zipFileName;

		private MultipleFileRenderBuilder() {
			this.files = new ArrayList<MultipleFileRender.FileEntry>();
		}

		public MultipleFileRenderBuilder add(final String showName,final String absolutePath){
			this.files.add(new FileEntry(showName, absolutePath));
			return this;
		}

		public MultipleFileRender build() {
			if(this.files.isEmpty()){
				LogKit.warn("没有要压缩的文件，请设置需要压缩的文件");
			}
			if(StrKit.isBlank(this.zipFileName)){
				this.zipFileName = new Date().getTime()+"";
			}
			return new MultipleFileRender(this.files,this.zipFileName);
		}

		public MultipleFileRenderBuilder setZipFileName(final String zipFileName){
			this.zipFileName = zipFileName;
			return this;
		}


	}

	public static MultipleFileRenderBuilder createBuilder(){
		return new MultipleFileRenderBuilder();
	}

	private final java.util.List<FileEntry> files;

	private final String              zipFileName;

	/**
	 * 通过传一系列的路径进行压缩后下载，压缩文件名称为时间戳+".zip"</br>
	 * 建议使用{@code MultipleFileRender.createBuilder().add(showName,absolutePath)....setZipFile(zipFileName).build()}来调用</br>
	 * .
	 *
	 * @param files
	 */
	public MultipleFileRender(final List<FileEntry> files) {
		this(files, new Date().getTime() + "");
	}

	/**
	 * 通过传一系列的路径进行压缩后下载，压缩文件名称为zipFileName+".zip"</br>
	 * 建议使用{@code MultipleFileRender.createBuilder().add(showName,absolutePath)....setZipFile(zipFileName).build()}来调用</br>
	 * .
	 *
	 * @param files
	 */
	public MultipleFileRender(final List<FileEntry> files, final String zipFileName) {
		this.zipFileName = zipFileName + ".zip";
		this.files = files;
	}

	/**
	 * 将一个key为显示名称，value为全路径的map作为压缩对象进行压缩，压缩后的文件名称为{@code new Date().getTime()}+".zip"</br>
	 * 使用此构造函数，请保证显示名称唯一。否则，请使用{@code MultipleFileRender.Builder().add(showName,absolutePath))}
	 *
	 * @param zipSources
	 */
	public MultipleFileRender(final Map<String, String> zipSources) {
		this(zipSources, new Date().getTime()+"");
	}


	/**
	 * 将一个key为显示名称，value为全路径的map作为压缩对象进行压缩，压缩后的文件名称为{@code zipFileName}+".zip"</br>
	 * 使用此构造函数，请保证显示名称唯一。否则，请使用{@code MultipleFileRender.Builder().add(showName,absolutePath))}
	 *
	 * @param zipSources
	 * @param zipFileName
	 */
	public MultipleFileRender(final Map<String, String> zipSources, final String zipFileName) {
		this.files = new ArrayList<MultipleFileRender.FileEntry>();
		for (final Entry<String, String> zipSource : zipSources.entrySet()) {
			this.files.add(new FileEntry(zipSource.getKey(), zipSource.getValue()));
		}
		this.zipFileName = zipFileName + ".zip";
	}

	/**
	 * 将文件夹{@code folderName}下面所有的文件压缩，最后的文件名称为{@code folderName}+".zip"
	 *
	 * @param folderName
	 */
	public MultipleFileRender(final String folderName){
		this(folderName, folderName);
	}

	/**
	 * 将文件夹{@code folderName}下面所有的文件压缩，最后的文件名称为{@code zipFileName}+".zip"
	 *
	 * @param folderName
	 * @param zipFileName
	 */
	public MultipleFileRender(final String folderName, final String zipFileName) {
		final File folderFile = new File(folderName);
		if (folderFile.isDirectory()) {
			this.files = new ArrayList<MultipleFileRender.FileEntry>();
			for (final File subFile : folderFile.listFiles()) {
				this.files.add(new FileEntry(subFile.getName(), subFile.getAbsolutePath()));
			}
		} else {
			RenderManager.me().getRenderFactory().getErrorRender(404).setContext(this.request, this.response).render();
			throw new RenderException("请检查" + folderName + "是否是文件夹");
		}
		this.zipFileName = zipFileName + ".zip";
	}

	protected String encodeFileName(final String fileName) {
		try {
			// return new String(fileName.getBytes("GBK"), "ISO8859-1");
			return new String(fileName.getBytes(getEncoding()), "ISO8859-1");
		} catch (final UnsupportedEncodingException e) {
			return fileName;
		}
	}
	@Override
	public void render() {
		this.response.setContentType("application/zip");
		this.response.setHeader("Content-Disposition",
				"attachment; filename=\"" + this.encodeFileName(this.zipFileName) + "\"");
		final byte[] buf = new byte[2048];
		OutputStream outputStream = null;
		ZipOutputStream zipOutputStream = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			outputStream = this.response.getOutputStream();
			zipOutputStream = new ZipOutputStream(outputStream);
			// Compress the files
			for (final FileEntry fileEntry : this.files) {
				fis = new FileInputStream(fileEntry.getAbsolutePath());
				bis = new BufferedInputStream(fis);
				// Add ZIP entry to output stream.
				zipOutputStream.putNextEntry(new ZipEntry(fileEntry.getShowName()));
				int bytesRead;
				while ((bytesRead = bis.read(buf)) != -1) {
					zipOutputStream.write(buf, 0, bytesRead);
				}
				bis.close();
				fis.close();
			}
			zipOutputStream.flush();
			zipOutputStream.close();
			outputStream.flush();
			this.response.flushBuffer();
			zipOutputStream = null;
			outputStream = null;
			bis = null;
			fis = null;
		} catch (final Exception e) {
			LogKit.error("文件渲染错误", e);
			RenderManager.me().getRenderFactory().getErrorRender(404).setContext(this.request, this.response).render();
		}
		finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			if (zipOutputStream != null) {
				try {
					zipOutputStream.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
