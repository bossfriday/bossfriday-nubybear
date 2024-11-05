package cn.bossfriday.im.common.enums;

import org.slf4j.Logger;

import java.io.PrintStream;

/**
 * BootstrapBanners
 *
 * @author chenx
 */
public enum BootstrapBanners {

    START(new String[]{
            " ######  ########    ###    ########  ######## ",
            "##    ##    ##      ## ##   ##     ##    ##    ",
            "##          ##     ##   ##  ##     ##    ##    ",
            " ######     ##    ##     ## ########     ##    ",
            "      ##    ##    ######### ##   ##      ##    ",
            "##    ##    ##    ##     ## ##    ##     ##    ",
            " ######     ##    ##     ## ##     ##    ##    "}),
    FAIL(new String[]{
            " _______    ___       __   __      ",
            "|   ____|  /   \\     |  | |  |     ",
            "|  |__    /  ^  \\    |  | |  |     ",
            "|   __|  /  /_\\  \\   |  | |  |     ",
            "|  |    /  _____  \\  |  | |  `----.",
            "|__|   /__/     \\__\\ |__| |_______|"}),
    ;

    private final String[] banner;

    BootstrapBanners(String[] banner) {
        this.banner = banner;
    }

    /**
     * printBanner
     *
     * @param logger
     */
    public void printBanner(Logger logger) {
        if (this.banner != null) {
            for (String line : this.banner) {
                logger.warn(line);
            }
        }
    }

    /**
     * printBanner
     *
     * @param out
     */
    public void printBanner(PrintStream out) {
        if (this.banner != null) {
            for (String line : this.banner) {
                out.println(line);
            }
        }
    }
}
