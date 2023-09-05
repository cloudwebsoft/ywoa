package com.cloudweb.oa.module.desktop;

import com.cloudweb.oa.api.IDesktopCard;

public class DesktopCardFactory {
    public static String TYPE_FLOW_DOING = "flow_doing";
    public static String TYPE_FLOW_MINE = "flow_mine";
    public static String TYPE_MODULE = "module";
    public static String TYPE_MENU_ITEM = "menu_item";
    public static String TYPE_MENU_LINK = "link";

    public static IDesktopCard getIDesktopCard(DesktopCard desktopCard) {
        if (TYPE_FLOW_DOING.equals(desktopCard.getCardType())) {
            return new DesktopCardFlowDoing(desktopCard);
        }
        else if (TYPE_FLOW_MINE.equals(desktopCard.getCardType())) {
            return new DesktopCardFlowMine(desktopCard);
        }
        else if (TYPE_MODULE.equals(desktopCard.getCardType())) {
            return new DesktopCardModule(desktopCard);
        }
        else if (TYPE_MENU_ITEM.equals(desktopCard.getCardType())) {
            return new DesktopCardMenuItem(desktopCard);
        }
        else if (TYPE_MENU_LINK.equals(desktopCard.getCardType())) {
            return new DesktopCardLink(desktopCard);
        }
        else {
            return null;
        }
    }
}
