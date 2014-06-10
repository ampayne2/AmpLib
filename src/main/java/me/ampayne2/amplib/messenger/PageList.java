package me.ampayne2.amplib.messenger;

import me.ampayne2.amplib.AmpJavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Organizes a list of strings into multiple pages.
 */
public class PageList {
    private final AmpJavaPlugin plugin;
    private final String name;
    private final int messagesPerPage;
    private List<String> strings;

    /**
     * Creates a new PageList.
     *
     * @param plugin          The {@link me.ampayne2.amplib.AmpJavaPlugin} instance.
     * @param name            The name of the PageList.
     * @param strings         The list of strings in the PageList.
     * @param messagesPerPage The strings that should be displayed per page.
     */
    public PageList(AmpJavaPlugin plugin, String name, List<String> strings, int messagesPerPage) {
        this.plugin = plugin;
        this.name = name;
        this.messagesPerPage = messagesPerPage;
        this.strings = strings;
    }

    /**
     * Creates a new PageList.
     *
     * @param plugin          The {@link me.ampayne2.amplib.AmpJavaPlugin} instance.
     * @param name            The name of the PageList.
     * @param messagesPerPage The strings that should be displayed per page.
     */
    public PageList(AmpJavaPlugin plugin, String name, int messagesPerPage) {
        this.plugin = plugin;
        this.name = name;
        this.messagesPerPage = messagesPerPage;
        this.strings = new ArrayList<>();
    }

    /**
     * Gets the amount of pages in the PageList.
     *
     * @return The amount of pages.
     */
    public int getPageAmount() {
        return (strings.size() + messagesPerPage - 1) / messagesPerPage;
    }

    /**
     * Sends a page of the PageList to a recipient.
     *
     * @param pageNumber The page number.
     * @param recipient  The recipient.
     */
    public void sendPage(int pageNumber, Object recipient) {
        int pageAmount = getPageAmount();
        pageNumber = clamp(pageNumber, 1, pageAmount);
        Messenger messenger = plugin.getMessenger();
        messenger.sendRawMessage(recipient, Messenger.HIGHLIGHT_COLOR + "<-------<| " + Messenger.PRIMARY_COLOR + name + ": Page " + pageNumber + "/" + pageAmount + " " + Messenger.HIGHLIGHT_COLOR + "|>------->");
        int startIndex = messagesPerPage * (pageNumber - 1);
        for (String string : strings.subList(startIndex, Math.min(startIndex + messagesPerPage, strings.size()))) {
            messenger.sendRawMessage(recipient, string);
        }
    }

    /**
     * Sets the strings of a page list.
     *
     * @param strings The strings.
     */
    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    /**
     * Clamps a value between a minimum and maximum value.
     *
     * @param value The value.
     * @param min   The minimum value.
     * @param max   The maximum value.
     * @return The clamped value.
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
