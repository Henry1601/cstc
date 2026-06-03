package burp;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import de.usd.cstchef.Utils.MessageType;
import de.usd.cstchef.view.View;

public class CstcContextMenuItemsProvider implements ContextMenuItemsProvider {
    private MontoyaApi api;
    private View view;

    private Timer timer = new Timer();

    public CstcContextMenuItemsProvider(MontoyaApi api, View view)
    {
        this.api = api;
        this.view = view;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        
        List<Component> menuItems = new ArrayList<>();
        JMenuItem incomingMenu = new JMenuItem("Send to CSTC (Incoming)");
        JMenuItem outgoingMenu = new JMenuItem("Send to CSTC (Outgoing)");
        JMenuItem incomingReqFormatMenu = new JMenuItem("Send request to CSTC (Formatting)");
        JMenuItem incomingResFormatMenu = new JMenuItem("Send response to CSTC (Formatting)");
        
        menuItems.add(outgoingMenu);
        menuItems.add(incomingMenu);
        menuItems.add(incomingReqFormatMenu);
        menuItems.add(incomingResFormatMenu);

        incomingMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightExtensionName();
                view.getIncomingRecipePanel().setInput(event.messageEditorRequestResponse().isPresent() ? event.messageEditorRequestResponse().get().requestResponse() : event.selectedRequestResponses().get(0));
            }
        });

        outgoingMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightExtensionName();
                view.getOutgoingRecipePanel().setInput(event.messageEditorRequestResponse().isPresent() ? event.messageEditorRequestResponse().get().requestResponse() : event.selectedRequestResponses().get(0));
            }
        });

        incomingResFormatMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightExtensionName();
                view.getFormatRecipePanel().setFormatMessage(event.messageEditorRequestResponse().isPresent() ? event.messageEditorRequestResponse().get().requestResponse() : event.selectedRequestResponses().get(0), MessageType.RESPONSE);
            }
        });

        incomingReqFormatMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightExtensionName();
                view.getFormatRecipePanel().setFormatMessage(event.messageEditorRequestResponse().isPresent() ? event.messageEditorRequestResponse().get().requestResponse() : event.selectedRequestResponses().get(0), MessageType.REQUEST);
            }
        });

        return menuItems;
    }

    private void highlightExtensionName() {
        TimerTask task = new TimerTask() {

        @Override
        public void run() {
            resetHighlighting();
        }
        
        };
        JTabbedPane parentTabbedPane = (JTabbedPane) BurpUtils.getInstance().getView().getParent();
        for(int i = 0; i < parentTabbedPane.getTabCount(); i++) {
            if(parentTabbedPane.getTitleAt(i).contains("CSTC")) {
                parentTabbedPane.setForegroundAt(i, new Color(0xff6633));
                timer.schedule(task, 3000);
                return;
            }
        }     
    }

    private void resetHighlighting() {
        JTabbedPane parentTabbedPane = (JTabbedPane) BurpUtils.getInstance().getView().getParent();
        for(int i = 0; i < parentTabbedPane.getTabCount(); i++) {
            if(parentTabbedPane.getTitleAt(i).contains("CSTC")) {
                parentTabbedPane.setForegroundAt(i, new Color(0x000000));
                return;
            }
        }
    }
}
