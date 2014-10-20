package com.raytron.graphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.SoftBevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.zxing.BarcodeFormat;

/**
 * A Raytron Barcode Generator UI.
 * 
 * @author Kedar Raybagkar
 * 
 */
public class RaytronBarcodeGeneratorUI extends JFrame implements PopupTaskHandler{

    /**
	 * 
	 */
    private static final long serialVersionUID = -1727180057667020476L;

    private final Rectangle GENERATE_PROGRESSBAR_RECTANGLE = new Rectangle(29, 230, 338, 46);

    private JPanel jPanel = null;
    
    private JTextField jtfRaytronProductFile = null;

    private JTextField jtfQRCodeWidthHeight = null;

    private JButton jfbFileChooserPreLicenseTemplate = null;

    private JTextField jtfPDFFile = null;

    private JButton jbSaveButton = null;

    private JButton jButtonGenerate = null;

    private JProgressBar jProgressBar = null;

    private JRadioButton jrbHeaderYes = null;

    private JRadioButton jrbHeaderNo;

    private ButtonGroup buttonGroup;

    private final String DEFAULT_DRIVE;
    private final String DEFAULT_FOLDER = Paths.get("MyData", "Raytron").toString();
    private final String DEFAULT_LOAD_FILE;
    private final String DEFAULT_SAVE_FILE;

    private BarcodeFormat format = BarcodeFormat.QR_CODE;

    private boolean addOverLayImage;

    protected boolean saveSampleImage;


    /**
     * This method initializes
     * 
     */
    public RaytronBarcodeGeneratorUI() {
        super();
        DEFAULT_DRIVE = getHardDiskDrive();
        DEFAULT_LOAD_FILE = Paths.get(DEFAULT_DRIVE, DEFAULT_FOLDER, "Product.csv").toString();
        DEFAULT_SAVE_FILE = Paths.get(DEFAULT_DRIVE, DEFAULT_FOLDER, BarcodeFormat.QR_CODE.name() + ".pdf").toString(); 
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        buildDimensions();
        initializePanel();
        addProductFile();
        addWidthHeightLabelAndField();
        addPDFFile();
        addHeaderToBePrinted();
        addBarCodeTypeLabelAndDropDown();
        addOverLayImmage();
        addSaveSampleImmage();
        addJButtonGenerate();
        addJProgressBar();
        setVisible(true);
    }

    private void buildDimensions() {
        Dimension mydim = new Dimension(400, 350);
        this.setSize(mydim);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int) (dim.getWidth() - mydim.getWidth()) / 2, (int) (dim.getHeight() - mydim.getHeight()) / 2);
        ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("raytron-instrument-services-logo-120x120.jpg"));
        setIconImage(icon.getImage());
        setTitle("Raytron QRCode Generator");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     * @throws IOException
     */
    private void initializePanel() {
        jPanel = (JPanel) this.getContentPane();
        jPanel.setLayout(null);
        jPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        jPanel.setForeground(Color.black);
        this.setContentPane(jPanel);
    }

    private void addHeaderToBePrinted() {
        addHeader();
        addJrbHeaderToBePrinted();
    }

    private void addWidthHeightLabelAndField() {
        addWidthHeightLabel();
        addJtfQRCodeWidthHeight();
    }

    private void addProductFile() {
        addRaytronProductFileLabel();
        addJtfProductFile();
        addJfbFileChooserRaytronProductFile();
    }

    private void addPDFFile() {
        addPDFFileLabel();
        addJtfPDFFile();
        addJbSaveButton();
    }

    /**
     */
    private void addBarCodeTypeLabelAndDropDown() {
        JLabel barcodeType = new JLabel();
        barcodeType.setText("Barcode Type");
        barcodeType.setBounds(new Rectangle(8, 134, 126, 23));
        jPanel.add(barcodeType);
        JComboBox<BarcodeFormat> ddBarCodeType = new JComboBox<BarcodeFormat>(new BarcodeFormat[] { BarcodeFormat.AZTEC, BarcodeFormat.CODABAR, BarcodeFormat.CODE_39, BarcodeFormat.CODE_128,
                BarcodeFormat.DATA_MATRIX, BarcodeFormat.PDF_417, BarcodeFormat.ITF, BarcodeFormat.EAN_8, BarcodeFormat.EAN_13, BarcodeFormat.UPC_A, BarcodeFormat.QR_CODE });
        ddBarCodeType.setSelectedIndex(10);
        ddBarCodeType.setBounds(new Rectangle(136, 134, 233, 23));
        ddBarCodeType.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<BarcodeFormat> barcode = (JComboBox<BarcodeFormat>)e.getSource();
                format = (BarcodeFormat) barcode.getSelectedItem();
                jtfPDFFile.setText(Paths.get(DEFAULT_DRIVE, DEFAULT_FOLDER, format.name() + ".pdf").toString());
            }
        });
        jPanel.add(ddBarCodeType, null);
    }

    /**
     */
    private void addOverLayImmage() {
        JLabel overLayImageLabel = new JLabel();
        overLayImageLabel.setText("Overlay RiS Image?");
        overLayImageLabel.setBounds(new Rectangle(8, 164, 126, 23));
        overLayImageLabel.setDisplayedMnemonic(KeyEvent.VK_R);
        jPanel.add(overLayImageLabel);
        JCheckBox overLayImageCheckBox = new JCheckBox();
        overLayImageCheckBox.setBounds(new Rectangle(134, 164, 50, 21));
        overLayImageCheckBox.setSelected(false);
        overLayImageCheckBox.setVisible(true);
        overLayImageCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                addOverLayImage = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        overLayImageCheckBox.setMnemonic(KeyEvent.VK_R);
        jPanel.add(overLayImageCheckBox);
    }

    /**
     */
    private void addSaveSampleImmage() {
        JLabel overLayImageLabel = new JLabel();
        overLayImageLabel.setText("Save Sample Image?");
        overLayImageLabel.setBounds(new Rectangle(8, 197, 126, 23));
        overLayImageLabel.setDisplayedMnemonic(KeyEvent.VK_I);
        jPanel.add(overLayImageLabel);
        JCheckBox saveSampleImageCheckBox = new JCheckBox();
        saveSampleImageCheckBox.setBounds(new Rectangle(134, 197, 50, 21));
        saveSampleImageCheckBox.setSelected(false);
        saveSampleImageCheckBox.setVisible(true);
        saveSampleImageCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                saveSampleImage = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        saveSampleImageCheckBox.setMnemonic(KeyEvent.VK_I);
        jPanel.add(saveSampleImageCheckBox);
    }

    /**
     * 
     */
    private void addRaytronProductFileLabel() {
        JLabel jlRaytronProductFile = new JLabel();
        jlRaytronProductFile.setText("Raytron Product File");
        jlRaytronProductFile.setLocation(new Point(8, 11));
        jlRaytronProductFile.setDisplayedMnemonic(KeyEvent.VK_O);
        jlRaytronProductFile.setName("Product File");
        jlRaytronProductFile.setBackground(Color.white);
        jlRaytronProductFile.setForeground(Color.black);
        jlRaytronProductFile.setSize(new Dimension(126, 21));
        jPanel.add(jlRaytronProductFile);
    }

    /**
     * 
     */
    private void addWidthHeightLabel() {
        JLabel jLabelWidthHeight = new JLabel();
        jLabelWidthHeight.setBounds(new Rectangle(8, 42, 126, 23));
        jLabelWidthHeight.setForeground(Color.black);
        jLabelWidthHeight.setText("Width == Height");
        jPanel.add(jLabelWidthHeight);
    }

    /**
     * 
     */
    private void addPDFFileLabel() {
        JLabel jlPDFFile = new JLabel();
        jlPDFFile.setBounds(new Rectangle(8, 71, 126, 24));
        jlPDFFile.setForeground(Color.black);
        jlPDFFile.setText("Save to PDF File");
        jlPDFFile.setDisplayedMnemonic(KeyEvent.VK_S);
        jPanel.add(jlPDFFile, null);
    }

    /**
     * 
     */
    private void addHeader() {
        JLabel jlHeader = new JLabel();
        jlHeader.setBounds(new Rectangle(9, 103, 126, 21));
        jlHeader.setForeground(Color.black);
        jlHeader.setText("Include Header");
        jPanel.add(jlHeader, null);
    }

    /**
     * This method initializes jtfXmlTemplateFile
     * 
     * @return javax.swing.JTextField
     */
    private void addJtfProductFile() {
        jtfRaytronProductFile = new JTextField();
        jtfRaytronProductFile.setBounds(new Rectangle(134, 10, 228, 23));
        jtfRaytronProductFile.setText(DEFAULT_LOAD_FILE);
        jtfRaytronProductFile.setToolTipText("Enter the file name of the Product File");
        jPanel.add(jtfRaytronProductFile, null);
    }

    /**
     * This method initializes jtfLicenseRequest
     * 
     * @return javax.swing.JTextField
     */
    private void addJtfQRCodeWidthHeight() {
        jtfQRCodeWidthHeight = new JTextField();
        jtfQRCodeWidthHeight.setBounds(new Rectangle(134, 43, 227, 22));
        jtfQRCodeWidthHeight.setText("1");
        jtfQRCodeWidthHeight
                .setToolTipText("Indicative width/height of the QR Code. If the data cannot be accomodated in the given size then the size will be auto increased.");
        jtfQRCodeWidthHeight.setName("QRCodeWidthHeight");
        jPanel.add(jtfQRCodeWidthHeight, null);
    }

    /**
     * This method initializes jfbFileChooserPreLicenseTemplate
     * 
     * @return javax.swing.JButton
     */
    private void addJfbFileChooserRaytronProductFile() {
        jfbFileChooserPreLicenseTemplate = new JButton();
        jfbFileChooserPreLicenseTemplate.setAction(new FileChooserAction(jtfRaytronProductFile, JFileChooser.OPEN_DIALOG, DEFAULT_LOAD_FILE, "Product File", "csv"));
        jfbFileChooserPreLicenseTemplate.setMnemonic(KeyEvent.VK_O);
        jfbFileChooserPreLicenseTemplate.setBounds(new Rectangle(360, 10, 25, 21));
        this.getClass().getClassLoader();
        jfbFileChooserPreLicenseTemplate.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("open.gif")));
        jPanel.add(jfbFileChooserPreLicenseTemplate, null);
    }

    private void addJrbHeaderToBePrinted() {
        jrbHeaderYes = new JRadioButton("Yes");
        jrbHeaderYes.setMnemonic(KeyEvent.VK_Y);
        jrbHeaderYes.setBounds(new Rectangle(200, 103, 50, 21));
        jrbHeaderYes.setActionCommand("Yes");
        jrbHeaderYes.setToolTipText("Selecting this will increase the QR Code size and may need a reduction in number of columns.");

        jrbHeaderNo = new JRadioButton("No");
        jrbHeaderNo.setMnemonic(KeyEvent.VK_N);
        jrbHeaderNo.setSelected(true);
        jrbHeaderNo.setBounds(new Rectangle(134, 103, 50, 21));
        jrbHeaderNo.setActionCommand("No");

        buttonGroup = new ButtonGroup();
        buttonGroup.add(jrbHeaderYes);
        buttonGroup.add(jrbHeaderNo);

        jPanel.add(jrbHeaderYes, null);
        jPanel.add(jrbHeaderNo, null);

    }

    /**
     * This method initializes jtfLicenseFile
     * 
     * @return javax.swing.JTextField
     */
    private void addJtfPDFFile() {
        jtfPDFFile = new JTextField();
        // jtfPDFFile.setBounds(new Rectangle(134, 103, 226, 22));
        jtfPDFFile.setBounds(new Rectangle(134, 72, 226, 22));
        jtfPDFFile.setToolTipText("Example Raytron.pdf");
        jtfPDFFile.setText(DEFAULT_SAVE_FILE);
        jPanel.add(jtfPDFFile);
    }

    /**
     * This method initializes jbSaveButton
     * 
     * @return javax.swing.JButton
     */
    private void addJbSaveButton() {
        jbSaveButton = new JButton();
        // jbSaveButton.setBounds(new Rectangle(360, 102, 24, 23));
        jbSaveButton.setBounds(new Rectangle(360, 72, 24, 23));
        jbSaveButton.setAction(new FileChooserAction(jtfPDFFile, JFileChooser.SAVE_DIALOG, DEFAULT_SAVE_FILE, "QR Code PDF File", "pdf"));
        jbSaveButton.setMnemonic(KeyEvent.VK_S);
        jbSaveButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("save.gif")));
        jbSaveButton.setToolTipText("Select the file to View or Create the file to save.");
        jPanel.add(jbSaveButton);
    }

    private void addJProgressBar() {
        jProgressBar = new JProgressBar();
        jProgressBar.setMinimum(0);
        jProgressBar.setMaximum(100);
        jProgressBar.setStringPainted(true);
        jProgressBar.setBounds(GENERATE_PROGRESSBAR_RECTANGLE);
        jProgressBar.setVisible(false);
        jPanel.add(jProgressBar, null);
    }

    /**
     * This method initializes jButtonGenerate
     * 
     * @return javax.swing.JButton
     */
    private void addJButtonGenerate() {
        jButtonGenerate = new JButton();
        jButtonGenerate.setBounds(GENERATE_PROGRESSBAR_RECTANGLE);
        jButtonGenerate.setForeground(Color.black);
        jButtonGenerate.setText("Generate");
        jButtonGenerate.setMnemonic(KeyEvent.VK_G);
        jButtonGenerate.setToolTipText("Generates the license and stores it in the License File");
        jButtonGenerate.setHideActionText(false);
        jButtonGenerate.setActionCommand("Generate");
        jButtonGenerate.addActionListener(new GenerateButtonActionListener(this));
        jPanel.add(jButtonGenerate, null);
    }

    public static void main(String[] args) {
        new RaytronBarcodeGeneratorUI();
    }

    private final class GenerateButtonActionListener implements ActionListener {
        private PopupTaskHandler uiInstance;

        public GenerateButtonActionListener(PopupTaskHandler raytronBarcodeGeneratorUI) {
            this.uiInstance = raytronBarcodeGeneratorUI;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            jButtonGenerate.setVisible(false);
            jProgressBar.setValue(0);
            jProgressBar.setVisible(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            String iFile = jtfRaytronProductFile.getText();
            String oFile = jtfPDFFile.getText();
            int qrCodeSize = Integer.parseInt(jtfQRCodeWidthHeight.getText());
            boolean includeHeader = "Yes".equals(buttonGroup.getSelection().getActionCommand());
            Task task = new Task(new TaskParameter(uiInstance, format, addOverLayImage, iFile, oFile, qrCodeSize, includeHeader, saveSampleImage));
            task.addPropertyChangeListener(new PropertyChangeListener() {
                
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress" == evt.getPropertyName()) {
                        int progress = (Integer) evt.getNewValue();
                        jProgressBar.setValue(progress);
                    }
                }
            });
            task.execute();
        }
    }

    /**
     * File Chooser.
     * 
     * @author Kedar Raybagkar
     * 
     */
    class FileChooserAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1773582138232590964L;
        private JTextField field_;
        JFileChooser chooser;

        public FileChooserAction(JTextField field, int dialogType, String name, String filterDisplayName, String filterExtenssion) {
            chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(filterDisplayName, filterExtenssion);
            chooser.setFileFilter(filter);
            chooser.setDialogType(dialogType);
            chooser.setSelectedFile(new File(field.getText()));
            chooser.setName(name);
            field_ = field;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = chooser.showOpenDialog(jPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                field_.setText(chooser.getSelectedFile().getPath());
            }
        }
    }

    /* (non-Javadoc)
     * @see com.raytron.graphics.PopupTaskHandler#taskCompleted()
     */
    @Override
    public void taskCompleted() {
        setCursor(null); // turn off the wait cursor
        jButtonGenerate.setVisible(true);
        jProgressBar.setVisible(false);
    }

    /* (non-Javadoc)
     * @see com.raytron.graphics.PopupTaskHandler#showInformationPopup(java.lang.Object, java.lang.String)
     */
    @Override
    public void showInformationPopup(Object message, String title) {
        JOptionPane.showMessageDialog(jPanel, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /* (non-Javadoc)
     * @see com.raytron.graphics.PopupTaskHandler#showErrorPopup(java.lang.Object, java.lang.String)
     */
    @Override
    public void showErrorPopup(Object message, String title) {
        JOptionPane.showMessageDialog(jPanel, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Returns the Writable hard disk other than C for windows.
     * As the program is directed for windows there is no need for mac/linux/unix structures.
     * 
     * @return Drive Letter
     */
    private String getHardDiskDrive() {
        File[] roots = File.listRoots();
        if (roots.length == 1) {
            return roots[0].getPath();
        }
        for (File file : roots) {
            String path = file.getPath();
            if (!path.startsWith("C:")) {
                if (file.canWrite()) {
                    return path;
                }
            }
        }
        return roots[0].getPath();
    }
}
