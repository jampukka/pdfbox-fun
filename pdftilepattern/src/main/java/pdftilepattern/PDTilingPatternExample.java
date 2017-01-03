package pdftilepattern;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;

/**
 * Starting with PDFBox version 2.0.4 PDTilingPattern is actually usable
 * @author janne
 */
public class PDTilingPatternExample {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;

    private static final int PATTERN_WIDTH = 600;
    private static final int PATTERN_HEIGHT = 600;
    private static final int X_STEP = 600;
    private static final int Y_STEP = 600;
    private static final double PATTERN_SX = 0.125;
    private static final double PATTERN_SY = 0.125;

    private static final String OUTPUT = "pdtilingpattern_example.pdf";


    public static void main(String[] args) throws Exception {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(WIDTH, HEIGHT));
        doc.addPage(page);

        PDResources resources = new PDResources();
        page.setResources(resources);

        PDTilingPattern pattern = getTilingPattern();
        PDPattern colorPattern = new PDPattern(new PDResources(), PDDeviceRGB.INSTANCE);

        COSName tilePattern = resources.add(pattern);
        resources.add(colorPattern);

        float[] black = new float[] { 0f, 0f, 0f };
        PDColor color = new PDColor(black, tilePattern, colorPattern);

        // Don't compress the content stream
        PDPageContentStream stream = new PDPageContentStream(doc, page, AppendMode.APPEND, false);
        // Set fill color to pattern
        stream.setNonStrokingColor(color);
        
        // Rectangle
        stream.addRect(50, 50, 400, 400);
        stream.fill();

        // Triangle
        stream.moveTo( 50, 750);
        stream.lineTo(250, 550);
        stream.lineTo(450, 750);
        stream.lineTo(250, 950);
        stream.fill();

        // Diamond
        stream.moveTo(550, 550);
        stream.lineTo(950, 550);
        stream.lineTo(750, 950);
        stream.fill();
        stream.close();

        File file = new File(OUTPUT);
        doc.save(file);
        doc.close();
    }


    private static PDTilingPattern getTilingPattern() throws IOException {
        // Lots of Triangles 
        String patternString = ""
                + " 52 508 m  92 400 l 160 472 l f\r\n"
                + "  8 352 m  24 240 l 100 320 l f\r\n"
                + " 44 184 m 124 104 l 160 196 l f\r\n"
                + "236 396 m 292 308 l 348 384 l f\r\n"
                + "200 488 m 312 524 l 240 592 l f\r\n"
                + "280 172 m 272 60  l 360 100 l f\r\n"
                + "444 528 m 500 448 l 556 544 l f\r\n"
                + "436 308 m 388 220 l 504 220 l f\r\n"
                + "488 84  m 560 8   l 592 92  l f\r\n";

        PDTilingPattern pattern = new PDTilingPattern();
        pattern.setTilingType(PDTilingPattern.TILING_CONSTANT_SPACING);
        pattern.setPaintType(PDTilingPattern.PAINT_UNCOLORED);
        pattern.setBBox(new PDRectangle(PATTERN_WIDTH, PATTERN_HEIGHT));
        pattern.setXStep(X_STEP);
        pattern.setYStep(Y_STEP);
        pattern.setMatrix(AffineTransform.getScaleInstance(PATTERN_SX, PATTERN_SY));

        // Don't compress the content stream
        try (OutputStream out = pattern.getContentStream().createOutputStream()) {
            out.write(patternString.getBytes(StandardCharsets.US_ASCII));
        }

        return pattern;
    }

}
