/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.common;

import com.panemu.tiwulfx.control.DateFieldController;
import com.panemu.tiwulfx.control.NumberField;
import com.panemu.tiwulfx.table.NumberColumn;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class TiwulFXUtil {

    private static Locale loc = new Locale("es", "PY");
    private static DateTimeFormatter dateTimeFormatter;
    private static DateFormat dateFormat;
    private static ResourceBundle literalBundle = ResourceBundle.getBundle("com.panemu.tiwulfx.res.literal", loc);
    public static int DEFAULT_LOOKUP_SUGGESTION_ITEMS = 10;
    public static int DEFAULT_LOOKUP_SUGGESTION_WAIT_TIMES = 500;
    public static int DEFAULT_DIGIT_BEHIND_DECIMAL = 2;

    /**
     * Default value for {@link NumberField#isAllowNegative()} and
     * {@link NumberColumn#isAllowNegative()}. Default is false
     */
    public static boolean DEFAULT_NEGATIVE_ALLOWED = false;
    public static int DURATION_FADE_ANIMATION = 300;

    /**
     * The default value is taken from literal.properties file, key: label.null
     */
    public static String DEFAULT_NULL_LABEL = TiwulFXUtil.getLiteral("label.null");
    public static boolean DEFAULT_EMPTY_STRING_AS_NULL = true;
    public static String DEFAULT_DATE_PROMPTEXT = "";
    private static String applicationId = ".tiwulfx";
    private static LiteralUtil literalUtil = new LiteralUtil(loc);
    private static GraphicFactory graphicFactory = new GraphicFactory();
    /**
     * Default number of rows displayed in
     * {@link com.panemu.tiwulfx.table.TableControl TableControl}. Default is
     * 500
     */
    public static int DEFAULT_TABLE_MAX_ROW = 500;

    private static ExceptionHandlerFactory exceptionHandlerFactory = new DefaultExceptionHandlerFactory();

    public static Locale getLocale() {
        return loc;
    }

    private static String getLocaleDatePattern() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, loc);
        SimpleDateFormat simpleFormat = (SimpleDateFormat) dateFormat;
        String pattern = simpleFormat.toPattern();
        return pattern;
    }

    public static DateTimeFormatter getDateFormatForLocalDate() {

        if (dateTimeFormatter == null) {
            dateTimeFormatter = DateTimeFormatter.ofPattern(getLocaleDatePattern());
        }
        return dateTimeFormatter;
    }

    public static DateFormat getDateFormatForJavaUtilDate() {
        if (dateFormat == null) {
            dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, getLocale());
        }
        return dateFormat;
    }

    public static void setDateFormat(String pattern) {
        dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        dateFormat = new SimpleDateFormat(pattern);
    }

    public static void setLocale(Locale loc) {
        TiwulFXUtil.loc = loc;
        dateTimeFormatter = DateTimeFormatter.ofPattern(getLocaleDatePattern());
        literalBundle = literalUtil.changeLocale(loc);
    }

    /**
     * Add Resource Bundle for internationalization. The baseName argument
     * should be a fully qualified class name.
     *
     * For example: if the file name is language.properties and it is located in
     * com.panemu.tiwulfx.res package then the correct value for basename is
     * "com.panemu.tiwulfx.res.language". Don't specify the extension, locale
     * language or locale country.
     *
     * @param baseName fully qualified class name.
     */
    public static void addLiteralBundle(String baseName) {
        literalBundle = literalUtil.addLiteral(baseName);
    }

    public static DecimalFormat getDecimalFormat() {
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        return (DecimalFormat) nf;
    }

    public static ResourceBundle getLiteralBundle() {
        return literalBundle;
    }

    public static void setLiteralBundle(ResourceBundle literalBundle) {
        TiwulFXUtil.literalBundle = literalBundle;
    }

    public static String getLiteral(String key) {
        try {
            return literalBundle.getString(key);
        } catch (Exception ex) {
            return key;
        }

    }

    public static String getLiteral(String key, Object... param) {
        try {
            return MessageFormat.format(literalBundle.getString(key), param);
        } catch (Exception ex) {
            return key;
        }

    }

    public static void setToolTip(Control control, String key) {
        control.setTooltip(new Tooltip(getLiteral(key)));
    }

    public static void openFile(String fileToOpen) throws Exception {
        Desktop.getDesktop().open(new File(fileToOpen));
    }
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);
        return date;
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        Instant instant = Instant.ofEpochMilli(date.getTime());
        LocalDate res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
        return res;
    }

    /**
     * Application id will be the folder's name where the configuration file is
     * stored inside user's home folder. The folder will be started with a dot.
     *
     * @param id
     */
    public static void setApplicationId(String id) {
        if (id != null && !id.contains(File.separator) && !id.contains(" ")) {
            applicationId = id;
        } else {
            throw new RuntimeException("Invalid application ID. It should not contains space and " + File.separator);
        }
        if (!applicationId.startsWith(".")) {
            applicationId = "." + applicationId;
        }
    }

    public static String getApplicationId() {
        return applicationId;
    }

    private static String getConfigurationPath() throws IOException {
        String home = System.getProperty("user.home");

        String confPath = home + File.separator + applicationId;

        File confFile = new File(confPath);
        if (!confFile.exists()) {
            confFile.mkdirs();
        }
        confPath = home + File.separator + applicationId + File.separator + "conf.properties";
        confFile = new File(confPath);
        if (!confFile.exists()) {
            confFile.createNewFile();
        }
        return confPath;
    }

    private static Properties loadProperties() {
        Properties p = new Properties();
        try {
            FileInputStream in = new FileInputStream(getConfigurationPath());
            p.load(in);
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p;

    }

    private static Properties confFile;

    private static Properties getConfigurations() {
        if (confFile == null) {
            confFile = loadProperties();
        }
        return confFile;
    }

    /**
     * Read property from configuration file. See
     * {@link #setApplicationId(String)}
     * <p>
     * @param propName
     * @return
     */
    public static String readProperty(String propName) {
        return getConfigurations().getProperty(propName);
    }

    /**
     * Delete items from configuration file. See
     * {@link #setApplicationId(String)}
     * <p>
     * @param propNames
     * @throws Exception
     */
    public synchronized static void deleteProperties(List<String> propNames) throws Exception {
        for (String propName : propNames) {
            getConfigurations().remove(propName);
        }
        writePropertiesToFile();
    }

    /**
     * Delete an item from configuration file. See
     * {@link #setApplicationId(String)}
     * <p>
     * @param propName
     * @throws Exception
     */
    public synchronized static void deleteProperties(String propName) throws Exception {
        getConfigurations().remove(propName);
        writePropertiesToFile();
    }

    /**
     * Save values to configuration file. See {@link #setApplicationId(String)}
     * <p>
     * @param mapPropertyValue propertyName, Value
     * @return
     */
    public synchronized static void writeProperties(Map<String, String> mapPropertyValue) throws Exception {
        Set<String> propNames = mapPropertyValue.keySet();
        for (String propName : propNames) {
            String value = mapPropertyValue.get(propName);
            getConfigurations().setProperty(propName, value);
        }
        writePropertiesToFile();
    }

    private synchronized static void writePropertiesToFile() {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getConfigurationPath());
            getConfigurations().store(out, "Last updated: " + (new Date()));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Save property-name's value to configuration file. See
     * {@link #setApplicationId(String)}
     * <p>
     * @param propName
     * @param value
     * @throws Exception
     */
    public synchronized static void writeProperties(String propName, String value) throws Exception {
        getConfigurations().setProperty(propName, value);
        writePropertiesToFile();
    }

    /**
     * Get default {@link ExceptionHandler} implementation. This implementation
     * will be used in TiwulFX components when uncatched error happens. To
     * override default implementation, create a custom implementation of
     * {@link ExceptionHandlerFactory} and set it to
     * {@link #setExceptionHandlerFactory(ExceptionHandlerFactory)}
     * <p>
     * @return ExceptionHandler
     */
    public static ExceptionHandler getExceptionHandler() {
        return exceptionHandlerFactory.createExceptionHandler();
    }

    /**
     * Set {@link ExceptionHandlerFactory} to override the default
     * {@link DefaultExceptionHandlerFactory}.
     * <p>
     * @param exceptionHandlerFactory the factory that is used by
     * {@link #getExceptionHandler()}
     */
    public static void setExceptionHandlerFactory(ExceptionHandlerFactory exceptionHandlerFactory) {
        if (exceptionHandlerFactory == null) {
            throw new IllegalArgumentException("ExceptionHandlerFactory cannot be null");
        }
        TiwulFXUtil.exceptionHandlerFactory = exceptionHandlerFactory;
    }

    /**
     * Set default TiwulFX css style to a scene. The default is
     * /com/panemu/tiwulfx/res/tiwulfx.css located inside tiwulfx jar file.
     *
     * @param scene
     */
    public static void setTiwulFXStyleSheet(Scene scene) {
        scene.getStylesheets().add(TiwulFXUtil.class.getResource("/com/panemu/tiwulfx/res/tiwulfx.css").toExternalForm());
    }

    /**
     * Set a class that responsible to provide graphics for TiwulFX UI
     * components. Developer can override the default by extending
     * {@link GraphicFactory} class and use the instance as a parameter to call
     * this method at the beginning of application launching process.
     *
     * @param graphicFactory
     */
    public static void setGraphicFactory(GraphicFactory graphicFactory) {
        TiwulFXUtil.graphicFactory = graphicFactory;
    }

    /**
     * Get currently used {@link GraphicFactory} instance
     *
     * @return
     */
    public static GraphicFactory getGraphicFactory() {
        return graphicFactory;
    }

    /**
     * Attach shortcut to increase or decrease date on dateField. It uses
     * {@link DateEventHandler} class
     * <p>
     * @param dateField
     * @param dateController
     */
    public static void attachShortcut(DatePicker dateField, DateFieldController dateController) {
        dateField.addEventFilter(KeyEvent.KEY_PRESSED, new DateEventHandler(dateField, dateController));
    }

}
