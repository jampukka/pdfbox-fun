package pdftilepattern;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.util.Matrix;

/**
 * @author janne
 */
public class TilePatternUtil {

    public static void setColorSpace(PDPageContentStream stream, COSName colorspace) throws IOException {
        String colorspaceOp = String.format("/%s cs%n", colorspace.getName());
        stream.appendRawCommands(colorspaceOp);
    }


    public static void setNonStrokingColorPatternUncolored(PDPageContentStream stream, COSName pattern) throws IOException {
        String strokingColorOp = String.format("/%s scn%n", pattern.getName());
        stream.appendRawCommands(strokingColorOp);
    }


    public static void setNonStrokingColorPatternColored(PDPageContentStream stream, COSName pattern, float[] components) throws IOException {
        for (int i = 0; i < components.length; i++) {
            stream.appendRawCommands(components[i]);
            stream.appendRawCommands(' ');
        }
        setNonStrokingColorPatternUncolored(stream, pattern);
    }


    /**
     * The tiling pattern should really be a PDF Dictionary with a stream
     * org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern isn't really that...
     */
    public static COSStream createPattern(int patternType, int paintType, int tilingType, int[] bbox, int xStep, int yStep, String stream, Matrix transform) throws IOException {
        COSStream pattern = new COSStream();
        pattern.setItem(COSName.TYPE, COSName.PATTERN);
        pattern.setInt(COSName.PATTERN_TYPE, patternType);
        pattern.setInt(COSName.PAINT_TYPE, paintType);
        pattern.setInt(COSName.TILING_TYPE, tilingType);
        COSArray bboxArr = new COSArray();
        for (int i = 0; i < bbox.length; i++) {
            bboxArr.add(COSInteger.get(bbox[i]));
        }
        pattern.setItem(COSName.BBOX, bboxArr);
        pattern.setItem(COSName.X_STEP, COSInteger.get(xStep));
        pattern.setItem(COSName.Y_STEP, COSInteger.get(yStep));
        pattern.setItem(COSName.RESOURCES, new PDResources());

        if (transform != null) {
            pattern.setItem(COSName.MATRIX, transform.toCOSArray());
        }

        OutputStream out = pattern.createOutputStream();
        out.write(stream.getBytes(StandardCharsets.US_ASCII));
        out.close();

        return pattern;
    }


    public static COSName addColorSpace(PDResources resources, COSArray colorspace) {
        COSDictionary dict = resources.getCOSObject();

        COSDictionary colorspaces;

        COSBase colorspacesBase = dict.getItem(COSName.COLORSPACE);
        if (colorspacesBase != null && colorspacesBase instanceof COSDictionary) { 
            colorspaces = (COSDictionary) colorspacesBase;
        } else {
            colorspaces = new COSDictionary();
        }

        COSName key = findNextKey(colorspaces, "cs");
        colorspaces.setItem(key, colorspace);

        dict.setItem(COSName.COLORSPACE, colorspaces);

        return key;
    }


    public static COSName addPattern(PDResources resources, COSStream pattern) {
        COSDictionary resourcesDictionary = resources.getCOSObject();
        COSDictionary patterns = 
                getDictionary(resourcesDictionary, COSName.PATTERN);

        COSName key = findNextKey(patterns, "p");
        patterns.setItem(key, pattern);

        resourcesDictionary.setItem(COSName.PATTERN, patterns);

        return key;
    }


    private static COSDictionary getDictionary(COSDictionary parent, COSName key) {
        COSDictionary dict;

        COSBase dictBase = parent.getItem(key);
        if (dictBase != null && dictBase instanceof COSDictionary) { 
            dict = (COSDictionary) dictBase;
        } else {
            dict = new COSDictionary();
        }

        return dict;
    }


    private static COSName findNextKey(COSDictionary dict, String prefix) {
        COSName key;
        do {
            int i = 1;
            key = COSName.getPDFName(prefix + i);
        } while (dict.containsKey(key));
        return key;
    }

}
