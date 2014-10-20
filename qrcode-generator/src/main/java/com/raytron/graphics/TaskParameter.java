package com.raytron.graphics;

import com.google.zxing.BarcodeFormat;

/**
 * Task parameter object class.
 * 
 * @author Kedar Raybagkar
 *
 */
public class TaskParameter {
    private final PopupTaskHandler popupTaskHandler;
    private final BarcodeFormat format;
    private final boolean overlayRISImage;
    private final String iFile;
    private final String oFile;
    private final int barcodeSize;
    private final boolean includeHeader;
    private final boolean saveSampleImage;

    public TaskParameter(PopupTaskHandler raytronQRCodeGenerator, BarcodeFormat format, boolean overlayRISImage, String iFile, String oFile, int barcodeSize,
            boolean includeHeader, boolean saveSampleImage) {
        this.popupTaskHandler = raytronQRCodeGenerator;
        this.format = format;
        this.overlayRISImage = overlayRISImage;
        this.iFile = iFile;
        this.oFile = oFile;
        this.barcodeSize = barcodeSize;
        this.includeHeader = includeHeader;
        this.saveSampleImage = saveSampleImage;
    }

    public PopupTaskHandler getRaytronQRCodeGenerator() {
        return popupTaskHandler;
    }

    public BarcodeFormat getFormat() {
        return format;
    }

    public boolean isOverlayRISImage() {
        return overlayRISImage;
    }

    public String getiFile() {
        return iFile;
    }

    public String getoFile() {
        return oFile;
    }

    public int getBarcodeSize() {
        return barcodeSize;
    }

    public boolean isIncludeHeader() {
        return includeHeader;
    }

    public boolean isSaveSampleImage() {
        return saveSampleImage;
    }
}