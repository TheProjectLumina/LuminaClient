package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class BookEditPacket implements BedrockPacket {
    public Action action;
    public int inventorySlot;
    public int pageNumber;
    public int secondaryPageNumber;
    public CharSequence text;
    public String photoName;
    public CharSequence title;
    public CharSequence author;
    public String xuid;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.BOOK_EDIT;
    }

    public enum Action {
        REPLACE_PAGE,
        ADD_PAGE,
        DELETE_PAGE,
        SWAP_PAGES,
        SIGN_BOOK
    }

    @Override
    public BookEditPacket clone() {
        try {
            return (BookEditPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public String getText() {
        return getText(String.class);
    }

    public <T extends CharSequence> T getText(Class<T> type) {
        return type.cast(text);
    }

    public String getTitle() {
        return getTitle(String.class);
    }

    public <T extends CharSequence> T getTitle(Class<T> type) {
        return type.cast(title);
    }

    public String getAuthor() {
        return getAuthor(String.class);
    }

    public <T extends CharSequence> T getAuthor(Class<T> type) {
        return type.cast(author);
    }
}

