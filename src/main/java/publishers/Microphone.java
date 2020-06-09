package publishers;

import utilities.BufferCircular;
import utilities.PacketAudio;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.SocketAddress;
import java.util.Arrays;

public class Microphone extends Publisher<PacketAudio> {
    byte[] bytes;
    int bytesRead;
    TargetDataLine targetDataLine;
    Timer timer;

    public Microphone(SocketAddress socketAddress, AudioFormat audioFormat) {
        try {
            PacketAudio[] packetAudios = new PacketAudio[16];
            for (PacketAudio packetVideo : packetAudios) packetVideo.socketAddress = socketAddress;
            buffer = new BufferCircular<>(packetAudios);

            targetDataLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, audioFormat));
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            timer = new Timer(1000 / 30, (ActionEvent actionEvent) -> publish());
            timer.start();
        } catch (LineUnavailableException exception) {
            exception.printStackTrace();
        }
    }

    @Override public void publish() {
        PacketAudio packetAudio = buffer.getAvailableSlot();
        bytesRead = targetDataLine.read(packetAudio.bytes, 0, Math.min(targetDataLine.available(), bytes.length));
        Arrays.fill(bytes, bytesRead, bytes.length, (byte) 0);
        buffer.markSlotFilled();
    }
}