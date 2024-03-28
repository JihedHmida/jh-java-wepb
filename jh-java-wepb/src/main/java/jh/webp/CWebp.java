package jh.webp;


import jh.webp.process.ProcessLocator;
import jh.webp.process.webp.DefaultCWepbLocator;

import java.io.IOException;

public class CWebp {
    private final StringBuffer command;
    private final ProcessLocator locator;

    public static void main(String[] args) {
        CWebp cWebp = new CWebp().quality(80)
                .input("C:/laragon/www/scom/article/1/5/1/151.jpg")
                .output("C:/laragon/www/scom/article/1/5/1/151.webp");

        System.out.println(cWebp.getCommand());

        cWebp.execute();
    }


    public CWebp() {
        this.locator = new DefaultCWepbLocator();
        command = new StringBuffer();
        command.append(this.locator.getExecutablePath()).append(" ");
    }

    public CWebp(ProcessLocator locator) {
        this.locator = locator;
        command = new StringBuffer();
        command.append(this.locator.getExecutablePath()).append(" ");
    }


    public CWebp nearLossless(int level) {
        if (level > 100 || level < 0) {
            return this;
        }
        command.append("-near_lossless ").append(level).append(" ");

        return this;
    }

    public CWebp quality(int quality) {
        if (quality > 100 || quality < 0) {
            return this;
        }
        command.append("-q ").append(quality).append(" ");
        return this;
    }

    public CWebp alphaQ(int alpha) {
        if (alpha > 100 || alpha < 0) {
            return this;
        }
        command.append("-alpha_q ").append(alpha).append(" ");
        return this;
    }

    public CWebp lowMemory() {
        command.append("-low_memory ");
        return this;
    }

    public CWebp crop(int x_position, int y_position, int width, int height) {
        if (width == 0 || height == 0) {
            return this;
        }
        command.append("-crop ");
        command.append(x_position).append(" ").append(y_position).append(" ");
        command.append(width).append(" ").append(height).append(" ");
        return this;
    }

    public CWebp resize(int width, int height) {
        command.append("-resize ");
        command.append(width).append(" ").append(height).append(" ");
        return this;
    }

    public CWebp output(String outputPath) {
        command.append("-o ").append(outputPath).append(" ");
        return this;
    }

    public CWebp input(String inputPath) {
        command.append(inputPath).append(" ");
        return this;
    }


    public void execute() {
        Process process = null;

        try {

//            ProcessBuilder processBuilder = new ProcessBuilder(command.toString());
//            process = processBuilder.start();

            process = Runtime.getRuntime().exec(command.toString());


            int exitCode = process.waitFor();
            System.out.println("Command exited with code " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

    }

    public String getCommand() {
        return command.toString();
    }
}
