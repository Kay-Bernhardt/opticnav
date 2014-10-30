package opticnav.daemon.device.broker;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.Callable;

import opticnav.daemon.device.ARDInstanceSubscriber;
import opticnav.daemon.protocol.GeoCoordFine;
import opticnav.daemon.protocol.PrimitiveReader;
import opticnav.daemon.protocol.consts.ARDdARDProtocol.Connected.Instance.SubscriberCommands;

class ARDInstanceSubscriberListener implements Callable<Void> {
    private final PrimitiveReader input;
    private final ARDInstanceSubscriber subscriber;

    public ARDInstanceSubscriberListener(InputStream input, ARDInstanceSubscriber subscriber) {
        this.input = new PrimitiveReader(input);
        this.subscriber = subscriber;
    }
    
    @Override
    public Void call() throws IOException {
        try {
        while (!Thread.currentThread().isInterrupted()) {
            final int commandCode = this.input.readUInt8();
            
            // TODO - replace with constants
            if (commandCode == SubscriberCommands.CREATE_MARKER) {
                final int id = this.input.readUInt31();
                final String name = this.input.readString();
                final int lng = this.input.readSInt32();
                final int lat = this.input.readSInt32();
                
                this.subscriber.markerCreate(id, name, new GeoCoordFine(lng, lat));
            } else if (commandCode == SubscriberCommands.MOVE_MARKER) {
                final int id = this.input.readUInt31();
                final int lng = this.input.readSInt32();
                final int lat = this.input.readSInt32();
                
                this.subscriber.markerMove(id, new GeoCoordFine(lng, lat));
            } else if (commandCode == SubscriberCommands.REMOVE_MARKER) {
                final int id = this.input.readUInt31();
                
                this.subscriber.markerRemove(id);
            } else {
                throw new IllegalStateException();
            }
        }
        } catch (EOFException|InterruptedIOException e) {
            // Ignore EOF
        }
        return null;
    }
}