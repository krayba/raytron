/**
 * 
 */
package com.raytron.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.aztec.AztecWriter;
import com.google.zxing.datamatrix.DataMatrixWriter;
import com.google.zxing.oned.CodaBarWriter;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.Code39Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.EAN8Writer;
import com.google.zxing.oned.ITFWriter;
import com.google.zxing.oned.UPCAWriter;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * @author kedar460043
 * 
 */
public class BarcodeFactory {

    /**
     * 
     */
    private BarcodeFactory() {
        // TODO Auto-generated constructor stub
    }

    public static Writer getBarcodeInstance(BarcodeFormat format) {
        switch (format) {
        case AZTEC:
            return new AztecWriter();
        case CODABAR:
            return new CodaBarWriter();
        case CODE_39:
            return new Code39Writer();
        case CODE_128:
            return new Code128Writer();
        case DATA_MATRIX:
            return new DataMatrixWriter();
        case PDF_417:
            return new PDF417Writer();
        case ITF:
            return new ITFWriter();
        case EAN_8:
            return new EAN8Writer();
        case EAN_13:
            return new EAN13Writer();
        case UPC_A:
            return new UPCAWriter();
        default:
            return new QRCodeWriter();
        }
    }
}
