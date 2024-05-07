package be.quodlibet.boxable.utils;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import be.quodlibet.boxable.Paragraph;

public class PageContentStreamOptimized {
	private static final Matrix ROTATION = Matrix.getRotateInstance(Math.PI * 0.5, 0, 0);

	private final PDPageContentStream pageContentStream;
	private boolean textMode;
	private float textCursorAbsoluteX;
	private float textCursorAbsoluteY;
	private boolean rotated;

	public PageContentStreamOptimized(PDPageContentStream pageContentStream) {
		this.pageContentStream = pageContentStream;
	}

	public void setRotated(boolean rotated) throws IOException {
		if (this.rotated == rotated)
			return;
		if (rotated) {
			if (textMode) {
				pageContentStream.setTextMatrix(ROTATION);
				textCursorAbsoluteX = 0;
				textCursorAbsoluteY = 0;
			}
		} else {
			endText();
		}
		this.rotated = rotated;
	}

	public void beginText() throws IOException {
		if (!textMode) {
			pageContentStream.beginText();
			if (rotated) {
				pageContentStream.setTextMatrix(ROTATION);
			}
			textMode = true;
			textCursorAbsoluteX = 0;
			textCursorAbsoluteY = 0;
		}
	}

	public void endText() throws IOException {
		if (textMode) {
			pageContentStream.endText();
			textMode = false;
		}
	}

	private PDFont[] currentFonts;
	private float currentFontSize;

	public void setFonts(PDFont[] fonts, float fontSize) throws IOException {
		if (fonts != currentFonts || fontSize != currentFontSize) {

			currentFonts = fonts;
			currentFontSize = fontSize;
		}
	}

	public void showText(String text) throws IOException {
		beginText();
		PDFont currentFont = null;
		int start = 0;
		int end = 0;
		for (int i = 0; i < text.length(); i++) {
			PDFont cFont = null;
			int codePoint = text.codePointAt(i);
			if (codePoint > Paragraph.LAST_BMP) {
				cFont = currentFonts.length > 1 ? currentFonts[1] : currentFonts[0];
				i++;
			} else {
				cFont = currentFonts[0];
			}
			if (currentFont != null && currentFont.getName().equals(cFont.getName())) {
				// do nothing
				end = i;
			} else {
				if (currentFont != null) {
					String s = text.substring(start, end + 1);
					start = end + 1;
					this.showText(currentFont, s);
				}
				end = i;
				currentFont = cFont;
			}

		}
		if (start < text.length()) {
			String s = text.substring(start, text.length());
			this.showText(currentFont, s);
		}
	}

	private void showText(PDFont font, String text) throws IOException {
//		beginText();
		pageContentStream.setFont(font, currentFontSize);
		pageContentStream.showText(text);
	}

	public void newLineAt(float tx, float ty) throws IOException {
		beginText();
		float dx = tx - textCursorAbsoluteX;
		float dy = ty - textCursorAbsoluteY;
		if (rotated) {
			pageContentStream.newLineAtOffset(dy, -dx);
		} else {
			pageContentStream.newLineAtOffset(dx, dy);
		}
		textCursorAbsoluteX = tx;
		textCursorAbsoluteY = ty;
	}

	public void drawImage(PDImageXObject image, float x, float y, float width, float height) throws IOException {
		endText();
		pageContentStream.drawImage(image, x, y, width, height);
	}

	private Color currentStrokingColor;

	public void setStrokingColor(Color color) throws IOException {
		if (color != currentStrokingColor) {
			pageContentStream.setStrokingColor(color);
			currentStrokingColor = color;
		}
	}

	private Color currentNonStrokingColor;

	public void setNonStrokingColor(Color color) throws IOException {
		if (color != currentNonStrokingColor) {
			pageContentStream.setNonStrokingColor(color);
			currentNonStrokingColor = color;
		}
	}

	public void addRect(float x, float y, float width, float height) throws IOException {
		endText();
		pageContentStream.addRect(x, y, width, height);
	}

	public void moveTo(float x, float y) throws IOException {
		endText();
		pageContentStream.moveTo(x, y);
	}

	public void lineTo(float x, float y) throws IOException {
		endText();
		pageContentStream.lineTo(x, y);
	}

	public void stroke() throws IOException {
		endText();
		pageContentStream.stroke();
	}

	public void fill() throws IOException {
		endText();
		pageContentStream.fill();
	}

	private float currentLineWidth = -1;

	public void setLineWidth(float lineWidth) throws IOException {
		if (lineWidth != currentLineWidth) {
			endText();
			pageContentStream.setLineWidth(lineWidth);
			currentLineWidth = lineWidth;
		}
	}

	private int currentLineCapStyle = -1;

	public void setLineCapStyle(int lineCapStyle) throws IOException {
		if (lineCapStyle != currentLineCapStyle) {
			endText();
			pageContentStream.setLineCapStyle(lineCapStyle);
			currentLineCapStyle = lineCapStyle;
		}
	}

	private float[] currentLineDashPattern;
	private float currentLineDashPhase;

	public void setLineDashPattern(float[] pattern, float phase) throws IOException {
		if ((pattern != currentLineDashPattern && !Arrays.equals(pattern, currentLineDashPattern)) || phase != currentLineDashPhase) {
			endText();
			pageContentStream.setLineDashPattern(pattern, phase);
			currentLineDashPattern = pattern;
			currentLineDashPhase = phase;
		}
	}

	public void close() throws IOException {
		endText();
		pageContentStream.close();
	}
}
