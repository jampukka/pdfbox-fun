package pdftilepattern;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.util.Matrix;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author janne
 */
public class TilePatternExample {

    private static final String OUTPUT = "tilepattern.pdf";

    public static void main(String[] args) throws Exception {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(100, 100));
        doc.addPage(page);

        PDResources resources = new PDResources();
        page.setResources(resources);

        COSName colorspaceResource = createCMYKColorspace(resources);
        COSName tilePattern = createTilePattern(resources);

        PDPageContentStream stream = new PDPageContentStream(doc, page);

        // Magenta background
        stream.setNonStrokingColor(0, 1.0f, 0, 0);
        stream.addRect(0, 0, 100, 100);
        stream.fill();

        // Yellow stroking
        stream.setStrokingColor(0, 0, 1.0f, 0);
        stream.setLineWidth(0.25f);

        TilePatternUtil.setColorSpace(stream, colorspaceResource);

        float[] cyan = new float[] { 1, 0, 0, 0 };
        TilePatternUtil.setNonStrokingColorPatternColored(stream, tilePattern, cyan);

        WKTReader wktReader = new WKTReader();

        // Simple polygon with a hole in the middle
        String polygonWKT = "POLYGON ((10 10, 90 10, 90 90, 10 90, 10 10), "
                + "(40 50, 50 60, 60 50, 50 40, 40 50))";

        Geometry geom = wktReader.read(polygonWKT);
        if (geom instanceof Polygon) {
            Polygon polygon = (Polygon) geom;

            addToPath(stream, polygon.getExteriorRing().getCoordinates());
            stream.closePath();

            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                addToPath(stream, polygon.getInteriorRingN(i).getCoordinates());
                stream.closePath();
            }

            // Fill and Stroke
            stream.appendRawCommands("b\n");
            // stream.fill();
        }

        stream.close();

        File file = new File(OUTPUT);
        doc.save(file);

        doc.close();
    }


    private static COSName createTilePattern(PDResources resources) throws IOException {
        int[] bbox = new int[] { 0, 0, 12, 12 };

        // Set LineCap to 2, stroke a line from (0,0) to (12,12)
        String pattern = "2 J 0 0 m 12 12 l S";

        // Repeat the pattern before it ends
        int xStep = 10;
        int yStep = 10;

        COSStream patternDict = TilePatternUtil.createPattern(
                PDTilingPattern.TYPE_TILING_PATTERN,
                PDTilingPattern.PAINT_UNCOLORED,
                PDTilingPattern.TILING_CONSTANT_SPACING,
                bbox, 
                xStep, 
                yStep, 
                pattern, 
                new Matrix());

        return TilePatternUtil.addPattern(resources, patternDict);
    }


    private static COSName createCMYKColorspace(PDResources resources) {
        COSArray colorspace = new COSArray();
        colorspace.add(COSName.PATTERN);
        colorspace.add(COSName.DEVICECMYK);
        return TilePatternUtil.addColorSpace(resources, colorspace);
    }


    private static void addToPath(PDPageContentStream stream, Coordinate[] coordinates) throws IOException {
        for (int i = 0; i < coordinates.length; i++) {
            float x = (float) coordinates[i].x;
            float y = (float) coordinates[i].y;
            if (i == 0) {
                stream.moveTo(x, y);
            } else {
                stream.lineTo(x, y);
            }
        }
    }

}
