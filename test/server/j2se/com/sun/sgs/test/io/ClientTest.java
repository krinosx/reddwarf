package com.sun.sgs.test.io;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.sgs.impl.io.CompleteMessageFilter;
import com.sun.sgs.impl.io.SocketEndpoint;
import com.sun.sgs.impl.io.IOConstants.TransportType;
import com.sun.sgs.io.IOHandle;
import com.sun.sgs.io.IOHandler;
import com.sun.sgs.io.IOConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * This is a simple test class that demonstrates potential usage of the IO
 * Framework.  
 *
 * @author      Sten Anderson
 * @version     1.0
 */
public class ClientTest extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "5150";
    
    private SocketTableModel model;
    private int numSockets = 20;
    private int numDisconnects = 0;
    boolean done = false;
    boolean shouldClose = false;
    Random random;
    
    public ClientTest() {
        super("Client Test");
        
        random = new Random();
        
        JTable table = new JTable(model = new SocketTableModel());
        JScrollPane pane = new JScrollPane(table);
        
        add(pane, BorderLayout.CENTER);
        
        JButton goButton = new JButton("Go!");
        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                start();
            }
        });
        
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               shouldClose = false;
               stop();
           }
        });
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
           public void windowClosing(WindowEvent e) {
               shouldClose = true;
               shutdown();
           }
        });
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(goButton);
        bottomPanel.add(stopButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        setBounds(200, 200, 600, 300);
        setVisible(true);
    }
    
    void shutdown() {
        done = true;
        model.shutdown();
    }
    
    public void dataChanged() {
        model.fireTableDataChanged();
    }
    

    public void start() {
        String host = System.getProperty("host", DEFAULT_HOST);
        String portString = System.getProperty("port", DEFAULT_PORT);
        int port = Integer.valueOf(portString);
        InetSocketAddress addr = new InetSocketAddress(host, port);
        IOConnector connector =
            new SocketEndpoint(addr, TransportType.RELIABLE,
                    Executors.newCachedThreadPool()).createConnector();
        model.connect(connector);
    }
    
    public void stop() {
        model.disconnect();
    }

    public static void main(String[] args) {
        new ClientTest();
    }
    
    private class SocketTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        
	private List<SocketInfo> list; 
        private List<String> columnHeaders;
        
        SocketTableModel() {
            columnHeaders = new ArrayList<String>();
            columnHeaders.add("ID");
            columnHeaders.add("Status");
            columnHeaders.add("Messages In");
            columnHeaders.add("Messages Out");
            columnHeaders.add("Bytes In");
            columnHeaders.add("Bytes Out");
            
            list = new ArrayList<SocketInfo>();
            for (int i = 0; i < numSockets; i++) {
                list.add(new SocketInfo(i));
            }
        }
        
        public void connect(IOConnector connector) {
            for (SocketInfo info : list) {
                info.connect(connector);
            }
        }
        
        public void disconnect() {
            for (SocketInfo info : list) {
                info.close();
            }
        }
        
        public void shutdown() {
            for (SocketInfo info : list) {
                info.close();
            }
        }
        
        public String getColumnName(int col) {
            return columnHeaders.get(col);
        }
        
        public int getRowCount() {
            return list.size();
        }
        
        public int getColumnCount() {
            return columnHeaders.size();
        }
        
        public Object getValueAt(int row, int col) {
            String curCol = columnHeaders.get(col);
            SocketInfo info = list.get(row);
            if (curCol.equals("ID")) {
                return info.getID();
            }
            else if (curCol.equals("Status")) {
                return info.getStatus();
            }
            else if (curCol.equals("Messages In")) {
                return info.getMessagesIn();
            }
            else if (curCol.equals("Messages Out")) {
                return info.getMessagesOut();
            }
            else if (curCol.equals("Bytes In")) {
                return info.getBytesIn();
            }
            else if (curCol.equals("Bytes Out")) {
                return info.getBytesOut();
            }
            
            return "";
        }
    }
    
    private class SocketInfo implements IOHandler {
        
        private IOHandle handle;
        private String status;
        private int messagesIn;
        private int messagesOut;
        private long bytesIn;
        private long bytesOut;
        private int id;
        private boolean connected = false;
        
        SocketInfo(int id) {
            this.id = id;
            status = "Not Connected";
        }
        
        public int getID() {
            return id;
        }
        
        public int getMessagesOut() {
            return messagesOut;
        }
        
        public int getMessagesIn() {
            return messagesIn;
        }
        
        public long getBytesIn() {
            return bytesIn;
        }
        
        public long getBytesOut() {
            return bytesOut;
        }
        
        public void close() {
            try {
                handle.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            
        }
        
        public void connect(IOConnector connector) {
            connector.connect(this, new CompleteMessageFilter());
        }
        
        public String getStatus() {
            return status;
        }
        
        public void bytesReceived(byte[] message, IOHandle h) {
            messagesIn++;
            bytesIn += message.length;
            for (byte b : message) {
                if (b != 1) {
                    status = "Error: " + b;
                }
            }
            dataChanged();
        }

        public void disconnected(IOHandle h) {
            connected = false;
            numDisconnects++;
            if (shouldClose && numDisconnects >= numSockets) {
                System.exit(0);
            }
            status = "Disconnected";
            dataChanged();
        }

        public void exceptionThrown(Throwable exception, IOHandle h) {
            System.out.println("ClientTest exceptionThrown");
            status = "Exception!";
            dataChanged();
            exception.printStackTrace();
        }

        public void connected(IOHandle h) {
            handle = h;
            connected = true;
            status = "Connected";
            dataChanged();
            Thread t = new Thread () {
                public void run() {
                    while (!done) {
                        //writeBytes(random.nextInt(2000) + 1);
                        writeBytes(100000);
                        try {
                            //Thread.sleep(random.nextInt(500) + 50);
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
                
            };
            t.start();
        }
        
        void writeBytes(int num) {
            if (!connected) {
                return;
            }
            byte[] buffer = new byte[num];
            for (int i = 0; i < num; i++) {
                buffer[i] = (byte) 1;
            }
            try {
                handle.sendBytes(buffer);
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            bytesOut += num;
            messagesOut++;
            dataChanged();
        }
    }

}
