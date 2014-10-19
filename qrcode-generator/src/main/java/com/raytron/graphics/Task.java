package com.raytron.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.raytron.barcode.BarcodeFactory;
import com.raytron.reader.FReader;

/**
 * A Swing worker implementation.
 * 
 * @author Kedar Raybagkar
 *
 */
class Task extends SwingWorker<Void, Void> {

    /**
     * Font
     */
    private static final Font R_I_S_FONT = new Font("Courier New", Font.PLAIN, 10);

    /**
     * Raytron Gradient Paint.
     */
    private static final GradientPaint R_I_S_GRADIENT_PAINT = new GradientPaint(5, 0, new Color(54, 172, 212), 0, 5, new Color(255, 255, 255), true);

    /**
     * Text to be wrapped around the actual image.
     */
    private static final String R_I_S = "RiS";
    
    /**
     * Cell Padding. 
     */
    private static final float CELL_PADDING = 4f;
    
    /**
     * Name of the Input file to be read from. 
     */
    private final String iFile;
    
    /**
     * Name of the output file. 
     */
    private final String oFile;
    
    /**
     * Barcode Size.
     */
    private final int barcodeSize;
    
    /**
     * Include header in the barcode.
     */
    private final boolean includeHeader;
    
    /**
     * UI handle for call backs.
     */
    private final RaytronBarcodeGeneratorUI raytronQRCodeGenerator;
    
    /**
     * Number of columns in table.
     */
    private int tableColumns;
    
    /**
     * Overlay RIS image. 
     */
    private final boolean overlayRISImage;
    
    /**
     * Image Writer
     */
    private final Writer writer;

    /**
     * Barcode format.
     */
    private final BarcodeFormat barcodeFormat;

    /**
     * Save sample image.
     */
    private final boolean saveSampleImage;

    /**
     * Default constructor.
     * @param parameter TODO
     */
    public Task(TaskParameter parameter) {
        this.raytronQRCodeGenerator = parameter.getRaytronQRCodeGenerator();
        this.iFile = parameter.getiFile();
        this.oFile = parameter.getoFile();
        this.barcodeSize = parameter.getBarcodeSize();
        this.includeHeader = parameter.isIncludeHeader();
        this.overlayRISImage = parameter.isOverlayRISImage();
        this.barcodeFormat = parameter.getFormat();
        this.saveSampleImage = parameter.isSaveSampleImage();
        writer = BarcodeFactory.getBarcodeInstance(barcodeFormat);
    }

    /*
     * Main task. Executed in background thread.
     */
    @Override
    public Void doInBackground() {
        // Initialize progress property.
        setProgress(0);
        Document document = new Document(PageSize.A4, 5f, 5f, 10f, 1f);
        FReader freader = null;
        try {
            // PdfWriter writer =
            PdfWriter.getInstance(document, new FileOutputStream(new File(oFile)));
            document.open();
            PdfPTable table = null;
            // setDefaultCellProperties(table.getDefaultCell());

            freader = new FReader(Paths.get(iFile), includeHeader);
            BufferedImage maxImage = getLargestImageFromFReader(freader);
            if (saveSampleImage) {
                saveImageToFile(maxImage);
            }
            table = createTableByCalculatingPageSizeAndColumns(document.getPageSize(), Image.getInstance(maxImage, null));
            setDefaultCellProperties(table.getDefaultCell());
            setProgress(freader.getPercentageRead());
            int columnPrinted = 0;
            while (freader.hasNext()) {
                String sLine = freader.next();
                setProgress(freader.getPercentageRead());
                if (!"".equals(sLine)) {
                    BitMatrix matrix = createBarcodeImage(sLine, barcodeSize);
                    if (matrix != null) {
                        BufferedImage image;
                        image = createImage(matrix);
                        com.lowagie.text.Image pic = Image.getInstance(image, null);
                        table.addCell(createPdfPCell(pic));
                        if (++columnPrinted >= tableColumns) {
                            columnPrinted = 0;
                        }
                    }
                }
            }
            if (columnPrinted < tableColumns) {
                addBlankCellsSoThatPageIsAdded(table, tableColumns);
            }
            setProgress(freader.getPercentageRead());
            document.add(table);
            // addHeaderFooter(document, writer);
            document.close();
            raytronQRCodeGenerator.showInformationPopup("File Genearted Successfully", "Status");
        } catch (Throwable e) {
            raytronQRCodeGenerator.showErrorPopup("Error while Generating file due to :" + e.getMessage(), "Status");
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
            if (freader != null) {
                try {
                    freader.close();
                } catch (IOException e) {
                }
            }
            raytronQRCodeGenerator.taskCompleted();
        }
        return null;
    }

    /**
     * Saves image to file.
     * @param image to be saved.
     */
    private void saveImageToFile(BufferedImage image) {
        try {
            ImageIO.write(image, "jpeg", new File(oFile + ".jpeg"));
        } catch (IOException e) {
        }
    }

    /**
     * Creates an image.
     * Method is responsible to call overlay RIS image based on the params received.
     * 
     * @param matrix through which the image to be rendered.
     * @return Image
     */
    private BufferedImage createImage(BitMatrix matrix) {
        if (overlayRISImage) {
            return getOverlayRISImage(MatrixToImageWriter.toBufferedImage(matrix));
        } else {
            return MatrixToImageWriter.toBufferedImage(matrix);
        }
    }

    /**
     * Adds blank cells to the page probably due to bug in iText.
     * @param table on which the columns to be added.
     * @param cols number of columns
     */
    private void addBlankCellsSoThatPageIsAdded(PdfPTable table, int cols) {
        for (int i = 0; i < cols; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(""));
            setDefaultCellProperties(cell);
            table.addCell(cell);
        }
    }

    /**
     * Creates and returns image for the maximum length of the data from the file reader.
     * 
     * @param reader from where the maximum length data is to be fetched.
     * @return PDF Table
     * @throws Throwable
     */
    private BufferedImage getLargestImageFromFReader(FReader reader) throws Throwable {
        BufferedImage image = null;
        String sLine = reader.getMaxLine();
        if (!"".equals(sLine)) {
            BitMatrix matrix = createBarcodeImage(sLine, barcodeSize);
            if (matrix != null) {
//                barcodeImage = Image.getInstance(image, null);
                image = createImage(matrix);
            }
        }
        return image;
    }

    /**
     * Returns a PDF Table after calculating number of columns that can be accomodated on the page.
     * @param pageSize rectangle
     * @param pic Image
     * @return PDF Table
     * @throws DocumentException
     */
    private PdfPTable createTableByCalculatingPageSizeAndColumns(Rectangle pageSize, com.lowagie.text.Image pic) throws DocumentException {
        float tableSize = pageSize.getWidth();
        tableColumns = (int) (tableSize / (pic.getWidth() + CELL_PADDING));
        float[] widths = new float[tableColumns];
        for (int i = 0; i < tableColumns; i++) {
            widths[i] = pic.getWidth();
        }
        PdfPTable table = new PdfPTable(tableColumns);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        return table;
    }

    /**
     * Overlays RIS image on the raw barcode image.
     * @param rawBarcodeImage
     * @return decorated image.
     */
    private BufferedImage getOverlayRISImage(BufferedImage rawBarcodeImage) {
        Graphics g = rawBarcodeImage.getGraphics();
        FontMetrics metrics = g.getFontMetrics(R_I_S_FONT);
        int tw = metrics.stringWidth(R_I_S);
        int th = metrics.getHeight();

        int w = rawBarcodeImage.getWidth() + tw;
        int h = rawBarcodeImage.getHeight() + th;

        BufferedImage newimage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D graphics = newimage.createGraphics();
        // GradientPaint gradientPaint = new GradientPaint(10, 10, c1, 20, 10, c2, true);

        // Set back ground of the generated image to white
        paintGraphicsAndSetFontColor(w, h, graphics);

        graphics.drawString(R_I_S, (w / 2) - (tw / 2), (th / 2));

        graphics.drawImage(rawBarcodeImage, (w / 2) - (rawBarcodeImage.getWidth() / 2), th / 2, rawBarcodeImage.getWidth(), rawBarcodeImage.getHeight(), null);

        graphics.setTransform(AffineTransform.getQuadrantRotateInstance(1));
        graphics.drawString(R_I_S, (h / 2) - (th / 2), -(w - tw / 2));

        graphics.setTransform(AffineTransform.getQuadrantRotateInstance(2));
        graphics.drawString(R_I_S, -(w - ((w / 2) - (tw / 2))), -(h - (th / 2)));

        graphics.setTransform(AffineTransform.getQuadrantRotateInstance(3));
        graphics.drawString(R_I_S, -(h - ((h / 2) - (tw / 2))), (th / 2));

        // release resources used by graphics context
        graphics.dispose();
        return newimage;
        // return image;
    }

    /**
     * Paint the graphics for the given width and height and set the font.
     * @param w width
     * @param h height
     * @param graphics
     */
    private void paintGraphicsAndSetFontColor(int w, int h, Graphics2D graphics) {
        graphics.setPaint(R_I_S_GRADIENT_PAINT);
        graphics.fillRect(0, 0, w, h);

        graphics.setColor(Color.BLACK);
        graphics.setFont(R_I_S_FONT);
    }

    /**
     * Sets the cell padding properties.
     * @param cell to be set
     * @param padding
     *            padding
     * @param paddingBottom
     *            bottom padding
     * @param paddingTop
     *            top padding
     * @param paddingLeft
     *            left padding
     * @param paddingRight
     *            right padding
     */
    private void setCellProperties(PdfPCell cell, float padding, float paddingBottom, float paddingTop, float paddingLeft, float paddingRight) {
        cell.setPadding(padding);
        cell.setPaddingBottom(paddingBottom);
        cell.setPaddingTop(paddingTop);
        cell.setPaddingLeft(paddingLeft);
        cell.setPaddingRight(paddingRight);
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
    }

    /**
     * Set the default cell properties with bottom and left padding to {@link #CELL_PADDING}
     * @param cell on which the properties to be set
     */
    private void setDefaultCellProperties(PdfPCell cell) {
        setCellProperties(cell, 0f, CELL_PADDING, 0f, CELL_PADDING, 0f);
    }

    /**
     * Creates a PDF Cell for the given image.
     * 
     * @param pic image
     * @return PDF cell
     */
    private PdfPCell createPdfPCell(com.lowagie.text.Image pic) {
        PdfPCell cell = new PdfPCell(pic, false);
        setDefaultCellProperties(cell);
        return cell;
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Returns the BitMatrix for for the given text.
     * @param barcodeText
     * @param qrCodeSize
     * @return BitMatrix
     * @throws WriterException
     */
    private BitMatrix createBarcodeImage(String barcodeText, int qrCodeSize) throws WriterException {
        //TODO: As now we have provided multiple formats we need to think about the width and height.
        return writer.encode(barcodeText, barcodeFormat, qrCodeSize, qrCodeSize);
    }

}