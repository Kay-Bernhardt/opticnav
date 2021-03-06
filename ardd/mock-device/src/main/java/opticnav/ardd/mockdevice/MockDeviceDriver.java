package opticnav.ardd.mockdevice;

import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import opticnav.ardd.ard.ARDConnected;
import opticnav.ardd.ard.ARDConnectionStatus;
import opticnav.ardd.ard.ARDInstance;
import opticnav.ardd.ard.ARDInstanceException;
import opticnav.ardd.ard.ARDInstanceJoinStatus;
import opticnav.ardd.ard.InstanceInfo;
import opticnav.ardd.broker.ard.ARDGatekeeperBroker;
import opticnav.ardd.protocol.GeoCoordFine;
import opticnav.ardd.protocol.PassCode;
import opticnav.ardd.protocol.Protocol;
import opticnav.ardd.protocol.chan.Channel;
import opticnav.ardd.protocol.chan.ChannelUtil;

public class MockDeviceDriver {
    private static final XLogger LOG = XLoggerFactory
            .getXLogger(MockDeviceDriver.class);

    public static void main(String[] args) throws Exception {
        Logger root = (Logger)org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        
        final Scanner in = new Scanner(System.in);
        final PassCode passCode = new PassCode(args[0]);
        final int sleepMillis = 1000;
        final Socket socket = new Socket("localhost", Protocol.DEFAULT_ARD_PORT);
        final Channel channel = ChannelUtil.fromSocket(socket);

        try (final ARDGatekeeperBroker broker = new ARDGatekeeperBroker(channel, Executors.newCachedThreadPool())) {
            ARDConnectionStatus connStatus = broker.connect(passCode);
            if (connStatus.getStatus() == ARDConnectionStatus.Status.CONNECTED) {
                try (final ARDConnected connected = connStatus.getConnection()) {
                    final List<InstanceInfo> instances = connected.listInstances();
                    
                    if (!instances.isEmpty()) {
                        int instanceID = chooseInstance(instances, in);
                        ARDInstanceJoinStatus instStatus = connected.joinInstance(instanceID);
                        if (instStatus.getStatus() == ARDInstanceJoinStatus.Status.JOINED) {
                            moveAround(instStatus.getInstance(), in, sleepMillis);
                        }
                    } else {
                        System.out.println("There are no instances to join");
                    }
                }
            } else {
                System.out.println("Could not connect: " + connStatus.getStatus());
            }
        }
    }
    
    private static int chooseInstance(List<InstanceInfo> list, Scanner in) {
        System.out.println("Choose an instance:");
        for (InstanceInfo info: list) {
            System.out.printf("%3d: %s\n", info.getId(), info.getName());
        }
        System.out.println();
        System.out.print("> ");
        
        return Integer.parseInt(in.nextLine());
    }
    
    private static void moveAround(ARDInstance instance, Scanner in, int sleepMillis)
            throws ARDInstanceException, InterruptedException {
        while (in.hasNextLine()) {
            final String line = in.nextLine();
            final GeoCoordFine geoCoord;
            if (line.equals("q")) {
                break;
            }
            geoCoord = GeoCoordFineUtil.reprToGeoCoord(line);
            
            instance.move(geoCoord);
            LOG.info("Moved: " + geoCoord);
            Thread.sleep(sleepMillis);
        }
    }
}
