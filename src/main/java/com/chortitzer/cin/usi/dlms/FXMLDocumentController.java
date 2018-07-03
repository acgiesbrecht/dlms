/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chortitzer.cin.usi.dlms;

import gnu.io.*;
import gurux.common.IGXMedia;
import gurux.common.ReceiveParameters;
import gurux.dlms.GXDLMSClient;
import gurux.dlms.GXDLMSException;
import gurux.dlms.GXReplyData;
import gurux.dlms.enums.Authentication;
import gurux.dlms.enums.DataType;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.enums.RequestTypes;
import gurux.dlms.internal.GXCommon;
import gurux.dlms.manufacturersettings.GXManufacturer;
import gurux.dlms.manufacturersettings.GXManufacturerCollection;
import gurux.dlms.objects.*;
import gurux.dlms.secure.GXDLMSSecureClient;
import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.net.GXNet;
import gurux.net.enums.NetworkType;
import gurux.serial.GXSerial;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmuc.j62056.DataMessage;
import org.openmuc.j62056.Iec21Port;

/**
 * FXML Controller class
 *
 * @author adriang
 */
public class FXMLDocumentController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger(FXMLDocumentController.class);

    GXDLMSClient client = new GXDLMSClient();
    GXNet media = new gurux.net.GXNet();

    //EntityManager em = Persistence.createEntityManagerFactory("pcb_PU").createEntityManager();

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {

            client.setClientAddress(1);
            client.setServerAddress(1);
            client.setInterfaceType(InterfaceType.WRAPPER);
            client.setAuthentication(Authentication.LOW);
            client.setPassword("12345678".getBytes("ASCII"));
            client.setUseLogicalNameReferencing(true);

            media.setHostName("10.10.0.8");
            media.setPort(4059);
            media.setProtocol(NetworkType.TCP);
System.out.println(LocalDateTime.now().toString() + " - Start");
            media.open();
            GXReplyData reply = new GXReplyData();
            byte[] data;
            data = client.snrmRequest();
            if (data != null) {
                readDLMSPacket(data, reply);
                //Has server accepted client.
                client.parseUAResponse(reply.getData());
            }

            for (byte[] it : client.aarqRequest()) {
                reply.clear();
                readDLMSPacket(it, reply);
            }
            //Parse reply.
            client.parseAareResponse(reply.getData());
            /// Read Association View from the meter.
            reply = new GXReplyData();
            System.out.println(LocalDateTime.now().toString() + " - Read");

            GXDLMSData nroSerie = new GXDLMSData("0.0.42.0.0.255");
            readObject(nroSerie,2);

            GXDLMSRegister energiaActivaPlus = new GXDLMSRegister("1.0.1.8.0.255");
            readObject(energiaActivaPlus,2);

            GXDLMSRegister energiaReactivaPlus = new GXDLMSRegister("1.0.3.8.0.255");
            readObject(energiaReactivaPlus,2);

            GXDLMSExtendedRegister demandaMaxima = new GXDLMSExtendedRegister("1.0.1.6.0.255");
            readObject(demandaMaxima,1);



            /*readDataBlock(client.getObjectsRequest(), reply);
            GXDLMSObjectCollection objects = client.parseObjects(reply.getData(), true);
            reply = new GXReplyData();*/
            System.out.println(nroSerie.getValue());
            System.out.println(energiaActivaPlus.getValue());
            System.out.println(energiaReactivaPlus.getValue());
            System.out.println(demandaMaxima.getValue());
            readDLMSPacket(client.disconnectRequest(), reply);
        } catch (Exception e) {
            System.out.println(e.toString());

        }finally {
            media.close();
            System.out.println(LocalDateTime.now().toString() + " - Finish");
        }

        /*
        GXCommunicate com = null;
        PrintWriter logFile = null;
        try {
            logFile = new PrintWriter(
                    new BufferedWriter(new FileWriter("logFile.txt")));
            com = getManufactureSettings();
            if (com == null) {
                return;
            }
            com.initializeConnection();
            //com.readAllObjects(logFile);

            GXDLMSRegister gxdlmsData = new GXDLMSRegister("1.0.16.8.0.255");

            com.readObject(gxdlmsData,2);


            System.out.println(gxdlmsData.getValue());
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            if (logFile != null) {
                logFile.close();
            }
            try {
                ///////////////////////////////////////////////////////////////
                // Disconnect.
                if (com != null) {
                    com.close();
                }
            } catch (Exception Ex2) {
                System.out.println(Ex2.toString());
            }
        }
        System.out.println("Done!");
        */
    }

    Object readObject(GXDLMSObject item, int attributeIndex)
            throws Exception {
        byte[] data = client.read(item.getName(), item.getObjectType(),
                attributeIndex)[0];
        GXReplyData reply = new GXReplyData();

        readDataBlock(data, reply);
        // Update data type on read.
        if (item.getDataType(attributeIndex) == DataType.NONE) {
            item.setDataType(attributeIndex, reply.getValueType());
        }
        return client.updateValue(item, attributeIndex, reply.getValue());
    }

    /**
     * Read DLMS Data from the device.
     * If access is denied return null.
     */
    void readDLMSPacket(byte[] data, GXReplyData reply) throws Exception {
        if (data == null || data.length == 0) {
            return;
        }
        Object eop = (byte) 0x7E;
        // In network connection terminator is not used.
        if (client.getInterfaceType() == InterfaceType.WRAPPER
                && media instanceof GXNet) {
            eop = null;
        }
        Integer pos = 0;
        boolean succeeded = false;
        ReceiveParameters<byte[]> p =
                new ReceiveParameters<byte[]>(byte[].class);
        p.setAllData(true);
        p.setEop(eop);
        p.setCount(5);
        p.setWaitTime(10000);
        synchronized (media.getSynchronous()) {
            while (!succeeded) {
                media.send(data, null);
                if (p.getEop() == null) {
                    p.setCount(1);
                }
                succeeded = media.receive(p);
                if (!succeeded) {
                    // Try to read again...
                    if (pos++ != 3) {
                        System.out.println("Data send failed. Try to resend "
                                + pos.toString() + "/3");
                        continue;
                    }
                    throw new RuntimeException(
                            "Failed to receive reply from the device in given time.");
                }
            }
            // Loop until whole DLMS packet is received.
            while (!client.getData(p.getReply(), reply)) {
                if (p.getEop() == null) {
                    p.setCount(1);
                }
                if (!media.receive(p)) {
                    throw new Exception(
                            "Failed to receive reply from the device in given time.");
                }
            }
        }
        if (reply.getError() != 0) {
            throw new GXDLMSException(reply.getError());
        }
    }

    void readDataBlock(byte[] data, GXReplyData reply) throws Exception {
        RequestTypes rt;
        if (data.length != 0) {
            readDLMSPacket(data, reply);
            while (reply.isMoreData()) {
                rt = reply.getMoreData();
                data = client.receiverReady(rt);
                readDLMSPacket(data, reply);
            }
        }
    }

    void readDataBlock(byte[][] data, GXReplyData reply) throws Exception {
        for (byte[] it : data) {
            reply.clear();
            readDataBlock(it, reply);
        }
    }

    Thread thx;
    SerialPort serialPort;
    InputStream in;
    OutputStream outputStream;
    SerialReader sr;

    void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(300, SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);

                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                outputStream = out;

                sr = new SerialReader(in);

                serialPort.addEventListener(sr);
                serialPort.notifyOnDataAvailable(true);

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public void write(String send) {
        try {
            outputStream.write(send.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            //in.close();
            serialPort.close();
            sr = null;
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
    }

    /**
     *
     */
    public class SerialReader implements SerialPortEventListener {

        private InputStream in;


        public SerialReader(InputStream in) {
            this.in = in;
        }

        Double IndicadorData = 0.0;


        public void serialEvent(SerialPortEvent arg0) {
            int data;
            byte[] buffer = new byte[1024];

            try {

                int len = 0;
                while ((data = in.read()) > -1) {

                    buffer[len++] = (byte) data;
                    if (data == 3) {
                        break;
                    }
                }
                if (len == 12) {
                    Double res = Double.parseDouble(new String(buffer, 0, len));


                    //int BolsaActual = Integer.parseInt(cboBolsaPeso.getSelectedItem().toString());
                    String s = "";
                    for (int i = 1; i < len; i++) {
                        s += String.valueOf(buffer[i]) + " ";
                    }
                    //System.out.println(len);
                    System.out.println(s);


                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    static GXCommunicate getManufactureSettings()
            throws Exception {
        IGXMedia media = null;
        GXCommunicate com;
        String path = "ManufacturerSettings";
        try {
            if (GXManufacturerCollection.isFirstRun(path)) {
                GXManufacturerCollection.updateManufactureSettings(path);
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        // 4059 is Official DLMS port.
        String id = "", host = "10.10.0.8", port = "4059", pw = "12345678";
        boolean trace = true, iec = false;
        Authentication auth = Authentication.LOW;
        int startBaudRate = 9600;
        String number = null;

        host = "10.10.0.8";

        media = new gurux.net.GXNet();
        port = "4059";
        iec = false;
        auth = Authentication.LOW;
        pw = "12345678";

        GXNet net = (GXNet) media;
        net.setPort(Integer.parseInt(port));
        net.setHostName(host);
        net.setProtocol(NetworkType.TCP);

        GXDLMSSecureClient dlms = new GXDLMSSecureClient();

        GXManufacturerCollection items = new GXManufacturerCollection();
        GXManufacturerCollection.readManufacturerSettings(items, path);
        GXManufacturer man = items.findByIdentification("ISK");
        if (man == null) {
            throw new RuntimeException("Invalid manufacturer.");
        }
        dlms.setObisCodes(man.getObisCodes());
        com = new GXCommunicate(10000, dlms, man, iec, auth, pw, media);
        com.Trace = trace;
        return com;
    }
}
