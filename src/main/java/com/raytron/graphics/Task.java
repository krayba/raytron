/**
 * 
 */
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

class Task extends SwingWorker<Void, Void> {

    /**
     * 
     */
    private static final Font R_I_S_FONT = new Font("Courier New", Font.PLAIN, 10);

    private static final GradientPaint R_I_S_GRADIENT_PAINT = new GradientPaint(5, 0, new Color(54, 172, 212), 0, 5, new Color(255, 255, 255), true);

    /**
     * 
     */
    private static final String R_I_S = "RiS";
    /**
     * 
     */
    private static final float CELL_PADDING = 4f;
    private final String iFile;
    private final String oFile;
    private final int qrCodeSize;
    private final boolean includeHeader;
    private final RaytronQRCodeGeneratorUI raytronQRCodeGenerator;
    private int tableColumns;
    private final boolean overlayRISImage;
    private final Writer writer;

    private final BarcodeFormat barCodeFormat;

    private final boolean saveSampleImage;

    public Task(final RaytronQRCodeGeneratorUI raytronQRCodeGenerator, final BarcodeFormat format, final boolean overlayRISImage, final String iFile, final String oFile,
            final int qrCodeSize, final boolean includeHeader, final boolean saveSampleImage) {
        this.raytronQRCodeGenerator = raytronQRCodeGenerator;
        this.iFile = iFile;
        this.oFile = oFile;
        this.qrCodeSize = qrCodeSize;
        this.includeHeader = includeHeader;
        this.overlayRISImage = overlayRISImage;
        this.barCodeFormat = format;
        this.saveSampleImage = saveSampleImage;
        writer = BarcodeFactory.getBarcodeInstance(format);
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
            table = calculateMaxLengthPicSize(document, freader);
            setDefaultCellProperties(table.getDefaultCell());
            setProgress(freader.getPercentageRead());
            int columnPrinted = 0;
            boolean imageSaved = (saveSampleImage ? false : true);
            while (freader.hasNext()) {
                String sLine = freader.next();
                setProgress(freader.getPercentageRead());
                if (!"".equals(sLine)) {
                    BitMatrix matrix = createBarcodeImage(sLine, qrCodeSize);
                    if (matrix != null) {
                        BufferedImage image;
                        image = createImage(matrix);
                        com.lowagie.text.Image pic = Image.getInstance(image, null);
                        if (!imageSaved) {
                            saveImageToFile(image);
                            imageSaved = true;
                        }

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
     * @param image
     */
    private void saveImageToFile(BufferedImage image) {
        try {
            ImageIO.write(image, "jpeg", new File(oFile + ".jpeg"));
        } catch (IOException e) {
        }
    }

    /**
     * @param matrix
     * @return
     */
    private BufferedImage createImage(BitMatrix matrix) {
        if (overlayRISImage) {
            return getOverlayRISImage(MatrixToImageWriter.toBufferedImage(matrix));
        } else {
            return MatrixToImageWriter.toBufferedImage(matrix);
        }
    }

    /**
     * @param table
     * @param columns2
     */
    private void addBlankCellsSoThatPageIsAdded(PdfPTable table, int columns2) {
        for (int i = 0; i < columns2; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(""));
            setDefaultCellProperties(cell);
            table.addCell(cell);
        }
    }

    private PdfPTable calculateMaxLengthPicSize(Document document, FReader reader) throws Throwable {
        PdfPTable table = null;
        String sLine = reader.getMaxLine();
        if (!"".equals(sLine)) {
            BitMatrix matrix = createBarcodeImage(sLine, qrCodeSize);
            if (matrix != null) {
                BufferedImage image = createImage(matrix);
                com.lowagie.text.Image pic = Image.getInstance(image, null);
                table = createTableByCalculatingPageSizeAndColumns(document, reader, pic);
                setDefaultCellProperties(table.getDefaultCell());
            }
        }
        return table;
    }

    /**
     * @param document
     * @param freader
     * @param pic
     * @return
     * @throws DocumentException
     */
    private PdfPTable createTableByCalculatingPageSizeAndColumns(Document document, FReader freader, com.lowagie.text.Image pic) throws DocumentException {
        PdfPTable table;
        Rectangle psize = document.getPageSize();
        float tableSize = psize.getWidth();
        tableColumns = (int) (tableSize / (pic.getWidth() + CELL_PADDING));
        float[] widths = new float[tableColumns];
        for (int i = 0; i < tableColumns; i++) {
            widths[i] = pic.getWidth();
        }
        table = new PdfPTable(tableColumns);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        return table;
    }

    /**
     * @param image
     * @return
     */
    private BufferedImage getOverlayRISImage(BufferedImage image) {
        Graphics g = image.getGraphics();
        FontMetrics metrics = g.getFontMetrics(R_I_S_FONT);
        int tw = metrics.stringWidth(R_I_S);
        int th = metrics.getHeight();

        int w = image.getWidth() + tw;
        int h = image.getHeight() + th;

        BufferedImage newimage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D graphics = newimage.createGraphics();
        // GradientPaint gradientPaint = new GradientPaint(10, 10, c1, 20, 10, c2, true);

        // Set back ground of the generated image to white
        paintGraphicsAndSetFontColor(w, h, graphics);

        graphics.drawString(R_I_S, (w / 2) - (tw / 2), (th / 2));

        graphics.drawImage(image, (w / 2) - (image.getWidth() / 2), th / 2, image.getWidth(), image.getHeight(), null);

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
     * @param w
     * @param h
     * @param graphics
     */
    private void paintGraphicsAndSetFontColor(int w, int h, Graphics2D graphics) {
        graphics.setPaint(R_I_S_GRADIENT_PAINT);
        graphics.fillRect(0, 0, w, h);

        graphics.setColor(Color.BLACK);
        graphics.setFont(R_I_S_FONT);
    }

    /**
     * @param cell
     * @param padding
     *            TODO
     * @param paddingBottom
     *            TODO
     * @param paddingTop
     *            TODO
     * @param paddingLeft
     *            TODO
     * @param paddingRight
     *            TODO
     */
    private void setCellProperties(PdfPCell cell, float padding, float paddingBottom, float paddingTop, float paddingLeft, float paddingRight) {
        cell.setPadding(padding);
        cell.setPaddingBottom(paddingBottom);
        cell.setPaddingTop(paddingTop);
        cell.setPaddingLeft(paddingLeft);
        cell.setPaddingRight(paddingRight);
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
    }

    private void setDefaultCellProperties(PdfPCell cell) {
        setCellProperties(cell, 0f, CELL_PADDING, 0f, CELL_PADDING, 0f);
    }

    /**
     * @param pic
     * @return
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
     * @param s
     * @param qrCodeSize
     * @return
     * @throws WriterException
     */
    private BitMatrix createBarcodeImage(String s, int qrCodeSize) throws WriterException {
        return writer.encode(s, barCodeFormat, qrCodeSize, qrCodeSize);
    }

}